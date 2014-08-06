package org.opentree.oti;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.opentree.graphdb.GraphDatabaseAgent;
import org.opentree.oti.constants.OTIRelType;
import org.opentree.oti.indexproperties.IndexedArrayProperties;
import org.opentree.oti.indexproperties.IndexedPrimitiveProperties;
import org.opentree.oti.indexproperties.OTINodeProperty;
import org.opentree.oti.indexproperties.OTPropertyArray;
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
	 */
	public Object doBasicSearchForStudies(Set<OTPropertyPredicate> properties, String searchValue, boolean checkFulltext, boolean verbose) {

   		// using fuzzy queries ... may want to use different queries for exact vs. fulltext indexes
		BooleanQuery fuzzyQuery = new BooleanQuery();
		boolean doExactSearch = false;
		boolean doFulltextSearch = false;
		
		if (properties != null) {
			for (OTPropertyPredicate property : properties) {
				fuzzyQuery.add(new FuzzyQuery(new Term(property.propertyName(),
						QueryParser.escape(searchValue.toLowerCase())), AbstractBaseQuery.getMinIdentity(searchValue)), Occur.SHOULD);
				
		    	if (IndexedArrayProperties.STUDIES_EXACT.properties().contains(property) ||
		    			IndexedPrimitiveProperties.STUDIES_EXACT.properties().contains(property)) {
		    		doExactSearch = true;
		    	}

		    	if (checkFulltext &&
		    			(IndexedArrayProperties.STUDIES_FULLTEXT.properties().contains(property) ||
		    					IndexedPrimitiveProperties.STUDIES_FULLTEXT.properties().contains(property))) {
		    		doFulltextSearch = true;
		    	}
			}			
		} else {
			throw new IllegalArgumentException("attempt to execute a property-based query with no specified properties");
		}
		
		fuzzyQuery.setMinimumNumberShouldMatch(1);
    	
    	return doBasicSearchForStudies(fuzzyQuery, doExactSearch, doFulltextSearch, verbose);
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

   		// using fuzzy queries ... may want to use different queries for exact vs. fulltext indexes
		BooleanQuery fuzzyQuery = new BooleanQuery();
		boolean doExactSearch = false;
		boolean doFulltextSearch = false;
		
		if (properties != null) {
			for (OTPropertyPredicate property : properties) {
				fuzzyQuery.add(new FuzzyQuery(new Term(property.propertyName(),
						QueryParser.escape(searchValue.toLowerCase())), AbstractBaseQuery.getMinIdentity(searchValue)), Occur.SHOULD);
				
		    	if (IndexedArrayProperties.TREES_EXACT.properties().contains(property) ||
		    			IndexedPrimitiveProperties.TREES_EXACT.properties().contains(property)) {
		    		doExactSearch = true;
		    	}

		    	if (checkFulltext &&
		    			(IndexedArrayProperties.TREES_FULLTEXT.properties().contains(property) ||
		    					IndexedPrimitiveProperties.TREES_FULLTEXT.properties().contains(property))) {
		    		doFulltextSearch = true;
		    	}
			}			
		} else {
			throw new IllegalArgumentException("attempt to execute a property-based query with no specified properties");
		}
				
		fuzzyQuery.setMinimumNumberShouldMatch(1);
		
		return doBasicSearchForTrees(fuzzyQuery, doExactSearch, doFulltextSearch, verbose);
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
		
   		// using fuzzy queries ... may want to use different queries for exact vs. fulltext indexes
		BooleanQuery fuzzyQuery = new BooleanQuery();
		boolean doExactSearch = false;
		boolean doFulltextSearch = false;
		
		if (properties != null) {
			for (OTPropertyPredicate property : properties) {
				fuzzyQuery.add(new FuzzyQuery(new Term(property.propertyName(),
						QueryParser.escape(searchValue.toLowerCase())), AbstractBaseQuery.getMinIdentity(searchValue)), Occur.SHOULD);
				
		    	if (IndexedArrayProperties.TREE_NODES_EXACT.properties().contains(property) ||
		    			IndexedPrimitiveProperties.TREE_NODES_EXACT.properties().contains(property)) {
		    		doExactSearch = true;
		    	}

		    	if (checkFulltext &&
		    			(IndexedArrayProperties.TREE_NODES_FULLTEXT.properties().contains(property) ||
		    					IndexedPrimitiveProperties.TREE_NODES_FULLTEXT.properties().contains(property))) {
		    		doFulltextSearch = true;
		    	}
			}			
		} else {
			throw new IllegalArgumentException("attempt to execute a property-based query with no specified properties");
		}
		
		fuzzyQuery.setMinimumNumberShouldMatch(1);

		return doBasicSearchForTreeNodes(fuzzyQuery, doExactSearch, doFulltextSearch, verbose);
	}
	
	/**
	 * Search the indexes using the provided Query object for studies that match.
	 * @param query
	 * @param doExactSearch
	 * @param doFulltextSearch
	 * @param verbose
	 * @return
	 */
	public Object doBasicSearchForStudies(Query query, boolean doExactSearch, boolean doFulltextSearch, boolean verbose) {
		
		if (!doExactSearch && !doFulltextSearch) {
			throw new IllegalArgumentException("Request to do a query where neither exact nor fulltext indexes are to be searched. This is illegal--at least one type of indexed must be searched.");
		}
		
		// prepare to record a list of found studies
		Set<Long> studyMetaNodeIds = new HashSet<Long>();

		// search the indexes, record all matching studies
		IndexHits<Node> hits = null;
        try {
        	if (doExactSearch) {
				hits = studyMetaNodesByPropertyExact.query(query);
				for (Node hit : hits) {
					studyMetaNodeIds.add(hit.getId());
				}
        	}
        	if (doFulltextSearch) {
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
	 * @param doExactSearch
	 * @param doFulltextSearch
	 * @param verbose
	 * @return
	 */
	public Object doBasicSearchForTrees(Query query, boolean doExactSearch, boolean doFulltextSearch, boolean verbose) {

		if (!doExactSearch && !doFulltextSearch) {
			throw new IllegalArgumentException("Request to do a query where neither exact nor fulltext indexes are to be searched. This is illegal--at least one type of indexed must be searched.");
		}

		// prepare to record a list of matching trees and the studies containing them
		HashMap<Long, HashSet<Long>> treeRootNodeIdsByStudyMetaNodeId = new HashMap<Long, HashSet<Long>>();
		
		// search the indexes and record matching trees and studies
		IndexHits<Node> hits = null;
        try {
        	if (doExactSearch) {
        		hits = treeRootNodesByPropertyExact.query(query);
        		for (Node hit : hits) {
					
					Long studyMetaNodeId = getStudyMetaNodeForTreeNode(hit).getId();
					if (!treeRootNodeIdsByStudyMetaNodeId.containsKey(studyMetaNodeId)) {
						treeRootNodeIdsByStudyMetaNodeId.put(studyMetaNodeId, new HashSet<Long>());
					}
					treeRootNodeIdsByStudyMetaNodeId.get(studyMetaNodeId).add(hit.getId());
				}
        	}
        	if (doFulltextSearch) {
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
	public Object doBasicSearchForTreeNodes(Query query, boolean doExactSearch, boolean doFulltextSearch, boolean verbose) {
		
		if (!doExactSearch && !doFulltextSearch) {
			throw new IllegalArgumentException("Request to do a query where neither exact nor fulltext indexes are to be searched. This is illegal--at least one type of indexed must be searched.");
		}

		// prepare to record a list of tree nodes and the trees and studies that contain them
		Map<Long, HashMap<Long, HashSet<Long>>> treeTipNodeIdsByTreeRootNodeIdByStudyMetaNodeId = new HashMap<Long, HashMap<Long, HashSet<Long>>>();
		
		IndexHits<Node> hits = null;
		try {
			if (doExactSearch) {
	    		hits = treeNodesByPropertyExact.query(query);
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
			
			if (doFulltextSearch) {
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
