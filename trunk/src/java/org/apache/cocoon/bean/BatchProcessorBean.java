/*
 * 
 * ============================================================================
 * The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *  1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  3. The end-user documentation included with the redistribution, if any,
 * must include the following acknowledgment: "This product includes software
 * developed by the Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself, if and
 * wherever such third-party acknowledgments normally appear.
 *  4. The names "Jakarta", "Avalon", "Excalibur" and "Apache Software
 * Foundation" must not be used to endorse or promote products derived from
 * this software without prior written permission. For written permission,
 * please contact apache@apache.org.
 *  5. Products derived from this software may not be called "Apache", nor may
 * "Apache" appear in their name, without prior written permission of the
 * Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation. For more information on the
 * Apache Software Foundation, please see <http://www.apache.org/> .
 *  
 */
package org.apache.cocoon.bean;

import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.notification.DefaultNotifyingBuilder;
import org.apache.cocoon.components.notification.SimpleNotifyingBean;
import org.apache.cocoon.components.notification.Notifying;
import org.apache.cocoon.components.notification.Notifier;
import org.apache.cocoon.matching.helpers.WildcardHelper;
import org.apache.cocoon.bean.helpers.Crawler;
import org.apache.cocoon.bean.helpers.DelayedOutputStream;
import org.apache.excalibur.source.*;

import java.util.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * BatchProcessorBean does XYZ
 * 
 * @author <a href="bloritsch.at.apache.org">Berin Loritsch</a>
 * @version CVS $ Revision: 1.1 $
 */
public class BatchProcessorBean extends ProcessorBean {

    private boolean m_linkFollowed = true;
    private boolean m_precompileOnly = false;
    private boolean m_extensionConfirmed = true;
    private String m_defaultFilename = Constants.INDEX_URI;
    private boolean m_brokenLinkGenerated = false;
    private String m_brokenLinkExtension = "";
    private List m_excludePatterns = new ArrayList();
    private List m_includePatterns = new ArrayList();
    private List m_includeLinkExtensions = null;

    // Internal Objects
    private List m_listeners = new ArrayList();

    private Crawler m_crawler;
    private String m_checksumsURI = null;
    private Map m_checksums;
    private SourceResolver m_sourceResolver;

    private String m_logPriority;

    public BatchProcessorBean() {
        m_crawler = new Crawler();
    }

    public boolean isLinkFollowed() {
        return m_linkFollowed;
    }

    public void setLinkFollowed(boolean linkFollowed) {
        m_linkFollowed = linkFollowed;
    }

    public String getLogPriority() {
        return m_logPriority;
    }

    public void setLogPriority(String logPriority) {
        m_logPriority = logPriority;
    }

    public boolean isPrecompileOnly() {
        return m_precompileOnly;
    }

    public void setPrecompileOnly(boolean precompileOnly) {
        m_precompileOnly = precompileOnly;
    }

    public boolean isExtensionConfirmed() {
        return m_extensionConfirmed;
    }

    public void setExtensionConfirmed(boolean extensionConfirmed) {
        m_extensionConfirmed = extensionConfirmed;
    }

    public String getDefaultFilename() {
        return m_defaultFilename;
    }

    public void setDefaultFilename(String defaultFilename) {
        m_defaultFilename = defaultFilename;
    }

    public boolean isBrokenLinkGenerated() {
        return m_brokenLinkGenerated;
    }

    public void setBrokenLinkGenerated(boolean brokenLinkGenerated) {
        m_brokenLinkGenerated = brokenLinkGenerated;
    }

    public String getBrokenLinkExtension() {
        return m_brokenLinkExtension;
    }

    public void setBrokenLinkExtension(String brokenLinkExtension) {
        m_brokenLinkExtension = brokenLinkExtension;
    }

    public List getIncludeLinkExtensions() {
        return m_includeLinkExtensions;
    }

    public void setIncludeLinkExtensions(List includeLinkExtensions) {
        m_includeLinkExtensions = includeLinkExtensions;
    }

    public String getChecksumsURI() {
        return m_checksumsURI;
    }

    public void setChecksumsURI(String checksumsURI) {
        m_checksumsURI = checksumsURI;
    }

    public void initialize() throws Exception {
        super.initialize();
        if (m_crawler.getRemainingCount() == 0 && !isPrecompileOnly()) {
            String error = "Please, specify at least one starting URI.";
            getInitializationLogger().fatalError(error);
            throw new ProcessingException(error);
        }

        m_sourceResolver =
            (SourceResolver) getServiceManager().lookup(SourceResolver.ROLE);
    }

    /**
     * Adds a target for processing
     * 
     * @param type
     *            Type of target - append, replace, insert.
     * @param root
     * @param sourceURI
     *            URI of the starting page
     * @param destURI
     *            URI specifying destination for the generated pages.
     * @throws IllegalArgumentException
     *             if destURI is missing
     */
    public void addTarget(
        String type,
        String root,
        String sourceURI,
        String destURI)
    throws IllegalArgumentException {
        Target target = new Target(type, root, sourceURI, destURI);
        addTargetToCrawler(target);
    }

    private void addTargetToCrawler(Target target) {
        target.setDefaultFilename(m_defaultFilename);
        target.setFollowLinks(m_linkFollowed);
        target.setConfirmExtension(m_extensionConfirmed);
        target.setLogger(m_logPriority);
        m_crawler.addTarget(target);
    }

    public void addTarget(String type, String sourceURI, String destURI)
    throws IllegalArgumentException {
        Target target = new Target(type, sourceURI, destURI);
        addTargetToCrawler(target);
    }

    public void addTarget(String sourceURI, String destURI)
    throws IllegalArgumentException {
        Target target = new Target(sourceURI, destURI);
        addTargetToCrawler(target);
    }

    public void addTargets(List uris, String destURI)
    throws IllegalArgumentException {
        Iterator i = uris.iterator();
        while (i.hasNext()) {
            Target target = new Target((String) i.next(), destURI);
            addTargetToCrawler(target);
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
        target.setDefaultFilename(m_defaultFilename);
        target.setFollowLinks(followLinks);
        target.setConfirmExtension(confirmExtension);
        target.setLogger(logger);
        m_crawler.addTarget(target);
    }

    public void addExcludePattern(String pattern) {
        int preparedPattern[] = WildcardHelper.compilePattern(pattern);
        m_excludePatterns.add(preparedPattern);
    }

    public void addIncludePattern(String pattern) {
        int preparedPattern[] = WildcardHelper.compilePattern(pattern);
        m_includePatterns.add(preparedPattern);
    }

    public void addIncludeLinkExtension(String extension) {
        if (m_includeLinkExtensions == null) {
            m_includeLinkExtensions = new ArrayList();
        }
        m_includeLinkExtensions.add(extension);
    }

    public void addListener(BeanListener listener) {
        m_listeners.add(listener);
    }

    public void pageGenerated(
        String sourceURI,
        String destURI,
        int pageSize,
        int linksInPage,
        int newLinksInPage,
        int pagesRemaining,
        int pagesComplete,
        long timeTaken) {
        Iterator i = m_listeners.iterator();
        while (i.hasNext()) {
            BeanListener l = (BeanListener) i.next();
            l.pageGenerated(
                sourceURI,
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
        Iterator i = m_listeners.iterator();
        while (i.hasNext()) {
            BeanListener l = (BeanListener) i.next();
            l.messageGenerated(msg);
        }
    }

    public void sendWarning(String uri, String warning) {
        Iterator i = m_listeners.iterator();
        while (i.hasNext()) {
            BeanListener l = (BeanListener) i.next();
            l.warningGenerated(uri, warning);
        }
    }

    public void sendBrokenLinkWarning(String uri, String warning) {
        Iterator i = m_listeners.iterator();
        while (i.hasNext()) {
            BeanListener l = (BeanListener) i.next();
            l.brokenLinkFound(uri, "", warning, null);
        }
    }

    public void pageSkipped(String uri, String message) {
        Iterator i = m_listeners.iterator();
        while (i.hasNext()) {
            BeanListener l = (BeanListener) i.next();
            l.pageSkipped(uri, message);
        }
    }

    public void dispose() {
        if ( m_sourceResolver != null ) {
            getServiceManager().release(m_sourceResolver);
            m_sourceResolver = null;
        }
        super.dispose();
    }

    /**
     * Process the URI list and process them all independently.
     * 
     * @exception Exception
     *                if an error occurs
     */
    public void process() throws Exception {

        if (m_sourceResolver == null) {
            this.initialize();
        }

        readChecksumFile();

        if (m_crawler.getRemainingCount() == 0) {
            precompile();
        } else {
            Iterator iterator = m_crawler.iterator();
            while (iterator.hasNext()) {
                Target target = (Target) iterator.next();
                if (isPrecompileOnly()) {
                    processXSP(target.getSourceURI());
                } else {
                    processTarget(m_crawler, target);
                }
            }
        }

        writeChecksumFile();

        if (getInitializationLogger().isInfoEnabled()) {
            getInitializationLogger().info(
                "  Memory used: "
                    + (Runtime.getRuntime().totalMemory()
                        - Runtime.getRuntime().freeMemory()));
            getInitializationLogger().info(
                "  Processed, Translated & Left: "
                    + m_crawler.getProcessedCount()
                    + ", "
                    + m_crawler.getTranslatedCount()
                    + ", "
                    + m_crawler.getRemainingCount());
        }
    }

    /**
     * Processes the given Target and return all links.
     * 
     * If links are to be followed, and extensions checked then the algorithm
     * is as follows:
     * <ul>
     * <li>file name for the URI is generated. URI MIME type is checked for
     * consistency with the URI and, if the extension is inconsistent or
     * absent, the file name is changed</li>
     * <li>the link view of the given URI is called and the file names for
     * linked resources are generated and stored.</li>
     * <li>for each link, absolute file name is translated to relative path.
     * </li>
     * <li>after the complete list of links is translated, the
     * link-translating view of the resource is called to obtain a
     * link-translated version of the resource with the given link map</li>
     * <li>list of absolute URI is returned, for every URI which is not yet
     * present in list of all translated URIs</li>
     * </ul>
     * 
     * If links are to be followed, but extensions are not checked, then the
     * algorithm will be:
     * <ul>
     * <li>The content for the page is generated</li>
     * <li>Whilst generating, all links are gathered by the LinkGatherer</li>
     * <li>Gathered links are added to the unprocessed links list, and
     * processing continues until all processing is complete</li>
     * </ul>
     * 
     * @param target
     *            a <code>Target</code> target to process
     * @exception Exception
     *                if an error occurs
     */
    private void processTarget(Crawler crawler, Target target)
    throws Exception {

        int status = 0;

        int linkCount = 0;
        int newLinkCount = 0;
        int pageSize = 0;
        long startTimeMillis = System.currentTimeMillis();

        if (target.confirmExtensions()) {
            if (!crawler.hasTranslatedLink(target)) {
                final String mimeType =
                    getType(
                        target.getDeparameterizedSourceURI(),
                        target.getParameters());
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
        if (target.followLinks()
            && target.confirmExtensions()
            && isCrawlablePage(target)) {
            final Iterator i =
                getLinks(
                    target.getDeparameterizedSourceURI(),
                    target.getParameters())
                    .iterator();

            while (i.hasNext()) {
                String linkURI = (String) i.next();
                Target linkTarget = target.getDerivedTarget(linkURI);

                if (linkTarget == null) {
                    pageSkipped(
                        linkURI,
                        "link does not share same root as parent");
                    continue;
                }

                if (!isIncluded(linkTarget.getSourceURI())) {
                    pageSkipped(
                        linkTarget.getSourceURI(),
                        "matched include/exclude rules");
                    continue;
                }

                if (!crawler.hasTranslatedLink(linkTarget)) {
                    try {
                        final String mimeType =
                            getType(
                                linkTarget.getDeparameterizedSourceURI(),
                                linkTarget.getParameters());
                        linkTarget.setMimeType(mimeType);
                        crawler.addTranslatedLink(linkTarget);
                        getInitializationLogger().info(
                            "  Link translated: " + linkTarget.getSourceURI());
                        if (crawler.addTarget(linkTarget)) {
                            newLinkCount++;
                        }
                    } catch (ProcessingException pe) {
                        this.sendBrokenLinkWarning(
                            linkTarget.getSourceURI(),
                            pe.getMessage());
                        if (isBrokenLinkGenerated()) {
                            if (crawler.addTarget(linkTarget)) {
                                newLinkCount++;
                            }
                        }
                    }
                }

                translatedLinks.put(
                    linkTarget.getOriginalSourceURI(),
                    linkTarget.getTranslatedURI(target.getPath()));
            }

            linkCount = translatedLinks.size();
        }

        try {
            // Process URI
            DelayedOutputStream output = new DelayedOutputStream();
            try {
                List gatheredLinks;
                if (!target.confirmExtensions()
                    && target.followLinks()
                    && isCrawlablePage(target)) {
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
                    for (Iterator it = gatheredLinks.iterator();
                        it.hasNext();
                        ) {
                        String linkURI = (String) it.next();
                        Target linkTarget = target.getDerivedTarget(linkURI);

                        if (linkTarget == null) {
                            pageSkipped(
                                linkURI,
                                "link does not share same root as parent");
                            continue;
                        }

                        if (!isIncluded(linkTarget.getSourceURI())) {
                            pageSkipped(
                                linkTarget.getSourceURI(),
                                "matched include/exclude rules");
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
                this.sendBrokenLinkWarning(
                    target.getSourceURI(),
                    DefaultNotifyingBuilder.getRootCause(pe).getMessage());
            } finally {
                if (output != null && status != -1) {

                    ModifiableSource source = getSource(target);
                    try {
                        pageSize = output.size();

                        if (m_checksumsURI == null
                            || !isSameContent(output, target)) {
                            OutputStream stream = source.getOutputStream();
                            output.setFileOutputStream(stream);
                            output.flush();
                            output.close();
                            pageGenerated(
                                target.getSourceURI(),
                                target.getAuthlessDestURI(),
                                pageSize,
                                linkCount,
                                newLinkCount,
                                crawler.getRemainingCount(),
                                crawler.getProcessedCount(),
                                System.currentTimeMillis() - startTimeMillis);
                        } else {
                            output.close();
                            pageSkipped(
                                target.getSourceURI(),
                                "Page not changed");
                        }
                    } catch (IOException ioex) {
                        getInitializationLogger().warn(ioex.toString());
                    } finally {
                        releaseSource(source);
                    }
                }
            }
        } catch (Exception rnfe) {
            getInitializationLogger().warn(
                "Could not process URI: " + target.getSourceURI());
            rnfe.printStackTrace();
            this.sendBrokenLinkWarning(
                target.getSourceURI(),
                "URI not found: " + rnfe.getMessage());
        }
    }

    /**
     * Generate a <code>resourceUnavailable</code> message.
     * 
     * @param target
     *            being unavailable
     * @exception IOException
     *                if an error occurs
     */
    private void resourceUnavailable(Target target)
    throws IOException, ProcessingException {
        if (isBrokenLinkGenerated()) {
            //Why decode this URI now?
            //String brokenFile = NetUtils.decodePath(destinationURI);

            if (m_brokenLinkExtension != null) {
                target.setExtraExtension(m_brokenLinkExtension);
            }
            SimpleNotifyingBean n = new SimpleNotifyingBean(this);
            n.setType("resource-not-found");
            n.setTitle("Resource not Found");
            n.setSource("Cocoon commandline (Main.java)");
            n.setMessage("Page Not Available.");
            n.setDescription("The requested resource couldn't be found.");
            n.addExtraDescription(
                Notifying.EXTRA_REQUESTURI,
                target.getSourceURI());
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
        Source src = m_sourceResolver.resolveURI(finalDestinationURI);
        if (!(src instanceof ModifiableSource)) {
            m_sourceResolver.release(src);
            throw new ProcessingException(
                "Source is not Modifiable: " + finalDestinationURI);
        }
        return (ModifiableSource) src;
    }

    public long getLastModified(Target target)
    throws IOException, ProcessingException {
        Source src = getSource(target);
        long lastModified = src.getLastModified();
        this.releaseSource(src);
        return lastModified;
    }

    public void releaseSource(Source source) {
        m_sourceResolver.release(source);
    }

    private boolean isIncluded(String uri) {
        boolean included;
        Iterator i;
        HashMap map = new HashMap();

        if (m_includePatterns.size() == 0) {
            included = true;
        } else {
            included = false;
            i = m_includePatterns.iterator();
            while (i.hasNext()) {
                int pattern[] = (int[]) i.next();
                if (WildcardHelper.match(map, uri, pattern)) {
                    included = true;
                    break;
                }
            }
        }
        if (m_excludePatterns.size() != 0) {
            i = m_excludePatterns.iterator();
            while (i.hasNext()) {
                int pattern[] = (int[]) i.next();
                if (WildcardHelper.match(map, uri, pattern)) {
                    included = false;
                    break;
                }
            }
        }
        return included;
    }

    private boolean isCrawlablePage(Target target) {
        if (m_includeLinkExtensions == null) {
            return true;
        } else {
            return m_includeLinkExtensions.contains(target.getExtension());
        }
    }

    /*
     * NB. This is a temporary solution - it may well be replaced by storing
     * the checksum info in the XML 'report' file, along with details of what
     * pages were created, etc.
     */
    private void readChecksumFile() throws Exception {
        m_checksums = new HashMap();

        try {
            Source checksumSource = m_sourceResolver.resolveURI(m_checksumsURI);
            BufferedReader reader =
                new BufferedReader(
                    new InputStreamReader(checksumSource.getInputStream()));
            String line;
            int lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.trim().startsWith("#") || line.trim().length() == 0) {
                    continue;
                }
                if (line.indexOf("\t") == -1) {
                    throw new ProcessingException(
                        "Missing tab at line "
                            + lineNo
                            + " of "
                            + m_checksumsURI);
                }
                String filename = line.substring(0, line.indexOf("\t"));
                String checksum = line.substring(line.indexOf("\t") + 1);
                m_checksums.put(filename, checksum);
            }
            reader.close();
        } catch (SourceNotFoundException e) {
            // return leaving checksums map m_empty
        }
    }

    private void writeChecksumFile() throws Exception {
        Source checksumSource = m_sourceResolver.resolveURI(m_checksumsURI);
        if (!(checksumSource instanceof ModifiableSource)) {
            throw new ProcessingException(
                "Checksum file is not Modifiable:" + checksumSource);
        }
        ModifiableSource source = (ModifiableSource) checksumSource;
        PrintWriter writer =
            new PrintWriter(new OutputStreamWriter(source.getOutputStream()));
        Iterator i = m_checksums.keySet().iterator();
        while (i.hasNext()) {
            String key = (String) i.next();
            String checksum = (String) m_checksums.get(key);
            writer.println(key + "\t" + checksum);
        }
        writer.close();
    }

    private boolean isSameContent(DelayedOutputStream stream, Target target) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(stream.getContent());
            String streamDigest =
                SourceUtil.encodeBASE64(new String(md5.digest()));
            String targetDigest =
                (String) m_checksums.get(target.getSourceURI());

            if (streamDigest.equals(targetDigest)) {
                return true;
            } else {
                m_checksums.put(target.getSourceURI(), streamDigest);
                return false;
            }
        } catch (NoSuchAlgorithmException e) {
            // or do something:
            return false;
        }
    }

}
