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
    include  the following  acknowledgment:   "This product includes software
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

package org.apache.cocoon.components.source.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.slide.SlideRepository;
import org.apache.cocoon.components.source.helpers.SourceCredential;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceParameters;
import org.apache.excalibur.source.SourceUtil;
import org.apache.slide.common.NamespaceAccessToken;

/**
 * A factory for sources from a Jakarta Slide repository.
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * @version CVS $Id: SlideSourceFactory.java,v 1.10 2003/12/14 15:25:02 unico Exp $
 * 
 * @avalon.component
 * @avalon.service type="SourceFactory"
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=slide
 */
public class SlideSourceFactory extends AbstractLogEnabled 
implements SourceFactory, ThreadSafe, Serviceable, Contextualizable {

    private ServiceManager m_manager;
    private SlideRepository m_repository;
    private Context m_context;


    public SlideSourceFactory() {
    }
    
    /**
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     *
     * @param context The context.
     */
    public void contextualize(Context context) throws ContextException {
        m_context = context;
    }
    
    /**
     * Lookup the SlideRepository.
     * 
     * @param manager ServiceManager.
     * 
     * @avalon.dependency type=SlideRepository optional=false
     */
    public void service(ServiceManager manager) throws ServiceException {
        m_repository = (SlideRepository) manager.lookup(SlideRepository.ROLE);
        m_manager = manager;
    }

    /**
     * Get a <code>Source</code> object.
     *
     * @param uri URI of the source.
     * @param parameters This is optional.
     *
     * @return A new source object.
     */
    public Source getSource(String location, Map parameters)
    throws MalformedURLException, IOException, SourceException {

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("resolve uri: " + location);
        }
        
        final String[] parts = SourceUtil.parseUrl(location);
        final String scheme = parts[SourceUtil.SCHEME];
        final String authority = parts[SourceUtil.AUTHORITY];
        final String query = parts[SourceUtil.QUERY];
        String path = parts[SourceUtil.PATH];
        
        String principal;
        String namespace;
        
        // parse the authority string for [usr][:pwd]@ns
        int index = authority.indexOf('@');
        if (index == -1) {
            principal = "guest";
            namespace = authority;
        }
        else {
            principal = authority.substring(0,index);
            namespace = authority.substring(index+1);
        }
        
        if (path == null || path.length() == 0) {
            path = "/";
        }
        
        SourceCredential credential;
        
        NamespaceAccessToken nat = m_repository.getNamespaceToken(namespace);
        if (nat == null) {
            throw new SourceException("No such namespace: " + namespace);
        }

        SourceParameters queryParameters = null;

        if (query == null || query.length() == 0) {
            queryParameters = new SourceParameters();
        } else {
            queryParameters = new SourceParameters(query);
        }

        String version = queryParameters.getParameter("version",null);
        String scope   = queryParameters.getParameter("scope",
            nat.getNamespaceConfig().getFilesPath());
        
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("scheme: " + scheme);
            getLogger().debug("principal: " + principal);
            getLogger().debug("namespace: " + namespace);
            getLogger().debug("path: " + path);
            getLogger().debug("version: " + version);
            getLogger().debug("scope: " + scope);
        }

        SlideSource source = new SlideSource(nat,scheme,scope,path,principal,version);

        source.enableLogging(getLogger());
        source.contextualize(m_context);
        source.service(m_manager);
        source.initialize();

        return source;
    }

    /**
     * Release a {@link Source} object.
     *
     * @param source Source, which should be released.
     */
    public void release(Source source) {
        if (null!=source) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Releasing source "+source.getURI());
            }
        }
    }

}

