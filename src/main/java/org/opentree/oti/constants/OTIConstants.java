package org.opentree.oti.constants;

import java.util.HashMap;
import java.util.List;

import org.opentree.oti.indexproperties.IndexedPrimitiveProperties;
import org.opentree.properties.OTPropertyPredicate;
import org.opentree.properties.OTVocabularyObject;
import org.opentree.properties.OTVocabularyPredicate;

/*
IS_SOURCE_META
NAME
TREE_ID 
SOURCE_ID 
LOCATION 
NODE_ID 
DESCENDANT_ORIGINAL_TAXON_NAMES 
DESCENDANT_MAPPED_TAXON_NAMES 
DESCENDANT_MAPPED_TAXON_NAMES_WHITESPACE_FILLED 
DESCENDANT_MAPPED_TAXON_OTT_IDS 
FOCAL_CLADE 
ROOTING_IS_SET 
IS_ROOT 
INGROUP_IS_SET 
INGROUP_START_NODE_ID 
IS_WORKING_COPY 
IS_SAVED_COPY 
PROCESSED_BY_TNRS 
CONTEXT_NAME 
NEXSON_ID 
PHYLOGRAFTER_ID 
IS_INGROUP_ROOT 
IS_WITHIN_INGROUP
*/

/*
	OT_AGE
	OT_AGE_MIN 
	OT_AGE_MAX 
	OT_AUTHOR_CONTRIBUTED 
	OT_BRANCH_LENGTH_DESCRIPTION 
	OT_BRANCH_LENGTH_MODE
	OT_NODE_LABEL 
	OT_NODE_LABEL_DESCRIPTION 
	OT_NODE_LABEL_MODE 
	OT_COMMENT 
	OT_CURATOR_NAME
	OT_DATA_DEPOSIT
	OT_FOCAL_CLADE 
	OT_INFERENCE_METHOD 
	OT_INGROUP_CLADE
	OT_IS_INGROUP 
	OT_IS_OTU 
	OT_ORIGINAL_LABEL 
	OT_OTT_ID 
	OT_OTT_TAXON_NAME 
	OT_PARENT 
    OT_PUBLICATION_REFERENCE 
    OT_STUDY_ID 
    OT_STUDY_LABEL 
    OT_STUDY_LAST_EDITOR 
    OT_STUDY_MODIFIED 
    OT_STUDY_UPLOADED 
    OT_STUDY_PUBLICATION
    OT_TAG 
    OT_TREEBASE_ID 
    OT_TREEBASE_OTU_ID 
    OT_TREE_LAST_EDITED
    OT_TREE_MODIFIED 
    OT_YEAR
 */

public enum OTIConstants {
	
	;

//	public static final String SOURCE_ID_SUFFIX = "SourceId";
//	public static final String TREE_ID_SUFFIX = "TreeId";
//	public static final String LOCAL_TREEID_PREFIX = "__local_id_";

//	public static final String WHITESPACE_SUBSTITUTE_FOR_SEARCH = "%s%";

	/* 

	
	/**
	 * All tree root node properties not specified here are fair game for user editing
	 *
	public static final OTPropertyPredicate[] HIDDEN_TREE_PROPERTIES = {

		// ot namespace properties
		OTVocabularyPredicate.OT_AGE,
		OTVocabularyPredicate.OT_AGE_MAX,
		OTVocabularyPredicate.OT_AGE_MIN,
		OTVocabularyPredicate.OT_AUTHOR_CONTRIBUTED,
		OTVocabularyPredicate.OT_CURATOR_NAME,
		OTVocabularyPredicate.OT_DATA_DEPOSIT,
		OTVocabularyPredicate.OT_FOCAL_CLADE, // TODO: make this settable via the node editor
		OTVocabularyPredicate.OT_INGROUP_CLADE,
		OTVocabularyPredicate.OT_IS_INGROUP,
		OTVocabularyPredicate.OT_IS_OTU,
		OTVocabularyPredicate.OT_NODE_LABEL,
		OTVocabularyPredicate.OT_ORIGINAL_LABEL,
		OTVocabularyPredicate.OT_OTT_ID,
		OTVocabularyPredicate.OT_OTT_TAXON_NAME,
		OTVocabularyPredicate.OT_PARENT,
		OTVocabularyPredicate.OT_PUBLICATION_REFERENCE,
		OTVocabularyPredicate.OT_SPECIFIED_ROOT,
		OTVocabularyPredicate.OT_STUDY_ID,
		OTVocabularyPredicate.OT_STUDY_LABEL,
		OTVocabularyPredicate.OT_STUDY_LAST_EDITOR,
		OTVocabularyPredicate.OT_STUDY_MODIFIED,
		OTVocabularyPredicate.OT_STUDY_PUBLICATION,
		OTVocabularyPredicate.OT_STUDY_UPLOADED,
		OTVocabularyPredicate.OT_TREE_LAST_EDITED,
		OTVocabularyPredicate.OT_TREE_MODIFIED,
		OTVocabularyPredicate.OT_TREEBASE_OTU_ID,
		OTVocabularyPredicate.OT_YEAR,
		
		/* not protected
		OT_BRANCH_LENGTH_DESCRIPTION 
		OT_BRANCH_LENGTH_MODE
		OT_BRANCH_LENGTH_TIME_UNITS
		OT_COMMENT 
		OT_INFERENCE_METHOD 
		OT_NODE_LABEL_DESCRIPTION 
		OT_NODE_LABEL_MODE 
	    OT_TAG 
	    OT_TREEBASE_ID *
		
	};

	/**
	 * All source meta node properties not specified here are fair game for user editing
	 *
	public static final OTPropertyPredicate[] HIDDEN_SOURCE_PROPERTIES = {
		
		// ot namespace properties
		OTVocabularyPredicate.OT_AGE,
		OTVocabularyPredicate.OT_AGE_MIN,
		OTVocabularyPredicate.OT_AGE_MAX,
		OTVocabularyPredicate.OT_BRANCH_LENGTH_DESCRIPTION,
		OTVocabularyPredicate.OT_BRANCH_LENGTH_MODE,
		OTVocabularyPredicate.OT_BRANCH_LENGTH_TIME_UNITS,
		OTVocabularyPredicate.OT_NODE_LABEL,
		OTVocabularyPredicate.OT_NODE_LABEL_DESCRIPTION,
		OTVocabularyPredicate.OT_NODE_LABEL_MODE,
		OTVocabularyPredicate.OT_FOCAL_CLADE,
		OTVocabularyPredicate.OT_INFERENCE_METHOD,
		OTVocabularyPredicate.OT_INGROUP_CLADE,
		OTVocabularyPredicate.OT_IS_INGROUP,
		OTVocabularyPredicate.OT_IS_OTU,
		OTVocabularyPredicate.OT_ORIGINAL_LABEL,
		OTVocabularyPredicate.OT_OTT_ID,
		OTVocabularyPredicate.OT_OTT_TAXON_NAME,
		OTVocabularyPredicate.OT_PARENT,
		OTVocabularyPredicate.OT_SPECIFIED_ROOT,
		OTVocabularyPredicate.OT_STUDY_LAST_EDITOR,
		OTVocabularyPredicate.OT_STUDY_MODIFIED,
		OTVocabularyPredicate.OT_STUDY_UPLOADED,
	    OTVocabularyPredicate.OT_TREEBASE_TREE_ID, 
		OTVocabularyPredicate.OT_TREEBASE_OTU_ID,
		OTVocabularyPredicate.OT_TREE_LAST_EDITED,
		OTVocabularyPredicate.OT_TREE_MODIFIED

	    /* not protected
		OT_AUTHOR_CONTRIBUTED 
		OT_COMMENT 
		OT_CURATOR_NAME
		OT_DATA_DEPOSIT
	    OT_PUBLICATION_REFERENCE 
	    OT_STUDY_ID 
	    OT_STUDY_LABEL 
	    OT_STUDY_PUBLICATION
	    OT_TAG 
	    OT_YEAR *
	};

	/**
	 * All tree node properties not specified here are fair game for user editing
	 *
	public static final OTPropertyPredicate[] HIDDEN_TREE_NODE_PROPERTIES = {		
				
		// ot namespace properties
		OTVocabularyPredicate.OT_AGE,
		OTVocabularyPredicate.OT_AGE_MIN,
		OTVocabularyPredicate.OT_AGE_MAX,
		OTVocabularyPredicate.OT_AUTHOR_CONTRIBUTED,
		OTVocabularyPredicate.OT_BRANCH_LENGTH_DESCRIPTION,
		OTVocabularyPredicate.OT_BRANCH_LENGTH_MODE,
		OTVocabularyPredicate.OT_BRANCH_LENGTH_TIME_UNITS,
		OTVocabularyPredicate.OT_NODE_LABEL_DESCRIPTION,
		OTVocabularyPredicate.OT_NODE_LABEL_MODE,
		OTVocabularyPredicate.OT_CURATOR_NAME,
		OTVocabularyPredicate.OT_DATA_DEPOSIT,
		OTVocabularyPredicate.OT_FOCAL_CLADE,
		OTVocabularyPredicate.OT_INFERENCE_METHOD,
		OTVocabularyPredicate.OT_INGROUP_CLADE,
		OTVocabularyPredicate.OT_IS_INGROUP,
		OTVocabularyPredicate.OT_IS_OTU,
		OTVocabularyPredicate.OT_ORIGINAL_LABEL,
		OTVocabularyPredicate.OT_OTT_ID,
		OTVocabularyPredicate.OT_OTT_TAXON_NAME,
		OTVocabularyPredicate.OT_PARENT,
		OTVocabularyPredicate.OT_PUBLICATION_REFERENCE,
		OTVocabularyPredicate.OT_SPECIFIED_ROOT,
		OTVocabularyPredicate.OT_STUDY_ID,
		OTVocabularyPredicate.OT_STUDY_LABEL,
		OTVocabularyPredicate.OT_STUDY_LAST_EDITOR,
		OTVocabularyPredicate.OT_STUDY_MODIFIED,
		OTVocabularyPredicate.OT_STUDY_UPLOADED,
		OTVocabularyPredicate.OT_STUDY_PUBLICATION,
		OTVocabularyPredicate.OT_TREEBASE_TREE_ID,
		OTVocabularyPredicate.OT_TREEBASE_OTU_ID,
		OTVocabularyPredicate.OT_TREE_LAST_EDITED,
		OTVocabularyPredicate.OT_TREE_MODIFIED,
		OTVocabularyPredicate.OT_YEAR
		
		/* not protected
		OT_NODE_LABEL 
		OT_COMMENT 
	    OT_TAG *
	};
	
	
	/**
	 * Properties to be included in the JSON generated by the JadeTreeToRepresentationConverter
	 *
	public static final OTPropertyPredicate[] VISIBLE_JSON_TREE_PROPERTIES = {

		// OTU-specific properties
		OTUNodeProperty.NAME, // TODO: need to change to use the correct taxon names
		OTUNodeProperty.NODE_ID,
		OTUNodeProperty.IS_WITHIN_INGROUP,
		OTUNodeProperty.PROCESSED_BY_TNRS,
		OTUNodeProperty.IS_SAVED_COPY,
		OTUNodeProperty.IS_WORKING_COPY,		

		// ot namespace properties
		OTVocabularyPredicate.OT_AGE,
		OTVocabularyPredicate.OT_AGE_MIN,
		OTVocabularyPredicate.OT_AGE_MAX,
		OTVocabularyPredicate.OT_NODE_LABEL,
		OTVocabularyPredicate.OT_COMMENT,
		OTVocabularyPredicate.OT_IS_OTU,
		OTVocabularyPredicate.OT_ORIGINAL_LABEL,
		OTVocabularyPredicate.OT_OTT_ID,
		OTVocabularyPredicate.OT_OTT_TAXON_NAME,
		OTVocabularyPredicate.OT_TAG,
		OTVocabularyPredicate.OT_TREEBASE_OTU_ID

		/* not visible
		OT_AUTHOR_CONTRIBUTED 
		OT_BRANCH_LENGTH_DESCRIPTION 
		OT_BRANCH_LENGTH_MODE
		OT_NODE_LABEL_DESCRIPTION 
		OT_NODE_LABEL_MODE 
		OT_CURATOR_NAME
		OT_DATA_DEPOSIT
		OT_FOCAL_CLADE 
		OT_INFERENCE_METHOD 
		OT_INGROUP_CLADE
		OT_IS_INGROUP 
		OT_PARENT 
	    OT_PUBLICATION_REFERENCE 
		OT_SPECIFIED_ROOT
	    OT_STUDY_ID 
	    OT_STUDY_LABEL 
	    OT_STUDY_LAST_EDITOR 
	    OT_STUDY_MODIFIED 
	    OT_STUDY_UPLOADED 
	    OT_STUDY_PUBLICATION
	    OT_TREEBASE_ID 
	    OT_TREE_LAST_EDITED
	    OT_TREE_MODIFIED 
	    OT_YEAR *
		
	}; */

	/*
	 * Properties to be indexed exactly under the defined property name (there may be others that we
	 * index in other ways, see StudyIndexer for examples).
	 *
	public static final IndexProperties[] TREE_NODE_PROPERTIES_FOR_SIMPLE_INDEXING = {
		SearchablePropertyForTreeNodes.ORIGINAL_LABEL,
		SearchablePropertyForTreeNodes.MAPPED_OTT_TAXON_NAME,
		SearchablePropertyForTreeNodes.MAPPED_OTT_ID,
		
		/* TODO: add to SearchableProperty enum to enable searching these
		OT_AGE
		OT_AGE_MIN 
		OT_AGE_MAX 
		OT_AUTHOR_CONTRIBUTED 
		OT_BRANCH_LENGTH_DESCRIPTION 
		OT_BRANCH_LENGTH_MODE
		OT_NODE_LABEL 
		OT_NODE_LABEL_DESCRIPTION 
		OT_NODE_LABEL_MODE 
		OT_COMMENT 
		OT_CURATOR_NAME
		OT_DATA_DEPOSIT
		OT_FOCAL_CLADE 
		OT_INFERENCE_METHOD 
		OT_INGROUP_CLADE
		OT_IS_INGROUP 
		OT_IS_OTU 
		OT_ORIGINAL_LABEL 
		OT_OTT_ID 
		OT_OTT_TAXON_NAME 
		OT_PARENT 
	    OT_PUBLICATION_REFERENCE 
	    OT_STUDY_ID 
	    OT_STUDY_LABEL 
	    OT_STUDY_LAST_EDITOR 
	    OT_STUDY_MODIFIED 
	    OT_STUDY_UPLOADED 
	    OT_STUDY_PUBLICATION
	    OT_TAG 
	    OT_TREEBASE_ID 
	    OT_TREEBASE_OTU_ID 
	    OT_TREE_LAST_EDITED
	    OT_TREE_MODIFIED 
	    OT_YEAR
	 *
		
	};
	
	/*
	 * Properties to be indexed exactly under the defined property name (there may be others that we
	 * index in other ways, see StudyIndexer for examples).
	 *
	public static final IndexProperties[] TREE_PROPERTIES_FOR_SIMPLE_INDEXING = {
		SearchablePropertyForTrees.BRANCH_LENGTH_MODE,
		SearchablePropertyForTrees.TAG
		
		/* TODO: add to SearchableProperty enum to enable searching these
		OT_AGE
		OT_AGE_MIN 
		OT_AGE_MAX 
		OT_AUTHOR_CONTRIBUTED 
		OT_BRANCH_LENGTH_DESCRIPTION 
		OT_BRANCH_LENGTH_MODE
		OT_NODE_LABEL 
		OT_NODE_LABEL_DESCRIPTION 
		OT_NODE_LABEL_MODE 
		OT_COMMENT 
		OT_CURATOR_NAME
		OT_DATA_DEPOSIT
		OT_FOCAL_CLADE 
		OT_INFERENCE_METHOD 
		OT_INGROUP_CLADE
		OT_IS_INGROUP 
		OT_IS_OTU 
		OT_ORIGINAL_LABEL 
		OT_OTT_ID 
		OT_OTT_TAXON_NAME 
		OT_PARENT 
	    OT_PUBLICATION_REFERENCE 
	    OT_STUDY_ID 
	    OT_STUDY_LABEL 
	    OT_STUDY_LAST_EDITOR 
	    OT_STUDY_MODIFIED 
	    OT_STUDY_UPLOADED 
	    OT_STUDY_PUBLICATION
	    OT_TAG 
	    OT_TREEBASE_ID 
	    OT_TREEBASE_OTU_ID 
	    OT_TREE_LAST_EDITED
	    OT_TREE_MODIFIED 
	    OT_YEAR
	 *
		
	};
	
	/**
	 * Properties to be indexed exactly under the defined property name (there may be others that we
	 * index in other ways, see StudyIndexer for examples).
	 *
	public static final IndexProperties[] STUDY_PROPERTIES_FOR_SIMPLE_INDEXING = {
		SearchablePropertyForStudies.CURATOR_NAME,
		SearchablePropertyForStudies.DATA_DEPOSIT,
		SearchablePropertyForStudies.PUBLICATION_REFERENCE,
		SearchablePropertyForStudies.STUDY_ID,
		SearchablePropertyForStudies.STUDY_PUBLICATION,
		SearchablePropertyForStudies.YEAR,
		SearchablePropertyForStudies.TAG
		
		/* TODO: add to SearchableProperty enum to enable searching these
		OT_AGE
		OT_AGE_MIN 
		OT_AGE_MAX 
		OT_AUTHOR_CONTRIBUTED 
		OT_BRANCH_LENGTH_DESCRIPTION 
		OT_BRANCH_LENGTH_MODE
		OT_NODE_LABEL 
		OT_NODE_LABEL_DESCRIPTION 
		OT_NODE_LABEL_MODE 
		OT_COMMENT 
		OT_CURATOR_NAME
		OT_DATA_DEPOSIT
		OT_FOCAL_CLADE 
		OT_INFERENCE_METHOD 
		OT_INGROUP_CLADE
		OT_IS_INGROUP 
		OT_IS_OTU 
		OT_ORIGINAL_LABEL 
		OT_OTT_ID 
		OT_OTT_TAXON_NAME 
		OT_PARENT 
	    OT_PUBLICATION_REFERENCE 
	    OT_STUDY_ID 
	    OT_STUDY_LABEL 
	    OT_STUDY_LAST_EDITOR 
	    OT_STUDY_MODIFIED 
	    OT_STUDY_UPLOADED 
	    OT_STUDY_PUBLICATION
	    OT_TAG 
	    OT_TREEBASE_ID 
	    OT_TREEBASE_OTU_ID 
	    OT_TREE_LAST_EDITED
	    OT_TREE_MODIFIED 
	    OT_YEAR
	 *

	}; */
	
	// We just use the enum to hold arbitrary constant variables as above, so no need to set a generalized structure.
	OTIConstants() {}
}
