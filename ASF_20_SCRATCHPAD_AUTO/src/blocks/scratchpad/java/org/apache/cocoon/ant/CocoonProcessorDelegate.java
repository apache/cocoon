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
package org.apache.cocoon.ant;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.Cocoon;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.environment.commandline.CommandLineContext;
import org.apache.cocoon.environment.commandline.LinkSamplingEnvironment;

/**
 * A facade for Cocoon processing
 *
 * @author    huber@apache.org
 * @version CVS $Id: CocoonProcessorDelegate.java,v 1.2 2004/03/05 10:07:25 bdelacretaz Exp $
 */
public class CocoonProcessorDelegate extends AbstractLogEnabled
         implements Contextualizable, Configurable, Initializable {

    private Cocoon cocoon;

    private org.apache.avalon.framework.context.Context context;

    private CommandLineContext clContext;
    private File contextDir;
    private File destDir;

    private boolean followLinks;

    private CocoonCrawling cocoonCrawling;


    /**
     * Constructor for the CocoonProcessorDelegate object
     *
     * @param  cocoon  Description of Parameter
     */
    public CocoonProcessorDelegate(Cocoon cocoon) {
        this.cocoon = cocoon;

    }


    /**
     *   Description of the Method
     *
     * @param  context               Description of Parameter
     * @exception  ContextException  Description of Exception
     */
    public void contextualize(org.apache.avalon.framework.context.Context context) throws ContextException {
        this.context = new DefaultContext(context);
    }


    /**
     *   Description of the Method
     *
     * @param  configuration               Description of Parameter
     * @exception  ConfigurationException  Description of Exception
     */
    public void configure(Configuration configuration) throws ConfigurationException {
        Configuration child;

        child = configuration.getChild("follow-links");
        this.followLinks = child.getValueAsBoolean();

        /* FIXME (SM): the code below is dead. What was it supposed to do?
        child = configuration.getChild("headers");
        Parameters headers = Parameters.fromConfiguration(child);
        */
    }


    /**
     *   Description of the Method
     *
     * @exception  Exception  Description of Exception
     */
    public void initialize() throws Exception {
        clContext = (CommandLineContext) this.context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
        contextDir = new File(clContext.getRealPath("/"));

        destDir = (File) this.context.get("dest-dir");

        cocoonCrawling = new CocoonCrawling();
        cocoonCrawling.enableLogging(getLogger());
    }


    /**
     *   Description of the Method
     *
     * @param  uris                     Description of Parameter
     * @exception  ProcessingException  Description of Exception
     */
    public void processAllUris(Set uris) throws ProcessingException {
        Iterator i = uris.iterator();
        while (i.hasNext()) {
            Object uriObjRef = i.next();

            UriType uriType = null;
            if (uriObjRef instanceof String) {
                uriType = new UriType((String) uriObjRef);
            } else if (uriObjRef instanceof UriType) {
                uriType = (UriType) uriObjRef;
            } else {
                // skip it don't know how to handle it
                continue;
            }

            if (uriType != null) {
                processUri(uriType);
            }
        }
    }


    /**
     * process a single Uri, resolve followingLink internally
     *
     * @param  uriType                  Description of Parameter
     * @exception  ProcessingException  Description of Exception
     */
    public void processUri(UriType uriType) throws ProcessingException {
        int maxIterations = -1;
        int iterations = 0;

        // get the links of the page
        if (!followLinks) {
            maxIterations = 1;
        }

        cocoonCrawling.setRoot(uriType);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Crawling root " + String.valueOf(uriType.getPath()));
        }

        for (Iterator cocoonCrawlingIterator = cocoonCrawling.iterator();
                (maxIterations > -1 ? iterations < maxIterations : true) &&
                cocoonCrawlingIterator.hasNext();
                iterations++) {

            UriType crawlingUriType = (UriType) cocoonCrawlingIterator.next();
            
            //
            // fix crawlingUriType append 'index.html' if uri ends with '/'
            //
            String uri = crawlingUriType.getUri();
            if (uri.charAt(uri.length() - 1) == '/') {
              crawlingUriType = new UriType( Constants.INDEX_URI + ".html" );
            }

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("processUri uri " + String.valueOf(crawlingUriType.getUri()));
            }

            Map parameters = crawlingUriType.getParameters();
            Map rewriteLinks = new HashMap();

            // get the page
            DelayedFileOutputStream dfos = new DelayedFileOutputStream();
            try {
                //
                // get a page, URI is defined by crawlingUriType
                //
                getPage(crawlingUriType, parameters, rewriteLinks, dfos);
            } catch (ResourceNotFoundException rnfe) {
                String message = "Unavailable resource of uri " + String.valueOf(crawlingUriType.getUri());
                getLogger().warn(message, rnfe);
                // continue, as fetch links for unavailable uri
                // will throw again a ResourceNotFoundException
                continue;
            } catch (Exception e) {
                String message = "Processing error of uri " + String.valueOf(crawlingUriType.getUri());
                getLogger().warn(message, e);

                // being optimistic that fetching of links
                // may work
            } finally {
                try {
                    dfos.flush();
                    dfos.close();
                } catch (IOException ioe) {
                    String message = "Cannot close output " + String.valueOf(crawlingUriType.getDestFile()) + ", " +
                            "for URI " + String.valueOf(crawlingUriType.getUri());
                    getLogger().warn(message, ioe);
                }
            }

            //
            // being more restrictive about for which uri links are requestes
            // fetch links if content type is not defined
            // or iff content type is of form text/* is this okay?
            //
            String contentType = crawlingUriType.getContentType();
            if (contentType == null || (contentType != null && contentType.startsWith("text"))) {
                // get the links
                try {
                    //
                    // get links of URI crawlingUriType
                    //
                    Collection links = getLinks(crawlingUriType, parameters);
                    if (links != null) {
                        for (Iterator i = links.iterator();
                                i.hasNext(); ) {
                            String linkUri = (String) i.next();
                            if (getLogger().isDebugEnabled()) {
                                getLogger().debug("processUri linkUri " + String.valueOf(linkUri));
                            }

                            UriType linkUriType = new UriType(crawlingUriType, linkUri);
                            if (getLogger().isDebugEnabled()) {
                                getLogger().debug("parent Uri " + String.valueOf(crawlingUriType.getUri()) + ", " +
                                        String.valueOf(crawlingUriType.getPath()));
                            }

                            if (getLogger().isDebugEnabled()) {
                                getLogger().debug("add uri " + String.valueOf(linkUriType.getUri()));
                            }
                            cocoonCrawling.add(linkUriType);

                            crawlingUriType.addLink(linkUriType);
                        }
                    }
                } catch (ResourceNotFoundException rnfe) {
                    String message = "Failed fetching links of unavailable resource uri " + String.valueOf(crawlingUriType.getUri());
                    getLogger().warn(message, rnfe);

                } catch (Exception e) {
                    String message = "Processing error while fetching links of uri " + String.valueOf(crawlingUriType.getUri());
                    getLogger().warn(message, e);
                }
            }
        }
    }


    /**
     *   dump a list of all visited uris, its content-type, and its outbound links
     */
    public void dumpVisitedLinks() {
        // dump visited Links
        for (Iterator visitedAlreadyIterator = cocoonCrawling.visitedAlreadyIterator();
                visitedAlreadyIterator.hasNext(); ) {
            UriType visitedAlreadyUriType = (UriType) visitedAlreadyIterator.next();

            getLogger().info("Visited Uri " + String.valueOf(visitedAlreadyUriType.getUri()) + ", " +
                    "contentType " + String.valueOf(visitedAlreadyUriType.getContentType()));

            // get outbound links of visitedAlreadyUriType
            Collection linksFromVisitedAlreadyUriType = visitedAlreadyUriType.getLinks();
            if (linksFromVisitedAlreadyUriType != null) {
                for (Iterator linksFromvisitedAlreadyIterator = linksFromVisitedAlreadyUriType.iterator();
                        linksFromvisitedAlreadyIterator.hasNext(); ) {
                    UriType linkFromVisitedAlreadyUriType = (UriType) linksFromvisitedAlreadyIterator.next();
                    getLogger().info("Visited Uri links " + String.valueOf(linkFromVisitedAlreadyUriType.getUri()));
                }
            }
        }
    }


    /**
     * Processes an Uri for its content.
     *
     * @param  parameters     a <code>Map</code> value containing request parameters
     * @param  links          a <code>Map</code> value
     * @param  uriType        Description of Parameter
     * @param  dfos           Description of Parameter
     * @exception  Exception  Description of Exception
     */
    protected void getPage(UriType uriType, Map parameters, Map links, DelayedFileOutputStream dfos) throws Exception {
        Map attributes = null;

        DelayedFileSavingEnvironment env = new DelayedFileSavingEnvironment(
                uriType,
                this.contextDir,
                attributes,
                parameters,
                links,
                dfos,
                getLogger());
        env.setDestDir(destDir);
        cocoon.process(env);

        uriType.setContentType(env.getContentType());
    }


    /**
     *   Gets the links attribute of the CocoonProcessorDelegate object
     *
     * @param  uriType        Description of Parameter
     * @param  parameters     Description of Parameter
     * @return                The links value
     * @exception  Exception  Description of Exception
     */
    protected Collection getLinks(UriType uriType, Map parameters) throws Exception {
        Map attributes = null;

        LinkSamplingEnvironment env = new LinkSamplingEnvironment(
                uriType.getDeparameterizedUri(),
                this.contextDir,
                attributes,
                parameters,
                clContext,
                getLogger());
        cocoon.process(env);
        return env.getLinks();
    }

}
