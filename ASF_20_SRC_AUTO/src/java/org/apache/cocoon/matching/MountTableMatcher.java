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
package org.apache.cocoon.matching;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.sitemap.PatternException;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;

/**
 * A matcher that manages a "mount table", allowing to add subsitemaps to a Cocoon application without
 * modifying the main sitemap. This is especially useful for prototypes and demos where installing
 * a separate instance of Cocoon is overkill.
 * <p>
 * The mount table is an xml file which has a format similar to the <code>map:mount</code> syntax:
 * <pre>
 *   &lt;mount-table&gt;
 *     &lt;mount uri-prefix="foo" src="file://path/to/foo/directory/"/&gt;
 *     &lt;mount uri-prefix="bar/baz" src="file://path/to/bar-baz/directory/"/&gt;
 *   &lt;/mount-table&gt;
 * </pre>
 * The matcher will scan the mount table for an "uri-prefix" value matching the beginning of the current
 * request URI, and if found, succeed and populate the "src" and "uri-prefix" sitemap variables.
 * <p>
 * Usage in the sitemap is therefore as follows:
 * <pre>
 *   &lt;map:match type="mount-table" pattern="path/to/mount-table.xml"&gt;
 *     &lt;map:mount uri-prefix="{uri-prefix}" src="{src}"/&gt;
 *   &lt;/map:match&gt;
 * </pre>
 * <p>
 * This matcher accepts a single configuration parameter, indicating if missing mount tables should be
 * silently ignored (defaults is <code>false</code>, meaning "don't ignore"):
 * <pre>
 *   &lt;map:matcher type="mount-table" src="org.apache.cocoon.matching.MountTableMatcher"&gt;
 *     &lt;map:parameter name="ignore-missing-tables" value="true"/&gt;
 *   &lt;/map:matcher&gt;
 * </pre>
 * <p>
 * This configuration is used in the main sitemap of Cocoon samples, to allow users to define their own mount
 * table, but not fail if it does not exist.
 * 
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: MountTableMatcher.java,v 1.7 2004/03/05 13:02:56 bdelacretaz Exp $
 */
public class MountTableMatcher extends AbstractLogEnabled implements Matcher, ThreadSafe, Serviceable, Parameterizable {

    private ServiceManager manager;
    private SourceResolver resolver;
    private Map mountTables = Collections.synchronizedMap(new HashMap());
    private boolean ignoreMissingTables = false;

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
    }

    public void parameterize(Parameters params) throws ParameterException {
        this.ignoreMissingTables = params.getParameterAsBoolean("ignore-missing-tables", false);
    }

    private Map getMountTable(String src) throws Exception {
        Source source = resolver.resolveURI(src);
        String uri = source.getURI();
        
        // Check if source exists
        if (!source.exists()) {
            resolver.release(source);
            if (this.ignoreMissingTables) {
                return Collections.EMPTY_MAP;
            } else {
                throw new PatternException("Mount table does not exist: '" + uri + "'");
            }
        }
        
        // Source exists
        Object[] values = (Object[])this.mountTables.get(uri);
        
        if (values != null) {
            // Check validity
            SourceValidity oldValidity = (SourceValidity)values[1];
            int valid = oldValidity.isValid();
            if (valid == 1) {
                // Valid without needing the new validity
                return (Map)values[0];
            }
            
            if (valid == 0 && oldValidity.isValid(source.getValidity()) == 1) {
                // Valid after comparing with the new validity
                return (Map)values[0];
            }
            
            // Invalid: fallback below to read the mount table
        } else {
            values = new Object[2];
        }
        
        // Read the mount table
        Map mounts = new HashMap();
        DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
        Configuration config = builder.build(SourceUtil.getInputSource(source));
        this.resolver.release(source);
        
        Configuration[] children = config.getChildren();
        for (int i = 0; i < children.length; i++) {
            Configuration child = children[i];
            if ("mount".equals(child.getName())) {
                String prefix = children[i].getAttribute("uri-prefix");
                // Append a '/' at the end of the prefix
                // this avoids flat uri matching which would cause
                // exceptions in the sub sitemap!
                if (!prefix.endsWith("/")) {
                    prefix = prefix + '/';
                }
                mounts.put(prefix, children[i].getAttribute("src"));
            } else {
                throw new PatternException(
                    "Unexpected element '" + child.getName() + "' (awaiting 'mount'), at " + child.getLocation());
            }
        }
        values[0] = mounts;
        values[1] = source.getValidity();
        
        // Cache it with the source validity
        this.mountTables.put(uri, values);
        
        return mounts;
        
    }

    public Map match(String pattern, Map objectModel, Parameters parameters) throws PatternException {
        Map mounts;
        
        try {
            mounts = getMountTable(pattern);
        } catch(PatternException pe) {
            throw pe;
        } catch(Exception e) {
            throw new PatternException(e);
        }
        
        // Get the request URI
        Request request = ObjectModelHelper.getRequest(objectModel);
        String uri = request.getSitemapURI();
        
        // and search for a matching prefix
        Iterator iter = mounts.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            String prefix = (String)entry.getKey();
            if (uri.startsWith(prefix)) {
                // Found it
                Map result = new HashMap(2);
                result.put("uri-prefix", prefix);
                result.put("src", entry.getValue());
                
                // Return immediately
                return result;
            }
        }
        
        // Not found
        return null;
    }
}
