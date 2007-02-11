package org.apache.cocoon.components.modules.input;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.thread.ThreadSafe;

import java.util.Map;

/**
 * SystemPropertyModule is an JXPath based InputModule implementation that
 * provides access to system properties.
 *
 * JXPath allows to apply XPath functions to system properties.
 *
 * If there is a security manager, its <code>checkPropertiesAccess</code>
 * method is called with no arguments. This may result in a security exception
 * which is wrapped into a configuration exception and re-thrown.
 *
 * @author Konstantin Piroumian
 * @version CVS $Id: SystemPropertyModule.java,v 1.2 2003/03/16 17:49:12 vgritsenko Exp $
 */
public class SystemPropertyModule extends AbstractJXPathModule
    implements ThreadSafe {

    protected Object getContextObject(Configuration modeConf,
                                      Map objectModel) {

        return System.getProperties();
    }
}
