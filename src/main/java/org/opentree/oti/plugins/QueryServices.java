package org.opentree.oti.plugins;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import jade.tree.*;

import org.opentree.oti.ConfigurationManager;
import org.opentree.oti.QueryRunner;
import org.opentree.oti.DatabaseManager;
import org.opentree.oti.constants.OTIGraphProperty;
import org.opentree.oti.constants.OTINodeProperty;
import org.opentree.oti.constants.OTIRelType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.Traversal;
import org.neo4j.server.plugins.*;
import org.neo4j.server.rest.repr.OTRepresentationConverter;
import org.neo4j.server.rest.repr.Representation;
import org.opentree.graphdb.GraphDatabaseAgent;
import org.opentree.otu.exceptions.NoSuchTreeException;
import org.opentree.properties.OTVocabularyPredicate;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class QueryServices extends ServerPlugin{
	
	/**
	 * @param nodeId
	 * @return
	 * @throws NoSuchTreeException 
	 */
	@Description( "Get the neo4j root node for a given tree id" )
	@PluginTarget( GraphDatabaseService.class )
	public Long getRootNodeIdForTreeId(@Source GraphDatabaseService graphDb,
			@Description( "The id of the tree to be found.")
			@Parameter(name = "treeId", optional = false) String treeId) throws NoSuchTreeException {

		QueryRunner browser = new QueryRunner(graphDb);

		// TODO: add check for whether tree is imported. If not then return this information
		
		Node rootNode = browser.getTreeRootNode(treeId, browser.LOCAL_LOCATION);
		return rootNode.getId();
	}
	
	/**
	 * @param nodeId
	 * @return
	 * @throws NoSuchTreeException 
	 */
	@Description( "Make a working copy of the tree below the designated root node" )
	@PluginTarget( Node.class )
	public Representation makeWorkingCopy(@Source Node root,
			@Description("The id of any node in the original tree, whose counterpart in the copied tree will be identified in the response. "
					+ "If no node with this id is found in the original tree, then null will be returned for the corresponding node id.")
				@Parameter(name="nodeIdOfInterest", optional=true) Long nodeIdOfInterest) throws NoSuchTreeException {

		DatabaseManager manager = new DatabaseManager(root.getGraphDatabase());

		// TODO: add check for whether the provided node is the root node of a tree. If not then return this information

		Transaction tx = root.getGraphDatabase().beginTx();
		Map<String, Object> result = null;
		try {
			result = manager.makeWorkingCopyOfTree(root, nodeIdOfInterest);
			tx.success();
		} finally {
			tx.finish();
		}
		
		if (result == null) {
			result = new HashMap<String, Object>();
			result.put("event", "failure");
			result.put("message", "designated tree could not be copied");
		} else {
			result.put("event", "success");
		}

		return OTRepresentationConverter.convert(result);
	}
	
	/**
	 * @param nodeId
	 * @return
	 * @throws NoSuchTreeException 
	 */
	@Description( "Save the working copy that this node is the root of" )
	@PluginTarget( Node.class )
	public Representation saveWorkingCopy(@Source Node root) throws NoSuchTreeException {

		DatabaseManager manager = new DatabaseManager(root.getGraphDatabase());

		// TODO: add check for whether the provided node is the root node of a tree. If not then return this information

		Transaction tx = root.getGraphDatabase().beginTx();
		Node savedRoot = null;
		try {
			savedRoot = manager.saveWorkingCopy(root);
			tx.success();
		} finally {
			tx.finish();
		}
		
		Map<String, Object> result = new HashMap<String, Object>();
		if (savedRoot == null) {
			result.put("event", "failure");
			result.put("message", "could not save working copy");
		} else {
			result.put("event", "success");
			result.put("saved_root_node_id", savedRoot.getId());
		}

		return OTRepresentationConverter.convert(result);
	}
	
	/**
	 * @param nodeId
	 * @return
	 * @throws NoSuchTreeException 
	 */
	@Description( "Discard the working copy that this node is the root of" )
	@PluginTarget( Node.class )
	public Representation discardWorkingCopy(@Source Node root) throws NoSuchTreeException {

		DatabaseManager manager = new DatabaseManager(root.getGraphDatabase());

		// TODO: add check for whether the provided node is the root node of a tree. If not then return this information

		Transaction tx = root.getGraphDatabase().beginTx();
		Node originalRoot = null;
		try {
			originalRoot  = manager.discardWorkingCopy(root);
			tx.success();
		} finally {
			tx.finish();
		}
		
		Map<String, Object> result = new HashMap<String, Object>();
		if (originalRoot  == null) {
			result.put("event", "failure");
			result.put("message", "could not save working copy");
		} else {
			result.put("event", "success");
			result.put("restored_original_root_node_id", originalRoot.getId());
		}

		return OTRepresentationConverter.convert(result);
	}
	
	
	/**
	 * @param nodeId
	 * @return
	 */
	@Description( "Remove a previously imported tree from the graph" )
	@PluginTarget( GraphDatabaseService.class )
	public Representation deleteTreeFromTreeId(@Source GraphDatabaseService graphDb,
			@Description( "The id of the tree to be deleted")
			@Parameter(name = "treeId", optional = false) String treeId) {
		
		DatabaseManager manager = new DatabaseManager(graphDb);
		QueryRunner browser = new QueryRunner(graphDb);
		
		Node root = browser.getTreeRootNode(treeId, browser.LOCAL_LOCATION);
		manager.deleteTree(root);

		// return result
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("event", "success");
		result.put("treeId", treeId);
		return OTRepresentationConverter.convert(result);
	}
	
	/**
	 * @param nodeId
	 * @return
	 */
	@Description( "Reroot the tree containing the indicated node, using that node as the new root. Returns the neo4j node id of the new root." )
	@PluginTarget( GraphDatabaseService.class )
	public Long rerootTree(@Source GraphDatabaseService graphDb,
			@Description( "The Neo4j node id of the node to be used as the root for its tree.")
			@Parameter(name = "nodeId", optional = false) Long nodeId) {
		DatabaseManager manager = new DatabaseManager(graphDb);
		Node rootNode = graphDb.getNodeById(nodeId);
		Node newroot = manager.rerootTree(rootNode);
		return newroot.getId();
	}
	
	/**
	 * @param nodeId
	 * @return
	 */
	@Description( "Set the ingroup of the tree containing the indicated node to that node." )
	@PluginTarget( GraphDatabaseService.class )
	public Long ingroupSelect(@Source GraphDatabaseService graphDb,
			@Description( "The Neo4j node id of the node to be used as the ingroup for its tree.")
			@Parameter(name = "nodeId", optional = false) Long nodeId) {
		DatabaseManager manager = new DatabaseManager(graphDb);
		Node rootNode = graphDb.getNodeById(nodeId);
		manager.designateIngroup(rootNode);
		return rootNode.getId();
	}
	
	/**
	 * @param nodeId
	 * @return
	 */
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

	@Description("Get alternative TNRS mappings for the specified node.")
	@PluginTarget(Node.class)
	public Representation getTNRSMappings(@Source Node node) {

		QueryRunner browser = new QueryRunner(node.getGraphDatabase());
		Map<String, Object> results = browser.getAlternativeMappingsForNode(node);
		
		return OTRepresentationConverter.convert(results);
	}
	
	@Description ("Hit the TNRS for all the names in a subtree. Return the results.")
	@PluginTarget( Node.class )
	public Representation doTNRSForDescendants(@Source Node root,
		@Description ("The url of the TNRS service to use. If not supplied then the public OT TNRS will be used.")
			@Parameter (name="TNRS Service URL", optional=true) String tnrsURL,
		@Description ("NOT IMPLEMENTED. If it were, this would just say: If set to false (default), only the original " +
				"otu labels will be used for TNRS. If set to true, currently mapped names will be used (if they exist).")
			@Parameter(name="useMappedNames", optional=true) boolean useMappedNames) throws IOException, ParseException {

		// start a transaction for edits
        GraphDatabaseAgent graphDb = new GraphDatabaseAgent(root.getGraphDatabase()) ;
		DatabaseManager manager = new DatabaseManager(graphDb);
		ConfigurationManager config = new ConfigurationManager(graphDb);
        Transaction tx = graphDb.beginTx();
		
		// get ids and names to send to tnrs
		LinkedList<Long> nodeIds = new LinkedList<Long>();
		LinkedList<String> names = new LinkedList<String>();
//		for (Node otu : DatabaseUtils.DESCENDANT_OTU_TRAVERSAL.traverse(root).nodes()) {
		for (Node child : Traversal.description().relationships(OTIRelType.CHILDOF, Direction.INCOMING).traverse(root).nodes()) {
			// TODO: allow the choice to use mapped or original names... currently that leads to nullpointerexceptions

			// record that we have TNRS'd this node (i.e. this clade on the tree)
			child.setProperty(OTINodeProperty.PROCESSED_BY_TNRS.propertyName(), true);
			
			// for tip nodes, record names to hit against tnrs
			if (!child.hasRelationship(OTIRelType.CHILDOF, Direction.INCOMING)) {
				if (child.hasProperty(OTINodeProperty.NAME.propertyName())) {
					nodeIds.add(child.getId());
					names.add((String) child.getProperty(OTINodeProperty.NAME.propertyName()));
				}
			}
		}
		
		if (tnrsURL == null) {
			tnrsURL = "http://dev.opentreeoflife.org/taxomachine/ext/TNRS/graphdb/contextQueryForNames/";
		}
		
		// gather the data to be sent to tnrs
		Map<String, Object> query = new HashMap<String, Object>();
		query.put("names", names);
		query.put("idInts", nodeIds);

        // set up the connection
        ClientConfig cc = new DefaultClientConfig();
        Client c = Client.create(cc);
        WebResource tnrs = c.resource(tnrsURL);

        // send the query (get the response)
        String respJSON = tnrs.accept(MediaType.APPLICATION_JSON_TYPE)
        		.type(MediaType.APPLICATION_JSON_TYPE).post(String.class, new JSONObject(query).toJSONString());
        
        JSONParser parser = new JSONParser();
        JSONObject response = (JSONObject) parser.parse(respJSON);
        
        root.setProperty(OTINodeProperty.CONTEXT_NAME.propertyName(), response.get("context"));
        root.setProperty(OTINodeProperty.PROCESSED_BY_TNRS.propertyName(), true);
        
        try {
	        // walk the results
	        for (Object nameResult : (JSONArray) response.get("results")) {
	
	        	JSONArray matches = (JSONArray) ((JSONObject) nameResult).get("matches");
	        	Node otuNode = graphDb.getNodeById((Long) ((JSONObject) nameResult).get("id"));
	        	
	        	// remove previous TNRS result nodes
	        	for (Relationship tnrsRel : otuNode.getRelationships(OTIRelType.TNRSMATCHFOR)) {
	        		Node tnrsNode = tnrsRel.getStartNode();
	        		tnrsRel.delete();
	        		tnrsNode.delete();
	        	}
	        	
	        	// remove previous taxon matching info
	        	otuNode.removeProperty(OTVocabularyPredicate.OT_OTT_ID.propertyName());
	        	otuNode.removeProperty(OTVocabularyPredicate.OT_OTT_TAXON_NAME.propertyName());
	        	
	            // if there is an exact match, store the match info in the graph node
	        	if (matches.size() == 1) {
	        		JSONObject match = ((JSONObject) matches.get(0));
	        		if ((Double) match.get("score") == 1.0) {
	        				        			
	        			otuNode.setProperty(OTVocabularyPredicate.OT_OTT_ID.propertyName(), Long.valueOf((String) match.get("matched_ott_id")));
	        			otuNode.setProperty(OTVocabularyPredicate.OT_OTT_TAXON_NAME.propertyName(),  match.get("matched_name"));

	        			// attach to taxonomy if there is one
	        			if (config.hasTaxonomy()) {
		        			manager.connectTreeNodeToTaxonomy(otuNode);
	        			}
	        			
	        			// also set the basic label property so other things will know we matched this name
	        			otuNode.setProperty(OTINodeProperty.NAME.propertyName(),  match.get("matched_name"));
	        		}
	        		
	        	} else {
	        		
	        		// create TNRS result nodes holding each match's info
	        		for (Object m : matches) {
	        			JSONObject match = (JSONObject) m;
	        			Node tnrsNode = graphDb.createNode();
	        			for (Object property : match.keySet()) {
	        				Object value = match.get(property);
	        				if (property.equals("flags")) {
	        					String[] flags = new String[((JSONArray) value).size()];
	        					int i = 0;
	        					for (Object flag : (JSONArray) value) {
	        						flags[i++] = (String) flag;
	        					}
	        				} else {
	        					tnrsNode.setProperty((String) property, value);
	        				}
	        			}
	        			tnrsNode.createRelationshipTo(otuNode, OTIRelType.TNRSMATCHFOR);
	        		}
	        	}
	        }
	        tx.success();
        } finally {
        	tx.finish();
        }
        
        // return relevant info
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("event", "success");
        result.put("treeId", QueryRunner.getRootOfTreeContaining(root).getProperty(OTINodeProperty.TREE_ID.propertyName()));
        result.put("rootNodeId", root.getId());
        result.put("unmatched_name_ids", response.get("unmatched_name_ids"));
        result.put("matched_name_ids", response.get("matched_name_ids"));
        result.put("unambiguous_name_ids", response.get("unambiguous_name_ids"));
        result.put("context", response.get("context"));
        return OTRepresentationConverter.convert(result);
        
        /*
		// save the result to a local file
        
        // TODO: the tnrs files get saved into the neo4j directory root. it would be better to save them in the
        // otu directory, but to do that we will have to do some some finagling...
        String savedResultsFilePath = "tnrs." + root.getId() + "." + System.currentTimeMillis() + ".json";
        File resultsFile = new File(savedResultsFilePath);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter( new FileWriter(resultsFile));
            writer.write(respJSON);

        } finally {
        	if ( writer != null) {
        		writer.close( );
        	}
        }
        
        // return some JSON with the information for to use when reloading the page
        Map<String, Object> results = new HashMap<String, Object>();
        results.put("event", "success");
        results.put("treeId", DatabaseUtils.getRootOfTreeContaining(root).getProperty(NodeProperty.TREE_ID.propertyName()));
        results.put("results_file", resultsFile.getAbsolutePath());
		
        return(OpentreeRepresentationConverter.convert(results)); */
	}
}
