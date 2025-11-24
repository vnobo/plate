package com.plate.boot.commons.base;

/**
 * Base marker interface for defining different views/perspectives of data objects.
 * This interface serves as a parent for various view sub-interfaces that can be
 * used to control serialization/deserialization of objects in different contexts.
 */
public interface BaseView {

    /**
     * Marker interface for public view.
     * Objects serialized with this view will include only publicly accessible fields.
     */
    interface Public {
    }

    /**
     * Marker interface for hidden view.
     * Objects serialized with this view will exclude certain sensitive or internal fields.
     */
    interface Hidden {
    }

    /**
     * Marker interface for detailed view.
     * Objects serialized with this view will include all available fields with full details.
     */
    interface Detail extends Public {
    }

    /**
     * Marker interface for admin view.
     * Objects serialized with this view will include sensitive fields like password.
     */
    interface Admin extends Detail {
    }
}