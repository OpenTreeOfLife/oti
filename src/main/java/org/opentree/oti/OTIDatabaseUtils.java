package org.opentree.oti;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.opentree.graphdb.DatabaseUtils;
import org.opentree.oti.constants.OTIConstants;
import org.opentree.oti.constants.OTIRelType;
import org.opentree.oti.indexproperties.OTINodeProperty;

/**
 * Utility class containing static methods, many of these may overlap with OTU methods.
 * 
 * ...OTU should probably be modified to import OTI and use its classes at some point.
 * 
 * @author cody
 *
 */
public class OTIDatabaseUtils {
	
	/*
	 * Just a convenience function for replacing whitespace in strings to facilitate exact searching
	 * @param str
	 * @return
	 *
	public static String substituteWhitespace(String str) {
		return str.replaceAll("\\s+", OTIConstants.WHITESPACE_SUBSTITUTE_FOR_SEARCH);
	} */
	
	/**
	 * Return the root node from the graph for the tree containing the specified node.
	 * 
	 * TODO: this should probably be in a utils class somewhere
	 * 
	 * @param node
	 * 		The node to start traversing from
	 * @return
	 */
	public static Node getRootOfTreeContaining(Node node) {

		Node root = node;
		boolean going = true;
		while (going) {
			if (root.hasRelationship(OTIRelType.CHILDOF, Direction.OUTGOING)) {
				root = root.getSingleRelationship(OTIRelType.CHILDOF, Direction.OUTGOING).getEndNode();
			} else {
				break;
			}
		}
		
		// only return the node if it is actually the root of a tree in the graph
		if (root.hasProperty(OTINodeProperty.IS_ROOT.propertyName())) {
			return root;
		} else {
			return null;
		}
	}
	
	/**
	 * Get the set of tip nodes descended from a tree node.
	 * 
	 * @param ancestor
	 * 		The start node for the traversal. All tip nodes descended from this node will be included in the result.
	 * @return
	 * 		A set containing the nodes found by the tree traversal. Returns an empty set if no nodes are found.
	 */
	public static Set<Node> getDescendantTips(Node ancestor) {
		HashSet<Node> descendantTips = new HashSet<Node>();
		
		for (Node curGraphNode : DatabaseUtils.descendantTipTraversal(OTIRelType.CHILDOF, Direction.INCOMING).traverse(ancestor).nodes()) {
			descendantTips.add(curGraphNode);
		}
		return descendantTips;
	}
	
}
