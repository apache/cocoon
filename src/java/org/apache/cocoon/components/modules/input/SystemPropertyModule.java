package org.apache.cocoon.components.modules.input;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.thread.ThreadSafe;

import java.util.Map;

/**
 * SystemPropertyModule is an JXPath based InputModule implementation that
 * provides access to system properties.
 * Available system properties are defined by Java's <a
 * href="http://java.sun.com/j2se/1.4.1/docs/api/java/lang/System.html#getProperties()">System.getProperties()</a>.
 *
 * JXPath allows to apply XPath functions to system properties.
 *
 * If there is a security manager, its <code>checkPropertiesAccess</code>
 * method is called with no arguments. This may result in a security exception
 * which is wrapped into a configuration exception and re-thrown.
 *
 * @author Konstantin Piroumian
 * @version CVS $Id: SystemPropertyModule.java,v 1.3 2003/05/03 16:02:42 jefft Exp $
 */
public class SystemPropertyModule extends AbstractJXPathModule
    implements ThreadSafe {

    protected Object getContextObject(Configuration modeConf,
                                      Map objectModel) {

        return System.getProperties();
    }
}
