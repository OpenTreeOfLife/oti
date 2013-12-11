package org.opentree.oti.plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
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

//	private String nexsonCommitsURLStr = "https://bitbucket.org/api/2.0/repositories/blackrim/avatol_nexsons/commits";
//	private String nexsonsBaseURL = "https://bitbucket.org/api/1.0/repositories/blackrim/avatol_nexsons/raw/";

//	private String nexsonCommitsURLStr = "https://api.github.com/repos/OpenTreeOfLife/treenexus/commits";
//	private String nexsonsBaseURL = "https://bitbucket.org/api/1.0/repositories/blackrim/avatol_nexsons/raw/";

	
	/*
	 * Return the url of the most recent commit in the public repo. Facilitates working with these independently in javascript.
	 * 
	 * @param graphDb
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws ParseException
	 *
	@Description("Return the url of the most recent commit in the public repo")
	@PluginTarget(GraphDatabaseService.class)
	public Representation getMostCurrentNexsonsURL(@Source GraphDatabaseService graphDb) throws InterruptedException, IOException, ParseException {

		JSONParser parser = new JSONParser();

		// get the commits from the public repo
		BufferedReader nexsonCommits = new BufferedReader(new InputStreamReader(new URL(nexsonCommitsURLStr).openStream()));
		JSONArray commitsJSON = (JSONArray) parser.parse(nexsonCommits);

		// get just the most recent commit
//		String mostRecentCommitHash = (String) ((JSONObject) commitsJSON.get(0)).get("sha");
		String mostRecentCommitURL = (String) ((JSONObject) commitsJSON.get(0)).get("url");

		Map<String, Object> result = new HashMap<String, Object>();
//		result.put("base_url", nexsonsBaseURL);
//		result.put("recenthash", mostRecentCommitHash);
		result.put("url", mostRecentCommitURL);
		return OTRepresentationConverter.convert(result);

//		return ValueRepresentation.string(nexsonsBaseURL + mostRecentCommitHash + "/");

	} */

	/*
	 * Get a list of the nexson files in the public repo commit at the specified url
	 * 
	 * @param graphDb
	 * @param url
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws ParseException
	 *
	@Description("Get a list of the nexsons currently in the public nexsons repo")
	@PluginTarget(GraphDatabaseService.class)
	public Representation getNexsonsListFromURL(@Source GraphDatabaseService graphDb, @Description("remote nexson url") @Parameter(name = "url", optional = false) String url) throws IOException {

		BufferedReader nexsonsDir = new BufferedReader(new InputStreamReader(new URL(url).openStream()));

		// prepare for indexing all studies
		LinkedList<String> availableStudies = new LinkedList<String>();
		LinkedList<String> errorStudies = new LinkedList<String>(); // currently not used

		// for each nexson in the latest commit
		String dirEntry = "";
		while (dirEntry != null) {
			dirEntry = nexsonsDir.readLine();
			try {
				Integer.valueOf(dirEntry);
				availableStudies.add(dirEntry);
			} catch (NumberFormatException ex) {
				errorStudies.add(dirEntry);
			}
		}

		return ListRepresentation.string(availableStudies);
	} */

	/**
	 * Just index a single remote nexson into the local db.
	 * 
	 * @param graphDb
	 * @param url
	 * @param sourceID
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws DuplicateSourceException 
	 */
	@Description("Add a single remote nexson into the local db under the specified source id. Sources will only be added if they have at least one tree. Returns true if the "
			+ "source is added, or false if it has no trees. Trees that cannot be read from nexson files that otherwise contain some good trees will be skipped.")
	@PluginTarget(GraphDatabaseService.class)
	public Representation indexSingleNexson(@Source GraphDatabaseService graphDb,
			@Description("remote nexson url") @Parameter(name = "url", optional = false) String url /*,
			@Description("source id under which this source will be indexed locally")
				@Parameter(name = "sourceId", optional = false) String sourceId */ ) throws MalformedURLException, IOException {

		DatabaseManager manager = new DatabaseManager(graphDb);
		NexsonSource study = readRemoteNexson(url);

		if (study.getTrees().iterator().hasNext() == false) {
			return ValueRepresentation.bool(false);
		} else {
			manager.addOrReplaceStudy(study);
			return ValueRepresentation.bool(true);
		} 
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
//		MessageLogger msgLogger = new MessageLogger("");

		// TODO: sometimes this returns a null for the first tree, but no errors. Why? Why don't we get an error?
//		return NexsonReader.readNexson(nexson, false, msgLogger);
		return new NexsonSource(nexson);
	}
}
