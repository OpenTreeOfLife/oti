package org.opentree.oti.indexproperties;

import org.opentree.graphdb.NodeIndexDescription;
import org.opentree.oti.OTINodeIndex;
import org.opentree.properties.OTPropertyPredicate;
import org.opentree.properties.OTVocabularyPredicate;

/**
 * An enum listing arrays of values to be indexed under specific properties for each type of search target.
 */
public enum IndexedArrayProperties {

	STUDIES_EXACT (
			OTINodeIndex.STUDY_METADATA_NODES_BY_PROPERTY_EXACT,
			new OTPropertyArray[] {			
	}),

	STUDIES_FULLTEXT (
			OTINodeIndex.STUDY_METADATA_NODES_BY_PROPERTY_FULLTEXT,
			new OTPropertyArray[] {
	}),
			
	TREES_EXACT (
			OTINodeIndex.TREE_ROOT_NODES_BY_PROPERTY_EXACT,
			new OTPropertyArray[] {
					OTPropertyArray.OT_ORIGINAL_LABEL,
					OTPropertyArray.OT_OTT_ID,
		    		OTPropertyArray.OT_OTT_TAXON_NAME,
	}),
	
	TREES_FULLTEXT (
			OTINodeIndex.TREE_ROOT_NODES_BY_PROPERTY_FULLTEXT,
			new OTPropertyArray[] {
					OTPropertyArray.OT_ORIGINAL_LABEL,
		    		OTPropertyArray.OT_OTT_TAXON_NAME,
	}),
    
	TREE_NODES_EXACT (
			OTINodeIndex.TREE_NODES_BY_PROPERTY_EXACT,
			new OTPropertyArray[] {
	}),

	TREE_NODES_FULLTEXT (
			OTINodeIndex.TREE_NODES_BY_PROPERTY_FULLTEXT,
			new OTPropertyArray[] {
	}),

	;
	
	private final OTPropertyArray[] properties;
	private final NodeIndexDescription index;

    IndexedArrayProperties(NodeIndexDescription index, OTPropertyArray[] properties) {
        this.properties = properties;
        this.index = index;
    }

	public OTPropertyArray[] properties() {
		return properties;
	}

	public NodeIndexDescription index() {
		return index;
	}
	
}
