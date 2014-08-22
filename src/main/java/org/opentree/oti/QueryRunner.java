package org.opentree.oti;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Query;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.opentree.graphdb.GraphDatabaseAgent;
import org.opentree.oti.indexproperties.IndexedArrayProperties;
import org.opentree.oti.indexproperties.IndexedPrimitiveProperties;
import org.opentree.oti.indexproperties.OTINodeProperty;
import org.opentree.properties.OTPropertyPredicate;
import org.opentree.properties.OTVocabularyPredicate;
import org.opentree.tnrs.queries.AbstractBaseQuery;

public class QueryRunner extends OTIDatabase {
	
	public final Index<Node> studyMetaNodesByPropertyExact = getNodeIndex(OTINodeIndex.STUDY_METADATA_NODES_BY_PROPERTY_EXACT);
	public final Index<Node> studyMetaNodesByPropertyFulltext = getNodeIndex(OTINodeIndex.STUDY_METADATA_NODES_BY_PROPERTY_FULLTEXT);
	public final Index<Node> treeRootNodesByPropertyExact = getNodeIndex(OTINodeIndex.TREE_ROOT_NODES_BY_PROPERTY_EXACT);
	public final Index<Node> treeRootNodesByPropertyFulltext = getNodeIndex(OTINodeIndex.TREE_ROOT_NODES_BY_PROPERTY_FULLTEXT);
	public final Index<Node> treeNodesByPropertyExact = getNodeIndex(OTINodeIndex.TREE_NODES_BY_PROPERTY_EXACT);
	public final Index<Node> treeNodesByPropertyFulltext = getNodeIndex(OTINodeIndex.TREE_NODES_BY_PROPERTY_FULLTEXT);

	public QueryRunner(EmbeddedGraphDatabase embeddedGraph) {
		super(embeddedGraph);
	}

	public QueryRunner(GraphDatabaseService gdbs) {
		super(gdbs);
	}

	public QueryRunner(GraphDatabaseAgent gdba) {
		super(gdba);
	}
	
	/**
	 * Search the indexes for study matching the search parameters
	 * @param property
	 * 		A SearchableProperty to specify the search domain
	 * @param searchValue
	 * 		The value to be searched for
	 * @return
	 * 		A list of strings containing the node ids of the source meta nodes for sources found during search
	 * @throws ParseException 
	 */
	public Object doBasicSearchForStudies(Set<OTPropertyPredicate> properties, String searchValue, boolean checkFulltext, boolean verbose) throws ParseException {

		BooleanQuery query = new BooleanQuery();
		query.setMinimumNumberShouldMatch(1);
		
		HashMap<String,Object> exactProperties = new HashMap<String,Object>();

		if (properties == null) {
			throw new IllegalArgumentException("attempt to execute a property-based query with no specified properties");
		}

		for (OTPropertyPredicate property : properties) {
			
	    	if (IndexedArrayProperties.STUDIES_EXACT.properties().contains(property) ||
	    			IndexedPrimitiveProperties.STUDIES_EXACT.properties().contains(property)) {
	    		exactProperties.put(property.propertyName(),searchValue);
	    	}

	    	if (checkFulltext &&
	    			(IndexedArrayProperties.STUDIES_FULLTEXT.properties().contains(property) ||
	    					IndexedPrimitiveProperties.STUDIES_FULLTEXT.properties().contains(property))) {
				query.add(new FuzzyQuery(new Term(property.propertyName(), QueryParser.escape(searchValue.toLowerCase())),
						AbstractBaseQuery.getMinIdentity(searchValue)), Occur.SHOULD);
	    	}
		}	

    	return doBasicSearchForStudies(query, exactProperties, verbose);
	}
	
	/**
	 * Search the indexes for trees matching the search parameters
	 * @param property
	 * 		A SearchableProperty to specify the search domain
	 * @param searchValue
	 * 		The value to be searched for
	 * @return
	 * 		A Map object containing information about hits to the search
	 */
	public Object doBasicSearchForTrees(Set<OTPropertyPredicate> properties, String searchValue, boolean checkFulltext, boolean verbose) {

		BooleanQuery query = new BooleanQuery();
		query.setMinimumNumberShouldMatch(1);
		
		HashMap<String,Object> exactProperties = new HashMap<String,Object>();

		if (properties == null) {
			throw new IllegalArgumentException("attempt to execute a property-based query with no specified properties");
		}

		for (OTPropertyPredicate property : properties) {
			
	    	if (IndexedArrayProperties.TREES_EXACT.properties().contains(property) ||
	    			IndexedPrimitiveProperties.TREES_EXACT.properties().contains(property)) {
	    		exactProperties.put(property.propertyName(),searchValue);
	    	}

	    	if (checkFulltext &&
	    			(IndexedArrayProperties.TREES_FULLTEXT.properties().contains(property) ||
	    					IndexedPrimitiveProperties.TREES_FULLTEXT.properties().contains(property))) {
				query.add(new FuzzyQuery(new Term(property.propertyName(), QueryParser.escape(searchValue.toLowerCase())),
						AbstractBaseQuery.getMinIdentity(searchValue)), Occur.SHOULD);
	    	}
		}	

		return doBasicSearchForTrees(query, exactProperties, verbose);
	}
	
	/**
	 * Search the indexes for tree nodes matching the search parameters
	 * @param property
	 * 		A SearchableProperty to specify the search domain
	 * @param searchValue
	 * 		The value to be searched for
	 * @return
	 * 		A Map object containing information about hits to the search
	 */
	public Object doBasicSearchForTreeNodes(Set<OTPropertyPredicate> properties, String searchValue, boolean checkFulltext, boolean verbose) {
		
		BooleanQuery query = new BooleanQuery();
		query.setMinimumNumberShouldMatch(1);
		
		HashMap<String,Object> exactProperties = new HashMap<String,Object>();

		if (properties == null) {
			throw new IllegalArgumentException("attempt to execute a property-based query with no specified properties");
		}

		for (OTPropertyPredicate property : properties) {
			
	    	if (IndexedArrayProperties.TREE_NODES_EXACT.properties().contains(property) ||
	    			IndexedPrimitiveProperties.TREE_NODES_EXACT.properties().contains(property)) {
	    		exactProperties.put(property.propertyName(),searchValue);
	    	}

	    	if (checkFulltext &&
	    			(IndexedArrayProperties.TREE_NODES_FULLTEXT.properties().contains(property) ||
	    					IndexedPrimitiveProperties.TREE_NODES_FULLTEXT.properties().contains(property))) {
				query.add(new FuzzyQuery(new Term(property.propertyName(), QueryParser.escape(searchValue.toLowerCase())),
						AbstractBaseQuery.getMinIdentity(searchValue)), Occur.SHOULD);
	    	}
		}	

    	return doBasicSearchForTreeNodes(query, exactProperties, verbose);
	}

	/**
	 * Search the indexes using the provided Query object for studies that match.
	 * @param query
	 * @param exactProperties
	 * @param verbose
	 * @return
	 */
	public Object doBasicSearchForStudies(Query query, Map<String,Object> exactProperties, boolean verbose) {		
		
		if (query == null && (exactProperties == null || exactProperties.isEmpty())) {
			throw new IllegalArgumentException("Request to do a query where neither exact nor fulltext indexes are to be searched. This is illegal--at least one type of indexed must be searched.");
		}
		
		// prepare to record a list of found studies
		Set<Long> studyMetaNodeIds = new HashSet<Long>();

		// search the indexes, record all matching studies
		IndexHits<Node> hits = null;
        try {
        	if (exactProperties != null) {
        		for (Entry<String,Object> p : exactProperties.entrySet()) {
					hits = studyMetaNodesByPropertyExact.get(p.getKey(),p.getValue()); // use index.get() method for exact matches!
					for (Node hit : hits) {
						studyMetaNodeIds.add(hit.getId());
					}
        		}
        	}
        	if (query != null) {
				hits = studyMetaNodesByPropertyFulltext.query(query);
				for (Node hit : hits) {
					studyMetaNodeIds.add(hit.getId());
				}
        	}
        } finally {
			hits.close();
		}
		
        // for each study found
		List<HashMap<String, Object>> studiesFound = new LinkedList<HashMap<String, Object>>();
		for (Long nid : studyMetaNodeIds) {

			// prepare to collect study metadata
			Node studyNode = graphDb.getNodeById(nid);
			HashMap<String, Object> study = new HashMap<String, Object>();
			
			// record additional metadata if requested
			if (verbose) {
				
				// TODO: this two-pass approach could be made more efficient--there is overlap between the exact and fulltext properties
				for (OTPropertyPredicate p : IndexedPrimitiveProperties.STUDIES_EXACT.properties()) {
					if (studyNode.hasProperty(p.propertyName())) {
						study.put(p.propertyName(), studyNode.getProperty(p.propertyName()));
					}
				}
				for (OTPropertyPredicate p : IndexedPrimitiveProperties.STUDIES_FULLTEXT.properties()) {
					if (studyNode.hasProperty(p.propertyName())) {
						study.put(p.propertyName(), studyNode.getProperty(p.propertyName()));
					}
				}

			// otherwise just record ids
			} else {
				study.put(OTVocabularyPredicate.OT_STUDY_ID.propertyName(),
						(String) studyNode.getProperty(OTVocabularyPredicate.OT_STUDY_ID.propertyName()));
			}
			
			studiesFound.add(study);
		}

		return studiesFound;
	}
	
	/**
	 * Search the indexes using the provided Query object for trees that match.
	 * @param query
	 * @param exactProperties
	 * @param verbose
	 * @return
	 */
	public Object doBasicSearchForTrees(Query query, Map<String,Object> exactProperties, boolean verbose) {		
		
		if (query == null && (exactProperties == null || exactProperties.isEmpty())) {
			throw new IllegalArgumentException("Request to do a query where neither exact nor fulltext indexes are to be searched. This is illegal--at least one type of indexed must be searched.");
		}
		
		// prepare to record a list of matching trees and the studies containing them
		HashMap<Long, HashSet<Long>> treeRootNodeIdsByStudyMetaNodeId = new HashMap<Long, HashSet<Long>>();
		
		// search the indexes and record matching trees and studies
		IndexHits<Node> hits = null;
        try {
        	if (exactProperties != null) {
        		for (Entry<String,Object> p : exactProperties.entrySet()) {
	        		hits = treeRootNodesByPropertyExact.get(p.getKey(),p.getValue()); // use index.get() method for exact matches!
	        		for (Node hit : hits) {
						Long studyMetaNodeId = getStudyMetaNodeForTreeNode(hit).getId();
						if (!treeRootNodeIdsByStudyMetaNodeId.containsKey(studyMetaNodeId)) {
							treeRootNodeIdsByStudyMetaNodeId.put(studyMetaNodeId, new HashSet<Long>());
						}
						treeRootNodeIdsByStudyMetaNodeId.get(studyMetaNodeId).add(hit.getId());
					}
        		}
        	}
        	if (query != null) {
				hits = treeRootNodesByPropertyFulltext.query(query);
				for (Node hit : hits) {

					Long studyMetaNodeId = getStudyMetaNodeForTreeNode(hit).getId();
					if (!treeRootNodeIdsByStudyMetaNodeId.containsKey(studyMetaNodeId)) {
						treeRootNodeIdsByStudyMetaNodeId.put(studyMetaNodeId, new HashSet<Long>());
					}
					treeRootNodeIdsByStudyMetaNodeId.get(studyMetaNodeId).add(hit.getId());
				}
        	}
        } finally {
			hits.close();
		}
		        
		// for each study containing a matched tree
		List<HashMap<String, Object>> treesFoundByStudy = new LinkedList<HashMap<String, Object>>();
		for (Long studyNodeId : treeRootNodeIdsByStudyMetaNodeId.keySet()) {

			// prepare to collect study metadata
			HashMap<String, Object> studyResult = new HashMap<String, Object>();
			Node studyNode = graphDb.getNodeById(studyNodeId);

			// record additional study metadata if requested
			if (verbose) {
				for (OTPropertyPredicate p : IndexedPrimitiveProperties.STUDIES_EXACT.properties()) {
					if (studyNode.hasProperty(p.propertyName())) {
						studyResult.put(p.propertyName(), studyNode.getProperty(p.propertyName()));
					}
				}
				for (OTPropertyPredicate p : IndexedPrimitiveProperties.STUDIES_FULLTEXT.properties()) {
					if (studyNode.hasProperty(p.propertyName())) {
						studyResult.put(p.propertyName(), studyNode.getProperty(p.propertyName()));
					}
				}
			
			// otherwise just record the study id
			} else {
				studyResult.put(OTVocabularyPredicate.OT_STUDY_ID.propertyName(), studyNode.getProperty(OTVocabularyPredicate.OT_STUDY_ID.propertyName()));
			}

			// for each matched tree in study
			List<HashMap<String, Object>> treeResults = new LinkedList<HashMap<String, Object>>();
			for (Long nodeId : treeRootNodeIdsByStudyMetaNodeId.get(studyNodeId)) {

				// prepare to collect tree metadata
				HashMap<String, Object> curTreeResult = new HashMap<String, Object>();
				Node treeRootNode = graphDb.getNodeById(nodeId);
				
				// provide additional tree metadata if requested
				if (verbose) {
					
					// TODO: this two-pass approach could be made more efficient--there is overlap between the exact and fulltext properties
					for (OTPropertyPredicate p : IndexedPrimitiveProperties.TREES_EXACT.properties()) {
						if (treeRootNode.hasProperty(p.propertyName())) {
							curTreeResult.put(p.propertyName(), treeRootNode.getProperty(p.propertyName()));
						}
					}
					for (OTPropertyPredicate p : IndexedPrimitiveProperties.TREES_FULLTEXT.properties()) {
						if (treeRootNode.hasProperty(p.propertyName())) {
							curTreeResult.put(p.propertyName(), treeRootNode.getProperty(p.propertyName()));
						}
					}

				// otherwise just record the tree id and nexson element id for the tree
				} else {
					curTreeResult.put(OTINodeProperty.TREE_ID.propertyName(), (String) treeRootNode.getProperty(OTINodeProperty.TREE_ID.propertyName()));
					curTreeResult.put(OTINodeProperty.NEXSON_ID.propertyName(), (String) treeRootNode.getProperty(OTINodeProperty.NEXSON_ID.propertyName())); 
				}
				
				treeResults.add(curTreeResult);
			}

			studyResult.put("matched_trees", treeResults);
			treesFoundByStudy.add(studyResult);
		}

		return treesFoundByStudy;
	}
	
	/**
	 * Search the indexes for tree nodes matching the search parameters
	 * @param query
	 * 		A Query object that will be used to search the indexes
	 * @return
	 * 		A Map object containing information about hits to the search
	 */
	public Object doBasicSearchForTreeNodes(Query query, Map<String,Object> exactProperties, boolean verbose) {
		
		if (query == null && (exactProperties == null || exactProperties.isEmpty())) {
			throw new IllegalArgumentException("Request to do a query where neither exact nor fulltext indexes are to be searched. This is illegal--at least one type of indexed must be searched.");
		}

		// prepare to record a list of tree nodes and the trees and studies that contain them
		Map<Long, HashMap<Long, HashSet<Long>>> treeTipNodeIdsByTreeRootNodeIdByStudyMetaNodeId = new HashMap<Long, HashMap<Long, HashSet<Long>>>();
		
		IndexHits<Node> hits = null;
		try {
	       	if (exactProperties != null) {
        		for (Entry<String,Object> p : exactProperties.entrySet()) {
	        		hits = treeRootNodesByPropertyExact.get(p.getKey(),p.getValue()); // use index.get() method for exact matches!
					for (Node hit : hits) {
		
						Node treeRootNode = OTIDatabaseUtils.getRootOfTreeContaining(hit);
						Long studyMetaNodeId = getStudyMetaNodeForTreeNode(treeRootNode).getId();
						Long treeRootNodeId = treeRootNode.getId();
						
						// add an entry for the study if this is the first time we've seen it
						if (!treeTipNodeIdsByTreeRootNodeIdByStudyMetaNodeId.containsKey(studyMetaNodeId)) {
							treeTipNodeIdsByTreeRootNodeIdByStudyMetaNodeId.put(studyMetaNodeId, new HashMap<Long, HashSet<Long>>());
						}
		
						// add an entry for the tree if this is the first time we've seen it
						if (!treeTipNodeIdsByTreeRootNodeIdByStudyMetaNodeId.get(studyMetaNodeId).containsKey(treeRootNodeId)) {
							treeTipNodeIdsByTreeRootNodeIdByStudyMetaNodeId.get(studyMetaNodeId).put(treeRootNodeId, new HashSet<Long>());
						}
						
						// record the id of the matched tree node
						treeTipNodeIdsByTreeRootNodeIdByStudyMetaNodeId.get(studyMetaNodeId).get(treeRootNodeId).add(hit.getId());
					}
        		}
			}
			
			if (query != null) {
	    		hits = treeNodesByPropertyFulltext.query(query);
				for (Node hit : hits) {
	
					Node treeRootNode = OTIDatabaseUtils.getRootOfTreeContaining(hit);
					Long studyMetaNodeId = getStudyMetaNodeForTreeNode(treeRootNode).getId();
					Long treeRootNodeId = treeRootNode.getId();
					
					// add an entry for the study if this is the first time we've seen it
					if (!treeTipNodeIdsByTreeRootNodeIdByStudyMetaNodeId.containsKey(studyMetaNodeId)) {
						treeTipNodeIdsByTreeRootNodeIdByStudyMetaNodeId.put(studyMetaNodeId, new HashMap<Long, HashSet<Long>>());
					}
	
					// add an entry for the tree if this is the first time we've seen it
					if (!treeTipNodeIdsByTreeRootNodeIdByStudyMetaNodeId.get(studyMetaNodeId).containsKey(treeRootNodeId)) {
						treeTipNodeIdsByTreeRootNodeIdByStudyMetaNodeId.get(studyMetaNodeId).put(treeRootNodeId, new HashSet<Long>());
					}
					
					// record the id of the matched tree node
					treeTipNodeIdsByTreeRootNodeIdByStudyMetaNodeId.get(studyMetaNodeId).get(treeRootNodeId).add(hit.getId());
				}
			}
			
		} finally {
			hits.close();
		}
		
		// prepare to record identifying information about the nodes found, organized by containing tree and study
		List<HashMap<String, Object>> treeNodesFoundByTreeAndByStudy = new LinkedList<HashMap<String, Object>>();
		
		// for each matched study
		for (Long studyNodeId : treeTipNodeIdsByTreeRootNodeIdByStudyMetaNodeId.keySet()) {

			// prepare to collect study metadata
			HashMap<String, Object> studyResult = new HashMap<String, Object>();
			Node studyNode = graphDb.getNodeById(studyNodeId);

			// provide additional study metadata if requested
			if (verbose) {
				
				// TODO: this two-pass approach could be made more efficient--there is overlap between the exact and fulltext properties
				for (OTPropertyPredicate p : IndexedPrimitiveProperties.STUDIES_EXACT.properties()) {
					if (studyNode.hasProperty(p.propertyName())) {
						studyResult.put(p.propertyName(), studyNode.getProperty(p.propertyName()));
					}
				}
				for (OTPropertyPredicate p : IndexedPrimitiveProperties.STUDIES_FULLTEXT.properties()) {
					if (studyNode.hasProperty(p.propertyName())) {
						studyResult.put(p.propertyName(), studyNode.getProperty(p.propertyName()));
					}
				}
			
			// otherwise just record the study id
			} else {
				studyResult.put(OTVocabularyPredicate.OT_STUDY_ID.propertyName(), studyNode.getProperty(OTVocabularyPredicate.OT_STUDY_ID.propertyName()));
			}
			
			// for each matched tree in the study
			List<HashMap<String, Object>> treeResults = new LinkedList<HashMap<String, Object>>();
			for (Long treeRootNodeId : treeTipNodeIdsByTreeRootNodeIdByStudyMetaNodeId.get(studyNodeId).keySet()) {
				
				// prepare to collect tree metadata
				HashMap<String, Object> curTreeResult = new HashMap<String, Object>();
				Node treeRootNode = graphDb.getNodeById(treeRootNodeId);
				
				// provide additional tree metadata if requested
				if (verbose) {
					
					// TODO: this two-pass approach could be made more efficient--there is overlap between the exact and fulltext properties
					for (OTPropertyPredicate p : IndexedPrimitiveProperties.TREES_EXACT.properties()) {
						if (treeRootNode.hasProperty(p.propertyName())) {
							curTreeResult.put(p.propertyName(), treeRootNode.getProperty(p.propertyName()));
						}
					}
					for (OTPropertyPredicate p : IndexedPrimitiveProperties.TREES_FULLTEXT.properties()) {
						if (treeRootNode.hasProperty(p.propertyName())) {
							curTreeResult.put(p.propertyName(), treeRootNode.getProperty(p.propertyName()));
						}
					}

				// otherwise just record the tree id and nexson element id for the tree
				} else {
					curTreeResult.put(OTINodeProperty.TREE_ID.propertyName(), (String) treeRootNode.getProperty(OTINodeProperty.TREE_ID.propertyName()));
					curTreeResult.put(OTINodeProperty.NEXSON_ID.propertyName(), (String) treeRootNode.getProperty(OTINodeProperty.NEXSON_ID.propertyName())); 
				}

				// for each matched tree node
				List<HashMap<String, Object>> matchedNodeResults = new LinkedList<HashMap<String, Object>>();
				for (Long matchedTreeNodeId : treeTipNodeIdsByTreeRootNodeIdByStudyMetaNodeId.get(studyNodeId).get(treeRootNodeId)) {
					
					// prepare to collect tree node metadata
					HashMap<String, Object> curNodeResult = new HashMap<String, Object>();
					Node matchedTreeNode = graphDb.getNodeById(matchedTreeNodeId);

					// provide additional tree node metadata if requested
					if (verbose) {
						
						// TODO: this two-pass approach could be made more efficient--there is overlap between the exact and fulltext properties
						for (OTPropertyPredicate p : IndexedPrimitiveProperties.TREE_NODES_EXACT.properties()) {
							if (matchedTreeNode.hasProperty(p.propertyName())) {
								curNodeResult.put(p.propertyName(), matchedTreeNode.getProperty(p.propertyName()));
							}
						}
						for (OTPropertyPredicate p : IndexedPrimitiveProperties.TREE_NODES_FULLTEXT.properties()) {
							if (matchedTreeNode.hasProperty(p.propertyName())) {
								curNodeResult.put(p.propertyName(), matchedTreeNode.getProperty(p.propertyName()));
							}
						}
						
					// otherwise just record the nexson node id
					} else {
						curNodeResult.put(OTINodeProperty.NEXSON_ID.propertyName(), (String) matchedTreeNode.getProperty(OTINodeProperty.NEXSON_ID.propertyName()));
					}

					matchedNodeResults.add(curNodeResult);
				}
				
				curTreeResult.put("matched_nodes", matchedNodeResults);
				treeResults.add(curTreeResult);
			}

			studyResult.put("matched_trees", treeResults);
			treeNodesFoundByTreeAndByStudy.add(studyResult);
		}
		
		return treeNodesFoundByTreeAndByStudy;
	}
}
