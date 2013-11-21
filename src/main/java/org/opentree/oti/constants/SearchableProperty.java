package org.opentree.oti.constants;

import org.opentree.graphdb.NodeIndexDescription;
import org.opentree.oti.OTINodeIndex;
import org.opentree.properties.OTPropertyPredicate;
import org.opentree.properties.OTVocabularyPredicate;

/**
 * An enum containing mappings identifying node properties and the indexes for which they are searchable.
 */
public enum SearchableProperty {

	// ===== source meta nodes
	
	CURATOR_NAME(
			"studies by curator name",
			OTVocabularyPredicate.OT_CURATOR_NAME,
			OTINodeIndex.STUDY_METADATA_NODES_BY_OT_PROPERTY),
			
    DATA_DEPOSIT(
    		"studies by data deposit field",
    		OTVocabularyPredicate.OT_DATA_DEPOSIT,
    		OTINodeIndex.STUDY_METADATA_NODES_BY_OT_PROPERTY),
    		
	PUBLICATION_REFERENCE (
			"studies by text citation field (ot:publicationRef)",
			OTVocabularyPredicate.OT_PUBLICATION_REFERENCE,
			OTINodeIndex.STUDY_METADATA_NODES_BY_OT_PROPERTY),
			
	STUDY_ID (
			"studies by study id",
			OTVocabularyPredicate.OT_STUDY_ID,
			OTINodeIndex.STUDY_METADATA_NODES_BY_OT_PROPERTY),
			
	STUDY_PUBLICATION (
			"studies by study pub (ot:studyPublication)",
			OTVocabularyPredicate.OT_STUDY_PUBLICATION,
			OTINodeIndex.STUDY_METADATA_NODES_BY_OT_PROPERTY),

	YEAR (
			"studies by year",
			OTVocabularyPredicate.OT_YEAR,
			OTINodeIndex.STUDY_METADATA_NODES_BY_OT_PROPERTY),
    
	TAG_SOURCE (
			"studies by tag",
			OTVocabularyPredicate.OT_TAG,
			OTINodeIndex.STUDY_METADATA_NODES_BY_OT_PROPERTY),
			
    // ===== tree root nodes

	ORIGINAL_TIP_LABEL_TREE (
    		"trees by original taxon name",
    		OTVocabularyPredicate.OT_ORIGINAL_LABEL,
    		OTINodeIndex.TREE_ROOT_NODES_BY_ORIGINAL_TAXON_NAME),
    		
/*    DESCENDANT_MAPPED_TAXON_NAMES (
    		"current taxon name (mapped?)",
    		OTUNodeProperty.NAME,
    		OTUNodeIndex.TREE_ROOT_NODES_BY_MAPPED_TAXON_NAME), */

    MAPPED_OTT_TAXON_NAME_TREE (
    		"trees by mapped ott taxon name",
    		OTVocabularyPredicate.OT_OTT_TAXON_NAME,
    		OTINodeIndex.TREE_ROOT_NODES_BY_MAPPED_TAXON_NAME),
    		
    MAPPED_OTT_ID_TREE (
    		"trees by mapped ott id",
    		OTVocabularyPredicate.OT_OTT_ID,
    		OTINodeIndex.TREE_ROOT_NODES_BY_MAPPED_TAXON_OTT_ID),
    
	BRANCH_LENGTH_MODE (
			"trees by branch length mode",
			OTVocabularyPredicate.OT_BRANCH_LENGTH_MODE,
			OTINodeIndex.TREE_ROOT_NODES_BY_OT_PROPERTY),
	
	TAG_TREE (
			"trees by tag",
			OTVocabularyPredicate.OT_TAG,
			OTINodeIndex.TREE_ROOT_NODES_BY_OT_PROPERTY),
    
    // ===== tree nodes
	
	// TODO: other properties for tree nodes?
			
	ORIGINAL_TIP_LABEL_NODE (
			"tree tip nodes by original label",
			OTVocabularyPredicate.OT_ORIGINAL_LABEL,
			OTINodeIndex.TREE_NODES_BY_OT_PROPERTY),
			
	MAPPED_OTT_TAXON_NAME_NODE (
			"tree tip nodes by mapped taxon name",
			OTVocabularyPredicate.OT_OTT_TAXON_NAME,
			OTINodeIndex.TREE_NODES_BY_OT_PROPERTY),

	MAPPED_OTT_ID_NODE (
			"tree tip nodes by mapped ott id",
			OTVocabularyPredicate.OT_OTT_ID,
			OTINodeIndex.TREE_NODES_BY_OT_PROPERTY),

				
	;

	
	public final String shortName;
    public final OTPropertyPredicate property;
    public final NodeIndexDescription index;
    
    SearchableProperty(String shortName, OTPropertyPredicate property, NodeIndexDescription indexDesc) {
    	this.shortName = shortName;
        this.property = property;
        this.index = indexDesc;
    }
}
