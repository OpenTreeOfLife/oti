package org.opentree.oti;

import org.opentree.graphdb.NodeIndexDescription;

/**
 * An enum to make keeping track of indexes easier. Please document new indexes here.
 * 
 * @author federation of botanist hackers
 *
 */
public enum OTINodeIndex implements NodeIndexDescription {

	// ===== tree indexes
	
	/**
     * Root nodes for trees, indexed by the tree ids. Nodes are indexed by the properties:
     * 
     * treeId -- key is the tree id string, which is the study id for the tree concatenated
     * via an underscore to the incoming treeId from the original nexson;
     * 
     * studyId -- key is the nexson study id.
	 */
    TREE_ROOT_NODES_BY_TREE_ID_OR_STUDY_ID ("treeRootNodesByTreeOrStudyId"),
        
    /*
     * Root nodes for both local and remote (i.e. not imported) trees, indexed by their originating source id. The property name
     * is either "localSourceId" or "remoteSourceId", and the key is the source id. In the case of nexsons this is the study id,
     * but for other sources (e.g. a file of newick trees, it could be any string that is a globally unique identifier to this database.
     *
    TREE_ROOT_NODES_BY_SOURCE_ID ("treeRootNodesBySourceId"), */

    /**
     * Root nodes for trees including a taxon with the supplied name. Property is "name", key is taxon name.
     */
    TREE_ROOT_NODES_BY_ORIGINAL_TAXON_NAME ("treeRootNodesByOriginalTaxonName"),
    
    /**
     * Root nodes for trees including a taxon with the supplied name. Spaces have been replaced with
     * OTIConstants.WHITESPACE_SUBSTITUTE_FOR_SEARCH to facilitate whole-phrase matching. Property is "name", key is taxon name.
     */
    TREE_ROOT_NODES_BY_ORIGINAL_TAXON_NAME_WHITESPACE_FILLED ("treeRootNodesByOriginalTaxonNameWhitespaceFilled"),

    /**
     * Root nodes for trees including a taxon with the supplied name. Property is "name", key is taxon name.
     */
    TREE_ROOT_NODES_BY_MAPPED_TAXON_NAME ("treeRootNodesByMappedTaxonName"),

    /**
     * Root nodes for trees including a taxon with the supplied name. Spaces have been replaced with
     * OTIConstants.WHITESPACE_SUBSTITUTE_FOR_SEARCH to facilitate whole-phrase matching. Property is "name", key is taxon name.
     */
    TREE_ROOT_NODES_BY_MAPPED_TAXON_NAME_WHITESPACE_FILLED ("treeRootNodesByMappedTaxonNameWhitespaceFilled"),
    
    /**
     * Root nodes for trees including a taxon with the supplied ott id. Property is "uid", key is ott id.
     */
    TREE_ROOT_NODES_BY_MAPPED_TAXON_OTT_ID ("treeRootNodesByMappedTaxonMappedOTTId"),

    /**
     * Root nodes for trees indexed by the specified ot namespace property. Property is the ot property name (e.g. "ot:curatorName")
     * and key is the value for that property (e.g. "Romina Gazis").
     */
    TREE_ROOT_NODES_BY_OT_PROPERTY ("treeRootNodesByOTProperty"),
    
    // ===== source indexes

    /**
     * Study metadata nodes indexed by the specified ot namespace properties. Property is the ot property name (e.g. "ot:studyId")
     * and key is the value for that property (e.g. 1003).
     */
	STUDY_METADATA_NODES_BY_OT_PROPERTY ("studyMetaNodesByOTProperty"),

    /*
     * Study metadata nodes indexed by their originating source id.
     * Property is either "localSourceId" or "remoteSourceId", and key is the source id. In the case of nexsons this is study id,
     * but other cases (e.g. a file of newick trees uploaded locally), this could be any identifier string globally unique to the db.
     *
    STUDY_METADATA_NODES_BY_STUDY_ID ("studyMetaNodesByStudyId"); */
    
    // ===== tree node indexes

    /**
     * Tree nodes indexed by the specified ot namespace property. Property is the ot property name (e.g. "ot:originalLabel")
     * and key is the value for that property (e.g. "Carex siderosticta Roalson 1347").
     */
    TREE_NODES_BY_OT_PROPERTY ("treeNodesByOTProperty"),
	
    /**
     * Tree nodes indexed by the specified ot namespace property. Spaces have been replaced with OTIConstants.WHITESPACE_SUBSTITUTE_FOR_SEARCH
     * to facilitate whole-word matching. Property is the ot property name (e.g. "ot:originalLabel") and key is the value for that property
     * (e.g. "Carex%s%siderosticta%s%Roalson%s%1347" if using the whitespace subsitute "%s%").
     */
    TREE_NODES_BY_OT_PROPERTY_WHITESPACE_FILLED ("treeNodesByOTPropertyWhitespaceFilled"),

    ;
    
    String name;
    
    OTINodeIndex(String name) {
    	this.name = name;
    }
    
    @Override
    public String indexName() {
    	return name;
    }
}
