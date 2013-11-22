package org.opentree.oti.indexproperties;

import org.opentree.properties.OTPropertyPredicate;
import org.opentree.properties.OTVocabularyPredicate;

/**
 * An enum mapping property arrays that are stored in the graph to the property types that their values represent.
 * Used when indexing multiple values for a single property (e.g. indexing trees by all their descendant taxa).
 * 
 * @author cody
 *
 */
public enum OTPropertyArray {

	OT_ORIGINAL_LABEL (OTVocabularyPredicate.OT_ORIGINAL_LABEL, OTINodeProperty.DESCENDANT_ORIGINAL_TIP_LABELS),
	OT_OTT_ID (OTVocabularyPredicate.OT_OTT_ID, OTINodeProperty.DESCENDANT_MAPPED_TAXON_OTT_IDS),
	OT_OTT_TAXON_NAME (OTVocabularyPredicate.OT_OTT_TAXON_NAME, OTINodeProperty.DESCENDANT_MAPPED_TAXON_NAMES),
	
	;
	
	public final OTPropertyPredicate typeProperty;
	public final OTPropertyPredicate graphProperty;

	OTPropertyArray(OTPropertyPredicate property, OTPropertyPredicate graphProperty) {
        this.typeProperty = property;
        this.graphProperty = graphProperty;
    }
}
