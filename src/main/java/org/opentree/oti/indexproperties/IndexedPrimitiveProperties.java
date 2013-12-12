package org.opentree.oti.indexproperties;

import org.opentree.graphdb.NodeIndexDescription;
import org.opentree.oti.OTINodeIndex;
import org.opentree.properties.OTPropertyPredicate;
import org.opentree.properties.OTVocabularyPredicate;

/**
 * An enum listing properties to be indexed exactly for each type of search target.
 */
public enum IndexedPrimitiveProperties {

	STUDIES_EXACT (
			OTINodeIndex.STUDY_METADATA_NODES_BY_PROPERTY_EXACT,
			new OTPropertyPredicate[] {
					OTVocabularyPredicate.OT_CURATOR_NAME,
		    		OTVocabularyPredicate.OT_DATA_DEPOSIT,
					OTVocabularyPredicate.OT_PUBLICATION_REFERENCE,
					OTVocabularyPredicate.OT_STUDY_ID,
					OTVocabularyPredicate.OT_STUDY_PUBLICATION,
					OTVocabularyPredicate.OT_TAG,
					OTVocabularyPredicate.OT_YEAR,
					OTINodeProperty.IS_DEPRECATED,
	}),
	
	STUDIES_FULLTEXT (
			OTINodeIndex.STUDY_METADATA_NODES_BY_PROPERTY_FULLTEXT,
			new OTPropertyPredicate[] {
					OTVocabularyPredicate.OT_CURATOR_NAME,
					OTVocabularyPredicate.OT_PUBLICATION_REFERENCE,
					OTVocabularyPredicate.OT_TAG,
	}),
	
	TREES_EXACT (
			OTINodeIndex.TREE_ROOT_NODES_BY_PROPERTY_EXACT,
			new OTPropertyPredicate[] {
					OTVocabularyPredicate.OT_BRANCH_LENGTH_MODE,
					OTVocabularyPredicate.OT_ORIGINAL_LABEL,
		    		OTVocabularyPredicate.OT_OTT_ID,
		    		OTVocabularyPredicate.OT_OTT_TAXON_NAME,
					OTVocabularyPredicate.OT_STUDY_ID,
					OTVocabularyPredicate.OT_TAG,
					OTINodeProperty.TREE_ID,
					OTINodeProperty.IS_DEPRECATED,
	}),
	
	TREES_FULLTEXT (
			OTINodeIndex.TREE_ROOT_NODES_BY_PROPERTY_FULLTEXT,
			new OTPropertyPredicate[] {
					OTVocabularyPredicate.OT_ORIGINAL_LABEL,
		    		OTVocabularyPredicate.OT_OTT_TAXON_NAME,
					OTVocabularyPredicate.OT_TAG,
	}),
    
	TREE_NODES_EXACT (
			OTINodeIndex.TREE_NODES_BY_PROPERTY_EXACT,
			new OTPropertyPredicate[] {
					OTVocabularyPredicate.OT_ORIGINAL_LABEL,
		    		OTVocabularyPredicate.OT_OTT_ID,
		    		OTVocabularyPredicate.OT_OTT_TAXON_NAME,
					OTVocabularyPredicate.OT_TAG,
	}),
	
	TREE_NODES_FULLTEXT (
			OTINodeIndex.TREE_NODES_BY_PROPERTY_FULLTEXT,
			new OTPropertyPredicate[] {
					OTVocabularyPredicate.OT_ORIGINAL_LABEL,
		    		OTVocabularyPredicate.OT_OTT_TAXON_NAME,
					OTVocabularyPredicate.OT_TAG,
	}),

	;
	
	private final OTPropertyPredicate[] properties;
	private final NodeIndexDescription index;

    IndexedPrimitiveProperties(NodeIndexDescription index, OTPropertyPredicate[] properties) {
        this.properties = properties;
        this.index = index;
    }

	public OTPropertyPredicate[] properties() {
		return properties;
	}

	public NodeIndexDescription index() {
		return index;
	}
	
}
