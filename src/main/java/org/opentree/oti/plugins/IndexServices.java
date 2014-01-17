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
import org.neo4j.graphdb.GraphDatabaseService;
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
import org.opentree.nexson.io.NexsonReader;
import org.opentree.nexson.io.NexsonSource;
import org.opentree.oti.QueryRunner;
import org.opentree.oti.DatabaseManager;
import org.opentree.oti.indexproperties.IndexedPrimitiveProperties;

/**
 * services for indexing. very preliminary, should probably be reorganized (later).
 * 
 * @author cody
 * 
 */
public class IndexServices extends ServerPlugin {

	/**
	 * DEPRECATED. Use `indexNexsons` instead.
	 * 
	 * @param graphDb
	 * @param url
	 * @param sourceID
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws DuplicateSourceException 
	 */
	@Description("DEPRECATED. Use `indexNexsons` instead. For compatibility, this *ALWAYS RETURNS* true. indexNexsons will provide more meaningful results.")
	@PluginTarget(GraphDatabaseService.class)
	@Deprecated
	public Representation indexSingleNexson(@Source GraphDatabaseService graphDb,
			@Description("remote nexson url") @Parameter(name = "url", optional = false) String url) throws MalformedURLException, IOException {

		String[] urls = new String[] {url};
		indexNexsons(graphDb, urls);
		return OpentreeRepresentationConverter.convert(true);
	
	}

	/**
	 * Index the nexson data accessible at the passed url(s).
	 * @param graphDb
	 * @param url
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	@Description("Index the nexson data at the provided urls. If a nexson study to be indexed has an identical ot:studyId value to a " +
			"previously indexed study, then the previous information for that study will be replaced by the incoming nexson. Returns an " +
			"array containing the study ids for the studies that were successfully read and indexed.")
	@PluginTarget(GraphDatabaseService.class)
	public Representation indexNexsons(@Source GraphDatabaseService graphDb,
			@Description("remote nexson urls") @Parameter(name = "urls", optional = false) String[] urls) throws MalformedURLException, IOException {

		if (urls.length < 1) {
			throw new IllegalArgumentException("You must provide at least one url for a nexson document to be indexed.");
		}
		
		ArrayList<String> results = new ArrayList<String>(urls.length);
		
		DatabaseManager manager = new DatabaseManager(graphDb);
		for (int i = 0; i < urls.length; i++) {

			NexsonSource study = readRemoteNexson(urls[i]);
	
			if (study.getTrees().iterator().hasNext()) {
				manager.addOrReplaceStudy(study);
				results.add(study.getId());
			}
		}

		return OpentreeRepresentationConverter.convert(results);

	}

	/**
	 * helper function for reading a nexson from a url
	 * 
	 * @param url
	 * @return Jade trees from nexson
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private NexsonSource readRemoteNexson(String url) throws MalformedURLException, IOException {
		BufferedReader nexson = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
		return new NexsonSource(nexson);
	}
}
