/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.bean;

import org.apache.avalon.framework.component.Component;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.bean.helpers.Crawler;
import org.apache.cocoon.bean.helpers.DelayedOutputStream;
import org.apache.cocoon.components.notification.SimpleNotifyingBean;
import org.apache.cocoon.components.notification.Notifier;
import org.apache.cocoon.components.notification.DefaultNotifyingBuilder;
import org.apache.cocoon.components.notification.Notifying;
import org.apache.cocoon.util.WildcardMatcherHelper;
import org.apache.commons.lang.SystemUtils;

import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
 * @version CVS $Id$
 */
public class CocoonBean extends CocoonWrapper {

    // User Supplied Parameters
    private boolean followLinks = true;
    private boolean precompileOnly = false;
    private boolean confirmExtension = true;
    private String defaultFilename = Constants.INDEX_URI;
    private boolean brokenLinkGenerate = false;
    private String brokenLinkExtension = "";
    private List excludePatterns = new ArrayList();
    private List includePatterns = new ArrayList();
    private List includeLinkExtensions = null;

    // Internal Objects
    private boolean initialized;
    private List listeners = new ArrayList();
    private boolean verbose;
    SourceResolver sourceResolver;

    private Crawler crawler;
    private String checksumsURI = null;
    private Map checksums;

    public CocoonBean() {
        this.crawler = new Crawler();
    }

    //
    // INITIALISATION METHOD
    //

    public void initialize() throws Exception {
        if (this.initialized == false) {
            super.initialize();

            this.sourceResolver =
                (SourceResolver) this.getComponentManager().lookup(
                    SourceResolver.ROLE);

            this.initialized = true;
        }
    }

    protected void finalize() throws Throwable {
        this.dispose();
        super.finalize();
    }

    //
    // GETTERS AND SETTERS FOR CONFIGURATION PROPERTIES
    //

    public void setFollowLinks(boolean follow) {
        this.followLinks = follow;
    }

    public void setConfirmExtensions(boolean confirmExtension) {
        this.confirmExtension = confirmExtension;
    }

    public void setPrecompileOnly(boolean precompileOnly) {
        this.precompileOnly = precompileOnly;
    }

    public boolean isPrecompileOnly() {
        return this.precompileOnly;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setDefaultFilename(String filename) {
        this.defaultFilename = filename;
    }

    public void setBrokenLinkGenerate(boolean brokenLinkGenerate) {
        this.brokenLinkGenerate = brokenLinkGenerate;
    }

    public void setBrokenLinkExtension(String brokenLinkExtension) {
        this.brokenLinkExtension = brokenLinkExtension;
    }

    public void setChecksumURI(String uri) {
        this.checksumsURI = uri;
    }

    public boolean followLinks() {
        return this.followLinks;
    }

    public boolean confirmExtensions() {
        return this.confirmExtension;
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
        this.crawler.addTarget(target);
    }

    public void addTarget(String type, String sourceURI, String destURI)
        throws IllegalArgumentException {
        Target target = new Target(type, sourceURI, destURI);
        target.setDefaultFilename(this.defaultFilename);
        target.setFollowLinks(this.followLinks);
        target.setConfirmExtension(this.confirmExtension);
        target.setLogger(this.logger);
        this.crawler.addTarget(target);
    }

    public void addTarget(String sourceURI, String destURI)
        throws IllegalArgumentException {
        Target target = new Target(sourceURI, destURI);
        target.setDefaultFilename(this.defaultFilename);
        target.setFollowLinks(this.followLinks);
        target.setConfirmExtension(this.confirmExtension);
        target.setLogger(this.logger);
        this.crawler.addTarget(target);
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
            this.crawler.addTarget(target);
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
        this.crawler.addTarget(target);
    }

    public int getTargetCount() {
        return this.crawler.getRemainingCount();
    }

    public void addExcludePattern(String pattern) {
        this.excludePatterns.add(pattern);
    }

    public void addIncludePattern(String pattern) {
        this.includePatterns.add(pattern);
    }

    public void addIncludeLinkExtension(String extension) {
        if (this.includeLinkExtensions == null) {
            this.includeLinkExtensions = new ArrayList();
        }
        this.includeLinkExtensions.add(extension);
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
        Iterator i = this.listeners.iterator();
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
        Iterator i = this.listeners.iterator();
        while (i.hasNext()) {
            BeanListener l = (BeanListener) i.next();
            l.messageGenerated(msg);
        }
    }

    public void sendWarning(String uri, String warning) {
        Iterator i = this.listeners.iterator();
        while (i.hasNext()) {
            BeanListener l = (BeanListener) i.next();
            l.warningGenerated(uri, warning);
        }
    }

    public void sendBrokenLinkWarning(String uri, String warning) {
        Iterator i = this.listeners.iterator();
        while (i.hasNext()) {
            BeanListener l = (BeanListener) i.next();
            l.brokenLinkFound(uri, "", warning, null);
        }
    }

    public void pageSkipped(String uri, String message) {
        Iterator i = this.listeners.iterator();
        while (i.hasNext()) {
            BeanListener l = (BeanListener) i.next();
            l.pageSkipped(uri, message);
        }
    }

    public void dispose() {
        if (this.initialized) {
            if (this.sourceResolver != null) {
                this.getComponentManager().release((Component)this.sourceResolver);
                this.sourceResolver = null;
            }
            super.dispose();
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

        if (this.crawler.getRemainingCount() == 0 && !this.precompileOnly) {
            this.log.info("No targets for to be processed.");
            return;
        }

        if (this.checksumsURI != null) {
            this.readChecksumFile();
        }

        if (this.crawler.getRemainingCount()>=0) {
            Iterator iterator = this.crawler.iterator();
            while (iterator.hasNext()) {
                Target target = (Target) iterator.next();
                if (!this.precompileOnly) {
                    this.processTarget(this.crawler, target);
								}
            }
        }

        if (this.checksumsURI != null) {
            this.writeChecksumFile();
        }

        if (this.log.isInfoEnabled()) {
              this.log.info(
                  "  Memory used: "
                      + (Runtime.getRuntime().totalMemory()
                          - Runtime.getRuntime().freeMemory()));
              this.log.info(
                  "  Processed, Translated & Left: "
                      + this.crawler.getProcessedCount()
                      + ", "
                      + this.crawler.getTranslatedCount()
                      + ", "
                      + this.crawler.getRemainingCount());
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
     * @exception Exception if an error occurs
     */
    private void processTarget(Crawler crawler, Target target) throws Exception {

        int status = 0;

        int linkCount = 0;
        int newLinkCount = 0;
        int pageSize = 0;
        long startTimeMillis = System.currentTimeMillis();

        if (target.confirmExtensions()) {
            if (!crawler.hasTranslatedLink(target)) {
                final String mimeType = this.getType(target.getDeparameterizedSourceURI(), target.getParameters());
                target.setMimeType(mimeType);
                crawler.addTranslatedLink(target);
            }
        }

        // IS THIS STILL NEEDED?
        //if ("".equals(destinationURI)) {
        //    return new ArrayList();
        //}

        // Process links
        final HashMap translatedLinks = new HashMap();
        if (target.followLinks() && target.confirmExtensions() && this.isCrawlablePage(target)) {
            final Iterator i =
                this.getLinks(target.getDeparameterizedSourceURI(), target.getParameters()).iterator();

            while (i.hasNext()) {
                String linkURI = (String) i.next();
                Target linkTarget = target.getDerivedTarget(linkURI);

                if (linkTarget == null) {
                    this.pageSkipped(linkURI, "link does not share same root as parent");
                    continue;
                }

                if (!this.isIncluded(linkTarget.getSourceURI())) {
                    this.pageSkipped(linkTarget.getSourceURI(), "matched include/exclude rules");
                    continue;
                }

                if (!crawler.hasTranslatedLink(linkTarget)) {
                    try {
                        final String mimeType =
                                this.getType(linkTarget.getDeparameterizedSourceURI(), linkTarget.getParameters());
                        linkTarget.setMimeType(mimeType);
                        crawler.addTranslatedLink(linkTarget);
                        this.log.info("  Link translated: " + linkTarget.getSourceURI());
                        if (crawler.addTarget(linkTarget)) {
                            newLinkCount++;
                        }
                    } catch (ProcessingException pe) {
                        this.sendBrokenLinkWarning(linkTarget.getSourceURI(), pe.getMessage());
                        if (this.brokenLinkGenerate) {
                           if (crawler.addTarget(linkTarget)) {
                               newLinkCount++;
                           }
                        }
                    }
                } else {
                    String originalURI = linkTarget.getOriginalSourceURI();
                    linkTarget = crawler.getTranslatedLink(linkTarget);
                    linkTarget.setOriginalURI(originalURI);
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
                if (!target.confirmExtensions() && target.followLinks() && this.isCrawlablePage(target)) {
                    gatheredLinks = new ArrayList();
                } else {
                    gatheredLinks = null;
                }

                final TreeMap headers = new TreeMap();
                headers.put("user-agent", this.userAgent);
                headers.put("accept", this.accept);

                status =
                    this.getPage(
                        target.getDeparameterizedSourceURI(),
                        this.getLastModified(target),
                        target.getParameters(),
                        headers,
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
                            this.pageSkipped(linkURI, "link does not share same root as parent");
                            continue;
                        }

                        if (!this.isIncluded(linkTarget.getSourceURI())) {
                            this.pageSkipped(linkTarget.getSourceURI(), "matched include/exclude rules");
                            continue;
                        }
                        if (crawler.addTarget(linkTarget)) {
                            newLinkCount++;
                        }
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

                    ModifiableSource source = this.getSource(target);
                    try {
                        pageSize = output.size();

                        if (this.checksumsURI == null || !this.isSameContent(output, target)) {
                            OutputStream stream = source.getOutputStream();
                            output.setFileOutputStream(stream);
                            output.flush();
                            output.close();
                            this.pageGenerated(target.getSourceURI(),
                                          target.getAuthlessDestURI(),
                                          pageSize,
                                          linkCount,
                                          newLinkCount,
                                          crawler.getRemainingCount(),
                                          crawler.getProcessedCount(),
                                          System.currentTimeMillis()- startTimeMillis);
                        } else {
                            output.close();
                            this.pageSkipped(target.getSourceURI(), "Page not changed");
                        }
                    } catch (IOException ioex) {
                        this.log.warn(ioex.toString());
                    } finally {
                        this.releaseSource(source);
                    }
                }
            }
        } catch (Exception rnfe) {
            this.log.warn("Could not process URI: " + target.getSourceURI());
            rnfe.printStackTrace();
            this.sendBrokenLinkWarning(target.getSourceURI(), "URI not found: "+rnfe.getMessage());
        }
    }

    /**
     * Generate a <code>resourceUnavailable</code> message.
     *
     * @param target being unavailable
     * @exception IOException if an error occurs
     */
    private void resourceUnavailable(Target target)
        throws IOException, ProcessingException {
        if (this.brokenLinkGenerate) {
            //Why decode this URI now?
            //String brokenFile = NetUtils.decodePath(destinationURI);

            if (this.brokenLinkExtension != null) {
                target.setExtraExtension(this.brokenLinkExtension);
            }
            SimpleNotifyingBean n = new SimpleNotifyingBean(this);
            n.setType("resource-not-found");
            n.setTitle("Resource not Found");
            n.setSource("Cocoon commandline (Main.java)");
            n.setMessage("Page Not Available.");
            n.setDescription("The requested resource couldn't be found.");
            n.addExtraDescription(Notifying.EXTRA_REQUESTURI, target.getSourceURI());
            n.addExtraDescription("missing-file", target.getSourceURI());

            ModifiableSource source = this.getSource(target);
            OutputStream stream = null;
            PrintStream out = null;
            try {
                stream = source.getOutputStream();
                out = new PrintStream(stream);
                Notifier.notify(n, out, "text/html");
            } finally {
                if (out != null) out.close();
                if (stream != null) stream.close();
                this.releaseSource(source);
            }
        }
    }

    public ModifiableSource getSource(Target target)
        throws IOException, ProcessingException {
        final String finalDestinationURI = target.getDestinationURI();
        Source src = this.sourceResolver.resolveURI(finalDestinationURI);
        if (!(src instanceof ModifiableSource)) {
            this.sourceResolver.release(src);
            throw new ProcessingException(
                "Source is not Modifiable: " + finalDestinationURI);
        }
        return (ModifiableSource) src;
    }

    public long getLastModified(Target target) throws IOException, ProcessingException {
        Source src = this.getSource(target);
        long lastModified = src.getLastModified();
        this.releaseSource(src);
        return lastModified;
    }

    public void releaseSource(Source source) {
        this.sourceResolver.release(source);
    }
    private boolean isIncluded(String uri) {
        boolean included;
        Iterator i;
        HashMap map = new HashMap();

        if (this.includePatterns.size() == 0) {
            included = true;
        } else {
            included = false;
            i = this.includePatterns.iterator();
            while (i.hasNext()){
                final String pattern = (String)i.next();
                if (WildcardMatcherHelper.match(pattern, uri) != null ) {
                    included=true;
                    break;
                }
            }
        }
        if (this.excludePatterns.size() != 0) {
            i = this.excludePatterns.iterator();
            while (i.hasNext()) {
                final String pattern = (String)i.next();
                if (WildcardMatcherHelper.match(pattern, uri) != null ) {
                    included=false;
                    break;
                }
            }
        }
        return included;
    }
    private boolean isCrawlablePage(Target target) {
        if (this.includeLinkExtensions == null) {
            return true;
        } else {
            return this.includeLinkExtensions.contains(target.getExtension());
        }
    }

    /* NB. This is a temporary solution - it may well be replaced by storing the checksum info
     *     in the XML 'report' file, along with details of what pages were created, etc.
     */
    private void readChecksumFile() throws Exception {
        this.checksums = new HashMap();
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader reader = null;
        try {
            Source checksumSource = this.sourceResolver.resolveURI(this.checksumsURI);
            is = checksumSource.getInputStream();
            isr = new InputStreamReader(is);
            reader = new BufferedReader(isr);
            String line;
            int lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.trim().startsWith("#") || line.trim().length() == 0) {
                    continue;
                }
                if (line.indexOf("\t") == -1) {
                    throw new ProcessingException("Missing tab at line " + lineNo + " of " + this.checksumsURI);
                }
                String filename = line.substring(0, line.indexOf("\t"));
                String checksum = line.substring(line.indexOf("\t") + 1);
                this.checksums.put(filename, checksum);
            }
            reader.close();
        } catch (SourceNotFoundException e) {
            // return leaving checksums map empty
        } finally {
            if (reader != null) reader.close();
            if (isr != null) isr.close();
            if (is != null) is.close();
        }
    }

    private void writeChecksumFile() throws Exception {
        Source checksumSource = this.sourceResolver.resolveURI(this.checksumsURI);
        if (!(checksumSource instanceof ModifiableSource)) {
            throw new ProcessingException("Checksum file is not Modifiable:" + checksumSource);
        }
        ModifiableSource source = (ModifiableSource) checksumSource;
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(source.getOutputStream()));
        Iterator i = this.checksums.keySet().iterator();
        while (i.hasNext()){
            String key = (String) i.next();
            String checksum = (String) this.checksums.get(key);
            writer.println(key + "\t" + checksum);
        }
        writer.close();
    }

    private boolean isSameContent(DelayedOutputStream stream, Target target) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(stream.getContent());
            String streamDigest = SourceUtil.encodeBASE64(new String(md5.digest()));
            String targetDigest = (String)this.checksums.get(target.getSourceURI());

            if (streamDigest.equals(targetDigest)) {
                return true;
            } else {
                this.checksums.put(target.getSourceURI(), streamDigest);
                return false;
            }
        } catch (NoSuchAlgorithmException e) {
            // or do something:
            return false;
        }
    }
    /**
     * Print a description of the software before running
     */
    public static String getProlog() {
        String lSep = SystemUtils.LINE_SEPARATOR;
        StringBuffer msg = new StringBuffer();
        msg.append("------------------------------------------------------------------------ ").append(lSep);
        msg.append(Constants.NAME).append(" ").append(Constants.VERSION).append(lSep);
        msg.append("Copyright (c) ").append(Constants.YEAR).append(" Apache Software Foundation. All rights reserved.").append(lSep);
        msg.append("------------------------------------------------------------------------ ").append(lSep).append(lSep);
        return msg.toString();
    }
}
