package org.opentree.oti;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
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
	 */
	public Object doBasicSearchForStudies(OTPropertyPredicate property, String searchValue, boolean isExactProperty, boolean isFulltextProperty, boolean verbose) {

		Set<Long> studyMetaNodeIds = new HashSet<Long>();

		// TODO: need something to allow searching on property types other than strings

   		// using fuzzy queries ... may want to use different queries for exact vs. fulltext indexes
		
		// TODO: this does not work when doing exact queries. probably need to define a term query for those. going to need work
		
		FuzzyQuery fuzzyQuery = new FuzzyQuery(new Term(property.propertyName(), QueryParser.escape(searchValue.toLowerCase())),
    			AbstractBaseQuery.getMinIdentity(searchValue));
		IndexHits<Node> hits = null;

        try {
        	if (isExactProperty) {
				hits = studyMetaNodesByPropertyExact.query(fuzzyQuery);
				for (Node hit : hits) {
					studyMetaNodeIds.add(hit.getId());
				}
        	}
        	if (isFulltextProperty) {
				hits = studyMetaNodesByPropertyFulltext.query(fuzzyQuery);
				for (Node hit : hits) {
					studyMetaNodeIds.add(hit.getId());
				}
        	}
        } finally {
			hits.close();
		}
			
		List<HashMap<String, Object>> studiesFound = new LinkedList<HashMap<String, Object>>();
		for (Long nid : studyMetaNodeIds) {

			Node studyNode = graphDb.getNodeById(nid);

			HashMap<String, Object> study = new HashMap<String, Object>();
			study.put(OTVocabularyPredicate.OT_STUDY_ID.propertyName(),
					(String) studyNode.getProperty(OTVocabularyPredicate.OT_STUDY_ID.propertyName()));
			
			// provide additional metadata if requested
			if (verbose) {
				for (OTPropertyPredicate p : IndexedPrimitiveProperties.STUDIES_EXACT.properties()) {
					if (studyNode.hasProperty(p.propertyName())) {
						study.put(p.propertyName(), studyNode.getProperty(p.propertyName()));
					}
				}
			}
			
			studiesFound.add(study);
		}

		return studiesFound;
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
	public Object doBasicSearchForTrees(OTPropertyPredicate property, String searchValue, boolean isExactProperty, boolean isFulltextProperty, boolean verbose) {

		// < study node id < tree root node ids >>
		HashMap<Long, HashSet<Long>> treeRootNodeIdsByStudyNodeId = new HashMap<Long, HashSet<Long>>();
	
   		// using fuzzy queries ... may want to use different queries for exact vs. fulltext indexes
		
		// TODO: this does not work when doing exact queries. probably need to define a term query for those. going to need work
		
		FuzzyQuery fuzzyQuery = new FuzzyQuery(new Term(property.propertyName(), QueryParser.escape(searchValue.toLowerCase())),
    			AbstractBaseQuery.getMinIdentity(searchValue));

		IndexHits<Node> hits = null;
        try {
        	if (isExactProperty) {

        		hits = treeRootNodesByPropertyExact.query(fuzzyQuery);
				for (Node hit : hits) {
					
//					Long studyRootNodeId = hit.getProperty(OTVocabularyPredicate.OT_STUDY_ID.propertyName());
					Long studyNodeId = getStudyMetaNodeForTreeNode(hit).getId();
					if (!treeRootNodeIdsByStudyNodeId.containsKey(studyNodeId)) {
						treeRootNodeIdsByStudyNodeId.put(studyNodeId, new HashSet<Long>());
					}
					
					treeRootNodeIdsByStudyNodeId.get(studyNodeId).add(hit.getId());
				}
        	}
        	if (isFulltextProperty) {
				hits = treeRootNodesByPropertyFulltext.query(fuzzyQuery);
				for (Node hit : hits) {

//					Long studyNodeId = hit.getProperty(OTVocabularyPredicate.OT_STUDY_ID.propertyName());
					Long studyNodeId = getStudyMetaNodeForTreeNode(hit).getId();
					if (!treeRootNodeIdsByStudyNodeId.containsKey(studyNodeId)) {
						treeRootNodeIdsByStudyNodeId.put(studyNodeId, new HashSet<Long>());
					}

					// add the tree root node id to the list of tree root nodes for this study node id
					treeRootNodeIdsByStudyNodeId.get(studyNodeId).add(hit.getId());
//					treeRootNodeIdsByStudyRootNodeId.get((String) hit.getProperty(OTVocabularyPredicate.OT_STUDY_ID.propertyName())).add(hit.getId());
				}
        	}
        } finally {
			hits.close();
		}
        
		// for each study containing a matched tree
		List<HashMap<String, Object>> treesFoundByStudy = new LinkedList<HashMap<String, Object>>();
		for (Long studyNodeId : treeRootNodeIdsByStudyNodeId.keySet()) {

			// prepare to collect study metadata
			HashMap<String, Object> studyResult = new HashMap<String, Object>();
			Node studyNode = graphDb.getNodeById(studyNodeId);

			// provide additional study metadata if requested
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
			for (Long nodeId : treeRootNodeIdsByStudyNodeId.get(studyNodeId)) {

				// prepare to collect tree metadata
				HashMap<String, Object> curTreeResult = new HashMap<String, Object>();
				Node treeRootNode = graphDb.getNodeById(nodeId);
				
				// provide additional tree metadata if requested
				if (verbose) {
					
					// TODO: this two-pass approach could probably be made much more efficient--there is overlap between the exact and fulltext properties
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
	 * @param property
	 * 		A SearchableProperty to specify the search domain
	 * @param searchValue
	 * 		The value to be searched for
	 * @return
	 * 		A Map object containing information about hits to the search
	 */
	public Object doBasicSearchForTreeNodes(OTPropertyPredicate property, String searchValue, boolean isExactProperty, boolean isFulltextProperty, boolean verbose) {
		
		// < study node id < tree root node id < matched tip node ids >>>
		Map<Long, HashMap<Long, HashSet<Long>>> studyToTreeToMatchedTipNodeIdMap = new HashMap<Long, HashMap<Long, HashSet<Long>>>();
	
   		// using fuzzy queries ... may want to use different queries for exact vs. fulltext indexes
		
		// TODO: this does not work when doing exact queries. probably need to define a term query for those. going to need work... or not... need to evaluate this

		FuzzyQuery fuzzyQuery = new FuzzyQuery(new Term(property.propertyName(), QueryParser.escape(searchValue.toLowerCase())),
				AbstractBaseQuery.getMinIdentity(searchValue));

		if (isExactProperty) {
			addTreeNodeSearchResultsTo(studyToTreeToMatchedTipNodeIdMap, treeNodesByPropertyExact, fuzzyQuery);
		}
		
		if (isFulltextProperty) {
			addTreeNodeSearchResultsTo(studyToTreeToMatchedTipNodeIdMap, treeNodesByPropertyFulltext, fuzzyQuery);
		}
		
		// prepare to record identifying information about the trees found, organized by study
		List<HashMap<String, Object>> treesFoundByStudy = new LinkedList<HashMap<String, Object>>();
		
		// for each matched study
		for (Long studyNodeId : studyToTreeToMatchedTipNodeIdMap.keySet()) {

			// prepare to collect study metadata
			HashMap<String, Object> studyResult = new HashMap<String, Object>();
			Node studyNode = graphDb.getNodeById(studyNodeId);

			// provide additional study metadata if requested
			if (verbose) {
				
				// TODO: this two-pass approach could probably be made much more efficient--there is overlap between the exact and fulltext properties
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
			
//			studyResult.put(OTVocabularyPredicate.OT_STUDY_ID.propertyName(), studyNode.getProperty(OTVocabularyPredicate.OT_STUDY_ID.propertyName()));
			
			// for each matched tree in the study
			List<HashMap<String, Object>> treeResults = new LinkedList<HashMap<String, Object>>();
			for (Long treeRootNodeId : studyToTreeToMatchedTipNodeIdMap.get(studyNodeId).keySet()) {
				
				// prepare to collect tree metadata
				HashMap<String, Object> curTreeResult = new HashMap<String, Object>();
				Node treeRootNode = graphDb.getNodeById(treeRootNodeId);
				
				// provide additional tree metadata if requested
				if (verbose) {
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
				
//				curTreeResult.put(OTINodeProperty.TREE_ID.propertyName(), (String) treeRootNode.getProperty(OTINodeProperty.TREE_ID.propertyName()));
//				curTreeResult.put(OTINodeProperty.NEXSON_ID.propertyName(), (String) treeRootNode.getProperty(OTINodeProperty.NEXSON_ID.propertyName()));

				// for each matched tree node
				List<HashMap<String, Object>> matchedNodeResults = new LinkedList<HashMap<String, Object>>();
				for (Long matchedTreeNodeId : studyToTreeToMatchedTipNodeIdMap.get(studyNodeId).get(treeRootNodeId)) {
					
					// prepare to collect tree node metadata
					HashMap<String, Object> curNodeResult = new HashMap<String, Object>();
					Node matchedTreeNode = graphDb.getNodeById(matchedTreeNodeId);

					// provide additional tree node metadata if requested
					if (verbose) {
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

					/*
					if (matchedTreeNode.hasProperty(OTVocabularyPredicate.OT_OTT_ID.propertyName())) {
						Long ottId = (Long) matchedTreeNode.getProperty(OTVocabularyPredicate.OT_OTT_ID.propertyName());
						curNodeResult.put(OTVocabularyPredicate.OT_OTT_ID.propertyName(), ottId);
					}
					
					if (matchedTreeNode.hasProperty(OTVocabularyPredicate.OT_OTT_TAXON_NAME.propertyName())) {
						String taxonName = (String) matchedTreeNode.getProperty(OTVocabularyPredicate.OT_OTT_TAXON_NAME.propertyName());
						curNodeResult.put(OTVocabularyPredicate.OT_OTT_TAXON_NAME.propertyName(), taxonName);
					}

					if (matchedTreeNode.hasProperty(OTVocabularyPredicate.OT_ORIGINAL_LABEL.propertyName())) {
						String taxonName = (String) matchedTreeNode.getProperty(OTVocabularyPredicate.OT_ORIGINAL_LABEL.propertyName());
						curNodeResult.put(OTVocabularyPredicate.OT_ORIGINAL_LABEL.propertyName(), taxonName);
					}
					*/

					matchedNodeResults.add(curNodeResult);
				}
				
				curTreeResult.put("matched_nodes", matchedNodeResults);
				treeResults.add(curTreeResult);
			}

			studyResult.put("matched_trees", treeResults);
			treesFoundByStudy.add(studyResult);
		}
		
		return treesFoundByStudy;
	}
	
	private void addTreeNodeSearchResultsTo(Map<Long, HashMap<Long, HashSet<Long>>> results, Index<Node> index, Query query) {
		IndexHits<Node> hits = null;
		try {
    		hits = index.query(query);
			for (Node hit : hits) {

				Node treeRootNode = OTIDatabaseUtils.getRootOfTreeContaining(hit);
				
//				String studyId = (String) treeRootNode.getProperty(OTVocabularyPredicate.OT_STUDY_ID.propertyName());

				Long studyNodeId = getStudyMetaNodeForTreeNode(treeRootNode).getId();
				Long treeRootNodeId = treeRootNode.getId();
				
				// add an entry for the study if this is the first time we've seen it
				if (!results.containsKey(studyNodeId)) {
					results.put(studyNodeId, new HashMap<Long, HashSet<Long>>());
				}

				// add an entry for the tree if this is the first time we've seen it
				if (!results.get(studyNodeId).containsKey(treeRootNodeId)) {
					results.get(studyNodeId).put(treeRootNodeId, new HashSet<Long>());
				}
				
				// record the id of the matched tree node
				results.get(studyNodeId).get(treeRootNodeId).add(hit.getId());
			}
        } finally {
			hits.close();
		}
	}
}
