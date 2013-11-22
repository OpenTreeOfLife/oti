package org.opentree.oti;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.opentree.graphdb.DatabaseUtils;
import org.opentree.graphdb.GraphDatabaseAgent;
import org.opentree.oti.constants.OTIRelType;
import org.opentree.oti.indexproperties.OTINodeProperty;
import org.opentree.properties.OTPropertyPredicate;
import org.opentree.properties.OTVocabularyPredicate;

public class DatabaseBrowser extends OTIDatabase {

	public final Index<Node> treeRootNodesByProperty = getNodeIndex(OTINodeIndex.TREE_ROOT_NODES_BY_PROPERTY_FULLTEXT);
	public final Index<Node> studyMetaNodesByProperty = getNodeIndex(OTINodeIndex.STUDY_METADATA_NODES_BY_PROPERTY_EXACT);
	
	private static Set<String> hiddenSourceProperties;
	private static Set<String> hiddenTreeProperties;
	private static Set<String> hiddenTreeNodeProperties;

	private static Set<String> availableSourceProperties;
	private static Set<String> availableTreeProperties;
	private static Set<String> availableTreeNodeProperties;
	
	public DatabaseBrowser(EmbeddedGraphDatabase embeddedGraph) {
		super(embeddedGraph);
		collectPropertySets();
	}

	public DatabaseBrowser(GraphDatabaseAgent gdb) {
		super(gdb);
		collectPropertySets();
	}

	public DatabaseBrowser(GraphDatabaseService graphService) {
		super(graphService);
		collectPropertySets();
	}

	public Iterable<String> getAvailableSourceProperties() {
		return availableSourceProperties;
	}

	public Iterable<String> getAvailableTreeProperties() {
		return availableTreeProperties;
	}

	public Iterable<String> getAvailableTreeNodeProperties() {
		return availableTreeNodeProperties;
	}
	
 	/**
	 * Retrieve a tree root node from the graph.
	 * @param treeId
	 * 		The id of the tree to get
	 * @return
	 * 		The root node for this tree, or null if no such tree exists
	 */
	public Node getTreeRootNode(String treeId, String location) {
		return DatabaseUtils.getSingleNodeIndexHit(treeRootNodesByProperty, OTINodeProperty.TREE_ID.propertyName(), treeId);
	}
	
	/**
	 * Retrieve a study metadata node from the graph.
	 * @param studyId
	 * 		The id of the source to get
	 * @return
	 * 		The metadata node for this study, or null if no such study exists
	 */
	public Node getSourceMetaNode(String studyId, String location) {
		return DatabaseUtils.getSingleNodeIndexHit(studyMetaNodesByProperty, OTVocabularyPredicate.OT_STUDY_ID.propertyName(), studyId);
	}
	
	/**
	 * Return a list containing the ids of all imported sources
	 * @return
	 */
	public List<String> getSourceIds() {
		return getSourceIds(null);
	}

	/**
	 * Return a list containing the ids of all imported sources, excluding those in the provided set
	 * @return
	 */
	public List<String> getSourceIds(Set<String> excludedSourceIds) {
		List<String> sourceIds = new LinkedList<String>();
		
		IndexHits<Node> sourcesFound = studyMetaNodesByProperty.query(OTVocabularyPredicate.OT_STUDY_ID.propertyName() + ":*");
		try {
			while (sourcesFound.hasNext()) {
				String sid = (String) sourcesFound.next().getProperty(OTVocabularyPredicate.OT_STUDY_ID.propertyName());
				if (excludedSourceIds != null) {
					if (!excludedSourceIds.contains(sid)) {
						sourceIds.add(sid);
					}
				}
			}
		} finally {
			sourcesFound.close();
		}
		
		return sourceIds;
	}
	
	/**
	 * Return a list containing all the tree ids for the specified source id.
	 * @return
	 */
	public List<String> getTreeIdsForSourceId(String sourceId) {
		return getTreeIdsForSourceId(sourceId, null);
	}
	
	/**
	 * Return a list containing all the tree ids for the specified source id except any tree ids that are in the excludedTreeIds variable.
	 * @return
	 */
	public List<String> getTreeIdsForSourceId(String sourceId, Set<String> excludedTreeIds) {
	
		List<String> treeIds = new LinkedList<String>();
		
		IndexHits<Node> hits = treeRootNodesByProperty.query(OTVocabularyPredicate.OT_STUDY_ID.propertyName() + ":" + sourceId);
		try {
			while (hits.hasNext()) {
				String tid = (String) hits.next().getProperty(OTINodeProperty.TREE_ID.propertyName());
				if (excludedTreeIds != null) {
					if (!excludedTreeIds.contains(tid)) {
						treeIds.add(tid);
					}
				}
			}
		} finally {
			hits.close();
		}
		
		return treeIds;
	}
	
	/**
	 * get a list of otu nodes based on a study metadatanode. Used by NexsonWriter.
	 * 
	 * Note, this may not be useful for OTI.
	 */
	public HashSet<Node> getOTUsFromMetadataNode(Node sourceMeta){
		HashSet<Node> tips =  new HashSet<Node>();
		for (Relationship rel: sourceMeta.getRelationships(Direction.OUTGOING, OTIRelType.METADATAFOR)){
			Node treeroot = rel.getEndNode();
			tips.addAll(OTIDatabaseUtils.getDescendantTips(treeroot));
		}
		return tips;
	}
	
	/**
	 * Populate the lists of available properties for searching. Used during construction.
	 */
	private void collectPropertySets() {
		
		hiddenSourceProperties = new HashSet<String>();
		hiddenTreeProperties = new HashSet<String>();
		hiddenTreeNodeProperties = new HashSet<String>();
		availableSourceProperties = new HashSet<String>();
		availableTreeProperties = new HashSet<String>();
		availableTreeNodeProperties = new HashSet<String>();
		
/*		for (OTPropertyPredicate p : OTIConstants.HIDDEN_SOURCE_PROPERTIES) {
			hiddenSourceProperties.add(p.propertyName());
		}

		for (OTPropertyPredicate p : OTIConstants.HIDDEN_TREE_PROPERTIES) {
			hiddenTreeProperties.add(p.propertyName());
		}
		
		for (OTPropertyPredicate p : OTIConstants.HIDDEN_TREE_NODE_PROPERTIES) {
			hiddenTreeNodeProperties.add(p.propertyName());
		}

		for (OTPropertyPredicate p : OTINodeProperty.values()) {
			hiddenSourceProperties.add(p.propertyName());
			hiddenTreeProperties.add(p.propertyName());
			hiddenTreeNodeProperties.add(p.propertyName());
		} */
		
		for (OTPropertyPredicate p : OTVocabularyPredicate.values()) {
			
			if (!hiddenSourceProperties.contains(p.propertyName())) {
				availableSourceProperties.add(p.propertyName());
			}
			
			if (!hiddenTreeProperties.contains(p.propertyName())) {
				availableTreeProperties.add(p.propertyName());
			}

			if (!hiddenTreeNodeProperties.contains(p.propertyName())) {
				availableTreeNodeProperties.add(p.propertyName());
			}
		}
	}
	
	/* 

	/**
	 * Return a map of relevant properties for the specified OTI node. Not used since the getTreeJSONs already provides all this
	 * data. But could be reinstated later if we need different data than what getTreeJSON provides.
	 * @param otu
	 * @return
	 *
	@Deprecated
	public Map<String, Object> getMetadataForOTU(Node otu) {
		
		Map<String, Object> metadata = new HashMap<String, Object>();

		// TODO: we may want to make this consistent with the  protected source property behavior tree root and source meta nodes
		for (OTPropertyPredicate property : OTIConstants.VISIBLE_JSON_TREE_PROPERTIES) {
			if (otu.hasProperty(property.propertyName())) {
				metadata.put(property.propertyName(), otu.getProperty(property.propertyName()));
			}
		}
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("metadata", metadata);
		
		return result;

	}
	
	/**
	 * Return a map containing the metadata for the corresponding source. Will fail if the provided node
	 * is not a source metadata node. A general purpose method that gathers information about local and remote sources.
	 * 
	 * @param sourceMeta
	 * 		The metadata node for the source
	 * @return
	 * 		A map containing information about this source
	 *
	public Map<String, Object> getMetadataForSource(Node sourceMeta) {
		
		// get properties indicated for public consumption
		Map<String, Object> metadata = new HashMap<String, Object>();
		for (String key : sourceMeta.getPropertyKeys()) {

			if (!hiddenSourceProperties.contains(key)) {
				
				Object value = (Object) "";
				if (sourceMeta.hasProperty(key)) {
					value = sourceMeta.getProperty(key);
				}
				metadata.put(key, value);
			}
		}

		// get the trees
		List<String> trees = new LinkedList<String>(); // will actually store the tree ids
		for (Relationship rel : sourceMeta.getRelationships(OTIRelType.METADATAFOR, Direction.OUTGOING)) {
			trees.add((String) rel.getEndNode().getProperty(OTINodeProperty.TREE_ID.propertyName()));
		}

		// put it together and what have you got
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("sourceId", sourceMeta.getProperty(OTVocabularyPredicate.OT_STUDY_ID.propertyName()));
		result.put("metadata", metadata);
		result.put("trees", trees);
//		result.put("has_local_copy", hasLocalCopy);
//		result.put("remotes_known", remotes);

		// bibbity bobbity boo
		return result;
	}
	 
	/*
	
	/**
	 * Get a JSON string containing tree metadata for the specified tree root node. Will fail if this node is not the
	 * root node of a tree.
	 * 
	 * @param root
	 * 		The root node of a tree
	 * @return
	 * 		A map containing information about this tree
	 *
	public static Map<String, Object> getMetadataForTree(Node root) {

		// gather properties suitable for public consumption
		Map<String, Object> metadata = new HashMap<String, Object>();
		for (String key : root.getPropertyKeys()) {

			if (!hiddenTreeProperties.contains(key)) {

				Object value = (Object) "";
				if (root.hasProperty(key)) {
					value = root.getProperty(key);
				}
				metadata.put(key, value);
			}
		}
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("treeId", root.getProperty((String) OTINodeProperty.TREE_ID.propertyName()));
		result.put("sourceId", root.getProperty((String) OTINodeProperty.STUDY_ID.propertyName()));
		result.put("metadata", metadata);
		
		return result;
	}
	
	/**
	 * Get the subtree of a given tree graph node. Does not perform error checks to make sure the tree exists.
	 * @param inRoot
	 * @param maxNodes
	 * @return
	 *
	public static JadeTree getJadeTreeForGraphNode(Node inRoot, int maxNodes) {
		
		TraversalDescription CHILDOF_TRAVERSAL = Traversal.description().relationships(OTIRelType.CHILDOF, Direction.INCOMING);
		JadeNode root = new JadeNode();
		HashMap<Node, JadeNode> traveledNodes = new HashMap<Node, JadeNode>();
//		int maxtips = maxNodes;
		HashSet<Node> includednodes = new HashSet<Node>();
		HashSet<Node> parents = new HashSet<Node>();
		for (Node curGraphNode : CHILDOF_TRAVERSAL.breadthFirst().traverse(inRoot).nodes()) {
			if (includednodes.size() > maxNodes && parents.size() > 1) {
				break;
			}
			JadeNode curNode = null;
			if (curGraphNode == inRoot) {
				curNode = root;
			} else {
				curNode = new JadeNode();
			}
			traveledNodes.put(curGraphNode, curNode);
			
			// TODO: fix this so it uses the specific taxon name properties
/*			if (curGraphNode.hasProperty(NodeProperty.NAME.propertyName())) {
				curNode.setName((String) curGraphNode.getProperty(NodeProperty.NAME.propertyName()));
				//				curNode.setName(GeneralUtils.cleanName(String.valueOf(curGraphNode.getProperty(NodeProperty.NAME.propertyName()))));
				// curNode.setName(GeneralUtils.cleanName(curNode.getName()));
			} *
			
			if (curGraphNode.hasProperty(OTVocabularyPredicate.OT_OTT_TAXON_NAME.propertyName())) {
				curNode.setName((String) curGraphNode.getProperty(OTVocabularyPredicate.OT_OTT_TAXON_NAME.propertyName()));
				
			} else if (curGraphNode.hasProperty(OTINodeProperty.NAME.propertyName())) {
				curNode.setName((String) curGraphNode.getProperty(OTINodeProperty.NAME.propertyName()));				
			}

			curNode.assocObject(OTINodeProperty.NODE_ID.propertyName(), curGraphNode.getId());
			curNode.assocObject(JadeNodeProperty.DISPLAY_PROPERTIES.propertyName(), OTIConstants.VISIBLE_JSON_TREE_PROPERTIES);

			// include tnrs information if we need to
			if (!curGraphNode.hasProperty(OTVocabularyPredicate.OT_OTT_ID.propertyName())) {

				Iterable<Relationship> tnrsHitRels = curGraphNode.getRelationships(OTIRelType.TNRSMATCHFOR);
				List<Object> tnrsHits = new LinkedList<Object>();

				for (Relationship tnrsRel : tnrsHitRels) {
					Map<String, Object> hit = new HashMap<String, Object>();
					Node tnrsNode = tnrsRel.getOtherNode(curGraphNode);
					for (String property : tnrsNode.getPropertyKeys()) {
						hit.put(property, tnrsNode.getProperty(property));
					}
					tnrsHits.add(hit);
				}
				
				if (!tnrsHits.isEmpty()) {
					curNode.assocObject("tnrsHits", tnrsHits.toArray());
				}
			}

			// add properties suitable for the JSON
			for (OTPropertyPredicate property : OTIConstants.VISIBLE_JSON_TREE_PROPERTIES) {
				if (curGraphNode.hasProperty(property.propertyName())) {
					curNode.assocObject(property.propertyName(), curGraphNode.getProperty(property.propertyName()));
				}
			}

			JadeNode parentJadeNode = null;
			Relationship incomingRel = null;

			if (curGraphNode.hasRelationship(Direction.OUTGOING, OTIRelType.CHILDOF) && curGraphNode != inRoot) {
				Node parentGraphNode = curGraphNode.getSingleRelationship(OTIRelType.CHILDOF, Direction.OUTGOING).getEndNode();
				if (includednodes.contains(parentGraphNode)) {
					includednodes.remove(parentGraphNode);
				}
				parents.add(parentGraphNode);
				if (traveledNodes.containsKey(parentGraphNode)) {
					parentJadeNode = traveledNodes.get(parentGraphNode);
					incomingRel = curGraphNode.getSingleRelationship(OTIRelType.CHILDOF, Direction.OUTGOING);
				}
			}

			// add the current node to the tree we're building
			includednodes.add(curGraphNode);
			if (parentJadeNode != null) {
				parentJadeNode.addChild(curNode);
			}
			
			// get the immediate children of the current node
//			LinkedList<Relationship> childRels = new LinkedList<Relationship>();
			int numchild = 0;
			for (Relationship childRel : curGraphNode.getRelationships(Direction.INCOMING, OTIRelType.CHILDOF)) {
//				childRels.add(childRel);
				numchild += 1;
			}
			if (numchild > 0) {
				// add a property of the jadenode if there are children, so if they aren't included in this jadetree
				// because of tree size limits, we can color the node to indicate it has children
				curNode.assocObject("haschild", true);
				curNode.assocObject("numchild", numchild);
			}
		}
		
		int nRootCrumbsAdded = 0;
		boolean going = true;
		JadeNode curJadeRoot = root;
		Node curGraphRoot = inRoot;
		while (going && nRootCrumbsAdded < 5) {
			if (curGraphRoot.hasRelationship(Direction.OUTGOING, OTIRelType.CHILDOF)) {
				Node graphRootParent = curGraphRoot.getSingleRelationship(OTIRelType.CHILDOF, Direction.OUTGOING).getEndNode();
				JadeNode jadeRootParent = new JadeNode();

				// TODO: should make method that adds properties out of the graph to a jadenode and use it here as well as above.
				if (graphRootParent.hasProperty(OTINodeProperty.NAME.propertyName())) {
					jadeRootParent.setName(GeneralUtils.cleanName(String.valueOf(graphRootParent.getProperty(OTINodeProperty.NAME.propertyName()))));
				}

				jadeRootParent.assocObject(OTINodeProperty.NODE_ID.propertyName(), graphRootParent.getId());
				jadeRootParent.assocObject(JadeNodeProperty.DISPLAY_PROPERTIES.propertyName(), OTIConstants.VISIBLE_JSON_TREE_PROPERTIES);
				
				// add properties suitable for the JSON
				for (OTPropertyPredicate property : OTIConstants.VISIBLE_JSON_TREE_PROPERTIES) {
					if (graphRootParent.hasProperty(property.propertyName())) {
						jadeRootParent.assocObject(property.propertyName(), graphRootParent.getProperty(property.propertyName()));
					}
				}
				
				jadeRootParent.addChild(curJadeRoot);
				curGraphRoot = graphRootParent;
				curJadeRoot = jadeRootParent;
				nRootCrumbsAdded++;
			} else {
				going = false;
				break;
			}
		}
		
		return new JadeTree(curJadeRoot);
	} */
	
}
