/* 
 * Copyright 2002-2005 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.source;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.impl.ResourceSourceFactory;
import org.apache.excalibur.source.impl.URLSourceFactory;

/**
 * A minimalist <code>SourceResolver</code> that handles a fixed restricted number of protocols. It is
 * used as a bootstrap resolver to load roles and imported files in a service manager.
 * <p>
 * The supported protocols schemes are:
 * <ul>
 * <li><code>resource</code> to load resources in the classpath,</li>
 * <li><code>context</code> to load resources from the context, defined by the <code>context-root</code>
 *     entry in the Avalon {@link Context} (either a {@link File} or an {@link URL}), or if not
 *     present, from the <code>user.dir</code> system property,</li>
 * <li>all standard JDK schemes (http, file, etc).
 * </ul>
 * Relative URIs are resolved relatively to the context root, i.e. similarily to "<code>context:</code>".
 * 
 * @version $Id$
 */
public final class SimpleSourceResolver extends AbstractLogEnabled
    implements ThreadSafe, Contextualizable, SourceResolver {
    
    // The base URI, initialized in contextualize()
    private String contextBase;
    
    // The two factories we use (no need for a selector nor a Map)
    private ResourceSourceFactory resourceFactory = new ResourceSourceFactory();
    private URLSourceFactory urlFactory = new URLSourceFactory();    
    
    public void enableLogging(Logger logger) {
        super.enableLogging(logger);
        resourceFactory.enableLogging(logger);
        urlFactory.enableLogging(logger);
    }

    public void contextualize(Context context) throws ContextException {
        try {
            // Similar to Excalibur's SourceResolverImpl, and consistent with ContextHelper.CONTEXT_ROOT_URL
            if( context.get("context-root") instanceof URL) {
                contextBase = ((URL)context.get("context-root")).toExternalForm();
            } else {
                contextBase = ((File)context.get("context-root")).toURL().toExternalForm();
            }
        } catch(ContextException ce) {
            //FIXME: Cocoon's CONTEXT_ROOT_URL context entry should be made consistent with Excalibur
            try {
                contextBase = new URL((String)context.get("root-url")).toExternalForm();
            } catch (MalformedURLException mue) {
                throw new ContextException("Malformed URL for root-url", mue);
            } catch (ContextException e) {
                // set the base URL to the current directory
                try {
                    contextBase = new File(System.getProperty("user.dir")).toURL().toExternalForm();
                } catch( MalformedURLException mue) {
                    throw new ContextException( "Malformed URL for user.dir, and no context-root exists", mue);
                }
            }
        } catch( MalformedURLException mue) {
            throw new ContextException("Malformed URL for context-root", mue);
        }
        
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Base URL set to " + this.contextBase);
        }
    }

    public Source resolveURI(String uri) throws MalformedURLException, IOException {
        return resolveURI(uri, contextBase, null);
    }

    public Source resolveURI(String uri, String base, Map params) throws MalformedURLException, IOException {
        if (uri.startsWith("resource://")) {
            return resourceFactory.getSource(uri, null);
        } else if (uri.startsWith("context://")) {
            // Strip "context://" and resolve relative to the context base
            return resolveURI(uri.substring("context://".length()), this.contextBase, params);
        } else {
            URL baseURL = new URL(base);
            URL url = new URL(baseURL, uri);
            return this.urlFactory.getSource(url.toExternalForm(), params);
        }
    }

    public void release(Source source) {
        // Don't care. The factories we use here don't need that
    }
}
