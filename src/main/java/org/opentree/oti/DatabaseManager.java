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
import org.opentree.oti.constants.OTINodeProperty;
import org.opentree.oti.constants.OTIRelType;
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
//	private ConfigurationManager config;
	private QueryRunner queryRunner;
	
	private HashSet<String> knownRemotes;
	private Node lastObservedIngroupStartNode = null;
	
	// used when copying trees to remember a specified node from the old tree that is in the new one
	Node workingCopyNodeOfInterest = null;

	// used when storing taxon information to use when for indexing tree root nodes
	List<String> originalTaxonNames;
	List<String> mappedTaxonNames;
	List<String> mappedTaxonNamesNoSpaces;
	List<Long> mappedOTTIds;
	
//	protected Index<Node> sourceMetaNodesBySourceId = getNodeIndex(OTINodeIndex.STUDY_METADATA_NODES_BY_STUDY_ID);
	protected Index<Node> studyMetaNodesByOTProperty = getNodeIndex(OTINodeIndex.STUDY_METADATA_NODES_BY_OT_PROPERTY);
	protected Index<Node> treeRootNodesByTreeId = getNodeIndex(OTINodeIndex.TREE_ROOT_NODES_BY_TREE_ID_OR_STUDY_ID);
	
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
//		config = new ConfigurationManager(graphDb);
		queryRunner = new QueryRunner(graphDb);
//		updateKnownRemotesInternal();
	}

	/**
	 * Access the graph db through the given embedded db object.
	 * 
	 * @param embeddedGraph
	 */
	public DatabaseManager(EmbeddedGraphDatabase embeddedGraph) {
		super(embeddedGraph);
		indexer = new StudyIndexer(graphDb);
//		config = new ConfigurationManager(graphDb);
		queryRunner = new QueryRunner(graphDb);
//		updateKnownRemotesInternal();
	}

	/**
	 * Open the graph db through the given agent object.
	 * 
	 * @param gdb
	 */
	public DatabaseManager(GraphDatabaseAgent gdb) {
		super(gdb);
		indexer = new StudyIndexer(graphDb);
//		config = new ConfigurationManager(graphDb);
		queryRunner = new QueryRunner(graphDb);
//		updateKnownRemotesInternal();
	}

	// ========== public methods
	
	// ===== adding sources and trees
	
	/**
	 * Install a study into the db, including loading all included trees.
	 * 
	 * @param source
	 * 		A NexsonSource object that contains the source metadata and trees
	 * 
	 * @param location
	 * 		Used to indicate remote vs local studies. To recognize a study as local, pass the location
	 * 		string in DatabaseManager.LOCAL_LOCATION. Using any other value for the location will result in this study
	 * 		being treated as a remote study.
	 * 
	 * @return
	 * 		The source metadata node for the newly added study
	 * @throws DuplicateSourceException 
	 */
	public Node addSource(NexsonSource source, String location) {
		return addSource(source, location, false);
	}
	
	/**
	 * Install a study into the db, including loading all included trees.
	 * 
	 * @param study
	 * 		A NexsonSource object that contains the source metadata and trees.
	 * 
	 * @param location
	 * 		Used to indicate remote vs local studies. To recognize a study as local, pass the location
	 * 		string in DatabaseManager.LOCAL_LOCATION. Using any other value for the location will result in this study
	 * 		being treated as a remote study.
	 * 
	 * @param overwrite
	 * 		Pass a value of true to cause any preexisting studies with this location and source id to be deleted and replaced
	 * 		by this source. Otherwise the method will throw an exception if there are preexisting studies.
	 * 
	 * @return
	 * 		The source metadata node for the newly added study
	 * @throws DuplicateSourceException 
	 */
	public Node addSource(NexsonSource study, String location, boolean overwrite) {
		
		// TODO: return meaningful information about the result to the rest query that calls this method

		Node studyMeta = null;
		
		Transaction tx = graphDb.beginTx();
		try {
			
			String studyId = study.getId();

			// an attempt to add a study with the same id as an existing study overwrites the existing study
			studyMeta = DatabaseUtils.getSingleNodeIndexHit(studyMetaNodesByOTProperty, OTVocabularyPredicate.OT_STUDY_ID.propertyName(), studyId);
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
			int i = 0;
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
				String treeId = (String) tree.getObject(OTINodeProperty.PHYLOGRAFTER_ID.propertyName());
				
				// create a unique tree id by including the study id, this is the convention from treemachine
				String treeUniqueId = studyId + "_" + treeId;

				// add the tree
				addTree(tree, treeUniqueId, studyMeta);

				i++;
			}
			
/*			if (location == LOCAL_LOCATION) { // if this is a local study then attach it to any existing remotes
				for (Node sourceMetaHit : queryRunner.getRemoteSourceMetaNodesForSourceId(studyId)) {
					if (sourceMetaHit.getProperty(OTINodeProperty.LOCATION.propertyName()).equals(LOCAL_LOCATION) == false) {
						studyMeta.createRelationshipTo(sourceMetaHit, OTIRelType.LOCALCOPYOF);
					}
				}

			} else { // remote study

				// check if there is a local study to attach this remote one to
				Node localSourceMeta = DatabaseUtils.getSingleNodeIndexHit(studyMetaNodesByOTProperty, LOCAL_LOCATION + OTUConstants.SOURCE_ID_SUFFIX, studyId);
				if (localSourceMeta != null) {
					localSourceMeta.createRelationshipTo(studyMeta, OTIRelType.LOCALCOPYOF);
				}
				
				// add the remote location if necessary
				if (!knownRemotes.contains(location)) {
					addKnownRemote(location);
				}
			} */
		
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
	
	/*
	 * Make a working copy of a local tree.
	 * 
	 * @param original
	 * 		The root node of the tree to be copied
	 * @return workingRootNode
	 * 		The root node of the working copy of the tree
	 *
	public Map<String, Object> makeWorkingCopyOfTree(Node original, Long nodeIdOfInterest) {
		
		Node working = graphDb.createNode();
		
		// connect the working root to the original root
		working.createRelationshipTo(original, OTIRelType.WORKINGCOPYOF);
		working.setProperty(OTINodeProperty.IS_WORKING_COPY.propertyName(), true);
		
		// connect the working root to the source metadata node
		Relationship originalSourceMetaRel = original.getSingleRelationship(OTIRelType.METADATAFOR, Direction.INCOMING);
		Node sourceMeta = originalSourceMetaRel.getStartNode();
		sourceMeta.createRelationshipTo(working, OTIRelType.METADATAFOR);

		// copy the properties
		DatabaseUtils.copyAllProperties(original, working);
		working.removeProperty(OTINodeProperty.IS_SAVED_COPY.propertyName());

		// copy the tree itself
		copyTreeRecursive(original, working, nodeIdOfInterest);

		// update indexes
		indexer.removeTreeRootNodeFromIndexes(original);
		indexer.addTreeRootNodeToIndexes(working);

		// disconnect the original root from the source metadata node
		originalSourceMetaRel.delete();
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("working_root_node_id", working.getId());
		
		if (nodeIdOfInterest != null) {
			if (workingCopyNodeOfInterest != null) {
				result.put("node_of_interest_new_id", workingCopyNodeOfInterest.getId());
			} else {
				result.put("node_of_interest_new_id", "null");
			}
		}
		
		workingCopyNodeOfInterest = null;
		
		return result;
		
	}
	
	/**
	 * Throw away a working tree and restore the original copy
	 * 
	 * @param working
	 * 		The root node of the working tree
	 * @return
	 * 		The root node of the original tree
	 *
	public Node discardWorkingCopy(Node working) {
		
		// get the source meta node
		Relationship workingSourceMetaRel = working.getSingleRelationship(OTIRelType.METADATAFOR, Direction.INCOMING);
		Node sourceMeta = workingSourceMetaRel.getStartNode();
		
		// reattach original root to the source meta and add it back to the indexes
		Node original = working.getSingleRelationship(OTIRelType.WORKINGCOPYOF, Direction.OUTGOING).getEndNode();
		sourceMeta.createRelationshipTo(original, OTIRelType.METADATAFOR);
		indexer.addTreeRootNodeToIndexes(original);
		
		// detach the working root from the original and the source meta
		working.getSingleRelationship(OTIRelType.METADATAFOR, Direction.INCOMING).delete();
		working.getSingleRelationship(OTIRelType.WORKINGCOPYOF, Direction.OUTGOING).delete();

		// delete the working tree for good
		indexer.removeTreeRootNodeFromIndexes(working);
		deleteTree(working);

		return original;
	}

	/**
	 * Replace a saved (i.e. original) tree with its working copy, and mark the newly saved (previously working) copy as saved.
	 * 
	 * @param working
	 * 		The root node of the working tree copy to be saved
	 * @return
	 * 		The root node of the newly saved tree (same node as was passed in)
	 *
	public Node saveWorkingCopy(Node working) {

		// get the original root node
		Relationship workingCopyRel = working.getSingleRelationship(OTIRelType.WORKINGCOPYOF, Direction.OUTGOING);
		Node original = workingCopyRel.getEndNode();
		
		// detach the original root from the working and delete the original tree
		workingCopyRel.delete();
		deleteTree(original);
		
		// reassign working copy to saved copy
		working.removeProperty(OTINodeProperty.IS_WORKING_COPY.propertyName());
		working.setProperty(OTINodeProperty.IS_SAVED_COPY.propertyName(), true);

		return working;
	} */
	
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
	
	/*
	 * Reroot the tree containing the `newroot` node on that node. Returns the root node of the rerooted tree.
	 * @param newroot
	 * @return
	 *
	public Node rerootTree(Node newroot) {
		
		// first get the current root node for this tree
		Node oldRoot = QueryRunner.getRootOfTreeContaining(newroot);

		Transaction tx = graphDb.beginTx(); // TODO: should remove transactions from here. Calling classes/methods should implement these instead

		// not rerooting
		if (oldRoot == newroot) {
			try {
				oldRoot.setProperty(OTINodeProperty.ROOTING_IS_SET.propertyName(), true);
				tx.success();
			} finally {
				tx.finish();
			}
			return oldRoot;
		}
		
		Node actualRoot = null;
		String treeID = null;
		treeID = (String) oldRoot.getProperty(OTINodeProperty.TREE_ID.propertyName());
		try {
			// tritomy the root
			int oldrootchildcount = DatabaseUtils.getNumberOfRelationships(oldRoot, OTIRelType.CHILDOF, Direction.INCOMING);
					
			if (oldrootchildcount == 2) {
				boolean retvalue = tritomyRoot(oldRoot, newroot);
				if (retvalue == false) {
					tx.success();
					tx.finish();
					return oldRoot;
				}
			}
			
			// process the reroot
			actualRoot = graphDb.createNode();
			
			Relationship nrprel = newroot.getSingleRelationship(OTIRelType.CHILDOF, Direction.OUTGOING);
			Node tempParent = nrprel.getEndNode();
			actualRoot.createRelationshipTo(tempParent, OTIRelType.CHILDOF);
			nrprel.delete();
			newroot.createRelationshipTo(actualRoot, OTIRelType.CHILDOF);
			processRerootRecursive(actualRoot);

			// switch the METADATAFOR relationship to the new root node
			Relationship prevStudyToTreeRootLinkRel = oldRoot.getSingleRelationship(OTIRelType.METADATAFOR, Direction.INCOMING);
			Node metadata = prevStudyToTreeRootLinkRel.getStartNode();
			prevStudyToTreeRootLinkRel.delete();
			metadata.createRelationshipTo(actualRoot, OTIRelType.METADATAFOR);
		
//			actualRoot.setProperty(OTUNodeProperty.TREE_ID.propertyName(), treeID);

			// disconnect the current root from the saved copy of this tree
			Relationship workingCopyRel = oldRoot.getSingleRelationship(OTIRelType.WORKINGCOPYOF, Direction.OUTGOING);
			Node rootNodeOfOriginalCopy = workingCopyRel.getEndNode();
			workingCopyRel.delete();
			
			// clean up properties
			DatabaseUtils.exchangeAllProperties(oldRoot, actualRoot); // TODO: are there properties we don't want to exchange?
			actualRoot.setProperty(OTINodeProperty.ROOTING_IS_SET.propertyName(), true);
			
			// update indexes
			indexer.removeTreeRootNodeFromIndexes(oldRoot);
			indexer.addTreeRootNodeToIndexes(actualRoot);
			
			// reset the ingroup
			actualRoot.setProperty(OTINodeProperty.INGROUP_IS_SET.propertyName(), false);
			actualRoot.removeProperty(OTINodeProperty.INGROUP_START_NODE_ID.propertyName());
			for (Node child : Traversal.description().relationships(OTIRelType.CHILDOF, Direction.INCOMING).traverse(actualRoot).nodes()) {
				child.removeProperty(OTINodeProperty.IS_WITHIN_INGROUP.propertyName());
				child.removeProperty(OTINodeProperty.IS_INGROUP_ROOT.propertyName());
			}

			// reattach to the saved copy
			actualRoot.createRelationshipTo(rootNodeOfOriginalCopy, OTIRelType.WORKINGCOPYOF);
			
			tx.success();
		} finally {
			tx.finish();
		}
		
		return actualRoot;
	} */
	
	/**
	 * Set the ingroup for the tree containing `innode` to `innode`.
	 * @param innode
	 */
	public void designateIngroup(Node innode) {

		// first get the root of the old tree
		Node root = QueryRunner.getRootOfTreeContaining(innode);

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
	
	/*
	 * A recursive function to facilitate copying trees
	 * 
	 * @param original
	 * @param copy
	 *
	private void copyTreeRecursive(Node original, Node copy, Long nodeIdOfInterest) {
		
		// if this node is one we want to remember, then do that
		if (nodeIdOfInterest != null) {
			if (original.getId() == nodeIdOfInterest) {
				workingCopyNodeOfInterest = copy;
			}
		}
		
		Map<Node, Node> childrenToCopy = new HashMap<Node, Node>();
		
		for (Relationship originalChildRel : original.getRelationships(Direction.INCOMING, OTIRelType.CHILDOF)) {
			
			// make a new copy of this child node and attach it to the copy of the parent
			Node copiedChild = graphDb.createNode();
			Relationship copiedChildRel = copiedChild.createRelationshipTo(copy, OTIRelType.CHILDOF);

			// remember this child so we can copy its children
			Node originalChild = originalChildRel.getStartNode();
			childrenToCopy.put(originalChild, copiedChild);

			// copy all properties
			DatabaseUtils.copyAllProperties(originalChild, copiedChild);
			DatabaseUtils.copyAllProperties(originalChildRel, copiedChildRel);
		}
		
		// recur on the children
		for (Entry<Node, Node> nodePairToCopy : childrenToCopy.entrySet()) {
			copyTreeRecursive(nodePairToCopy.getKey(), nodePairToCopy.getValue(), nodeIdOfInterest);
		}
	} */
	
	/*
	 * Add a known remote to the graph property for known remotes, which is a primitive string array. We
	 * could also just add nodes for all remotes and index them
	 * @param remote
	 *
	private void addKnownRemote(String newRemote) {
		
		List<String> knownRemotesPrev = browser.getKnownRemotes();
		String[] knownRemotesNew = new String[knownRemotesPrev.size()+1];
		
		int i = 0;
		for (String r : knownRemotesPrev) {
			knownRemotesNew[i++] = r;
		}

		knownRemotesNew[i] = newRemote;
		graphDb.getNodeById((long)0).setProperty(OTIGraphProperty.KNOWN_REMOTES.propertyName(), knownRemotesNew);
		
		updateKnownRemotesInternal();
	} */
	
	/*
	 * Just update the internal cache of known remotes. Called when we add a remote and also during construction.
	 * We keep this cached so we don't have to check the graph property array every time we add a source.
	 *
	private void updateKnownRemotesInternal() {
		knownRemotes = new HashSet<String>();
		for (String remote : browser.getKnownRemotes()) {
			knownRemotes.add(remote);
		}
	} */
	
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
//			withinIngroup = true;
			curGraphNode.setProperty(OTINodeProperty.INGROUP_START_NODE_ID.propertyName(), true);
			lastObservedIngroupStartNode = curGraphNode;
		}
		
		// set the ingroup flag if we're within the ingroup
//		if (withinIngroup) {
//			curGraphNode.setProperty(NodeProperty.IS_WITHIN_INGROUP.propertyName(), true);
//		}
		
		// add properties
		if (curJadeNode.getName() != null) {
			curGraphNode.setProperty(OTVocabularyPredicate.OT_OTT_TAXON_NAME.propertyName(), curJadeNode.getName());
			setNodePropertiesFromMap(curGraphNode, curJadeNode.getAssoc()); // why not?
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
			curGraphNode.setProperty(OTVocabularyPredicate.OT_IS_OTU.propertyName(), true);
			
			// for otu nodes, connect them to the taxonomy
			connectTreeNodeToTaxonomy(curGraphNode);
		}

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
		
		originalTaxonNames = new ArrayList<String>();
		mappedTaxonNames = new ArrayList<String>();
		mappedTaxonNamesNoSpaces = new ArrayList<String>();
		mappedOTTIds = new ArrayList<Long>();

		for (JadeNode treeNode : tree.getRoot().getDescendantLeaves()) {

			originalTaxonNames.add((String) treeNode.getObject(OTVocabularyPredicate.OT_ORIGINAL_LABEL.propertyName()));

			if (treeNode.hasAssocObject(OTVocabularyPredicate.OT_OTT_ID.propertyName())) {
				// If the node has not been explicitly mapped, we will not record the name as a mapped name
				String name = treeNode.getName();
				mappedTaxonNames.add(name);
				mappedTaxonNamesNoSpaces.add(name.replace("\\s+", OTIConstants.WHITESPACE_SUBSTITUTE_FOR_SEARCH));
			}

			Long ottId = (Long) treeNode.getObject(OTVocabularyPredicate.OT_OTT_ID.propertyName());
			if (ottId != null) {
				mappedOTTIds.add(ottId);
			}
		}
		assignTaxonArraysToNode(node);
	}
	
	/**
	 * Collects taxonomic names and ids for all the tips of the provided JadeTree and stores this info as node properties
	 * of the provided graph node. Used to store taxonomic mapping info for the root nodes of trees in the graph.
	 * @param node
	 * @param tree
	 */
	private void collectTipTaxonArrayPropertiesFromGraph(Node node) {
		
		originalTaxonNames = new ArrayList<String>();
		mappedTaxonNames = new ArrayList<String>();
		mappedTaxonNamesNoSpaces = new ArrayList<String>();
		mappedOTTIds = new ArrayList<Long>();

		for (Node tip : QueryRunner.getDescendantTips(node)) {

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
	}
	
	/**
	 * A helper function for the collectTipTaxonArrayProperties functions. Exists only to ensure consistency and simplify code.
	 * @param node
	 */
	private void assignTaxonArraysToNode(Node node) {
		
		// store the properties we just collected
		node.setProperty(OTINodeProperty.DESCENDANT_ORIGINAL_TAXON_NAMES.propertyName(), GeneralUtils.convertToStringArray(originalTaxonNames));
		node.setProperty(OTINodeProperty.DESCENDANT_MAPPED_TAXON_NAMES.propertyName(), GeneralUtils.convertToStringArray(mappedTaxonNames));
		node.setProperty(OTINodeProperty.DESCENDANT_MAPPED_TAXON_NAMES_WHITESPACE_FILLED.propertyName(), GeneralUtils.convertToStringArray(mappedTaxonNamesNoSpaces));
		node.setProperty(OTINodeProperty.DESCENDANT_MAPPED_TAXON_OTT_IDS.propertyName(), GeneralUtils.convertToLongArray(mappedOTTIds));
		
		// clean up the mess... just to be sure we don't accidentally use this information somewhere else
		originalTaxonNames = null;
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
