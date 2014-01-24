package org.opentree.oti.indexproperties;

import java.util.Collections;
import java.util.HashMap;
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

	private final static HashMap <String, OTPropertyPredicate> indexedStudyProperties = new HashMap<String, OTPropertyPredicate>();
	private final static HashMap <String, OTPropertyPredicate> indexedTreeProperties = new HashMap<String, OTPropertyPredicate>();
	private final static HashMap <String, OTPropertyPredicate> indexedTreeNodeProperties = new HashMap<String, OTPropertyPredicate>();
	
	public OTIProperties() {
		collectIndexedStudyProperties();
		collectIndexedTreeProperties();
		collectIndexedTreeNodeProperties();
	}

	/**
	 * Return a map view containing known study properties.
	 * @return
	 */
	public Map<String, OTPropertyPredicate> getIndexedStudyProperties() {
		return Collections.unmodifiableMap(indexedStudyProperties);
	}

	/**
	 * Return a map view containing known study properties.
	 * @return
	 */
	public Map<String, OTPropertyPredicate> getIndexedTreeProperties() {
		return Collections.unmodifiableMap(indexedTreeProperties);
	}

	/**
	 * Return a map view containing known study properties.
	 * @return
	 */
	public Map<String, OTPropertyPredicate> getIndexedTreeNodeProperties() {
		return Collections.unmodifiableMap(indexedTreeNodeProperties);
	}

	/**
	 * Collect known properties for studies.
	 */
	private void collectIndexedStudyProperties() {
		
		for (OTPropertyPredicate p : IndexedPrimitiveProperties.STUDIES_EXACT.properties()) {
			indexedStudyProperties.put(p.propertyName(), p);
		}
		for (OTPropertyPredicate p : IndexedPrimitiveProperties.STUDIES_FULLTEXT.properties()) {
			indexedStudyProperties.put(p.propertyName(), p);
		}
		for (OTPropertyArray p : IndexedArrayProperties.STUDIES_EXACT.properties()) {
			indexedStudyProperties.put(p.typeProperty.propertyName(), p.typeProperty);
		}
		for (OTPropertyArray p : IndexedArrayProperties.STUDIES_FULLTEXT.properties()) {
			indexedStudyProperties.put(p.typeProperty.propertyName(), p.typeProperty);
		}
	}
	
	/**
	 * Collect known properties for trees.
	 */
	private void collectIndexedTreeProperties() {
		
		for (OTPropertyPredicate p : IndexedPrimitiveProperties.TREES_EXACT.properties()) {
			indexedTreeProperties.put(p.propertyName(), p);
		}
		for (OTPropertyPredicate p : IndexedPrimitiveProperties.TREES_FULLTEXT.properties()) {
			indexedTreeProperties.put(p.propertyName(), p);
		}
		for (OTPropertyArray p : IndexedArrayProperties.TREES_EXACT.properties()) {
			indexedTreeProperties.put(p.typeProperty.propertyName(), p.typeProperty);
		}
		for (OTPropertyArray p : IndexedArrayProperties.TREES_FULLTEXT.properties()) {
			indexedTreeProperties.put(p.typeProperty.propertyName(), p.typeProperty);
		}
	}
	
	/**
	 * Collect known properties for tree nodes.
	 */
	private void collectIndexedTreeNodeProperties() {
		
		for (OTPropertyPredicate p : IndexedPrimitiveProperties.TREE_NODES_EXACT.properties()) {
			indexedTreeNodeProperties.put(p.propertyName(), p);
		}
		for (OTPropertyPredicate p : IndexedPrimitiveProperties.TREE_NODES_FULLTEXT.properties()) {
			indexedTreeNodeProperties.put(p.propertyName(), p);
		}
		for (OTPropertyArray p : IndexedArrayProperties.TREE_NODES_EXACT.properties()) {
			indexedTreeNodeProperties.put(p.typeProperty.propertyName(), p.typeProperty);
		}
		for (OTPropertyArray p : IndexedArrayProperties.TREE_NODES_FULLTEXT.properties()) {
			indexedTreeNodeProperties.put(p.typeProperty.propertyName(), p.typeProperty);
		}
	}
	
}
