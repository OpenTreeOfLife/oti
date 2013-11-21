package org.opentree.oti.constants;

import org.neo4j.graphdb.RelationshipType;

public enum OTIRelType implements RelationshipType {

	/**
	 * Connects tree nodes to their parents.
	 */
	CHILDOF,
	
	/**
	 * Connects study metadata nodes to the trees that are included in the study.
	 */
	METADATAFOR,
	
	/**
	 * Connects taxon nodes (imported from the OTT taxonomy by taxomachine) to tree nodes to which they have been assigned.
	 */
	EXEMPLAROF,
	
	;
}
