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
 * @version CVS $Id: ResolverImpl.java,v 1.2 2004/03/05 13:02:39 bdelacretaz Exp $
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
