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
import org.neo4j.graphdb.Node;
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
import org.opentree.nexson.io.NexsonReader;
import org.opentree.nexson.io.NexsonSource;
import org.opentree.oti.QueryRunner;
import org.opentree.oti.DatabaseManager;
import org.opentree.oti.indexproperties.IndexedPrimitiveProperties;
import org.opentree.properties.OTVocabularyPredicate;

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
		
		ArrayList<String> indexedIDs = new ArrayList<String>(urls.length);
		HashMap<String, String> idsWithErrors = new HashMap<String, String>();
		DatabaseManager manager = new DatabaseManager(graphDb);
		for (int i = 0; i < urls.length; i++) {
			try {
				NexsonSource study = readRemoteNexson(urls[i]);
				if (study.getTrees().iterator().hasNext()) {
					manager.addOrReplaceStudy(study);
					indexedIDs.add(study.getId());
				}
			} catch (Exception ex) {
				idsWithErrors.put(urls[i], ex.getMessage());
			}
		}
		HashMap<String, Object> results = new HashMap<String, Object>(); // will be converted to JSON object
		results.put("indexed", indexedIDs);
		results.put("errors", idsWithErrors);
		return OpentreeRepresentationConverter.convert(results);

	}


	/**
	 * Remove nexson data (if found) by study id
	 * @param graphDb
	 * @param url
	 * @return
	 * @throws IOException
	 */
	@Description("Unindex (remove) the nexson data for these study ids. If no matching " +
            "study is found, do nothing. Returns arrays containing the study ids for " +
			"the studies that were successfully removed from the index, and those that could " +
            "not be found (and throws exceptions for those whose removal failed.")
	@PluginTarget(GraphDatabaseService.class)
	public Representation unindexNexsons(@Source GraphDatabaseService graphDb,
			@Description("doomed nexson ids") @Parameter(name = "ids", optional = false) String[] ids) throws IOException {

		if (ids.length < 1) {
			throw new IllegalArgumentException("You must provide at least one id for a nexson document to be removed.");
		}
		
		DatabaseManager manager = new DatabaseManager(graphDb);
		
		// record ids according the result of their attempted removal
		ArrayList<String> idsDeleted = new ArrayList<String>(ids.length);
		ArrayList<String> idsNotFound = new ArrayList<String>(ids.length);
		HashMap<String, String> idsWithErrors = new HashMap<String, String>();
		for (String studyId : ids) {

			Node studyMeta = manager.getStudyMetaNodeForStudyId(studyId);
	        if (studyMeta == null) {
	        	// could not find study with this id
	        	idsNotFound.add(studyId);

	        } else {
	        	// found it. carry out the sentence
	        	try {
	        		manager.deleteSource(studyMeta);
	                idsDeleted.add(studyId);
	        	} catch (Exception ex) {
	        		idsWithErrors.put(studyId,ex.getMessage());
	        	}
 	        }
		}

		HashMap<String, Object> results = new HashMap<String, Object>(); // will be converted to JSON object
		results.put("deleted", idsDeleted);
		results.put("not_found", idsNotFound);
		results.put("errors", idsWithErrors);
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
