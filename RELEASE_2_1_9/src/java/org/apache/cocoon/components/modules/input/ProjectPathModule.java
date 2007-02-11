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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.environment.ObjectModelHelper;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * ProjectPathModule provides relative and absolute paths with regards to the root of a project.
 * <p>Config:
 * <pre>
 *    &lt;component-instance logger=&quot;core.modules.input&quot;
 *           name=&quot;myproject&quot;
 *           class=&quot;org.apache.cocoon.components.modules.input.ProjectPathModule&quot;&gt;
 *          &lt;uri-prefix&gt;my/project/&lt;/uri-prefix&gt;
 *    &lt;/component-instance&gt;
 * </pre>
 * </p>
 * <p>Usage:
 * <pre>
 *    &lt;map:transform src=&quot;skins/{forrest:skin}/xslt/fo/document2html.xsl&quot;&gt;
 *       &lt;map:parameter name=&quot;base&quot; value=&quot;{myproject:relative}&quot;/&gt;
 *    &lt;/map:transform&gt;
 *</pre>
 * And then prepend this to all image paths:
 * <pre>
 *  ...
 *  &lt;xsl:param name=&quot;base&quot;/&gt;
 *  ...
 *  &lt;xsl:template match=&quot;img&quot;&gt;
 *      &lt;img src=&quot;{concat($base, @src)}&quot; ...
 *      ...
 *  &lt;/xsl:template&gt;
 *  </pre>
 * Then if you are in my/project/some/folder/page.html, the image will have a relative path bact to the root of the project.
 * <pre>
 *   &lt;img src=&quot;../../imagename.png&quot;/&gt;
 * </pre>
 * Using 'myproject:path' would have given you: /some/folder/page.html<br/>
 * Using 'myproject:folder' would have given you: /some/folder/
 * </p>
 * 
 * @version $Id$
 *
 */
public class ProjectPathModule extends AbstractInputModule implements ThreadSafe {

    protected static final String PROJECT_PARAM_NAME = "uri-prefix";
    protected static final String PROJECT_PARAM_DEFAULT = "/";

    protected String projectBase;

    final  static Vector returnNames;
    static {
        Vector tmp = new Vector();
        tmp.add("relative");
        tmp.add("path");
        tmp.add("folder");
        returnNames = tmp;
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration conf) throws ConfigurationException {
        this.projectBase = conf.getChild(PROJECT_PARAM_NAME).getValue();
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Configuration supplied: " + this.projectBase);
        }
        if (this.projectBase == null) {
            this.projectBase = PROJECT_PARAM_DEFAULT;
            if (getLogger().isWarnEnabled()) {
                getLogger().warn("No configuration supplied, using default: " + PROJECT_PARAM_DEFAULT);
            }
        }
        if (this.projectBase.length() == 0) {
            this.projectBase = PROJECT_PARAM_DEFAULT;
            if (getLogger().isWarnEnabled()) {
                getLogger().warn("Empty configuration supplied, using default: " + PROJECT_PARAM_DEFAULT);
            }
        }
    }

    /**
     * @see org.apache.cocoon.components.modules.input.InputModule#getAttribute(java.lang.String, org.apache.avalon.framework.configuration.Configuration, java.util.Map)
     */
    public Object getAttribute(String name, Configuration modeConf, Map objectModel)
    throws ConfigurationException {
        String uri = ObjectModelHelper.getRequest(objectModel).getServletPath();
        StringBuffer result = new StringBuffer(uri.length());
        int baseIndex = uri.indexOf(this.projectBase);
        if (baseIndex != -1) {
            uri = uri.substring(baseIndex + this.projectBase.length());
        } else {
            throw new ConfigurationException( "No project-base path found in URI");
        }
        try {
            // provide a relative path back to the project
            if (name.startsWith("relative")) {
                int nextIndex = 0;
                while ((nextIndex = uri.indexOf('/', nextIndex) + 1) > 0) {
                    result.append("../");
                }
            } else if (name.startsWith("path")) {
                // provide the full path from the project
                result.append("/");
                result.append(uri);
            } else if (name.startsWith("folder")) {
                // provide the folder path from the project
                result.append("/");
                result.append(uri.substring(0,uri.lastIndexOf("/") + 1));
            } else {
                 if (getLogger().isWarnEnabled()) {
                     getLogger().warn("Invalid verb: " + name);
                 }
            }
            return result;
        } catch( final Exception mue ) {
            throw new ConfigurationException( "Problems resolving project path.", mue);
        }
    }

    /**
     * @see org.apache.cocoon.components.modules.input.InputModule#getAttributeNames(org.apache.avalon.framework.configuration.Configuration, java.util.Map)
     */
    public Iterator getAttributeNames( Configuration modeConf, Map objectModel )
    throws ConfigurationException {
        return ProjectPathModule.returnNames.iterator();
    }

    /**
     * @see org.apache.cocoon.components.modules.input.InputModule#getAttributeValues(java.lang.String, org.apache.avalon.framework.configuration.Configuration, java.util.Map)
     */
    public Object[] getAttributeValues( String name, Configuration modeConf, Map objectModel )
    throws ConfigurationException {
        List values = new LinkedList();
        values.add( this.getAttribute(name, modeConf, objectModel) );
        
        return values.toArray();
    }
}
