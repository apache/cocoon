/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.SourceResolver;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * ContextPathModule provides a real filesystem path for a virtual
 * context-relative path.  If this mapping cannot be performed (e.g. Cocoon is
 * running in a .war file), <code>null</code> will be returned. Compared to
 * the {@link RealPathModule} this module is able to provide the "real" absolute 
 * path even if the application is mounted outside the webapp tree of Cocoon.
 * <p>
 * Note: the primary use for this is to support external code that wants a
 * filesystem path.  For example, The FOP 0.20.x serializer doesn't like
 * relative image paths, and doesn't understand Cocoon URLs (context:, cocoon:
 * etc).  So we pass the *2fo.xsl stylesheet a real filesystem path to where we
 * keep our images:
 * </p>
 * <p>
 * A absolute path argument like {contextpath:/resources} will be resolved  
 * from the root context path (ie. COCOON_HOME/build/webapp) whereas a relative
 * path attribute like {contextpath:./resources} will be resolved from the
 * location of the sitemap that uses it. If that sitemap is mounted outside the
 * usual COCOON_HOME/build/webapp the path resolved with this modules points to
 * the correct location.
 * </p>
 * <p>
 * <pre>
 * &lt;map:transform src=&quot;skins/{forrest:skin}/xslt/fo/document2fo.xsl&quot;&gt;
 *    &lt;map:parameter name=&quot;basedir&quot; value=&quot;{contextpath:resources}/&quot;/&gt;
 * &lt;/map:transform&gt;
 * </pre>
 *
 * And then prepend this to all image paths:
 * <pre>
 *  ...
 *  &lt;xsl:param name=&quot;basedir&quot; select=&quot;&apos;&apos;&quot;/&gt;
 *  ...
 *  &lt;xsl:template match=&quot;img&quot;&gt;
 *      &lt;xsl:variable name=&quot;imgpath&quot; select=&quot;concat($basedir, @src)&quot;/&gt;
 *      &lt;fo:external-graphic src=&quot;{$imgpath}&quot; ...
 *      ...
 *  &lt;/xsl:template&gt;
 *  </pre>
 *  </p>
 *
 * @version $Id$
 */
public class ContextPathModule extends AbstractInputModule implements Serviceable, ThreadSafe {

    private ServiceManager m_manager;
    private SourceResolver m_resolver;

    final  static Vector returnNames;
    static {
        Vector tmp = new Vector();
        tmp.add("contextPath");
        returnNames = tmp;
    }

        /** (non-Javadoc)
	 *      * @see Serviceable#service(ServiceManager)
	 *           */
    public void service(ServiceManager manager) throws ServiceException {
        m_manager = manager;
        m_resolver = (SourceResolver) m_manager.lookup(SourceResolver.ROLE);
    }

    /** (non-Javadoc)
     *
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     *
     */
    public void dispose() {
        super.dispose();
        if ( this.m_manager != null ) {
            this.m_manager.release( this.m_resolver );
            this.m_manager = null;
            this.m_resolver = null;
        }
    }

    public Object getAttribute( String name, Configuration modeConf, Map objectModel ) throws ConfigurationException {
        try {
            if(name.startsWith("/")) {
                return m_resolver.resolveURI("context:/"+name).getURI().substring("file:".length());
            }
            return m_resolver.resolveURI(name).getURI().substring("file:".length());
        } catch( final IOException mue ) {
            throw new ConfigurationException( "Cannot resolve realpath", mue);
        }
    }

    public Iterator getAttributeNames( Configuration modeConf, Map objectModel ) throws ConfigurationException {

        return ContextPathModule.returnNames.iterator();
    }


    public Object[] getAttributeValues( String name, Configuration modeConf, Map objectModel )
        throws ConfigurationException {

            List values = new LinkedList();
            values.add( this.getAttribute(name, modeConf, objectModel) );

            return values.toArray();
    }
}
