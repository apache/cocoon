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
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.components.source.helpers.SourceCredential;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceParameters;
import org.apache.slide.common.NamespaceAccessToken;

/**
 * A factory for sources from a Jakarta Slide repository.
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: SlideSourceFactory.java,v 1.6 2003/12/02 19:44:02 joerg Exp $
 */
public class SlideSourceFactory extends AbstractLogEnabled
  implements SourceFactory, ThreadSafe, Serviceable, Contextualizable {

    /** The ServiceManager instance */
    private ServiceManager manager = null;
    private Context context;

    /**
     * Set the current <code>ServiceManager</code> instance used by this
     * <code>Serviceable</code>.
     *
     * @param manager ServiceManager.
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /**
     * Get a <code>Source</code> object.
     *
     * @param uri URI of the source.
     * @param parameters This is optional.
     *
     * @return A new source object.
     */
    public Source getSource(String uri,
                            Map parameters)
                              throws MalformedURLException, IOException,
                                     SourceException {
        this.getLogger().debug("Creating source object for '"+uri+"'");

        String scheme = SourceUtil.getScheme(uri);

        String path = SourceUtil.getPathWithoutAuthority(uri);

        if ((path==null) || (path.length()==0)) {
            path = "/";
        } else if ( !path.startsWith("/")) {
            path = "/"+path;
        }

        String query = SourceUtil.getQuery(uri);
        SourceParameters queryParameters = null;

        if ((query==null) || (query.length()==0)) {
            queryParameters = new SourceParameters();
        } else {
            queryParameters = new SourceParameters(query);
        }

        this.getLogger().debug("Path is "+path);
        this.getLogger().debug("Query is "+query);
        this.getLogger().debug("Source parameters:  "+
                               queryParameters.toString());

        //String repositoryname = queryParameters.getParameter("cocoon-repository", null);

        String namespace = queryParameters.getParameter("cocoon-repository-namespace",
                               null);
        String principal = queryParameters.getParameter("cocoon-source-principal",
                               "guest");
        String password = queryParameters.getParameter("cocoon-source-password",
                              null);
        String revision = queryParameters.getParameter("cocoon-source-revision",
                              null);
        String branch = queryParameters.getParameter("cocoon-source-branch",
                            null);

        getLogger().debug("Used prinical '"+principal+"' for source");

        SourceCredential credential;

        if (password!=null) {
            credential = new SourceCredential(principal, password);
        } else {
            credential = new SourceCredential(principal);
        }

        if (path.length()==0) {
            path = "/";
        }

        SlideRepository repository = null;
        try {
            repository = (SlideRepository) this.manager.lookup(SlideRepository.ROLE);

            if ( !(repository instanceof SlideRepository)) {
                getLogger().error("Can't get Slide repository");
                return null;
            }

            SlideRepository sliderepository = (SlideRepository) repository;

            NamespaceAccessToken nat = sliderepository.getNamespaceToken(namespace);

            if (nat==null) {
                throw new SourceException("Repository with the namespace '"+
                                          namespace+"' couldn't be found");
            }

            SlideSource source = new SlideSource(nat, scheme, path,
                                                 credential, revision,
                                                 branch);

            source.enableLogging(getLogger());
            source.contextualize(this.context);
            source.service(this.manager);

            return source;

        } catch (ServiceException se) {
            getLogger().error("Could not lookup for service.", se);
        } finally {
            if (repository!=null) {
                this.manager.release(repository);
            }
            repository = null;
        }

        return null;
    }

    /**
     * Release a {@link Source} object.
     *
     * @param source Source, which should be released.
     */
    public void release(Source source) {
        if (null!=source) {
            this.getLogger().debug("Releasing source "+source.getURI());
            // simply do nothing
        }
    }

    /**
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     *
     * @param context The context.
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

}

