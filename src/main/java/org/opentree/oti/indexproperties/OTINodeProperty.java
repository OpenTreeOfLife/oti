package org.opentree.oti.indexproperties;

import org.opentree.properties.OTPropertyPredicate;

/**
 * Node properties specific to OTU. These are stored in graph nodes. Different types
 * of nodes may have different properties.
 * 
 */
public enum OTINodeProperty implements OTPropertyPredicate {
	
	// TODO: switch as many of these as possible over to the nexson and ot: vocabulary
	// properties as possible. waiting for that spec to stabilize to do so

	// ===== source meta nodes
	
	/**
	 * A boolean to identify source metadata nodes.
	 */
	IS_STUDY_META ("is_study_meta", boolean.class),
	
	// ===== source meta and tree root nodes

	/**
	 * A unique string used to identify this tree within the db. The convention is to use the study id concatenated
	 * by an underscore to an id unique for trees within studies, e.g. 10_1. For trees incoming from nexsons, we attempt
	 * to use any incoming tree id. IF THIS IS ABSENT, WE SHOULD EXPLODE (this behavior differs from OTU, which invents new ids).
	 */
	TREE_ID ("oti_tree_id", String.class),
			
	/**
	 * The native neo4j id of the graph node.
	 */
	NODE_ID ("id", Long.class),
	
	/**
	 * For studies or trees marked as deprecated.
	 */
	IS_DEPRECATED ("is_deprecated", boolean.class),
	
	// ===== tree root nodes

	/**
	 * A primitive string array containing all the original tip labels applied to tip children of a given tree node.
	 * This is stored as a property of the root of each imported tree.
	 */
	DESCENDANT_ORIGINAL_TIP_LABELS ("tip_original_labels", String[].class),
	
	/**
	 * A primitive string array containing all the currently mapped taxon names applied to tip children of a given tree node.
	 * This is stored as a property of the root of each imported tree.
	 */
	DESCENDANT_MAPPED_TAXON_NAMES ("tip_mapped_names", String[].class),

	/**
	 * A primitive string array containing all the ott ids for taxa mapped to the tip children of a given tree node.
	 * This is stored as a property of the root of each imported tree.
	 */
	DESCENDANT_MAPPED_TAXON_OTT_IDS ("tip_mapped_ottids", long[].class),
	
	/**
	 * OTT ids for higher taxa that contain children mapped to children of the given tree node.
	 */
	COMPATIBLE_HIGHER_TAXON_OTT_IDS ("compatible_higher_taxon_ottids", long[].class),
	
	/**
	 * A primitive string array containing all the ott ids for taxa mapped to the tip children of a given tree node.
	 * This is stored as a property of the root of each imported tree.
	 */
	DESCENDANT_TREEBASE_OTU_IDS ("tip_treebase_otu_ids", String[].class),
	
	/**
	 * A boolean indicating that this node is the root for its tree. Should always (and only) be set to true for the root
	 * node. All other tree nodes should lack this property entirely.
	 */
	IS_ROOT ("is_root", boolean.class), // TODO: switch this to an OT namespace property if we can
	
	/**
	 * A boolean indicating whether the ingroup is set for this tree. Stored as a property of the tree root node. If the
	 * tree root lacks this property, then the ingroup can be inferred not to be set.
	 */
	INGROUP_IS_SET ("ingroup_is_set", boolean.class), // TODO: switch this to an OT namespace property if we can
	
	/**
	 * The nodeid of the root node for the designated ingroup for this tree. Should not be set unless the ingroup has been
	 * designated.
	 */
	INGROUP_START_NODE_ID ("ingroup_node_id", boolean.class), // TODO: switch this to an OT namespace property if we can
	
	// ===== all tree nodes

	/**
	 * The id property of this element within the nexson file. These ids are returned in query results.
	 */
	NEXSON_ID ("nexson_id", String.class),
	
	/**
	 * The branch length of the parent branch of this node.
	 */
	PARENT_BRANCH_LENGTH("parent_branch_length", Double.class),
	
	/**
	 * A flag specifying that the clade represented by the node is the ingroup for the tree. Is only set on the root node
	 * of the ingroup clade. A phylografter property imported by NexsonReader.
	 */
	IS_INGROUP_ROOT ("ingroup_start", boolean.class), // TODO: switch this to an OT namespace property if we can

	/**
	 * A flag specifying that this node is part of the ingroup for this tree. This property is nominally a boolean but should
	 * only be set on nodes that are actually part of the ingroup, implying that nodes without this property in trees that
	 * have their ingroup set are thus part of the outgroup.
	 */
	IS_WITHIN_INGROUP ("within_ingroup", boolean.class),
	
	;
		
	private final String propertyName;
	private final Class<?> type;
    
	OTINodeProperty(String name, Class<?> type) {
    	this.propertyName = name;
        this.type = type;
    }

	public String propertyName() {
		return propertyName;
	}

	public Class<?> type() {
		return type;
	}
}
