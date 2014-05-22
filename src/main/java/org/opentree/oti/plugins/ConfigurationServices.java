package org.opentree.oti.plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.server.plugins.Description;
import org.neo4j.server.plugins.Parameter;
import org.neo4j.server.plugins.PluginTarget;
import org.neo4j.server.plugins.ServerPlugin;
import org.neo4j.server.plugins.Source;
import org.neo4j.server.rest.repr.ListRepresentation;
import org.neo4j.server.rest.repr.MappingRepresentation;
import org.neo4j.server.rest.repr.OTRepresentationConverter;
import org.neo4j.server.rest.repr.OpentreeRepresentationConverter;
import org.neo4j.server.rest.repr.Representation;
import org.neo4j.server.rest.repr.ValueRepresentation;
import org.opentree.MessageLogger;
import org.opentree.graphdb.DatabaseUtils;
import org.opentree.graphdb.GraphDatabaseAgent;
import org.opentree.nexson.io.NexsonReader;
import org.opentree.nexson.io.NexsonSource;
import org.opentree.oti.DatabaseBrowser;
import org.opentree.oti.QueryRunner;
import org.opentree.oti.DatabaseManager;
import org.opentree.oti.indexproperties.IndexedPrimitiveProperties;
import org.opentree.oti.constants.OTIGraphProperty;
import org.opentree.oti.constants.OTIRelType;
//import org.opentree.otu.constants.OTURelType;
import org.opentree.taxonomy.TaxonomyLoaderOTT;
import org.opentree.properties.OTVocabularyPredicate;

/**
 * services for indexing. very preliminary, should probably be reorganized (later).
 * 
 * @author cody
 * 
 */
public class ConfigurationServices extends ServerPlugin {
	
	@Description( "Install the OTT taxonomy" )
	@PluginTarget( GraphDatabaseService.class )
	public Representation installOTT(@Source GraphDatabaseService graphDb,
			@Description( "Taxonomy file")
			@Parameter(name = "taxonomyFile", optional = false) String taxonomyFile,
			@Description( "Synonym file")
			@Parameter(name = "synonymFile", optional = true) String synonymFile) {

		GraphDatabaseAgent gdb = new GraphDatabaseAgent(graphDb);
		gdb.setGraphProperty(OTIGraphProperty.TAXONOMY_IS_LOADING.propertyName(), true);
		
		TaxonomyLoaderOTT loader = new TaxonomyLoaderOTT(graphDb);

		// turn off unnecessary features
		loader.setAddSynonyms(synonymFile == null ? false : true);
		loader.setAddBarrierNodes(false);
		loader.setbuildPreferredIndexes(false);
		loader.setbuildPreferredRels(false);

		// make sure we build the optional ott id index
		loader.setCreateOTTIdIndexes(true);
		
		// load the taxonomy
		loader.loadOTTIntoGraph("ott", taxonomyFile, synonymFile == null ? "" : synonymFile);
		
		gdb.setGraphProperty(OTIGraphProperty.HAS_TAXONOMY.propertyName(), true);
		gdb.removeGraphProperty(OTIGraphProperty.TAXONOMY_IS_LOADING);
		
		Map<String, Object> results = new HashMap<String, Object>();
		results.put("event", "success");
		return OTRepresentationConverter.convert(results);
	}

	@Description( "Connect the OTT taxonomy to all terminal tree nodes with mapped ott ids in all indexed trees" )
	@PluginTarget( GraphDatabaseService.class )
	public Representation connectAllTreesToOTT(@Source GraphDatabaseService graphDb) {

		DatabaseBrowser browser = new DatabaseBrowser(graphDb);
		DatabaseManager manager = new DatabaseManager(graphDb);

		List<String> localSourceIds = browser.getSourceIds();

		Transaction tx = graphDb.beginTx();
		try {
			for (String sourceId : localSourceIds) {
				for (String treeId : browser.getTreeIdsForSourceId(sourceId)) {
					Node root = browser.getTreeRootNode(treeId);
					for (Node otu : DatabaseUtils.descendantTipTraversal(OTIRelType.CHILDOF, Direction.INCOMING).traverse(root).nodes()) {
						manager.connectTreeNodeToTaxonomy(otu);
					}
				}
			}
			tx.success();
			
		} finally {
			tx.finish();
		}
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("event", "success");
		return OTRepresentationConverter.convert(result);		
	}
	
	
}
