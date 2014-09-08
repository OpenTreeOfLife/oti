package org.opentree.oti.plugins;

import java.util.HashMap;
import java.util.HashSet;
import org.opentree.oti.QueryRunner;
import org.opentree.oti.indexproperties.OTIProperties;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.neo4j.graphdb.GraphDatabaseService;
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
			@Description("The property to be searched on. A list of searchable properties is available from the getSearchablePropertiesForStudies service."
					+ "To find all studies, omit both the property and the value from your query.")
				@Parameter(name = "property", optional = true) String property,
			@Description("The value to be searched. This must be passed as a string, but will be converted to the datatype corresponding to the "
					+ "specified searchable value. To find all studies, omit both the property and the value from your query.")
				@Parameter(name = "value", optional = true) String value,
			@Description("Whether to perform exact matching ONLY. Defaults to false, i.e. fuzzy matching is enabled. Fuzzy matching is only applicable for some string properties.")
				@Parameter(name="exact", optional = true) Boolean checkExactOnly,
			@Description("Whether or not to include all metadata. By default, only the nexson ids of elements will be returned.")
				@Parameter(name = "verbose", optional = true) Boolean verbose) throws ParseException {
		
		// set null optional parameters to default values
		verbose = verbose == null ? false : verbose;
		boolean doFuzzyMatching = checkExactOnly == null ? true : ! checkExactOnly;

		// prepare for search
		HashMap<String, Object> results = new HashMap<String, Object>();
		QueryRunner runner = new QueryRunner(graphDb);

		if (property == null && value == null) {
			// no property specified, find all studies
			results.put("matched_studies", runner.doBasicSearchForTrees(new MatchAllDocsQuery(), null, verbose));

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
	@Description("Perform a simple search for trees in indexed studies")
	@PluginTarget(GraphDatabaseService.class)
	public Representation find_trees(@Source GraphDatabaseService graphDb,
			@Description("The property to be searched on. A list of searchable properties is available from the getSearchablePropertiesForTrees service.")
				@Parameter(name = "property", optional = false) String property,
			@Description("The value to be searched. This must be passed as a string, but will be converted to the datatype corresponding to the "
					+ "specified searchable value.") @Parameter(name = "value", optional = false) String value,
			@Description("Whether to perform exact matching ONLY. Defaults to false, i.e. fuzzy matching is enabled. Only applicable for some string properties.")
				@Parameter(name="exact", optional = true) Boolean checkExactOnly,
			@Description("Whether or not to include all metadata. By default, only the nexson ids of elements will be returned.")
				@Parameter(name = "verbose", optional = true) Boolean verbose) {
		
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
		results.put("tree_properties", properties.getIndexedStudyProperties().keySet());
		
		return OTRepresentationConverter.convert(results);
	}
}
