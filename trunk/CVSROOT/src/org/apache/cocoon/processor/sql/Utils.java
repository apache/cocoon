package org.apache.cocoon.processor.sql;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.StringCharacterIterator;
import java.util.Properties;
import org.w3c.dom.*;

/**
 * Utility methods for this processor.
 *
 * @author <a href="mailto:balld@webslingerZ.com">Donald Ball</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:14 $
 */

public class Utils {

    public static final String ERROR_ELEMENT = "error-element";
    public static final String ERROR_MESSAGE_ATTRIBUTE = "error-message-attribute";
    public static final String ERROR_MESSAGE_ELEMENT = "error-message-element";
    public static final String ERROR_STACKTRACE_ATTRIBUTE = "error-stacktrace-attribute";
    public static final String ERROR_STACKTRACE_ELEMENT = "error-stacktrace-element";
    public static final String ERROR_MESSAGE_TEXT = "error-message-text";

    public static Element createErrorElement(Document document, Properties props, Exception e) {
        Element element = document.createElement(props.getProperty(ERROR_ELEMENT));
        String message_attribute = props.getProperty(ERROR_MESSAGE_ATTRIBUTE);
        String message_element = props.getProperty(ERROR_MESSAGE_ELEMENT);
        String message_text = props.getProperty(ERROR_MESSAGE_TEXT);
        if (message_text == null) message_text = e.getMessage();
        if (!message_attribute.equals(""))
            element.setAttribute(message_attribute,e.getMessage());
        if (!message_element.equals("")) {
            Element child = document.createElement(message_element);
            child.appendChild(document.createTextNode(e.getMessage()));
            element.appendChild(child);
        }
        String stacktrace_attribute = props.getProperty(ERROR_STACKTRACE_ATTRIBUTE);
        String stacktrace_element = props.getProperty(ERROR_STACKTRACE_ELEMENT);
        String stacktrace = null;
        if (!stacktrace_attribute.equals("") && !stacktrace_element.equals("")) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            stacktrace = sw.toString();
        }
        if (!stacktrace_attribute.equals(""))
            element.setAttribute(stacktrace_attribute,stacktrace);
        if (!stacktrace_element.equals("")) {
            Element child = document.createElement(stacktrace_element);
            child.appendChild(document.createTextNode(stacktrace));
            element.appendChild(child);
        }
        return element;
    }

    public static String replace(String source, char target, String replacement) {
        StringBuffer sb = new StringBuffer();
        StringCharacterIterator iter = new StringCharacterIterator(source);
        for (char c = iter.first(); c != iter.DONE; c = iter.next())
                if (c == target) sb.append(replacement); else sb.append(c);
        return sb.toString();
    }

}
