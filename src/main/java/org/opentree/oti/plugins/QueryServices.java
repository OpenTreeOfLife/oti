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
public class QueryServices extends ServerPlugin {
	
	@Description("Returns information about all studies in the database.")
	@PluginTarget(GraphDatabaseService.class)
	public Representation findAllStudies(@Source GraphDatabaseService graphDb,
		@Description("The property to be searched on. A list of searchable properties is available from the getSearchablePropertiesForStudies service.")
			@Parameter(name = "includeTreeMetadata", optional = true) Boolean includeTreeMetadata,
		@Description("Whether or not to include all metadata. By default, only the nexson ids of elements will be returned.")
			@Parameter(name = "verbose", optional = true) Boolean verbose) {

		// set null optional parameters to default values
		verbose = verbose == null ? false : verbose;
		includeTreeMetadata = includeTreeMetadata == null ? false : includeTreeMetadata;

		QueryRunner runner = new QueryRunner(graphDb);
		if (includeTreeMetadata) {
			return OTRepresentationConverter.convert(runner.doBasicSearchForTrees(new MatchAllDocsQuery(), null, verbose));
		} else {
			return OTRepresentationConverter.convert(runner.doBasicSearchForStudies(new MatchAllDocsQuery(), null, verbose));
		}
	}
	
	/**
	 * Perform a simple search for studies
	 * @param graphDb
	 * @param propertyName
	 * @param value
	 * @return
	 * @throws ParseException 
	 */
	@Description("Perform a simple search for indexed studies")
	@PluginTarget(GraphDatabaseService.class)
	public Representation singlePropertySearchForStudies(@Source GraphDatabaseService graphDb,
			@Description("The property to be searched on. A list of searchable properties is available from the getSearchablePropertiesForStudies service.")
				@Parameter(name = "property", optional = false) String property,
			@Description("The value to be searched. This must be passed as a string, but will be converted to the datatype corresponding to the "
					+ "specified searchable value.") @Parameter(name = "value", optional = false) String value,
			@Description("Whether to perform exact matching ONLY. Defaults to false, i.e. fuzzy matching is enabled. Only applicable for some string properties.")
				@Parameter(name="exact", optional = true) Boolean checkExactOnly,
			@Description("Whether or not to include all metadata. By default, only the nexson ids of elements will be returned.")
				@Parameter(name = "verbose", optional = true) Boolean verbose) throws ParseException {
		
		// set null optional parameters to default values
		verbose = verbose == null ? false : verbose;
		boolean doFuzzyMatching = checkExactOnly == null ? true : ! checkExactOnly;

		HashSet<OTPropertyPredicate> searchProperties = new OTIProperties().getIndexedStudyProperties().get(property);
		HashMap<String, Object> results = new HashMap<String, Object>();
				
		if (searchProperties != null) {
			QueryRunner runner = new QueryRunner(graphDb);
			results.put("matched_studies", runner.doBasicSearchForStudies(searchProperties, value, doFuzzyMatching, verbose));
		} else {
			results.put("error", "unrecognized property: " + property);
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
	public Representation singlePropertySearchForTrees(@Source GraphDatabaseService graphDb,
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
	 * Perform a simple search for tree nodes (currently just tips)
	 * @param graphDb
	 * @param propertyName
	 * @param value
	 * @return
	 */
	@Description("Perform a simple search for trees nodes (currently only supports tip nodes) in indexed studies")
	@PluginTarget(GraphDatabaseService.class)
	public Representation singlePropertySearchForTreeNodes(@Source GraphDatabaseService graphDb,
			@Description("The property to be searched on. A list of searchable properties is available from the getSearchablePropertiesForTrees service.")
				@Parameter(name = "property", optional = false) String property,
			@Description("The value to be searched. This must be passed as a string, but will be converted to the datatype corresponding to the "
					+ "specified searchable value.") @Parameter(name = "value", optional = false) String value,
			@Description("Whether to perform exact matching ONLY. Defaults to false, i.e. fuzzy matching is enabled. Only applicable for some string properties.")
				@Parameter(name="exact", optional = true) Boolean checkExactOnly,
			@Description("Whether or not to include all metadata. By default, only the nexson ids of elements will be returned.")
				@Parameter(name = "verbose", optional = true) Boolean verbose) {
		
		// set null parameters to default values
		verbose = verbose == null ? false : verbose;
		boolean doFuzzyMatching = checkExactOnly == null ? true : ! checkExactOnly;

		HashSet<OTPropertyPredicate> searchProperties = new OTIProperties().getIndexedTreeNodeProperties().get(property);
		HashMap<String, Object> results = new HashMap<String, Object>();

		if (searchProperties != null) {
			QueryRunner runner = new QueryRunner(graphDb);			
			results.put("matched_studies", runner.doBasicSearchForTreeNodes(searchProperties, value, doFuzzyMatching, verbose));
			
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
	@Description("Get a list of properties that can be used to search for studies")
	@PluginTarget(GraphDatabaseService.class)
	public Representation getSearchablePropertiesForStudies (@Source GraphDatabaseService graphDb) {

		return OTRepresentationConverter.convert(new OTIProperties().getIndexedStudyProperties().keySet());
	}

	/**
	 * Return a map containing available property names and the names of the SearchableProperty enum elements they
	 * correspond to.
	 * 
	 * @param graphDb
	 * @return
	 */
	@Description("Get a list of properties that can be used to search for trees")
	@PluginTarget(GraphDatabaseService.class)
	public Representation getSearchablePropertiesForTrees (@Source GraphDatabaseService graphDb) {

		return OTRepresentationConverter.convert(new OTIProperties().getIndexedTreeProperties().keySet());
	}

	/**
	 * Return a map containing available property names and the names of the SearchableProperty enum elements they
	 * correspond to.
	 * 
	 * @param graphDb
	 * @return
	 */
	@Description("Get a list of properties that can be used to search for tree nodes")
	@PluginTarget(GraphDatabaseService.class)
	public Representation getSearchablePropertiesForTreeNodes (@Source GraphDatabaseService graphDb) {

		return OTRepresentationConverter.convert(new OTIProperties().getIndexedTreeNodeProperties().keySet());
	}
}
