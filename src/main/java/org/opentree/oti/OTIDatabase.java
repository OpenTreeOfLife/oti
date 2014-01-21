package org.opentree.oti;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.opentree.graphdb.DatabaseAbstractBase;
import org.opentree.graphdb.GraphDatabaseAgent;
import org.opentree.graphdb.NodeIndexDescription;
import org.opentree.oti.constants.OTIRelType;

public class OTIDatabase extends DatabaseAbstractBase {

//	public static final String LOCAL_LOCATION = "local";
	
	public OTIDatabase(GraphDatabaseService graphService) {
		super(graphService);
	}

	public OTIDatabase(EmbeddedGraphDatabase embeddedGraph) {
		super(embeddedGraph);
	}

	public OTIDatabase(GraphDatabaseAgent gdb) {
		super(gdb);
	}
	
	// Currently OTI uses different kinds of indexes, which are described by the `parameters` argument
	// To specify other types of indexes we can just pass the relevant parameters as String... arguments,
	// which will invoke the analagous underlying method in DatabaseAbstractBase. See DatabaseManager for an example.
	public Index<Node> getNodeIndex(NodeIndexDescription index) {
		return graphDb.getNodeIndex(index.indexName(), index.parameters());
	}
	
	/**
	 * Returns the study metadata node for the study containing the tree containing the specified node. Throws
	 * various exceptions if fail conditions are met (e.g. more than one study metadata node, which would indicate
	 * a corrupt db).
	 * 
	 * @param treeNode
	 * @return
	 */
	public Node getStudyMetaNodeForTreeNode(Node treeNode) {
		Node rootNode = OTIDatabaseUtils.getRootOfTreeContaining(treeNode);
		return rootNode.getSingleRelationship(OTIRelType.METADATAFOR, Direction.BOTH).getOtherNode(treeNode);
	}
}
