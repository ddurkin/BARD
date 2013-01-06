package bard.dm.minimumassayannotation

import bard.db.registration.AttributeType
import bard.db.dictionary.Element

/**
 * Holds a single attribute (a key/value pair) value.
 * This should correspond to an Element in the data model
 */
class Attribute {
    String key;
    def value;
    AttributeType attributeType
    Boolean typeIn //defines whether or not the user can type-in a value for the Value field or does it have to come from the dictionary
    String qualifier //Used to describe the qualifier in result-type (e.g., '<')
    String concentrationUnits //Used to describe the concentration units when the attribute is a numeric concentration value.

    public Attribute(String key, def value, AttributeType attributeType, Boolean typeIn = false, String qualifier = null, String concentrationUnits = null) {
        this.key = key
        this.value = value
        this.attributeType = attributeType
        this.typeIn = typeIn
        this.qualifier = qualifier
        this.concentrationUnits = concentrationUnits
    }

    public Attribute(Attribute attribute) {
        this.key = attribute.key
        this.value = attribute.value
        this.attributeType = attribute.attributeType
        this.typeIn = attribute.typeIn
        this.qualifier = attribute.qualifier
        this.concentrationUnits = attribute.concentrationUnits
    }
}