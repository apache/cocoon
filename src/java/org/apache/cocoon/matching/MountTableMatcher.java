/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: MountTableMatcher.java,v 1.4 2003/12/22 13:48:46 joerg Exp $
 */
public class MountTableMatcher extends AbstractLogEnabled implements Matcher, ThreadSafe, Serviceable, Parameterizable {

    private ServiceManager manager;
    private SourceResolver resolver;
    private Map mountTables = new HashMap();
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
                mounts.put(children[i].getAttribute("uri-prefix"), children[i].getAttribute("src"));
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
