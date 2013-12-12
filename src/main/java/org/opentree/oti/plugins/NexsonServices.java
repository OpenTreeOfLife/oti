package org.opentree.oti.plugins;


/**
 * Space for other services, such as getting information about nexson properties, etc. Currently not necessary.
 * @author cody
 *
 */
public class NexsonServices {

	/*
	 
	/**
	 * @param nodeId
	 * @return
	 * @throws NoSuchTreeException 
	 *
	@Description( "Get the neo4j root node for a given tree id" )
	@PluginTarget( GraphDatabaseService.class )
	public Long getRootNodeIdForTreeId(@Source GraphDatabaseService graphDb,
			@Description( "The id of the tree to be found.")
			@Parameter(name = "treeId", optional = false) String treeId) throws TreeNotFoundException {

		QueryRunner queryRunner = new QueryRunner(graphDb);

		// TODO: add check for whether tree is imported. If not then return this information
		
//		Node rootNode = queryRunner.getTreeRootNode(treeId);
//		return rootNode.getId();
		
		return Long.valueOf(-999); // placeholder for unimplemented method return
	}
	 
	/**
	 * @param nodeId
	 * @return
	 *
	@Description( "Return a string containing a JSON string for the subtree below the indicated tree node" )
	@PluginTarget( GraphDatabaseService.class )
	public Representation getTreeJson(@Source GraphDatabaseService graphDb,
			@Description( "The Neo4j node id of the node to be used as the root for the tree (can be used to extract subtrees as well).")
			@Parameter(name = "nodeId", optional = false) Long nodeId) {
//		DatabaseBrowser browser = new DatabaseBrowser(graphDb);

		// TODO: add check for whether tree is imported. If not then return error instead of just empty tree
		Node rootNode = graphDb.getNodeById(nodeId);
		JadeTree jadeTree = QueryRunner.getJadeTreeForGraphNode(rootNode, 300);

		return OTRepresentationConverter.convert(jadeTree);
//		return t.getRoot().getJSON(false);
	}
	
	@Description( "Get tree metadata" )
	@PluginTarget( GraphDatabaseService.class )
	public Representation getTreeMetaData(@Source GraphDatabaseService graphDb,
			@Description( "The database tree id for the tree")
			@Parameter(name = "treeId", optional = false) String treeId) {
		
		QueryRunner browser = new QueryRunner(graphDb);
		Node root = browser.getTreeRootNode(treeId, browser.LOCAL_LOCATION);
		return OTRepresentationConverter.convert(browser.getMetadataForTree(root));
	}
	
	@Description( "Get the id for the source associated with the specified tree id" )
	@PluginTarget( GraphDatabaseService.class )
	public String getSourceIdForTreeId(@Source GraphDatabaseService graphDb,
			@Description( "The tree id to use")
			@Parameter(name = "treeId", optional = false) String treeId) {
	
		QueryRunner browser = new QueryRunner(graphDb);

		Node treeRoot = browser.getTreeRootNode(treeId, browser.LOCAL_LOCATION);
		Node sourceMeta = treeRoot.getSingleRelationship(OTIRelType.METADATAFOR, Direction.INCOMING).getStartNode();

		return (String) sourceMeta.getProperty(OTINodeProperty.STUDY_ID.propertyName());
	}
	
	@Description("Get OTU metadata. Currently not used because we get all this infor from the getTreeJSON service")
	@PluginTarget(Node.class)
	@Deprecated
	public Representation getOTUMetaData(@Source Node node) {

		QueryRunner browser = new QueryRunner(node.getGraphDatabase());
		return OTRepresentationConverter.convert(browser.getMetadataForOTU(node));
	}
	*/
}
