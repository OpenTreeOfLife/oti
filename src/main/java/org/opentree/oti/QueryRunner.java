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
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.opentree.graphdb.GraphDatabaseAgent;
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
			
			for (OTPropertyPredicate p : IndexedPrimitiveProperties.STUDIES_EXACT.properties()) {
				if (studyNode.hasProperty(p.propertyName())) {
					study.put(p.propertyName(), studyNode.getProperty(p.propertyName()));
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
	public Object doBasicSearchForTrees(OTPropertyPredicate property, String searchValue, boolean isExactProperty, boolean isFulltextProperty) {

		// < studyid < tree root node ids >>
		HashMap<String, HashSet<Long>> treeRootNodeIdsByStudyId = new HashMap<String, HashSet<Long>>();
	
   		// using fuzzy queries ... may want to use different queries for exact vs. fulltext indexes
		
		// TODO: this does not work when doing exact queries. probably need to define a term query for those. going to need work
		
		FuzzyQuery fuzzyQuery = new FuzzyQuery(new Term(property.propertyName(), QueryParser.escape(searchValue.toLowerCase())),
    			AbstractBaseQuery.getMinIdentity(searchValue));

		IndexHits<Node> hits = null;
        try {
        	if (isExactProperty) {
				hits = treeRootNodesByPropertyExact.query(fuzzyQuery);
				for (Node hit : hits) {
					
					String studyId = (String) hit.getProperty(OTVocabularyPredicate.OT_STUDY_ID.propertyName());
					if (!treeRootNodeIdsByStudyId.containsKey(studyId)) {
						treeRootNodeIdsByStudyId.put(studyId, new HashSet<Long>());
					}
					
					treeRootNodeIdsByStudyId.get(studyId).add(hit.getId());
				}
        	}
        	if (isFulltextProperty) {
				hits = treeRootNodesByPropertyFulltext.query(fuzzyQuery);
				for (Node hit : hits) {

					String studyId = (String) hit.getProperty(OTVocabularyPredicate.OT_STUDY_ID.propertyName());
					if (!treeRootNodeIdsByStudyId.containsKey(studyId)) {
						treeRootNodeIdsByStudyId.put(studyId, new HashSet<Long>());
					}

					treeRootNodeIdsByStudyId.get((String) hit.getProperty(OTVocabularyPredicate.OT_STUDY_ID.propertyName())).add(hit.getId());
				}
        	}
        } finally {
			hits.close();
		}
        
		// record identifying information about the trees found, organized by study
		List<HashMap<String, Object>> treesFoundByStudy = new LinkedList<HashMap<String, Object>>();
		for (String studyId : treeRootNodeIdsByStudyId.keySet()) {

			HashMap<String, Object> studyResult = new HashMap<String, Object>();
			List<HashMap<String, String>> treeResults = new LinkedList<HashMap<String, String>>();
			
			for (Long nodeId : treeRootNodeIdsByStudyId.get(studyId)) {
				
				Node treeRootNode = graphDb.getNodeById(nodeId);

				HashMap<String, String> curTreeResult = new HashMap<String, String>();
				curTreeResult.put(OTINodeProperty.TREE_ID.propertyName(), (String) treeRootNode.getProperty(OTINodeProperty.TREE_ID.propertyName()));
				curTreeResult.put(OTINodeProperty.NEXSON_ID.propertyName(), (String) treeRootNode.getProperty(OTINodeProperty.NEXSON_ID.propertyName())); 
				treeResults.add(curTreeResult);
			}

			studyResult.put(OTVocabularyPredicate.OT_STUDY_ID.propertyName(), studyId);
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
	public Object doBasicSearchForTreeNodes(OTPropertyPredicate property, String searchValue, boolean isExactProperty, boolean isFulltextProperty) {
		
		// < studyid < tree root node id < matched tip node ids >>>
		Map<String, HashMap<Long, HashSet<Long>>> studyToTreeToMatchedTipMap = new HashMap<String, HashMap<Long, HashSet<Long>>>();
	
   		// using fuzzy queries ... may want to use different queries for exact vs. fulltext indexes
		
		// TODO: this does not work when doing exact queries. probably need to define a term query for those. going to need work

		FuzzyQuery fuzzyQuery = new FuzzyQuery(new Term(property.propertyName(), QueryParser.escape(searchValue.toLowerCase())),
				AbstractBaseQuery.getMinIdentity(searchValue));

		if (isExactProperty) {
			addTreeNodeSearchResultsTo(studyToTreeToMatchedTipMap, treeNodesByPropertyExact, fuzzyQuery);
		}
		
		if (isFulltextProperty) {
			addTreeNodeSearchResultsTo(studyToTreeToMatchedTipMap, treeNodesByPropertyFulltext, fuzzyQuery);
		}
		
		// record identifying information about the trees found, organized by study
		List<HashMap<String, Object>> treesFoundByStudy = new LinkedList<HashMap<String, Object>>();
		for (String studyId : studyToTreeToMatchedTipMap.keySet()) {

			// initialize study results container (for each matched study)
			HashMap<String, Object> studyResult = new HashMap<String, Object>();
			studyResult.put(OTVocabularyPredicate.OT_STUDY_ID.propertyName(), studyId);
			
			List<HashMap<String, Object>> treeResults = new LinkedList<HashMap<String, Object>>();
			for (Long treeRootNodeId : studyToTreeToMatchedTipMap.get(studyId).keySet()) {
				
				Node treeRootNode = graphDb.getNodeById(treeRootNodeId);

				// collect tree results (for each tree in the study)
				HashMap<String, Object> curTreeResult = new HashMap<String, Object>();
				curTreeResult.put(OTINodeProperty.TREE_ID.propertyName(), (String) treeRootNode.getProperty(OTINodeProperty.TREE_ID.propertyName()));
				curTreeResult.put(OTINodeProperty.NEXSON_ID.propertyName(), (String) treeRootNode.getProperty(OTINodeProperty.NEXSON_ID.propertyName()));

				List<HashMap<String, Object>> matchedNodeResults = new LinkedList<HashMap<String, Object>>();
				for (Long matchedTreeNodeId : studyToTreeToMatchedTipMap.get(studyId).get(treeRootNodeId)) {
					
					Node matchedTreeNode = graphDb.getNodeById(matchedTreeNodeId);
					
					// collect node results for each matched node in the tree
					HashMap<String, Object> curNodeResult = new HashMap<String, Object>();
					curNodeResult.put(OTINodeProperty.NEXSON_ID.propertyName(), (String) matchedTreeNode.getProperty(OTINodeProperty.NEXSON_ID.propertyName()));

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
	
	private void addTreeNodeSearchResultsTo(Map<String, HashMap<Long, HashSet<Long>>> results, Index<Node> index, Query query) {
		IndexHits<Node> hits = null;
		try {
    		hits = index.query(query);
			for (Node hit : hits) {

				Node treeRootNode = OTIDatabaseUtils.getRootOfTreeContaining(hit);
				String studyId = (String) treeRootNode.getProperty(OTVocabularyPredicate.OT_STUDY_ID.propertyName());
				Long treeRootNodeId = treeRootNode.getId();
				
				// add an entry for the study if this is the first time we've seen it
				if (!results.containsKey(studyId)) {
					results.put(studyId, new HashMap<Long, HashSet<Long>>());
				}

				// add an entry for the tree if this is the first time we've seen it
				if (!results.get(studyId).containsKey(treeRootNodeId)) {
					results.get(studyId).put(treeRootNodeId, new HashSet<Long>());
				}
				
				// record the node id
				results.get(studyId).get(treeRootNodeId).add(hit.getId());
			}
        } finally {
			hits.close();
		}
	}
}
