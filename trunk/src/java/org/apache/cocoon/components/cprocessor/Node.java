
package org.apache.cocoon.components.cprocessor;

/**
 * Common super interface for sitemap statements.
 * 
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 */
public interface Node {

    /**
     * Get the location of this node.
     *
     * @return  xml locator text.
     */
    String getLocation();

}
