package org.apache.cocoon.woody.event;

/**
 * Abstract implementation of {@link FormHandler}, which checks the type
 * of the WidgetEvent and calls the more specific {@link #handleActionEvent}
 * or {@link #handleValueChangedEvent} methods.
 */
public abstract class AbstractFormHandler implements FormHandler {

    public void handleEvent(WidgetEvent widgetEvent) {
        if (widgetEvent instanceof ActionEvent)
            handleActionEvent((ActionEvent)widgetEvent);
        else if (widgetEvent instanceof ValueChangedEvent)
            handleValueChangedEvent((ValueChangedEvent)widgetEvent);
    }

    /**
     * Called when an ActionEvent occured.
     */
    public abstract void handleActionEvent(ActionEvent actionEvent);

    /**
     * Called when an ValueChangedEvent occured.
     */
    public abstract void handleValueChangedEvent(ValueChangedEvent valueChangedEvent);

}
