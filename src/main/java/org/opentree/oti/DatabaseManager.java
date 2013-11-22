package org.opentree.oti;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jade.tree.JadeNode;
import jade.tree.JadeTree;
import opentree.taxonomy.contexts.TaxonomyNodeIndex;

import org.opentree.GeneralUtils;
import org.opentree.graphdb.DatabaseUtils;
import org.opentree.graphdb.GraphDatabaseAgent;
import org.opentree.nexson.io.NexsonSource;
import org.opentree.oti.constants.OTIConstants;
import org.opentree.oti.constants.OTIGraphProperty;
import org.opentree.oti.constants.OTIRelType;
import org.opentree.oti.indexproperties.IndexedPrimitiveProperties;
import org.opentree.oti.indexproperties.OTINodeProperty;
import org.opentree.properties.BasicType;
import org.opentree.properties.OTVocabularyPredicate;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.Traversal;

public class DatabaseManager extends OTIDatabase {

	private StudyIndexer indexer;
	private Node lastObservedIngroupStartNode = null;
	
	// used when copying trees to remember a specified node from the old tree that is in the new one
	Node workingCopyNodeOfInterest = null;

	// used when storing taxon information to use when for indexing tree root nodes
	List<String> originalTipLabels;
	List<String> originalTipLabelsNoSpaces;
	List<String> mappedTaxonNames;
	List<String> mappedTaxonNamesNoSpaces;
	List<Long> mappedOTTIds;
	
	protected Index<Node> studyMetaNodesByProperty = getNodeIndex(OTINodeIndex.STUDY_METADATA_NODES_BY_PROPERTY_EXACT);
	
	// this is a taxomachine index, so we specify index type parameters to override the OTU default behavior of opening indexes as fulltext
	protected Index<Node> taxonNodesByOTTId = getNodeIndex(TaxonomyNodeIndex.TAXON_BY_OTT_ID, IndexManager.PROVIDER, "lucene", "type", "exact");

	// ===== constructors

	/**
	 * Access the graph db through the given service object.
	 * 
	 * @param graphService
	 */
	public DatabaseManager(GraphDatabaseService graphService) {
		super(graphService);
		indexer = new StudyIndexer(graphDb);
	}

	/**
	 * Access the graph db through the given embedded db object.
	 * 
	 * @param embeddedGraph
	 */
	public DatabaseManager(EmbeddedGraphDatabase embeddedGraph) {
		super(embeddedGraph);
		indexer = new StudyIndexer(graphDb);
	}

	/**
	 * Open the graph db through the given agent object.
	 * 
	 * @param gdb
	 */
	public DatabaseManager(GraphDatabaseAgent gdb) {
		super(gdb);
		indexer = new StudyIndexer(graphDb);
	}

	// ========== public methods
	
	// ===== adding sources and trees
	
	/**
	 * Install a study into the db, including loading all included trees.
	 * 
	 * @param study
	 * 		A NexsonSource object that contains the source metadata and trees.
	 * 
	 * @return
	 * 		The source metadata node for the newly added study
	 * 
	 */
	public Node addOrReplaceStudy(NexsonSource study) {
		
		// TODO: return meaningful information about the result to the rest query that calls this method

		Node studyMeta = null;
		
		Transaction tx = graphDb.beginTx();
		try {
			
			String studyId = study.getId();

			// an attempt to add a study with the same id as an existing study overwrites the existing study
			studyMeta = DatabaseUtils.getSingleNodeIndexHit(studyMetaNodesByProperty, OTVocabularyPredicate.OT_STUDY_ID.propertyName(), studyId);
			if (studyMeta != null) {
				deleteSource(studyMeta);
			}
			
			// create the study
			studyMeta = graphDb.createNode();
			studyMeta.setProperty(OTINodeProperty.IS_STUDY_META.propertyName(), true);
			
			// set studyproperties
			setNodePropertiesFromMap(studyMeta, study.getProperties());

			// add the trees
			boolean noValidTrees = true;
			Iterator<JadeTree> treesIter = study.getTrees().iterator();
			while (treesIter.hasNext()) {

				JadeTree tree = treesIter.next();

				// TODO: sometimes the nexson reader returns null trees. this is a hack to deal with that.
				// really we should fix the nexson reader so it doesn't return null trees
				if (tree == null) {
					continue;
				} else if (noValidTrees == true) {
					noValidTrees = false;
				}

				// get the tree id from the nexson
				// TODO: verify that this is the property we want to be using for this
				String treeId = (String) tree.getObject(OTINodeProperty.NEXSON_ID.propertyName());
				
				// create a unique tree id by including the study id, this is the convention from treemachine
				String treeUniqueId = studyId + "_" + treeId;

				// add the tree
				addTree(tree, treeUniqueId, studyMeta);
			}
		
			indexer.addStudyMetaNodeToIndexes(studyMeta);
			
			tx.success();
		} finally {
			tx.finish();
		}
		
		return studyMeta;
	}
	
	/**
	 * Adds a tree in a JadeTree format into the database under the specified study.
	 * 
	 * @param tree
	 * 		A JadeTree object containing the tree to be added
	 * @param treeId
	 * 		The id string to use for this tree. Will be used in indexing so must be unique across all trees in the database
	 * @param sourceMetaNode
	 * 		The source metadata node for the source that this tree will be added to
	 * @return
	 * 		The root node for the added tree.
	 */
	public Node addTree(JadeTree tree, String treeId, Node sourceMetaNode) {

		// get the study id from the source meta node
		String studyId = (String) sourceMetaNode.getProperty(OTVocabularyPredicate.OT_STUDY_ID.propertyName());

		// add the tree to the graph
		Node root = preorderAddTreeToDB(tree.getRoot(), null);
		
		// set this property now so that get root traversals will work
		root.setProperty(OTINodeProperty.IS_ROOT.propertyName(), true);			

		// designate the ingroup if we found one, and then reset the variable!
		if (lastObservedIngroupStartNode != null) {
			designateIngroup(lastObservedIngroupStartNode);
			lastObservedIngroupStartNode = null;
		}

		// attach to source and set the id information
		sourceMetaNode.createRelationshipTo(root, OTIRelType.METADATAFOR);
		root.setProperty(OTVocabularyPredicate.OT_STUDY_ID.propertyName(), studyId);

		// add node properties
		root.setProperty(OTINodeProperty.TREE_ID.propertyName(), treeId);
		setNodePropertiesFromMap(root, tree.getAssoc());

		// gather information about the taxa represented in this tree
		collectTipTaxonArrayPropertiesFromJadeTree(root, tree);
		
		indexer.addTreeRootNodeToIndexes(root);
		
		return root;
	}

	// ===== delete methods

	/**
	 * Deletes a tree
	 * @param treeId
	 */
	public void deleteTree(Node root) {

		Transaction tx = graphDb.beginTx();
		try {

			// clean up the tree indexes
			indexer.removeTreeRootNodeFromIndexes(root);

			// collect the tree nodes
			HashSet<Node> todelete = new HashSet<Node>();
			TraversalDescription CHILDOF_TRAVERSAL = Traversal.description().relationships(OTIRelType.CHILDOF, Direction.INCOMING);
			todelete.add(root);
			for (Node curGraphNode : CHILDOF_TRAVERSAL.breadthFirst().traverse(root).nodes()) {
				todelete.add(curGraphNode);
			}
			
			// remove them
			for (Node nd : todelete) {
				for (Relationship rel : nd.getRelationships()) {
					rel.delete();
				}
				nd.delete();
			}
			
			tx.success();

		} finally {
			tx.finish();
		}
	}

	/**
	 * Remove a study and all its trees.
	 * @param studyMeta
	 * @throws NoSuchTreeException 
	 */
	public void deleteSource(Node sourceMeta) {
		
		Transaction tx = graphDb.beginTx();
		try {

			// clean up the source indexes
			indexer.removeStudyMetaNodeFromIndexes(sourceMeta);

			// remove all trees
			for (Relationship rel : sourceMeta.getRelationships(OTIRelType.METADATAFOR, Direction.OUTGOING)) {
				deleteTree(rel.getEndNode()); // will also remove the METADATAFOR rels pointing at this metadata node
			}

			// delete remaining relationships
			for (Relationship rel : sourceMeta.getRelationships()) {
				rel.delete();
			}
			
			// delete the source meta node itself
			sourceMeta.delete();			
			
			tx.success();
			
		} finally {
			tx.finish();
		}
	}
	
	// ===== other methods
	
	/*
	 * Set properties on a node according to the passed in map. Uses the existing object types for the values.
	 * @param node
	 * @param keys
	 * @param values
	 *
	public void setProperties(Node node, Map<String, Object> properties) {

		Transaction tx = graphDb.beginTx();
		try {
			for (Entry<String, Object> property : properties.entrySet()) {
				node.setProperty(property.getKey(), property.getValue());
			}
			updateIndexes(node);
			tx.success();
		} finally {
			tx.finish();
		}
		
	} */
	
	/*
	 * Set properties on a node according to the passed in arrays. Should convert the object types according to
	 * the BasicType names indicated in `types`.
	 * 
	 * TODO: I think there is a bug in here...
	 * 
	 * @param keys
	 * @param values
	 * @param types
	 *
	public void setProperties(Node node, String[] keys, String[] values, String[] types) {
		
		Transaction tx = graphDb.beginTx();
		try {

			int i = 0;
			BasicType t;
			
			for (String key : keys) {
				try {
					t = BasicType.valueOf(types[i].toUpperCase().trim());
				} catch (IllegalArgumentException ex) {
					tx.failure();
					throw new IllegalArgumentException("The type " + types[i] + " is not valid property type.");
				}

				node.setProperty(key, t.convertToValue(values[i++]));
			}
			
			updateIndexes(node);
			
			tx.success();
		} catch (ArrayIndexOutOfBoundsException ex) {
			tx.failure();
			throw new IllegalArgumentException("All the input arrays must be the same length.");
		} finally {
			tx.finish();
		}
	} */
	
	/*
	 * Update the indexes for a given node.
	 * @param node
	 *
	public void updateIndexes(Node node) {
		Node root = QueryRunner.getRootOfTreeContaining(node);
		if (root != null) {
			// if this is a tree root node
			collectTipTaxonArrayPropertiesFromGraph(root);
			
			indexer.removeTreeRootNodeFromIndexes(root);
			indexer.addTreeRootNodeToIndexes(root);
		} else {
			if (node.hasProperty(OTINodeProperty.IS_SOURCE_META.propertyName())) {
				// if this is a source meta node
				indexer.removeSourceMetaNodeFromIndexes(node);
				indexer.addSourceMetaNodeToIndexes(node);
			}
		}
	} */
	
	/**
	 * Set the ingroup for the tree containing `innode` to `innode`.
	 * @param innode
	 */
	public void designateIngroup(Node innode) {

		// first get the root of the old tree
		Node root = OTIDatabaseUtils.getRootOfTreeContaining(innode);

		TraversalDescription CHILDOF_TRAVERSAL = Traversal.description().relationships(OTIRelType.CHILDOF, Direction.INCOMING);
		Transaction tx = graphDb.beginTx();
		try {
			root.setProperty(OTINodeProperty.INGROUP_IS_SET.propertyName(), true);
			if (root != innode) {
				for (Node node : CHILDOF_TRAVERSAL.breadthFirst().traverse(root).nodes()) {
					if (node.hasProperty(OTINodeProperty.IS_WITHIN_INGROUP.propertyName()))
						node.removeProperty(OTINodeProperty.IS_WITHIN_INGROUP.propertyName());
				}
			}
			innode.setProperty(OTINodeProperty.IS_WITHIN_INGROUP.propertyName(), true);
			for (Node node : CHILDOF_TRAVERSAL.breadthFirst().traverse(innode).nodes()) {
				node.setProperty(OTINodeProperty.IS_WITHIN_INGROUP.propertyName(), true);
			}
			root.setProperty(OTINodeProperty.INGROUP_IS_SET.propertyName(), true);
			root.setProperty(OTINodeProperty.INGROUP_START_NODE_ID.propertyName(), innode.getId());
			tx.success();
		} finally {
			tx.finish();
		}		
	}
	
	/**
	 * Create a relationship associating a tree node with the taxonomy node to which it has been assigned.
	 * Uses the ott id property of the tree node to identify the taxonomy node. Has no effect if the tree
	 * node has not been assigned an ott id. Requires that the taxonomy has been loaded into the graph.
	 * @param node
	 */
	public void connectTreeNodeToTaxonomy(Node node) {
		
		// nothing to be done if this node doesn't have an ottid
		if (!node.hasProperty(OTVocabularyPredicate.OT_OTT_ID.propertyName())) {
			return;
		}
		
		Long ottId = (Long) node.getProperty(OTVocabularyPredicate.OT_OTT_ID.propertyName());
		Node taxonNode = DatabaseUtils.getSingleNodeIndexHit(taxonNodesByOTTId, OTVocabularyPredicate.OT_OTT_ID.propertyName(), ottId);
		
		if (taxonNode != null) {
			
			for (Relationship existingRel : node.getRelationships(OTIRelType.EXEMPLAROF, Direction.OUTGOING)) {
				existingRel.delete();
			}
			
			node.createRelationshipTo(taxonNode, OTIRelType.EXEMPLAROF);
		}
	}
	
	// ========== private methods
	
	/**
	 * A recursive function used to replicate the tree JadeNode structure below the passed in JadeNode in the graph.
	 * @param curJadeNode
	 * @param parentGraphNode
	 * @return
	 */
	private Node preorderAddTreeToDB(JadeNode curJadeNode, Node parentGraphNode) {

		Node curGraphNode = graphDb.createNode();

		// remember the ingroup if we hit one
		if (curJadeNode.hasAssocObject(OTINodeProperty.IS_INGROUP_ROOT.propertyName()) == true) {
			curGraphNode.setProperty(OTINodeProperty.INGROUP_START_NODE_ID.propertyName(), true);
			lastObservedIngroupStartNode = curGraphNode;
		}
				
		// add properties
		if (curJadeNode.getName() != null) {
			curGraphNode.setProperty(OTVocabularyPredicate.OT_OTT_TAXON_NAME.propertyName(), curJadeNode.getName());
			
//			String mappedTaxonNameNoSpaces = OTIDatabaseUtils.substituteWhitespace(curJadeNode.getName());
//			curJadeNode.assocObject(OTINodeProperty.OT_MAPPED_TAXON_NAME_NO_SPACES.propertyName(), mappedTaxonNameNoSpaces);
//			curGraphNode.setProperty(OTINodeProperty.OT_MAPPED_TAXON_NAME_NO_SPACES.propertyName(), mappedTaxonNameNoSpaces);
			
			setNodePropertiesFromMap(curGraphNode, curJadeNode.getAssoc());
		}

		// TODO: add bl
		// dbnode.setProperty("bl", innode.getBL());
		// TODO: add support
		
		if (parentGraphNode != null) {
			curGraphNode.createRelationshipTo(parentGraphNode, OTIRelType.CHILDOF);
		}

		for (JadeNode childJadeNode : curJadeNode.getChildren()) {
			preorderAddTreeToDB(childJadeNode, curGraphNode);
		}
		
		// mark the tips as OTU nodes
		if (curJadeNode.getChildCount() < 1) {
			curGraphNode.setProperty(OTVocabularyPredicate.OT_IS_LEAF.propertyName(), true);
			
			// for otu nodes, connect them to the taxonomy
			connectTreeNodeToTaxonomy(curGraphNode);
			
			// also calculate special values for tip nodes
//			String originalLabelNoSpaces = OTIDatabaseUtils.substituteWhitespace((String) curJadeNode.getObject(OTVocabularyPredicate.OT_ORIGINAL_LABEL.propertyName()));
//			curJadeNode.assocObject(OTINodeProperty.OT_ORIGINAL_LABEL_NO_SPACES.propertyName(), originalLabelNoSpaces);
//			curGraphNode.setProperty(OTINodeProperty.OT_ORIGINAL_LABEL_NO_SPACES.propertyName(), originalLabelNoSpaces);
			
		}

		indexer.addTreeNodeToIndexes(curGraphNode);

		return curGraphNode;
	}
	
	/**
	 * Import entries from a map into the database as properties of the specified node.
	 * @param node
	 * @param properties
	 */
	private static void setNodePropertiesFromMap(Node node, Map<String, Object> properties) {
		for (Entry<String, Object> property : properties.entrySet()) {
			node.setProperty(property.getKey(), property.getValue());
		}
	}
	
	/**
	 * Collects taxonomic names and ids for all the tips of the provided JadeTree and stores this info as node properties
	 * of the provided graph node. Used to store taxonomic mapping info for the root nodes of trees in the graph.
	 * @param node
	 * @param tree
	 */
	private void collectTipTaxonArrayPropertiesFromJadeTree(Node node, JadeTree tree) {
		
		originalTipLabels = new ArrayList<String>();
		originalTipLabelsNoSpaces = new ArrayList<String>();
		mappedTaxonNames = new ArrayList<String>();
		mappedTaxonNamesNoSpaces = new ArrayList<String>();
		mappedOTTIds = new ArrayList<Long>();

		for (JadeNode tip : tree.getRoot().getDescendantLeaves()) {

			originalTipLabels.add((String) tip.getObject(OTVocabularyPredicate.OT_ORIGINAL_LABEL.propertyName()));
//			originalTipLabelsNoSpaces.add((String) tip.getObject(OTINodeProperty.OT_ORIGINAL_LABEL_NO_SPACES.propertyName()));

			if (tip.hasAssocObject(OTVocabularyPredicate.OT_OTT_ID.propertyName())) {
				// If the node has not been explicitly mapped, we will not record the name as a mapped name
				mappedTaxonNames.add(tip.getName());
//				mappedTaxonNamesNoSpaces.add((String) tip.getObject(OTINodeProperty.OT_MAPPED_TAXON_NAME_NO_SPACES.propertyName()));
				
			}

			Long ottId = (Long) tip.getObject(OTVocabularyPredicate.OT_OTT_ID.propertyName());
			if (ottId != null) {
				mappedOTTIds.add(ottId);
			}
		}
		
		assignTaxonArraysToNode(node);
	}
	
	/*
	 * Collects taxonomic names and ids for all the tips of the provided tree stored in the graph, and stores this info
	 * as node properties of the provided graph node. Used to store taxonomic mapping info for the root nodes of trees in the graph.
	 * @param node
	 * @param tree
	 *
	private void collectTipTaxonArrayPropertiesFromGraph(Node node) {
		
		originalTaxonNames = new ArrayList<String>();
		mappedTaxonNames = new ArrayList<String>();
		mappedTaxonNamesNoSpaces = new ArrayList<String>();
		mappedOTTIds = new ArrayList<Long>();

		for (Node tip : OTIDatabaseUtils.getDescendantTips(node)) {

			originalTaxonNames.add((String) tip.getProperty(OTVocabularyPredicate.OT_ORIGINAL_LABEL.propertyName()));

			// only record explicit mapped names as mapped names
			if (tip.hasProperty(OTVocabularyPredicate.OT_OTT_TAXON_NAME.propertyName())) {
				String name = (String) tip.getProperty(OTVocabularyPredicate.OT_OTT_TAXON_NAME.propertyName());
				mappedTaxonNames.add(name);
				mappedTaxonNamesNoSpaces.add(name.replace("\\s+", OTIConstants.WHITESPACE_SUBSTITUTE_FOR_SEARCH));
			}

			if (tip.hasProperty(OTVocabularyPredicate.OT_OTT_ID.propertyName())) {
				Long ottId = (Long) tip.getProperty(OTVocabularyPredicate.OT_OTT_ID.propertyName());
				mappedOTTIds.add(ottId);
			}
		}
		assignTaxonArraysToNode(node);
	} */
	
	/**
	 * A helper function for the collectTipTaxonArrayProperties functions. Exists only to ensure consistency and simplify code.
	 * @param node
	 */
	private void assignTaxonArraysToNode(Node node) {
		
		// store the properties we just collected
		node.setProperty(OTINodeProperty.DESCENDANT_ORIGINAL_TIP_LABELS.propertyName(), GeneralUtils.convertToStringArray(originalTipLabels));
//		node.setProperty(OTINodeProperty.DESCENDANT_MAPPED_TAXON_NAMES_WHITESPACE_FILLED.propertyName(), GeneralUtils.convertToStringArray(mappedTaxonNamesNoSpaces));
		node.setProperty(OTINodeProperty.DESCENDANT_MAPPED_TAXON_NAMES.propertyName(), GeneralUtils.convertToStringArray(mappedTaxonNames));
//		node.setProperty(OTINodeProperty.DESCENDANT_ORIGINAL_TIP_LABELS_WHITESPACE_FILLED.propertyName(), GeneralUtils.convertToStringArray(originalTipLabelsNoSpaces));
		node.setProperty(OTINodeProperty.DESCENDANT_MAPPED_TAXON_OTT_IDS.propertyName(), GeneralUtils.convertToLongArray(mappedOTTIds));
		
		// clean up the mess... just to be sure we don't accidentally use this information somewhere else
		originalTipLabels = null;
		mappedTaxonNames = null;
		mappedTaxonNamesNoSpaces = null;
		mappedOTTIds = null;
	}
	
	/*
	 * Used by the rerooting function
	 * @param oldRoot
	 * @param newRoot
	 * @return
	 *
	private boolean tritomyRoot(Node oldRoot, Node newRoot) {
		Node thisNode = null;// this will be the node that is sunk
		// find the first child that is not a tip
		for (Relationship rel : oldRoot.getRelationships(OTIRelType.CHILDOF, Direction.INCOMING)) {
			Node tnode = rel.getStartNode();
			if (tnode.hasRelationship(Direction.INCOMING, OTIRelType.CHILDOF) && tnode.getId() != newRoot.getId()) {
				thisNode = tnode;
				break;
			}
		}
		if (thisNode == null) {
			return false;
		}
		for (Relationship rel : thisNode.getRelationships(OTIRelType.CHILDOF, Direction.INCOMING)) {
			Node eNode = rel.getStartNode();
			eNode.createRelationshipTo(oldRoot, OTIRelType.CHILDOF);
			rel.delete();
		}
		thisNode.getSingleRelationship(OTIRelType.CHILDOF, Direction.OUTGOING).delete();
		thisNode.delete();
		return true;
	} */

	/*
	 * Recursive function to process a re-rooted tree to fix relationship direction, etc.
	 * @param innode
	 *
	private void processRerootRecursive(Node innode) {
		if (innode.hasProperty(OTINodeProperty.IS_ROOT.propertyName()) || innode.hasRelationship(Direction.INCOMING, OTIRelType.CHILDOF) == false) {
			return;
		}
		Node parent = null;
		if (innode.hasRelationship(Direction.OUTGOING, OTIRelType.CHILDOF)) {
			parent = innode.getSingleRelationship(OTIRelType.CHILDOF, Direction.OUTGOING).getEndNode();
			processRerootRecursive(parent);
		}

		DatabaseUtils.exchangeNodeProperty(parent, innode, OTINodeProperty.NAME.propertyName());

		// Rearrange topology
		innode.getSingleRelationship(OTIRelType.CHILDOF, Direction.OUTGOING).delete();
		parent.createRelationshipTo(innode, OTIRelType.CHILDOF);
	} */

}
