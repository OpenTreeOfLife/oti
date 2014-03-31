package org.opentree.oti.indexproperties;

import java.util.HashSet;
import java.util.Set;

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
					OTVocabularyPredicate.OT_AUTHOR_CONTRIBUTED,
					OTVocabularyPredicate.OT_COMMENT,
					OTVocabularyPredicate.OT_CURATOR_NAME,
			    		OTVocabularyPredicate.OT_DATA_DEPOSIT,
			    		OTVocabularyPredicate.OT_FOCAL_CLADE, // a study property?
		    			OTVocabularyPredicate.OT_FOCAL_CLADE_OTT_TAXON_NAME,
		    			OTVocabularyPredicate.OT_FOCAL_CLADE_TAXON_NAME,
		    			OTVocabularyPredicate.OT_FOCAL_CLADE_OTT_TAXON_ID,
					OTVocabularyPredicate.OT_PUBLICATION_REFERENCE,
					OTVocabularyPredicate.OT_STUDY_ID,
					OTVocabularyPredicate.OT_STUDY_LABEL,
					OTVocabularyPredicate.OT_STUDY_LAST_EDITOR,
					OTVocabularyPredicate.OT_STUDY_MODIFIED,
					OTVocabularyPredicate.OT_STUDY_PUBLICATION,
					OTVocabularyPredicate.OT_STUDY_UPLOADED,
					OTVocabularyPredicate.OT_TAG,
					OTVocabularyPredicate.OT_YEAR,
					OTINodeProperty.IS_DEPRECATED,
	}),
	
	STUDIES_FULLTEXT (
			OTINodeIndex.STUDY_METADATA_NODES_BY_PROPERTY_FULLTEXT,
			new OTPropertyPredicate[] {
					OTVocabularyPredicate.OT_COMMENT,
					OTVocabularyPredicate.OT_CURATOR_NAME,
					OTVocabularyPredicate.OT_FOCAL_CLADE_OTT_TAXON_NAME,
					OTVocabularyPredicate.OT_FOCAL_CLADE_TAXON_NAME,
					OTVocabularyPredicate.OT_PUBLICATION_REFERENCE,
					OTVocabularyPredicate.OT_STUDY_LABEL,
					OTVocabularyPredicate.OT_TAG,
	}),
	
	TREES_EXACT (
			OTINodeIndex.TREE_ROOT_NODES_BY_PROPERTY_EXACT,
			new OTPropertyPredicate[] {
					OTVocabularyPredicate.OT_BRANCH_LENGTH_DESCRIPTION,
					OTVocabularyPredicate.OT_BRANCH_LENGTH_TIME_UNITS,
					OTVocabularyPredicate.OT_BRANCH_LENGTH_MODE,
					OTVocabularyPredicate.OT_COMMENT,
//					OTVocabularyPredicate.OT_FOCAL_CLADE, // a study property?
					OTVocabularyPredicate.OT_INFERENCE_METHOD,
//					OTVocabularyPredicate.OT_INGROUP_CLADE, // this seems unnecessary; it's a nexson id only meaningful in the context of the file
					OTVocabularyPredicate.OT_NODE_LABEL_DESCRIPTION,
					OTVocabularyPredicate.OT_NODE_LABEL_MODE,
					OTVocabularyPredicate.OT_ORIGINAL_LABEL,
		    		OTVocabularyPredicate.OT_OTT_ID,
		    		OTVocabularyPredicate.OT_OTT_TAXON_NAME,
//		    		OTVocabularyPredicate.OT_SPECIFIED_ROOT, // this seems unnecessary; it's a nexson id only meaningful in the context of the file
					OTVocabularyPredicate.OT_STUDY_ID,
					OTVocabularyPredicate.OT_TAG,
					OTVocabularyPredicate.OT_TREE_LAST_EDITED,
					OTVocabularyPredicate.OT_TREE_MODIFIED,
					OTVocabularyPredicate.OT_TREEBASE_TREE_ID,
					OTINodeProperty.TREE_ID,
					OTINodeProperty.IS_DEPRECATED,
	}),
	
	TREES_FULLTEXT (
			OTINodeIndex.TREE_ROOT_NODES_BY_PROPERTY_FULLTEXT,
			new OTPropertyPredicate[] {
					OTVocabularyPredicate.OT_BRANCH_LENGTH_DESCRIPTION,
					OTVocabularyPredicate.OT_BRANCH_LENGTH_TIME_UNITS,
					OTVocabularyPredicate.OT_BRANCH_LENGTH_MODE,
					OTVocabularyPredicate.OT_COMMENT,
					OTVocabularyPredicate.OT_INFERENCE_METHOD,
					OTVocabularyPredicate.OT_NODE_LABEL_DESCRIPTION,
					OTVocabularyPredicate.OT_NODE_LABEL_MODE,
					OTVocabularyPredicate.OT_ORIGINAL_LABEL,
		    		OTVocabularyPredicate.OT_OTT_TAXON_NAME,
					OTVocabularyPredicate.OT_TAG,
	}),
    
	TREE_NODES_EXACT (
			OTINodeIndex.TREE_NODES_BY_PROPERTY_EXACT,
			new OTPropertyPredicate[] {
					OTVocabularyPredicate.OT_AGE,
					OTVocabularyPredicate.OT_AGE_MIN,
					OTVocabularyPredicate.OT_AGE_MAX,
					OTVocabularyPredicate.OT_NODE_LABEL,
					OTVocabularyPredicate.OT_COMMENT,
					OTVocabularyPredicate.OT_IS_INGROUP,
					OTVocabularyPredicate.OT_IS_LEAF,
					OTVocabularyPredicate.OT_ORIGINAL_LABEL, // otu property, might need to implement otus separately from nodes
					OTVocabularyPredicate.OT_OTT_ID, // otu property, might need to implement otus separately from nodes
					OTVocabularyPredicate.OT_OTT_TAXON_NAME, // otu property, might need to implement otus separately from nodes
					OTVocabularyPredicate.OT_PARENT,
					OTVocabularyPredicate.OT_TAG,
					OTVocabularyPredicate.OT_TREEBASE_OTU_ID, // otu property, might need to implement otus separately from nodes
	}),
	
	TREE_NODES_FULLTEXT (
			OTINodeIndex.TREE_NODES_BY_PROPERTY_FULLTEXT,
			new OTPropertyPredicate[] {
					OTVocabularyPredicate.OT_NODE_LABEL,
					OTVocabularyPredicate.OT_COMMENT,
					OTVocabularyPredicate.OT_ORIGINAL_LABEL, // otu property, might need to implement otus separately from nodes
		    		OTVocabularyPredicate.OT_OTT_TAXON_NAME, // otu property, might need to implement otus separately from nodes
					OTVocabularyPredicate.OT_TAG,
	}),

	;
	
	private final NodeIndexDescription index;
	private final HashSet<OTPropertyPredicate> properties;

    IndexedPrimitiveProperties(NodeIndexDescription index, OTPropertyPredicate[] propertiesArr) {
        this.index = index;
        this.properties = new HashSet<OTPropertyPredicate>();
        for (OTPropertyPredicate p : propertiesArr) {
        	this.properties.add(p);
        }
    }

	public Set<OTPropertyPredicate> properties() {
		return properties;
	}

	public NodeIndexDescription index() {
		return index;
	}
	
}
