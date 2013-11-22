package org.opentree.oti;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.opentree.graphdb.DatabaseAbstractBase;
import org.opentree.graphdb.GraphDatabaseAgent;
import org.opentree.graphdb.NodeIndexDescription;

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
}
