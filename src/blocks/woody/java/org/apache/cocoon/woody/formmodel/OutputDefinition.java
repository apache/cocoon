package org.apache.cocoon.woody.formmodel;

/**
 * The {@link WidgetDefinition} part of a {@link Output} widget.
 */
public class OutputDefinition extends AbstractDatatypeWidgetDefinition {
    public Widget createInstance() {
        return new Output(this);
    }
}
