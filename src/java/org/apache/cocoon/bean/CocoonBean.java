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

import org.apache.cocoon.util.NetUtils;
import org.apache.cocoon.util.MIMEUtils;
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
import java.util.TreeMap;
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
 * @version CVS $Id: CocoonBean.java,v 1.19 2003/08/29 14:05:50 upayavira Exp $
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
        targets.add(new Target(type, root, sourceURI, destURI));
    }

    public void addTarget(String type, String sourceURI, String destURI)
        throws IllegalArgumentException {
        targets.add(new Target(type, sourceURI, destURI));
    }

    public void addTarget(String sourceURI, String destURI)
        throws IllegalArgumentException {
        targets.add(new Target(sourceURI, destURI));
    }

    public void addTargets(List uris, String destURI)
        throws IllegalArgumentException {
        Iterator i = uris.iterator();
        while (i.hasNext()) {
            Target target = new Target((String) i.next(), destURI);
            targets.add(target);
        }
    }

    public void addExcludePattern(String pattern) {
		int preparedPattern[] = WildcardHelper.compilePattern(pattern);
		excludePatterns.add(preparedPattern);
    }
    
    public void addIncludePattern(String pattern) {
		int preparedPattern[] = WildcardHelper.compilePattern(pattern);
        includePatterns.add(preparedPattern);
    }

    public void addListener(BeanListener listener) {
        this.listeners.add(listener);
    }

    public void pageGenerated(String uri, int linksInPage, int pagesRemaining) {
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            BeanListener l = (BeanListener) i.next();
            l.pageGenerated(uri, linksInPage, pagesRemaining);
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
            l.brokenLinkFound(uri, warning);
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
                            target = (Target) i.next();
                            targetMap.put(target, target);
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

        long startTimeMillis = System.currentTimeMillis();
        int status = 0;
        
        String uri = target.getSourceURI();
        int linkCount = 0;

        // Get parameters, deparameterized URI and path from URI
        final TreeMap parameters = new TreeMap();
        final String deparameterizedURI =
            NetUtils.deparameterize(uri, parameters);
        final String path = NetUtils.getPath(uri);
        final String suri =
            NetUtils.parameterize(deparameterizedURI, parameters);

        // Get file name from URI (without path)
        String pageURI = deparameterizedURI;
        if (pageURI.indexOf("/") != -1) {
            pageURI = pageURI.substring(pageURI.lastIndexOf("/") + 1);
            if (pageURI.length() == 0) {
                pageURI = "./";
            }
        }

        String filename;
        if (confirmExtension) {
            filename = (String) allTranslatedLinks.get(suri);
            if (filename == null) {
                filename = mangle(suri);
                final String type = getType(deparameterizedURI, parameters);
                final String ext = NetUtils.getExtension(filename);
                final String defaultExt = MIMEUtils.getDefaultExtension(type);
                if (defaultExt != null) {
                    if ((ext == null) || (!ext.equals(defaultExt))) {
                        filename += defaultExt;
                    }
                }
                allTranslatedLinks.put(suri, filename);
            }
        } else {
            filename = suri;
        }
        // Store processed URI list to avoid eternal loop
        allProcessedLinks.put(target, target);

        if ("".equals(filename)) {
            return new ArrayList();
        }

        // Process links
        final List absoluteLinks = new ArrayList();
        final HashMap translatedLinks = new HashMap();
        List gatheredLinks = new ArrayList();

        if (followLinks && confirmExtension) {
            final Iterator i =
                this.getLinks(deparameterizedURI, parameters).iterator();

            while (i.hasNext()) {
                String link = (String) i.next();
                // Fix relative links starting with "?"
                String relativeLink = link;
                if (relativeLink.startsWith("?")) {
                    relativeLink = pageURI + relativeLink;
                }

                String absoluteLink =
                    NetUtils.normalize(NetUtils.absolutize(path, relativeLink));
                
                if (!isIncluded(absoluteLink)) {
                    //@TODO@ Log/report skipped link
                    continue;
                }
                
                {
                    final TreeMap p = new TreeMap();
                    absoluteLink =
                        NetUtils.parameterize(
                            NetUtils.deparameterize(absoluteLink, p),
                            p);
                }
                String translatedAbsoluteLink =
                    (String) allTranslatedLinks.get(absoluteLink);
                if (translatedAbsoluteLink == null) {
                    try {
                        translatedAbsoluteLink =
                            this.translateURI(absoluteLink);
                        log.info("  Link translated: " + absoluteLink);
                        allTranslatedLinks.put(
                            absoluteLink,
                            translatedAbsoluteLink);
                        absoluteLinks.add(absoluteLink);
                    } catch (ProcessingException pe) {
                        this.sendBrokenLinkWarning(absoluteLink, pe.getMessage());
                    }
                }

                // I have to add also broken links to the absolute links
                // to be able to generate the "broken link" page
                absoluteLinks.add(absoluteLink);
                final String translatedRelativeLink =
                    NetUtils.relativize(path, translatedAbsoluteLink);
                translatedLinks.put(link, translatedRelativeLink);
            }

            linkCount = translatedLinks.size();
        }

        try {
            // Process URI
            DelayedOutputStream output = new DelayedOutputStream();
            try {
                status =
                    getPage(
                        deparameterizedURI,
                        getLastModified(target, filename),
                        parameters,
                        confirmExtension ? translatedLinks : null,
                        gatheredLinks,
                        output);

                if (status >= 400) {
                    throw new ProcessingException(
                        "Resource not found: " + status);
                }

                if (followLinks && !confirmExtension) {
                    for (Iterator it = gatheredLinks.iterator();
                        it.hasNext();
                        ) {
                        String link = (String) it.next();
                        if (link.startsWith("?")) {
                            link = pageURI + link;
                        }
                        String absoluteLink =
                            NetUtils.normalize(NetUtils.absolutize(path, link));
                        {
                            final TreeMap p = new TreeMap();
                            absoluteLink =
                                NetUtils.parameterize(
                                    NetUtils.deparameterize(absoluteLink, p),
                                    p);
                        }
                        if (isIncluded(absoluteLink)) {
                            absoluteLinks.add(absoluteLink);
                        } else {
                            // @TODO@ Log/report skipped link
                        }
                    }
                    linkCount = gatheredLinks.size();
                }

                pageGenerated(uri, linkCount, 0); // @todo@ get the number of pages remaining here
            } catch (ProcessingException pe) {
                output.close();
                output = null;
                this.resourceUnavailable(target, uri, filename);
                this.sendBrokenLinkWarning(
                    filename,
                    DefaultNotifyingBuilder.getRootCause(pe).getMessage());
            } finally {
                if (output != null && status != -1) {

                    ModifiableSource source = getSource(target, filename);
                    try {
                        OutputStream stream = source.getOutputStream();

                        output.setFileOutputStream(stream);
                        output.flush();
                        output.close();
                    } catch (IOException ioex) {
                        log.warn(ioex.toString());
                    } finally {
                        releaseSource(source);
                    }
                }
                
            }
        } catch (Exception rnfe) {
            log.warn("Could not process URI: " + deparameterizedURI);
            this.sendBrokenLinkWarning(deparameterizedURI, "URI not found");
        }

        List targets = new ArrayList();
        for (Iterator i = absoluteLinks.iterator(); i.hasNext();) {
            String link = (String) i.next();
            Target derivedTarget = target.getDerivedTarget(link);
            if (derivedTarget != null) {
                targets.add(target.getDerivedTarget(link));
            }
        }
/*  Commenting out timestamp - will reimplement properly using the BeanListener interface
        double d = (System.currentTimeMillis()- startTimeMillis);
        String time = " [" + (d/1000) + " seconds]";
        System.out.println("        "+ time);
*/
        return targets;
    }

    /**
     * Translate an URI into a file name.
     *
     * @param uri a <code>String</code> value to map
     * @return a <code>String</code> vlaue for the file
     * @exception Exception if an error occurs
     */
    private String translateURI(String uri) throws Exception {
        if (null == uri || "".equals(uri)) {
            log.warn("cannot translate empty uri");
            if (verbose) sendMessage("cannot translate empty uri");
            return "";
        }
        HashMap parameters = new HashMap();
        String deparameterizedURI = NetUtils.deparameterize(uri, parameters);

        String filename = mangle(uri);
        if (confirmExtension) {
            String type = getType(deparameterizedURI, parameters);
            String ext = NetUtils.getExtension(filename);
            String defaultExt = MIMEUtils.getDefaultExtension(type);
            if (defaultExt != null) {
                if ((ext == null) || (!ext.equals(defaultExt))) {
                    filename += defaultExt;
                }
            }
        }

        return filename;
    }

    /**
     * Generate a <code>resourceUnavailable</code> message.
     *
     * @param target being unavailable
     * @exception IOException if an error occurs
     */
    private void resourceUnavailable(Target target, String uri, String filename)
        throws IOException, ProcessingException {
        if (brokenLinkGenerate) {
            String brokenFile = NetUtils.decodePath(filename);
            if (brokenLinkExtension != null) {
                brokenFile = brokenFile + brokenLinkExtension;
            }
            SimpleNotifyingBean n = new SimpleNotifyingBean(this);
            n.setType("resource-not-found");
            n.setTitle("Resource not Found");
            n.setSource("Cocoon commandline (Main.java)");
            n.setMessage("Page Not Available.");
            n.setDescription("The requested resource couldn't be found.");
            n.addExtraDescription(Notifying.EXTRA_REQUESTURI, uri);
            n.addExtraDescription("missing-file", uri);

            ModifiableSource source = getSource(target, filename);
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

    /**
     * Mangle a URI.
     *
     * @param uri a URI to mangle
     * @return a mangled URI
     */
    private String mangle(String uri) {
        if (log.isDebugEnabled()) {
            log.debug("mangle(\"" + uri + "\")");
        }
        if (uri.charAt(uri.length() - 1) == '/') {
            uri += defaultFilename;
        }
        uri = uri.replace('"', '\'');
        uri = uri.replace('?', '_');
        uri = uri.replace(':', '_');
        if (log.isDebugEnabled()) {
            log.debug(uri);
        }
        return uri;
    }
    
    public ModifiableSource getSource(Target target, String filename)
        throws IOException, ProcessingException {
        final String finalDestinationURI = target.getFinalURI(filename);
        Source src = sourceResolver.resolveURI(finalDestinationURI);
        if (!(src instanceof ModifiableSource)) {
            sourceResolver.release(src);
            throw new ProcessingException(
                "Source is not Modifiable: " + finalDestinationURI);
        }
        return (ModifiableSource) src;
    }

    public long getLastModified(Target target, String filename) throws IOException, ProcessingException {
        return getSource(target, filename).getLastModified();
    }
        
    public void releaseSource(ModifiableSource source) {
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
}
