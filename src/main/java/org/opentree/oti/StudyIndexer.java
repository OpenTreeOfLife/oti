package org.opentree.oti;

import org.opentree.graphdb.GraphDatabaseAgent;
import org.opentree.graphdb.NodeIndexDescription;
import org.opentree.oti.constants.OTIConstants;
import org.opentree.oti.constants.OTIRelType;
import org.opentree.oti.indexproperties.IndexedArrayProperties;
import org.opentree.oti.indexproperties.IndexedPrimitiveProperties;
import org.opentree.oti.indexproperties.OTINodeProperty;
import org.opentree.oti.indexproperties.OTPropertyArray;
import org.opentree.properties.OTPropertyPredicate;
import org.opentree.properties.OTVocabularyPredicate;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;

public class StudyIndexer extends OTIDatabase {

	// property indexes
	public final Index<Node> studyMetaNodesByPropertyExact = getNodeIndex(OTINodeIndex.STUDY_METADATA_NODES_BY_PROPERTY_EXACT);
	public final Index<Node> studyMetaNodesByPropertyFulltext = getNodeIndex(OTINodeIndex.STUDY_METADATA_NODES_BY_PROPERTY_FULLTEXT);
	public final Index<Node> treeRootNodesByPropertyExact = getNodeIndex(OTINodeIndex.TREE_ROOT_NODES_BY_PROPERTY_EXACT);
	public final Index<Node> treeRootNodesByPropertyFulltext = getNodeIndex(OTINodeIndex.TREE_ROOT_NODES_BY_PROPERTY_FULLTEXT);
	public final Index<Node> treeNodesByPropertyExact = getNodeIndex(OTINodeIndex.TREE_NODES_BY_PROPERTY_EXACT);
	public final Index<Node> treeNodesByPropertyFulltext = getNodeIndex(OTINodeIndex.TREE_NODES_BY_PROPERTY_FULLTEXT);
		
	// ===== constructors
	
	public StudyIndexer(GraphDatabaseAgent gdba) {
		super(gdba);
	}
	
	public StudyIndexer(GraphDatabaseService gdbs) {
		super(gdbs);
	}

	// ===== indexing source metadata nodes
	
	/**
	 * Generalized method for adding source metadata nodes to indexes. This method uses properties stored in
	 * the graph during study import, and thus should be called *after* a study has been added to the graph.
	 * 
	 * requires the study to 
	 * @param studyMetaNode
	 * @param property
	 */
	public void addStudyMetaNodeToIndexes(Node studyMetaNode) {
		indexNodeByPrimitiveProperties(studyMetaNode, IndexedPrimitiveProperties.STUDIES_EXACT);
		indexNodeByPrimitiveProperties(studyMetaNode, IndexedPrimitiveProperties.STUDIES_FULLTEXT);
		indexNodeByArrayProperties(studyMetaNode, IndexedArrayProperties.STUDIES_EXACT);
		indexNodeByArrayProperties(studyMetaNode, IndexedArrayProperties.STUDIES_FULLTEXT);
	}

	/**
	 * Remove the indicated node from all source metadata node indexes.
	 */
	public void removeStudyMetaNodeFromIndexes(Node studyMetaNode) {
		studyMetaNodesByPropertyExact.remove(studyMetaNode);
		studyMetaNodesByPropertyFulltext.remove(studyMetaNode);
	}
		
	// ===== indexing tree root nodes

	/**
	 * Install the indicated tree root node into the indexes. Uses graph traversals and node properties set during study
	 * import, and thus should be called *after* the study has been added to the graph.
	 * 
	 * @param treeRootNode
	 */
	public void addTreeRootNodeToIndexes(Node treeRootNode) {

		/*

		TODO: check to make sure trees are getting indexed by ot:studyId
		
		treeRootNodesByTreeOrStudyId.add(treeRootNode, OTVocabularyPredicate.OT_STUDY_ID.propertyName(),
				treeRootNode.getSingleRelationship(OTIRelType.METADATAFOR, Direction.INCOMING)
					.getEndNode().getProperty(OTVocabularyPredicate.OT_STUDY_ID.propertyName()));
		*/
		
		indexNodeByPrimitiveProperties(treeRootNode, IndexedPrimitiveProperties.TREES_EXACT);
		indexNodeByPrimitiveProperties(treeRootNode, IndexedPrimitiveProperties.TREES_FULLTEXT);
		indexNodeByArrayProperties(treeRootNode, IndexedArrayProperties.TREES_EXACT);
		indexNodeByArrayProperties(treeRootNode, IndexedArrayProperties.TREES_FULLTEXT);
	}

	/**
	 * Remove the indicated node from the tree root node indexes.
	 *
	 * @param treeRootNode
	 */
	public void removeTreeRootNodeFromIndexes(Node treeRootNode) {
		treeRootNodesByPropertyExact.remove(treeRootNode);
		treeRootNodesByPropertyFulltext.remove(treeRootNode);
	}
	
	// ===== indexing tree nodes
	
	/**
	 * Install the indicated tree node into the indexes. Uses node properties set during study
	 * import, and thus should be called *after* the study has been added to the graph.
	 * 
	 * @param treeNode
	 */
	public void addTreeNodeToIndexes(Node treeNode) {
		indexNodeByPrimitiveProperties(treeNode, IndexedPrimitiveProperties.TREE_NODES_EXACT);
		indexNodeByPrimitiveProperties(treeNode, IndexedPrimitiveProperties.TREE_NODES_FULLTEXT);
		indexNodeByArrayProperties(treeNode, IndexedArrayProperties.TREE_NODES_EXACT);
		indexNodeByArrayProperties(treeNode, IndexedArrayProperties.TREE_NODES_FULLTEXT);
	}
	
	/**
	 * Remove the indicated node from the tree node indexes.
	 *
	 * @param treeNode
	 */
	public void removeTreeNodeFromIndexes(Node treeNode) {
		treeNodesByPropertyExact.remove(treeNode);
		treeNodesByPropertyFulltext.remove(treeNode);
	}
	
	// ===== generalized private methods used during indexing

	/**
	 * Index a node into the supplied index under each specified property and its value for the graph node.
	 * @param node
	 * @param index
	 */
	private void indexNodeByPrimitiveProperties(Node node, IndexedPrimitiveProperties indexedProperties) {
		for (OTPropertyPredicate property : indexedProperties.properties()) {
			indexSingleProperty(getNodeIndex(indexedProperties.index()), node, property.propertyName());
		}
	}
	
	/**
	 * Index a node into the supplied index under each specified property and all the values in the array stored for that property on the graph node.
	 * @param node
	 * @param index
	 */
	private void indexNodeByArrayProperties(Node node, IndexedArrayProperties indexedProperties) {
		for (OTPropertyArray property : indexedProperties.properties()) {
			
			Index<Node> index = getNodeIndex(indexedProperties.index());
			String graphArrayPropertyLabel = property.graphProperty.propertyName();
			String indexPropertyLabel = property.typeProperty.propertyName();
			Class<?> type = property.typeProperty.type();
			
			// TODO: need more complexity here for indexing various kinds of properties. booleans? datetime strings? floating point values?
			
			if (type.equals(String.class)) {
				addStringArrayEntriesToIndex(node, index, graphArrayPropertyLabel, indexPropertyLabel);

			} else if (type.equals(Long.class)) {
				addLongArrayEntriesToIndex(node, index, graphArrayPropertyLabel, indexPropertyLabel);
				
			}
		}
	}

	/**
	 * Index a node into the supplied index under the value of the specified property for that node.
	 * @param index
	 * @param node
	 * @param property
	 */
	private void indexSingleProperty(Index<Node> index, Node node, String property) {
		if (node.hasProperty(property)) {
			index.add(node, property, node.getProperty(property));
		}
	}
	
	/**
	 * Helper method
	 * @param node
	 * @param index
	 * @param graphNodePropertyLabel
	 * @param indexPropertyLabel
	 */
	private void addStringArrayEntriesToIndex(Node node, Index<Node> index, String graphNodePropertyLabel, String indexPropertyLabel) {
		if (node.hasProperty(graphNodePropertyLabel)) {
			String[] array = (String[]) node.getProperty(graphNodePropertyLabel);
			for (int i = 0; i < array.length; i++) {
				index.add(node, indexPropertyLabel, array[i]);
			}
		}
	}

	/**
	 * Helper method
	 * @param node
	 * @param index
	 * @param graphNodePropertyLabel
	 * @param indexPropertyLabel
	 */
	private void addLongArrayEntriesToIndex(Node node, Index<Node> index, String nodePropertyName, String indexProperty) {
		if (node.hasProperty(nodePropertyName)) {
			long[] array = (long[]) node.getProperty(nodePropertyName);
			for (int i = 0; i < array.length; i++) {
				index.add(node, indexProperty, array[i]);
			}
		}
	}
}
