package org.opentree.oti.plugins;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.opentree.oti.QueryRunner;
import org.opentree.oti.indexproperties.IndexedArrayProperties;
import org.opentree.oti.indexproperties.IndexedPrimitiveProperties;
import org.opentree.oti.indexproperties.OTPropertyArray;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.plugins.*;
import org.neo4j.server.rest.repr.OTRepresentationConverter;
import org.neo4j.server.rest.repr.Representation;
import org.opentree.properties.OTPropertyPredicate;
import org.opentree.properties.OTVocabularyPredicate;

/**
 * Search services for the oti nexson database.
 * @author cody
 *
 */
public class QueryServices extends ServerPlugin {
	
	/**
	 * Perform a simple search for studies
	 * @param graphDb
	 * @param propertyName
	 * @param value
	 * @return
	 */
	@Description("Perform a simple search for indexed studies")
	@PluginTarget(GraphDatabaseService.class)
	public Representation singlePropertySearchForStudies(@Source GraphDatabaseService graphDb,
			@Description("The property to be searched on. A list of searchable properties is available from the getSearchablePropertiesForStudies service.")
				@Parameter(name = "property", optional = false) String property,
			@Description("The value to be searched. This must be passed as a string, but will be converted to the datatype corresponding to the "
					+ "specified searchable value.") @Parameter(name = "value", optional = false) String value) {
		
		QueryRunner runner = new QueryRunner(graphDb);
		boolean isExactProperty = false;
		boolean isFulltextProperty = false;
		OTPropertyPredicate searchProperty = null;

		// check exact array properties
		for (OTPropertyArray p : IndexedArrayProperties.STUDIES_EXACT.properties()) {
			if (p.typeProperty.propertyName().equals(property)) {
				searchProperty = p.typeProperty;
				isExactProperty = true;
				break;
			}
		}
		
		// specified property not exact array property, check for simple ones
		if (!isExactProperty) {
			for (OTPropertyPredicate p : IndexedPrimitiveProperties.STUDIES_EXACT.properties()) {
				if (p.propertyName().equals(property)) {
					searchProperty = p;
					isExactProperty = true;
					break;
				}
			}
		}

		// check fulltext array properties
		for (OTPropertyArray p : IndexedArrayProperties.STUDIES_FULLTEXT.properties()) {
			if (p.typeProperty.propertyName().equals(property)) {
				searchProperty = p.typeProperty;
				isFulltextProperty = true;
				break;
			}
		}
		
		// specified property not fulltext array property, check for simple ones
		if (!isFulltextProperty) {
			for (OTPropertyPredicate p : IndexedPrimitiveProperties.STUDIES_FULLTEXT.properties()) {
				if (p.propertyName().equals(property)) {
					searchProperty = p;
					isFulltextProperty = true;
					break;
				}
			}
		}
				
		HashMap<String, Object> results = new HashMap<String, Object>();
		if (searchProperty != null) {
			results.put("matched_studies", runner.doBasicSearchForStudies(searchProperty, value, isExactProperty, isFulltextProperty));
		} else {
			results.put("error", "uncrecognized property: " + property);
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
					+ "specified searchable value.") @Parameter(name = "value", optional = false) String value) {
		
		QueryRunner runner = new QueryRunner(graphDb);
		boolean isExactProperty = false;
		boolean isFulltextProperty = false;
		OTPropertyPredicate searchProperty = null;

		// check exact array properties
		for (OTPropertyArray p : IndexedArrayProperties.TREES_EXACT.properties()) {
			if (p.typeProperty.propertyName().equals(property)) {
				searchProperty = p.typeProperty;
				isExactProperty = true;
				break;
			}
		}
		
		// specified property not exact array property, check for simple ones
		if (!isExactProperty) {
			for (OTPropertyPredicate p : IndexedPrimitiveProperties.TREES_EXACT.properties()) {
				if (p.propertyName().equals(property)) {
					searchProperty = p;
					isExactProperty = true;
					break;
				}
			}
		}

		// check fulltext array properties
		for (OTPropertyArray p : IndexedArrayProperties.TREES_FULLTEXT.properties()) {
			if (p.typeProperty.propertyName().equals(property)) {
				searchProperty = p.typeProperty;
				isFulltextProperty = true;
				break;
			}
		}
		
		// specified property not fulltext array property, check for simple ones
		if (!isFulltextProperty) {
			for (OTPropertyPredicate p : IndexedPrimitiveProperties.TREES_FULLTEXT.properties()) {
				if (p.propertyName().equals(property)) {
					searchProperty = p;
					isFulltextProperty = true;
					break;
				}
			}
		}
		
		HashMap<String, Object> results = new HashMap<String, Object>();
		if (searchProperty != null) {
			results.put("matched_studies", runner.doBasicSearchForTrees(searchProperty, value, isExactProperty, isFulltextProperty));
		} else {
			results.put("error", "uncrecognized property: " + property);
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
					+ "specified searchable value.") @Parameter(name = "value", optional = false) String value) {
		
		// TODO: option to allow exact searching only
		
		QueryRunner runner = new QueryRunner(graphDb);
		boolean isExactProperty = false;
		boolean isFulltextProperty = false;
		OTPropertyPredicate searchProperty = null;

		// check exact array properties
		for (OTPropertyArray p : IndexedArrayProperties.TREE_NODES_EXACT.properties()) {
			if (p.typeProperty.propertyName().equals(property)) {
				searchProperty = p.typeProperty;
				isExactProperty = true;
				break;
			}
		}
		
		// specified property not exact array property, check for simple ones
		if (!isExactProperty) {
			for (OTPropertyPredicate p : IndexedPrimitiveProperties.TREE_NODES_EXACT.properties()) {
				if (p.propertyName().equals(property)) {
					searchProperty = p;
					isExactProperty = true;
					break;
				}
			}
		}

		// check fulltext array properties
		for (OTPropertyArray p : IndexedArrayProperties.TREE_NODES_FULLTEXT.properties()) {
			if (p.typeProperty.propertyName().equals(property)) {
				searchProperty = p.typeProperty;
				isFulltextProperty = true;
				break;
			}
		}
		
		// specified property not fulltext array property, check for simple ones
		if (!isFulltextProperty) {
			for (OTPropertyPredicate p : IndexedPrimitiveProperties.TREE_NODES_FULLTEXT.properties()) {
				if (p.propertyName().equals(property)) {
					searchProperty = p;
					isFulltextProperty = true;
					break;
				}
			}
		}
		
		HashMap<String, Object> results = new HashMap<String, Object>();
		if (searchProperty != null) {
			results.put("matched_studies", runner.doBasicSearchForTreeNodes(searchProperty, value, isExactProperty, isFulltextProperty));
		} else {
			results.put("error", "uncrecognized property: " + property);
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

		HashSet<String> properties = new HashSet<String>();
		
		addPrimitivePropertiesToSet(properties, IndexedPrimitiveProperties.STUDIES_EXACT);
		addPrimitivePropertiesToSet(properties, IndexedPrimitiveProperties.STUDIES_FULLTEXT);
		addArrayPropertiesToSet(properties, IndexedArrayProperties.STUDIES_EXACT);
		addArrayPropertiesToSet(properties, IndexedArrayProperties.STUDIES_FULLTEXT);
		
		return OTRepresentationConverter.convert(properties);
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

		HashSet<String> properties = new HashSet<String>();
		
		addPrimitivePropertiesToSet(properties, IndexedPrimitiveProperties.TREES_EXACT);
		addPrimitivePropertiesToSet(properties, IndexedPrimitiveProperties.TREES_FULLTEXT);
		addArrayPropertiesToSet(properties, IndexedArrayProperties.TREES_EXACT);
		addArrayPropertiesToSet(properties, IndexedArrayProperties.TREES_FULLTEXT);
		
		return OTRepresentationConverter.convert(properties);
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

		HashSet<String> properties = new HashSet<String>();
		
		addPrimitivePropertiesToSet(properties, IndexedPrimitiveProperties.TREE_NODES_EXACT);
		addPrimitivePropertiesToSet(properties, IndexedPrimitiveProperties.TREE_NODES_FULLTEXT);
		addArrayPropertiesToSet(properties, IndexedArrayProperties.TREE_NODES_EXACT);
		addArrayPropertiesToSet(properties, IndexedArrayProperties.TREE_NODES_FULLTEXT);
		
		return OTRepresentationConverter.convert(properties);
	}
	
	/**
	 * helper function
	 * @param pSet
	 * @param indexedProperties
	 */
	private void addPrimitivePropertiesToSet(Set<String> pSet, IndexedPrimitiveProperties indexedProperties) {
		for (OTPropertyPredicate p : indexedProperties.properties()) {
			pSet.add(p.propertyName());
		}
	}
	
	/**
	 * helper function
	 * @param pSet
	 * @param indexedProperties
	 */
	private void addArrayPropertiesToSet(Set<String> pSet, IndexedArrayProperties indexedProperties) {
		for (OTPropertyArray a : indexedProperties.properties()) {
			pSet.add(a.typeProperty.propertyName());
		}
	}
}
