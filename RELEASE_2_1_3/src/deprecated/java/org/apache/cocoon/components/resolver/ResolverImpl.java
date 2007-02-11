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
package org.apache.cocoon.components.resolver;

import org.apache.excalibur.xml.EntityResolver;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.Constants;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;


/**
 * A component that uses catalogs for resolving entities.
 * This implementation uses the XML Entity and URI Resolvers from
 * http://xml.apache.org/commons/
 * published by Norman Walsh. More information on the catalogs can be
 * found at
 * http://cocoon.apache.org/userdocs/concepts/catalog.html
 *
 * The catalog is by default loaded from "WEB-INF/entities/catalog".
 * This can be configured by the "catalog" parameter in the cocoon.xconf:
 * &lt;entity-resolver&gt;
 *   &lt;parameter name="catalog" value="mycatalog"/&gt;
 * &lt;/entity-resolver&gt;
 *
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @author <a href="mailto:crossley@apache.org">David Crossley</a>
 * @version CVS $Id: ResolverImpl.java,v 1.1 2003/03/12 09:35:37 cziegeler Exp $
 * @since 2.0rc1
 */
public class ResolverImpl extends AbstractLogEnabled
  implements EntityResolver,
             Resolver,
             Contextualizable,
             Composable,
             Parameterizable,
             ThreadSafe,
             Disposable {

    /** The catalog manager */
    protected CatalogManager catalogManager = new CatalogManager();

    /** The catalog resolver */
    protected CatalogResolver catalogResolver = new CatalogResolver(catalogManager);

    /** The component manager */
    protected ComponentManager manager = null;

    /** The context */
    protected org.apache.cocoon.environment.Context context;

    /** Contextualize this class */
    public void contextualize(Context context)
            throws ContextException {
        this.context = (org.apache.cocoon.environment.Context)
                        context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
    }


    /**
     * Set the configuration. Load the system catalog and apply any
     * parameters that may have been specified in cocoon.xconf
     * @param params The configuration information
     * @exception ParameterException
     */
    public void parameterize(Parameters params) throws ParameterException {

        /* Over-ride debug level that is set by CatalogManager.properties */
        String verbosity = params.getParameter("verbosity", "");
        if (verbosity != "") {
            if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Setting Catalog resolver "
                + "verbosity level to " + verbosity);
            }
            int verbosityLevel = 0;
            try {
                verbosityLevel = Integer.parseInt(verbosity);
                catalogManager.setVerbosity(verbosityLevel);
            } catch (NumberFormatException ce1) {
                this.getLogger().warn("Trouble setting Catalog verbosity",
                                        ce1);
            }
        }

        /* Load the built-in catalog */
        String catalogFile = params.getParameter("catalog",
                                "/WEB-INF/entities/catalog");
        try {
            String catalogURL = null;
            catalogURL = this.context.getRealPath(catalogFile);
            if (catalogURL == null) {
                catalogURL =
                    this.context.getResource(catalogFile).toExternalForm();
            }
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("System OASIS Catalog URL is "
                    + catalogURL);
            }
            catalogResolver.getCatalog().parseCatalog(catalogURL);
        } catch (Exception e) {
            this.getLogger().warn("Could not get Catalog URL", e);
        }

        /* Load a single additional local catalog */
        String localCatalogFile = params.getParameter("local-catalog", null);
        if (localCatalogFile != null) {
            try {
                if (this.getLogger().isDebugEnabled()) {
                    this.getLogger().debug("Additional Catalog is "
                        + localCatalogFile);
                }
                catalogResolver.getCatalog().parseCatalog(localCatalogFile);
            } catch (Exception e) {
                this.getLogger().warn("Could not get local Catalog file", e);
            }
        }
    }

    /**
     * Set the global component manager.
     * @param manager The global component manager
     * @exception ComponentException
     */
    public void compose(ComponentManager manager) throws ComponentException {
        if ((this.manager == null) && (manager != null)) {
            this.manager = manager;
        }
    }

    /**
     * Allow the application to resolve external entities.
     *
     * <p>The Parser will call this method before opening any external
     * entity except the top-level document entity (including the
     * external DTD subset, external entities referenced within the
     * DTD, and external entities referenced within the document
     * element): the application may request that the parser resolve
     * the entity itself, that it use an alternative URI, or that it
     * use an entirely different input source.</p>
     *
     * <p>Application writers can use this method to redirect external
     * system identifiers to secure and/or local URIs, to look up
     * public identifiers in a catalogue, or to read an entity from a
     * database or other input source (including, for example, a dialog
     * box).</p>
     *
     * <p>If the system identifier is a URL, the SAX parser must
     * resolve it fully before reporting it to the application.</p>
     *
     * @param publicId The public identifier of the external entity
     *        being referenced, or null if none was supplied.
     * @param systemId The system identifier of the external entity
     *        being referenced.
     * @return An InputSource object describing the new input source,
     *         or null to request that the parser open a regular
     *         URI connection to the system identifier.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @exception java.io.IOException A Java-specific IO exception,
     *            possibly the result of creating a new InputStream
     *            or Reader for the InputSource.
     * @see org.xml.sax.InputSource
     */
    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException {
/*
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("CER resolution: publicId="
                + publicId + " systemId=" + systemId);
        }
*/
        InputSource altInputSource = catalogResolver.resolveEntity(publicId,
                                        systemId);
        if (altInputSource != null) {
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("Resolved catalog entity: "
                    + publicId + " " + altInputSource.getSystemId());
            }
        }
/*
        else {
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("CER: altInputSource is null");
            }
        }
*/
        return altInputSource;
    }

    /**
     * Dispose
     */
    public void dispose() {
    }
}
