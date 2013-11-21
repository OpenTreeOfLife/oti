package org.opentree.oti;

import org.opentree.graphdb.GraphDatabaseAgent;
import org.opentree.oti.constants.OTIConstants;
import org.opentree.oti.constants.OTINodeProperty;
import org.opentree.oti.constants.OTIRelType;
import org.opentree.oti.constants.SearchableProperty;
import org.opentree.properties.OTVocabularyPredicate;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;

public class StudyIndexer extends OTIDatabase {

	// tree root indexes
	public final Index<Node> treeRootNodesByTreeIdOrStudyId = getNodeIndex(OTINodeIndex.TREE_ROOT_NODES_BY_TREE_ID_OR_STUDY_ID);
	public final Index<Node> treeRootNodesByOTProperty = getNodeIndex(OTINodeIndex.TREE_ROOT_NODES_BY_OT_PROPERTY);
	
	public final Index<Node> treeRootNodesByOriginalTaxonName = getNodeIndex(OTINodeIndex.TREE_ROOT_NODES_BY_ORIGINAL_TAXON_NAME);
	public final Index<Node> treeRootNodesByMappedTaxonName = getNodeIndex(OTINodeIndex.TREE_ROOT_NODES_BY_MAPPED_TAXON_NAME);
	public final Index<Node> treeRootNodesByMappedTaxonNameNoSpaces = getNodeIndex(OTINodeIndex.TREE_ROOT_NODES_BY_MAPPED_TAXON_NAME_WHITESPACE_FILLED);
	public final Index<Node> treeRootNodesByMappedTaxonOTTId = getNodeIndex(OTINodeIndex.TREE_ROOT_NODES_BY_MAPPED_TAXON_OTT_ID);

	// source meta indexes
	public final Index<Node> studyMetaNodesByOTProperty = getNodeIndex(OTINodeIndex.STUDY_METADATA_NODES_BY_OT_PROPERTY);
	
	// TODO: make tree node indexes
	
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
//		studyMetaNodesByOTProperty.add(studyMetaNode, OTVocabularyPredicate.OT_STUDY_ID.propertyName(),
//				studyMetaNode.getProperty(OTVocabularyPredicate.OT_STUDY_ID.propertyName()));
		indexNodeBySearchableProperties(studyMetaNode, OTIConstants.SOURCE_PROPERTIES_FOR_SIMPLE_INDEXING);
	}

	/**
	 * Remove the indicated node from all source metadata node indexes.
	 */
	public void removeStudyMetaNodeFromIndexes(Node studyMetaNode) {
//		studyMetaNodesByOTProperty.remove(sourceMetaNode);
		studyMetaNodesByOTProperty.remove(studyMetaNode);
	}
		
	// ===== indexing tree root nodes

	/**
	 * Install the indicated tree root node into the indexes. Uses graph traversals and node properties set during study
	 * import, and thus should be called *after* the study has been added to the graph.
	 * 
	 * @param treeRootNode
	 */
	public void addTreeRootNodeToIndexes(Node treeRootNode) {

		treeRootNodesByTreeIdOrStudyId.add(treeRootNode, OTINodeProperty.TREE_ID.propertyName(),
				treeRootNode.getProperty(OTINodeProperty.TREE_ID.propertyName()));
		
		treeRootNodesByTreeIdOrStudyId.add(treeRootNode, OTVocabularyPredicate.OT_STUDY_ID.propertyName(),
				treeRootNode.getSingleRelationship(OTIRelType.METADATAFOR, Direction.INCOMING)
					.getEndNode().getProperty(OTVocabularyPredicate.OT_STUDY_ID.propertyName()));
		
		// add to property indexes
		indexNodeBySearchableProperties(treeRootNode, OTIConstants.TREE_PROPERTIES_FOR_SIMPLE_INDEXING);

		// add to taxonomy indexes
		addTreeToTaxonomicIndexes(treeRootNode);
	}
	
	/**
	 * Remove the indicated node from the tree root node indexes.
	 *
	 * @param treeRootNode
	 */
	public void removeTreeRootNodeFromIndexes(Node treeRootNode) {
		treeRootNodesByTreeIdOrStudyId.remove(treeRootNode);
//		treeRootNodesBySourceId.remove(treeRootNode);
		treeRootNodesByOTProperty.remove(treeRootNode);
		treeRootNodesByMappedTaxonName.remove(treeRootNode);
		treeRootNodesByMappedTaxonNameNoSpaces.remove(treeRootNode);
		treeRootNodesByMappedTaxonOTTId.remove(treeRootNode);
	}
	
	// === private methods used during tree root indexing
	
	/**
	 * Add the tree to the taxonomic indexes
	 * @param treeRootNode
	 */
	private void addTreeToTaxonomicIndexes(Node root) {
		
		addStringArrayEntriesToIndex(root,
				treeRootNodesByOriginalTaxonName,
				OTINodeProperty.DESCENDANT_ORIGINAL_TAXON_NAMES.propertyName(),
				OTVocabularyPredicate.OT_ORIGINAL_LABEL.propertyName());

		addStringArrayEntriesToIndex(root,
				treeRootNodesByMappedTaxonName,
				OTINodeProperty.DESCENDANT_MAPPED_TAXON_NAMES.propertyName(),
				OTVocabularyPredicate.OT_OTT_TAXON_NAME.propertyName());
		
		addStringArrayEntriesToIndex(root,
				treeRootNodesByMappedTaxonNameNoSpaces,
				OTINodeProperty.DESCENDANT_MAPPED_TAXON_NAMES_WHITESPACE_FILLED.propertyName(),
				OTVocabularyPredicate.OT_OTT_TAXON_NAME.propertyName());
		
		addLongArrayEntriesToIndex(root,
				treeRootNodesByMappedTaxonOTTId,
				OTINodeProperty.DESCENDANT_MAPPED_TAXON_OTT_IDS.propertyName(),
				OTVocabularyPredicate.OT_OTT_ID.propertyName());
	}
	
	// ===== generalized private methods used during indexing

	/**
	 * Index a node into the supplied index under all the specified properties.
	 * @param node
	 * @param index
	 */
	private void indexNodeBySearchableProperties(Node node, SearchableProperty[] searchablePoperties) {
		for (SearchableProperty search : searchablePoperties) {
			Index<Node> index = getNodeIndex(search.index);
			if (node.hasProperty(search.property.propertyName())) {
				index.add(node, search.property.propertyName(), node.getProperty(search.property.propertyName()));
			}
		}
	}
	
	private void addStringArrayEntriesToIndex(Node node, Index<Node> index, String nodePropertyName, String indexProperty) {
		if (node.hasProperty(nodePropertyName)) {
			String[] array = (String[]) node.getProperty(nodePropertyName);
			for (int i = 0; i < array.length; i++) {
				index.add(node, indexProperty, array[i]);
			}
		}
	}

	private void addLongArrayEntriesToIndex(Node node, Index<Node> index, String nodePropertyName, String indexProperty) {
		if (node.hasProperty(nodePropertyName)) {
			long[] array = (long[]) node.getProperty(nodePropertyName);
			for (int i = 0; i < array.length; i++) {
				index.add(node, indexProperty, array[i]);
			}
		}
	}
}
