package org.opentree.oti.indexproperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.opentree.properties.OTPropertyPredicate;

/**
 * Just a container to collect and inclusive return lists of indexed properties. These lists (stored as maps) are
 * generated on construction of this class, which is more efficient than generating them every time we need them,
 * but it could potentially be made even more efficient (e.g. just once for every compilation of the software) by
 * switching to an enum.
 * 
 * @author cody
 *
 */
public class OTIProperties {

	private final static Map <String, HashSet<OTPropertyPredicate>> indexedStudyProperties = new HashMap<String, HashSet<OTPropertyPredicate>>();
	private final static Map <String, HashSet<OTPropertyPredicate>> indexedTreeProperties = new HashMap<String, HashSet<OTPropertyPredicate>>();
	private final static Map <String, HashSet<OTPropertyPredicate>> indexedTreeNodeProperties = new HashMap<String, HashSet<OTPropertyPredicate>>();
	
	public OTIProperties() {
		collectIndexedStudyProperties();
		collectIndexedTreeProperties();
		collectIndexedTreeNodeProperties();
	}

	/**
	 * Return a map view containing known study properties.
	 * @return
	 */
	public Map<String, HashSet<OTPropertyPredicate>> getIndexedStudyProperties() {
		return Collections.unmodifiableMap(indexedStudyProperties);
	}

	/**
	 * Return a map view containing known study properties.
	 * @return
	 */
	public Map<String, HashSet<OTPropertyPredicate>> getIndexedTreeProperties() {
		return Collections.unmodifiableMap(indexedTreeProperties);
	}

	/**
	 * Return a map view containing known study properties.
	 * @return
	 */
	public Map<String, HashSet<OTPropertyPredicate>> getIndexedTreeNodeProperties() {
		return Collections.unmodifiableMap(indexedTreeNodeProperties);
	}
	
	/**
	 * Collect known properties for studies.
	 */
	private void collectIndexedStudyProperties() {
				
		for (OTPropertyPredicate p : IndexedPrimitiveProperties.STUDIES_EXACT.properties()) {
			addProperty(indexedStudyProperties, p.propertyName(), p);
		}
		for (OTPropertyPredicate p : IndexedPrimitiveProperties.STUDIES_FULLTEXT.properties()) {
			addProperty(indexedStudyProperties, p.propertyName(), p);
		}
		for (OTPropertyArray p : IndexedArrayProperties.STUDIES_EXACT.properties()) {
			addProperty(indexedStudyProperties, p.typeProperty.propertyName(), p.typeProperty);
		}
		for (OTPropertyArray p : IndexedArrayProperties.STUDIES_FULLTEXT.properties()) {
			addProperty(indexedStudyProperties, p.typeProperty.propertyName(), p.typeProperty);
		}
	}
	
	/**
	 * Collect known properties for trees.
	 */
	private void collectIndexedTreeProperties() {
		
		for (OTPropertyPredicate p : IndexedPrimitiveProperties.TREES_EXACT.properties()) {
			addProperty(indexedTreeProperties, p.propertyName(), p);
		}
		for (OTPropertyPredicate p : IndexedPrimitiveProperties.TREES_FULLTEXT.properties()) {
			addProperty(indexedTreeProperties, p.propertyName(), p);
		}
		for (OTPropertyArray p : IndexedArrayProperties.TREES_EXACT.properties()) {
			addProperty(indexedTreeProperties, p.typeProperty.propertyName(), p.typeProperty);
		}
		for (OTPropertyArray p : IndexedArrayProperties.TREES_FULLTEXT.properties()) {
			addProperty(indexedTreeProperties, p.typeProperty.propertyName(), p.typeProperty);
		}
	}
	
	/**
	 * Collect known properties for tree nodes.
	 */
	private void collectIndexedTreeNodeProperties() {
		
		for (OTPropertyPredicate p : IndexedPrimitiveProperties.TREE_NODES_EXACT.properties()) {
			addProperty(indexedTreeNodeProperties, p.propertyName(), p);
		}
		for (OTPropertyPredicate p : IndexedPrimitiveProperties.TREE_NODES_FULLTEXT.properties()) {
			addProperty(indexedTreeNodeProperties, p.propertyName(), p);
		}
		for (OTPropertyArray p : IndexedArrayProperties.TREE_NODES_EXACT.properties()) {
			addProperty(indexedTreeNodeProperties, p.typeProperty.propertyName(), p.typeProperty);
		}
		for (OTPropertyArray p : IndexedArrayProperties.TREE_NODES_FULLTEXT.properties()) {
			addProperty(indexedTreeNodeProperties, p.typeProperty.propertyName(), p.typeProperty);
		}
	}
	
	/**
	 * Helper method, reduces code repetition.
	 */
	private void addProperty(Map<String, HashSet<OTPropertyPredicate>> propertySet, String propertyName, OTPropertyPredicate property) {
		if (! propertySet.containsKey(propertyName)) {
			propertySet.put(propertyName, new HashSet<OTPropertyPredicate>());
		}
		propertySet.get(propertyName).add(property);
	}
}
