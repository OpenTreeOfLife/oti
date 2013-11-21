package org.opentree.oti.plugins;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import jade.tree.*;

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
import org.opentree.exceptions.TreeNotFoundException;
import org.opentree.graphdb.GraphDatabaseAgent;
import org.opentree.properties.OTVocabularyPredicate;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

/**
 * Search services for the oti nexson database.
 * @author cody
 *
 */
public class QueryServices extends ServerPlugin {
	
	// TODO: add queries
	
}
