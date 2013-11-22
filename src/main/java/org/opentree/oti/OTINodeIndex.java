package org.opentree.oti;

import org.opentree.graphdb.NodeIndexDescription;

/**
 * An enum to make keeping track of indexes easier. Please document new indexes here.
 * 
 * @author federation of botanist hackers
 *
 */
public enum OTINodeIndex implements NodeIndexDescription {

    // ===== source indexes

    /**
     * Study metadata nodes indexed by any searchable property. See the SearchableProperty enum for more info.
     */
	STUDY_METADATA_NODES_BY_PROPERTY_EXACT ("studyMetaNodesByPropertyExact", new String[] {"type", "exact", "to_lower_case", "true"}),

    /**
     * Study metadata nodes indexed by any searchable property. See the SearchableProperty enum for more info.
     */
	STUDY_METADATA_NODES_BY_PROPERTY_FULLTEXT ("studyMetaNodesByPropertyFulltext", new String[] {"type", "fulltext", "to_lower_case", "true"}),

	// ===== tree indexes
	
    /**
     * Root nodes for trees indexed by any searchable property. See the SearchableProperty enum for more info.
     */
    TREE_ROOT_NODES_BY_PROPERTY_EXACT ("treeRootNodesByPropertyExact",  new String[] {"type", "exact", "to_lower_case", "true"}),

    /**
     * Root nodes for trees indexed by any searchable property. See the SearchableProperty enum for more info.
     */
    TREE_ROOT_NODES_BY_PROPERTY_FULLTEXT ("treeRootNodesByPropertyFulltext",  new String[] {"type", "fulltext", "to_lower_case", "true"}),
    
    // ===== tree node indexes

    /**
     * Tree child nodes (currently just tips) indexed by any searchable property. See the SearchableProperty enum for more info.
     */
    TREE_NODES_BY_PROPERTY_EXACT ("treeNodesByPropertyExact", new String[] {"type", "exact", "to_lower_case", "true"}),

    /**
     * Tree child nodes (currently just tips) indexed by any searchable property. See the SearchableProperty enum for more info.
     */
    TREE_NODES_BY_PROPERTY_FULLTEXT ("treeNodesByPropertyFulltext", new String[] {"type", "fulltext", "to_lower_case", "true"}),
	
    ;
    
    String name;
    String[] parameters;
    
    OTINodeIndex(String name, String... parameters) {
    	this.name = name;
    	this.parameters = parameters;
    }
    
    @Override
    public String indexName() {
    	return name;
    }
    
    @Override
    public String[] parameters() {
    	return parameters;
    }
}
