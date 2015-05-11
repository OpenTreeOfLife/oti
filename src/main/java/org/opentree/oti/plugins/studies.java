package org.opentree.oti.plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.opentree.nexson.io.NexsonSource;
import org.opentree.oti.DatabaseManager;
import org.opentree.oti.QueryRunner;
import org.opentree.oti.indexproperties.OTIProperties;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.server.plugins.*;
import org.neo4j.server.rest.repr.OTRepresentationConverter;
import org.neo4j.server.rest.repr.Representation;
import org.opentree.properties.OTPropertyPredicate;

/**
 * Search services for the oti nexson database.
 * @author cody
 *
 */
public class studies extends ServerPlugin {
	
	/**
	 * Perform a simple search for studies
	 * @param graphDb
	 * @param propertyName
	 * @param value
	 * @return
	 * @throws ParseException 
	 */
	@Description("Perform a simple search for indexed studies. To find all studies, omit both the property and the value from your query.")
	@PluginTarget(GraphDatabaseService.class)
	public Representation find_studies(@Source GraphDatabaseService graphDb,
			@Description("The property to be searched on. A list of searchable properties is available from the "
					+ "[properties](#properties) service. To find all studies, omit both the property and the value from your query.")
			@Parameter(name = "property", optional = true)
			String property,
			
			@Description("The value to be searched. This must be passed as a string, but will be converted to the datatype "
					+ "corresponding to the specified searchable value. To find all studies, omit both the property and the "
					+ "value from your query.")
			@Parameter(name = "value", optional = true)
			String value,
			
			@Description("Whether to perform exact matching ONLY. Defaults to false, i.e. fuzzy matching is enabled. Fuzzy "
					+ "matching is only available for some string properties.")
			@Parameter(name="exact", optional = true)
			Boolean checkExactOnly,
			
			@Description("Whether or not to include all metadata. By default, only the nexson ids of elements will be returned.")
			@Parameter(name = "verbose", optional = true)
			Boolean verbose) throws ParseException {
		
		// set null optional parameters to default values
		verbose = verbose == null ? false : verbose;
		boolean doFuzzyMatching = checkExactOnly == null ? true : ! checkExactOnly;

		// prepare for search
		HashMap<String, Object> results = new HashMap<String, Object>();
		QueryRunner runner = new QueryRunner(graphDb);

		if (property == null && value == null) {
			// no property specified, find all studies
			results.put("matched_studies", runner.doBasicSearchForStudies(new MatchAllDocsQuery(), null, verbose));

		} else if ((property == null && value != null) || (property != null && value == null)) {
			// property or value specified but not both, return error
			results.put("error", "You either specified a property but not a value, or a value but not a property. " +
					    "If you specify a property or a value, you must specify both. You may alternatively specify " +
					    "neither to find all studies.");

		} else { // user specified a property and a value, attempt to search
		
			HashSet<OTPropertyPredicate> searchProperties = new OTIProperties().getIndexedStudyProperties().get(property);
					
			if (searchProperties != null) {
				results.put("matched_studies", runner.doBasicSearchForStudies(searchProperties, value, doFuzzyMatching, verbose));
			} else {
				results.put("error", "unrecognized property: " + property);
			}
		}
		
		return OTRepresentationConverter.convert(results);
	}
	
	/**
	 * Perform a simple search for trees
	 * @param graphDb
	 * @param propertyName
	 * @param value
	 * @return
	 */
	@Description("Perform a simple search for trees in indexed studies.")
	@PluginTarget(GraphDatabaseService.class)
	public Representation find_trees(@Source GraphDatabaseService graphDb,
			@Description("The property to be searched on. A list of searchable properties is available from the "
					+ "[properties](#properties) service.")
			@Parameter(name = "property", optional = false)
			String property,
			
			@Description("The value to be searched. This must be passed as a string, but will be converted to the datatype "
					+ "corresponding to the specified searchable value.")
			@Parameter(name = "value", optional = false)
			String value,
			
			@Description("Whether to perform exact matching ONLY. Defaults to false, i.e. fuzzy matching is enabled. Fuzzy "
					+ "matching is only available for some string properties.")
			@Parameter(name="exact", optional = true)
			Boolean checkExactOnly,
			
			@Description("Whether or not to include all metadata. By default, only the nexson ids of elements will be returned.")
			@Parameter(name = "verbose", optional = true)
			Boolean verbose) {
		
		// set null optional parameters to default values
		verbose = verbose == null ? false : verbose;
		boolean doFuzzyMatching = checkExactOnly == null ? true : ! checkExactOnly;

		HashSet<OTPropertyPredicate> searchProperties = new OTIProperties().getIndexedTreeProperties().get(property);
		HashMap<String, Object> results = new HashMap<String, Object>();

		if (searchProperties != null) {
			QueryRunner runner = new QueryRunner(graphDb);
			results.put("matched_studies", runner.doBasicSearchForTrees(searchProperties, value, doFuzzyMatching, verbose));
		} else {
			results.put("error", "unrecognized property: " + property);
		}
		
		return OTRepresentationConverter.convert(results);

	}
	
	/**
	 * Return a map containing available property names and the names of the SearchableProperty enum elements they
	 * correspond to.
	 * 
	 * @param graphDb
	 * @return
	 */
	@Description("Get a list of properties that can be used to search for studies and trees.")
	@PluginTarget(GraphDatabaseService.class)
	public Representation properties (@Source GraphDatabaseService graphDb) {
		
		HashMap<String, Object> results = new HashMap<String, Object>();

		OTIProperties properties = new OTIProperties();
		results.put("study_properties", properties.getIndexedStudyProperties().keySet());
		results.put("tree_properties", properties.getIndexedTreeProperties().keySet());
		
		return OTRepresentationConverter.convert(results);
	}
	
	/**
	 * Index the nexson data accessible at the passed url(s).
	 * @param graphDb
	 * @param url
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	@Description("DEPRECATED. Use the index_study service instead.")
	@PluginTarget(GraphDatabaseService.class)
	@Deprecated
	// TODO: remove this from the next iteration of the API 
	public Representation index_studies(@Source GraphDatabaseService graphDb,
			@Description("remote nexson urls")
			@Parameter(name = "urls", optional = false)
			String[] urls) throws MalformedURLException, IOException {

		if (urls.length < 1) {
			throw new IllegalArgumentException("You must provide at least one url for a nexson document to be indexed.");
		}
		
		ArrayList<String> indexedIDs = new ArrayList<String>(urls.length);
		DatabaseManager manager = new DatabaseManager(graphDb);
		for (int i = 0; i < urls.length; i++) {
			NexsonSource study = readRemoteNexson(urls[i]);
			manager.addOrReplaceStudy(study);
			indexedIDs.add(study.getId());
		}
		HashMap<String, Object> results = new HashMap<String, Object>(); // will be converted to JSON object
		results.put("indexed", indexedIDs);
		results.put("errors", new ArrayList());
		return OTRepresentationConverter.convert(results);
	}


	/**
	 * Index the nexson data accessible at the passed url(s).
	 * @param graphDb
	 * @param url
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	@Description("Index the nexson study at the provided url. If the study to be indexed has an identical ot:studyId value to a " +
	"previously indexed study, then the previous information for that study will be replaced by the incoming nexson. Returns true " +
	"on successful indexing, or reports an error if one is encountered.")
	@PluginTarget(GraphDatabaseService.class)
	public Representation index_study(@Source GraphDatabaseService graphDb,
			@Description("remote nexson url")
			@Parameter(name = "url", optional = false)
			String url) throws MalformedURLException, IOException {

		DatabaseManager manager = new DatabaseManager(graphDb);

		NexsonSource study = readRemoteNexson(url);
		manager.addOrReplaceStudy(study);

		return OTRepresentationConverter.convert(true);
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
	public Representation unindex_studies(@Source GraphDatabaseService graphDb,
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
		return OTRepresentationConverter.convert(results);

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
