/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.modules.input;


import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * Old name for {@link DefaultsModule}.
 * @deprecated Use DefaultsModule instead; this is not a 'meta' module and is
 * thus misnamed.
 * @version CVS $Id: DefaultsMetaModule.java,v 1.4 2004/03/05 13:02:48 bdelacretaz Exp $
 */

/* Deprecated 2003-03-19. Suggest we keep this class for compat with 2.0.x
 * until at least v2.2 (JT) */

public class DefaultsMetaModule extends DefaultsModule
    implements InputModule, Configurable, ThreadSafe {

    public void configure(Configuration config) throws ConfigurationException {
        super.configure( config );
    }

    public Object[] getAttributeValues( String name, Configuration modeConf, Map objectModel ) 
        throws ConfigurationException {
        return super.getAttributeValues( name, modeConf, objectModel );
    }

    public Iterator getAttributeNames( Configuration modeConf, Map objectModel ) 
        throws ConfigurationException {
        return super.getAttributeNames( modeConf, objectModel );
     }

    public Object getAttribute( String name, Configuration modeConf, Map objectModel ) 
        throws ConfigurationException {
        return super.getAttribute( name, modeConf, objectModel );
    }
}
