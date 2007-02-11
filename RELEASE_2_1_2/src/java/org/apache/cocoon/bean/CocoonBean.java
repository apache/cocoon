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
package org.apache.cocoon.bean;

import org.apache.cocoon.Constants;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.bean.helpers.DelayedOutputStream;
import org.apache.cocoon.components.notification.SimpleNotifyingBean;
import org.apache.cocoon.components.notification.Notifier;
import org.apache.cocoon.components.notification.DefaultNotifyingBuilder;
import org.apache.cocoon.components.notification.Notifying;
import org.apache.cocoon.matching.helpers.WildcardHelper;

import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.Source;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * <p>The Cocoon Bean simplifies usage of the Cocoon object. Allows to create, 
 * configure Cocoon instance and process requests, one by one or multiple 
 * with link traversal.</p>
 *
 * <p><b>WARNING:</b> This interface is not stable and could be changed in 
 * backward incompatible way without prior notice.</p> 

 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:nicolaken@apache.org">Nicola Ken Barozzi</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a>
 * @version CVS $Id: CocoonBean.java,v 1.30 2003/09/22 22:51:02 joerg Exp $
 */
public class CocoonBean extends CocoonWrapper {

    // User Supplied Parameters
    private boolean followLinks = true;
    private boolean precompileOnly = false;
    private boolean confirmExtension = true;
    private String defaultFilename = Constants.INDEX_URI;
    private List targets = new ArrayList();
    private boolean brokenLinkGenerate = false;
    private String brokenLinkExtension = "";
    private List excludePatterns = new ArrayList();
    private List includePatterns = new ArrayList();
    private List includeLinkExtensions = null;
    
    // Internal Objects
    private Map allProcessedLinks;
    private Map allTranslatedLinks;
    private boolean initialized;
    private List listeners = new ArrayList();
    private boolean verbose;
    SourceResolver sourceResolver;
    
    //
    // INITIALISATION METHOD
    //

    public void initialize() throws Exception {
        if (this.initialized == false) {
            super.initialize();

            if (targets.size() == 0 && !precompileOnly) {
                String error = "Please, specify at least one starting URI.";
                log.fatalError(error);
                throw new ProcessingException(error);
            }

            this.sourceResolver =
                (SourceResolver) getComponentManager().lookup(
                    SourceResolver.ROLE);

            initialized = true;
        }
    }

    protected void finalize() throws Throwable {
        dispose();
        super.finalize();
    }

    //
    // GETTERS AND SETTERS FOR CONFIGURATION PROPERTIES
    //

    public void setFollowLinks(boolean follow) {
        followLinks = follow;
    }

    public void setConfirmExtensions(boolean confirmExtension) {
        this.confirmExtension = confirmExtension;
    }

    public void setPrecompileOnly(boolean precompileOnly) {
        this.precompileOnly = precompileOnly;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setDefaultFilename(String filename) {
        defaultFilename = filename;
    }

    public void setBrokenLinkGenerate(boolean brokenLinkGenerate) {
        this.brokenLinkGenerate = brokenLinkGenerate;
    }

    public void setBrokenLinkExtension(String brokenLinkExtension) {
        this.brokenLinkExtension = brokenLinkExtension;
    }

    public boolean followLinks() {
        return followLinks;
    }
    
    public boolean confirmExtensions() {
        return confirmExtension;
    }
    /**
     * Adds a target for processing
     *
     * @param type Type of target - append, replace, insert.
     * @param root
     * @param sourceURI URI of the starting page
     * @param destURI URI specifying destination for the generated pages.
     * @throws IllegalArgumentException if destURI is missing
     */
    public void addTarget(
            String type,
            String root,
            String sourceURI,
            String destURI)
            throws IllegalArgumentException {
        Target target = new Target(type, root, sourceURI, destURI);
        target.setDefaultFilename(this.defaultFilename);
        target.setFollowLinks(this.followLinks);
        target.setConfirmExtension(this.confirmExtension);
        target.setLogger(this.logger);
        targets.add(target);
    }

    public void addTarget(String type, String sourceURI, String destURI)
        throws IllegalArgumentException {
        Target target = new Target(type, sourceURI, destURI);
        target.setDefaultFilename(this.defaultFilename);
        target.setFollowLinks(this.followLinks);
        target.setConfirmExtension(this.confirmExtension);
        target.setLogger(this.logger);
        targets.add(target);
    }

    public void addTarget(String sourceURI, String destURI)
        throws IllegalArgumentException {
        Target target = new Target(sourceURI, destURI);
        target.setDefaultFilename(this.defaultFilename);
        target.setFollowLinks(this.followLinks);
        target.setConfirmExtension(this.confirmExtension);
        target.setLogger(this.logger);
        targets.add(target);
    }

    public void addTargets(List uris, String destURI)
        throws IllegalArgumentException {
        Iterator i = uris.iterator();
        while (i.hasNext()) {
            Target target = new Target((String) i.next(), destURI);
            target.setDefaultFilename(this.defaultFilename);
            target.setFollowLinks(this.followLinks);
            target.setConfirmExtension(this.confirmExtension);
            target.setLogger(this.logger);
            targets.add(target);
        }
    }

    public void addTarget(
        String type,
        String root,
        String sourceURI,
        String destURI,
        boolean followLinks,
        boolean confirmExtension,
        String logger)
        throws IllegalArgumentException {
        
        Target target;
        if (root == null && type == null) {
            target = new Target(sourceURI, destURI);
        } else if (root == null) {
            target = new Target(type, sourceURI, destURI);
        } else {
            target = new Target(type, root, sourceURI, destURI);
        }
        target.setDefaultFilename(this.defaultFilename);
        target.setFollowLinks(followLinks);
        target.setConfirmExtension(confirmExtension);
        target.setLogger(logger);
        targets.add(target);
    }

    public void addExcludePattern(String pattern) {
        int preparedPattern[] = WildcardHelper.compilePattern(pattern);
        excludePatterns.add(preparedPattern);
    }
    
    public void addIncludePattern(String pattern) {
        int preparedPattern[] = WildcardHelper.compilePattern(pattern);
        includePatterns.add(preparedPattern);
    }

    public void addIncludeLinkExtension(String extension) {
        if (includeLinkExtensions == null) {
            includeLinkExtensions = new ArrayList();
        }
        includeLinkExtensions.add(extension);
    }
    
    public void addListener(BeanListener listener) {
        this.listeners.add(listener);
    }

    public void pageGenerated(String sourceURI, 
                              String destURI, 
                              int pageSize, 
                              int linksInPage, 
                              int newLinksInPage, 
                              int pagesRemaining, 
                              int pagesComplete, 
                              long timeTaken) {
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            BeanListener l = (BeanListener) i.next();
            l.pageGenerated(sourceURI, 
                            destURI, 
                            pageSize, 
                            linksInPage, 
                            newLinksInPage,
                            pagesRemaining,
                            pagesComplete,
                            timeTaken);
        }
    }

    public void sendMessage(String msg) {
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            BeanListener l = (BeanListener) i.next();
            l.messageGenerated(msg);
        }
    }

    public void sendWarning(String uri, String warning) {
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            BeanListener l = (BeanListener) i.next();
            l.warningGenerated(uri, warning);
        }
    }

    public void sendBrokenLinkWarning(String uri, String warning) {
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            BeanListener l = (BeanListener) i.next();
            l.brokenLinkFound(uri, "", warning, null);
        }
    }

    public void pageSkipped(String uri, String message) {
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            BeanListener l = (BeanListener) i.next();
            l.pageSkipped(uri, message);
        }
    }

    public void dispose() {
        if (this.initialized) {
            if (this.sourceResolver != null) {
                getComponentManager().release(this.sourceResolver);
                this.sourceResolver = null;
            }
        }
    }

    /**
     * Process the URI list and process them all independently.
     * @exception Exception if an error occurs
     */
    public void process() throws Exception {

        if (!this.initialized) {
            this.initialize();
        }

        allProcessedLinks = new HashMap();
        allTranslatedLinks = new HashMap();

        Map targetMap = new HashMap();
        Iterator i = targets.iterator();
        while (i.hasNext()) {
            Target target = (Target) i.next();
            targetMap.put(target, target);
        }

        int nCount = 0;
        while (targetMap.size() > 0) {
            Target target = (Target) targetMap.keySet().iterator().next();
            try {
                if (!allProcessedLinks.containsKey(target)) {
                    if (precompileOnly) {
                        processXSP(target.getSourceURI());
                    } else if (this.followLinks) {
                        i = processTarget(target).iterator();
                        while (i.hasNext()) {
                            Target link = (Target) i.next();
                            targetMap.put(link, link);
                        }
                    } else {
                        processTarget(target);
                    }
                }
            } catch (ResourceNotFoundException rnfe) {
                this.sendBrokenLinkWarning(target.getSourceURI(), rnfe.getMessage());
            }

            targetMap.remove(target);
            nCount++;

            if (log.isInfoEnabled()) {
                log.info(
                    "  Memory used: "
                        + (Runtime.getRuntime().totalMemory()
                            - Runtime.getRuntime().freeMemory()));
                log.info(
                    "  Processed, Translated & Left: "
                        + allProcessedLinks.size()
                        + ", "
                        + allTranslatedLinks.size()
                        + ", "
                        + targetMap.size());
            }
        }

        if (nCount == 0) {
            super.precompile();
        }
    }

    /**
     * Processes the given Target and return all links.
     *
     * If links are to be followed, and extensions checked then the algorithm is as
     * follows:
     * <ul>
     *  <li>file name for the URI is generated. URI MIME type is checked for
     *      consistency with the URI and, if the extension is inconsistent
     *      or absent, the file name is changed</li>
     *  <li>the link view of the given URI is called and the file names for linked
     *      resources are generated and stored.</li>
     *  <li>for each link, absolute file name is translated to relative path.</li>
     *  <li>after the complete list of links is translated, the link-translating
     *      view of the resource is called to obtain a link-translated version
     *      of the resource with the given link map</li>
     *  <li>list of absolute URI is returned, for every URI which is not yet
     *      present in list of all translated URIs</li>
     * </ul>
     *
     * If links are to be followed, but extensions are not checked, then the
     * algorithm will be:
     * <ul>
     *   <li>The content for the page is generated</li>
     *   <li>Whilst generating, all links are gathered by the LinkGatherer</li>
     *   <li>Gathered links are added to the unprocessed links list, and
     *       processing continues until all processing is complete
     *   </li>
     * </ul>
     *
     * @param target a <code>Target</code> target to process
     * @return a <code>Collection</code> containing all links found, as
     * Target objects.
     * @exception Exception if an error occurs
     */
    private Collection processTarget(Target target) throws Exception {

        int status = 0;
        
        int linkCount = 0;
        int newLinkCount = 0;
        int pageSize = 0;
        
        long startTimeMillis = System.currentTimeMillis();

        if (target.confirmExtensions()) {
            if (null == allTranslatedLinks.get(target.getSourceURI())) {
                final String mimeType = getType(target.getDeparameterizedSourceURI(), target.getParameters());
                target.setMimeType(mimeType);
                allTranslatedLinks.put(target.getSourceURI(), target.getDestinationURI());
            }
        }
        // Store processed URI list to avoid eternal loop
        allProcessedLinks.put(target, target);

        // IS THIS STILL NEEDED?
        //if ("".equals(destinationURI)) {
        //    return new ArrayList();
        //}

        // Process links
        final HashMap translatedLinks = new HashMap();
        final List targets = new ArrayList();
        if (target.followLinks() && target.confirmExtensions() && isCrawlablePage(target)) {
            final Iterator i =
                this.getLinks(target.getDeparameterizedSourceURI(), target.getParameters()).iterator();

            while (i.hasNext()) {
                String linkURI = (String) i.next();
                Target linkTarget = target.getDerivedTarget(linkURI);

                if (linkTarget == null) {
                    pageSkipped(linkURI, "link does not share same root as parent");
                    continue;
                }

                if (!isIncluded(linkTarget.getSourceURI())) {
                    pageSkipped(linkTarget.getSourceURI(), "matched include/exclude rules");
                    continue;
                }

                if (null == allTranslatedLinks.get(linkTarget.getSourceURI())) {
                    try {
                        final String mimeType = 
                                getType(target.getDeparameterizedSourceURI(), target.getParameters());
                        target.setMimeType(mimeType);
                        allTranslatedLinks.put(target.getSourceURI(), target.getDestinationURI());
                        log.info("  Link translated: " + linkTarget.getSourceURI());
                        targets.add(linkTarget);
                    } catch (ProcessingException pe) {
                        this.sendBrokenLinkWarning(linkTarget.getSourceURI(), pe.getMessage());
                        if (this.brokenLinkGenerate) {
                           targets.add(linkTarget);
                        }
                    }
                }

                translatedLinks.put(linkTarget.getOriginalSourceURI(), linkTarget.getTranslatedURI(target.getPath()));
            }

            linkCount = translatedLinks.size();
        }

        try {
            // Process URI
            DelayedOutputStream output = new DelayedOutputStream();
            try {
                List gatheredLinks;
                if (!target.confirmExtensions() && target.followLinks() && isCrawlablePage(target)) {
                    gatheredLinks = new ArrayList();
                } else {
                    gatheredLinks = null;
                }
        
                status =
                    getPage(
                        target.getDeparameterizedSourceURI(),
                        getLastModified(target),
                        target.getParameters(),
                        target.confirmExtensions() ? translatedLinks : null,
                        gatheredLinks,
                        output);

                if (status >= 400) {
                    throw new ProcessingException(
                        "Resource not found: " + status);
                }

                if (gatheredLinks != null) {
                    for (Iterator it = gatheredLinks.iterator();it.hasNext();) {
                        String linkURI = (String) it.next();
                        Target linkTarget = target.getDerivedTarget(linkURI);

                        if (linkTarget == null) {
                            pageSkipped(linkURI, "link does not share same root as parent");
                            continue;
                        }

                        if (!isIncluded(linkTarget.getSourceURI())) {
                            pageSkipped(linkTarget.getSourceURI(), "matched include/exclude rules");
                            continue;
                        }
                        targets.add(linkTarget);
                    }
                    linkCount = gatheredLinks.size();
                }

            } catch (ProcessingException pe) {
                output.close();
                output = null;
                this.resourceUnavailable(target);
                this.sendBrokenLinkWarning(target.getSourceURI(),
                    DefaultNotifyingBuilder.getRootCause(pe).getMessage());
            } finally {
                if (output != null && status != -1) {

                    ModifiableSource source = getSource(target);
                    try {
                        pageSize = output.size();
                        OutputStream stream = source.getOutputStream();

                        output.setFileOutputStream(stream);
                        output.flush();
                        output.close();
                        pageGenerated(target.getSourceURI(), 
                                      target.getAuthlessDestURI(), 
                                      pageSize,
                                      linkCount,
                                      newLinkCount,
                                      0, //pagesRemaining,  @TODO@ Implement this
                                      0, //pagesComplete,   @TODO@ Implement this
                                      System.currentTimeMillis()- startTimeMillis);

                    } catch (IOException ioex) {
                        log.warn(ioex.toString());
                    } finally {
                        releaseSource(source);
                    }
                }
            }
        } catch (Exception rnfe) {
            log.warn("Could not process URI: " + target.getSourceURI());
            this.sendBrokenLinkWarning(target.getSourceURI(), "URI not found");
        }

        return targets;
    }

    /**
     * Generate a <code>resourceUnavailable</code> message.
     *
     * @param target being unavailable
     * @exception IOException if an error occurs
     */
    private void resourceUnavailable(Target target)
        throws IOException, ProcessingException {
        if (brokenLinkGenerate) {
            //Why decode this URI now?
            //String brokenFile = NetUtils.decodePath(destinationURI);
            
            if (brokenLinkExtension != null) {
                target.setExtraExtension(brokenLinkExtension);
            }
            SimpleNotifyingBean n = new SimpleNotifyingBean(this);
            n.setType("resource-not-found");
            n.setTitle("Resource not Found");
            n.setSource("Cocoon commandline (Main.java)");
            n.setMessage("Page Not Available.");
            n.setDescription("The requested resource couldn't be found.");
            n.addExtraDescription(Notifying.EXTRA_REQUESTURI, target.getSourceURI());
            n.addExtraDescription("missing-file", target.getSourceURI());

            ModifiableSource source = getSource(target);
            try {
                OutputStream stream = source.getOutputStream();

                PrintStream out = new PrintStream(stream);
                Notifier.notify(n, out, "text/html");
                out.flush();
                out.close();
            } finally {
                releaseSource(source);
            }
        }
    }
    
    public ModifiableSource getSource(Target target)
        throws IOException, ProcessingException {
        final String finalDestinationURI = target.getDestinationURI();
        Source src = sourceResolver.resolveURI(finalDestinationURI);
        if (!(src instanceof ModifiableSource)) {
            sourceResolver.release(src);
            throw new ProcessingException(
                "Source is not Modifiable: " + finalDestinationURI);
        }
        return (ModifiableSource) src;
    }

    public long getLastModified(Target target) throws IOException, ProcessingException {
        Source src = getSource(target);
        long lastModified = src.getLastModified();
        this.releaseSource(src);
        return lastModified;
    }
        
    public void releaseSource(Source source) {
        sourceResolver.release(source);
    }
    private boolean isIncluded(String uri) {
        boolean included;
        Iterator i;
        HashMap map = new HashMap();
        
        if (includePatterns.size() == 0) {
            included = true;
        } else {
            included = false;
            i = includePatterns.iterator();
            while (i.hasNext()){ 
                int pattern[] = (int[])i.next();
                if (WildcardHelper.match(map, uri, pattern)) {
                    included=true;
                    break;
                }
            }
        }
        if (excludePatterns.size() != 0) {
            i = excludePatterns.iterator();
            while (i.hasNext()) {
                int pattern[] = (int[])i.next();
                if (WildcardHelper.match(map, uri, pattern)) {
                    included=false;
                    break;
                }
            }
        }
        return included;
    }
    private boolean isCrawlablePage(Target target) {
        if (includeLinkExtensions == null) {
            return true;
        } else {
            return includeLinkExtensions.contains(target.getExtension());
        }
    }
}
