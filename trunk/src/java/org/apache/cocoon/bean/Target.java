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
package org.apache.cocoon.bean;

import java.util.TreeMap;

import org.apache.cocoon.Constants;
import org.apache.cocoon.util.MIMEUtils;
import org.apache.cocoon.util.NetUtils;
import org.apache.cocoon.ProcessingException;

/**
 * A Target is a single page for generation. It encapsulates the URI 
 * arithmetic required to transform the URI of the page to be generated 
 * (the source URI) into the URI to which the resulting page should be 
 * written (the destination URI).
 *
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a>
 * @version CVS $Id: Target.java,v 1.10 2004/03/08 13:57:38 cziegeler Exp $
 */
public class Target {
    // Defult type is append
    private static final String APPEND_TYPE = "append";
    private static final String REPLACE_TYPE = "replace";
    private static final String INSERT_TYPE = "insert";

    private final String type;
    private final String root;
    private final String sourceURI;
    private final String destURI;
    private final String deparameterizedSourceURI;
    private final TreeMap parameters;
    
    private String parentURI = null;
    private String originalURI = null;
    private String mimeType = null; 
    private String defaultFilename = Constants.INDEX_URI;
    private String finalDestinationURI = null;
    private String extension = null;    

    private boolean followLinks;
    private boolean confirmExtension;
    private String logger;
                 
    private transient int _hashCode;
    private transient String _toString;

    public Target(String type,
                  String root,
                  String sourceURI,
                  String destURI)
    throws IllegalArgumentException {
        this.type = type;
        this.root = root;
        
        if (destURI == null || destURI.length() == 0) {
            throw new IllegalArgumentException("You must specify a destination directory when defining a target");
        }
        if (!destURI.endsWith("/")) {
            destURI += "/";
        }
        this.destURI = destURI;
        
        this.parameters = new TreeMap();
        sourceURI = NetUtils.normalize(root + sourceURI);
        this.deparameterizedSourceURI = NetUtils.deparameterize(sourceURI, this.parameters);
        this.sourceURI = NetUtils.parameterize(this.deparameterizedSourceURI, this.parameters);
    }

    public Target(String type, String sourceURI, String destURI)
        throws IllegalArgumentException {
        this(type, "", sourceURI, destURI);
    }

    public Target(String sourceURI, String destURI)
        throws IllegalArgumentException {
        this(APPEND_TYPE, "", sourceURI, destURI);
    }

    public Target getDerivedTarget(String originalLinkURI)
        throws IllegalArgumentException {

        String linkURI = originalLinkURI;
        // Fix relative links starting with "?"
        if (linkURI.startsWith("?")) {
            linkURI = this.getPageURI() + linkURI;
        }
        linkURI =
            NetUtils.normalize(NetUtils.absolutize(this.getPath(), linkURI));

        // Ignore pages outside the root folder
        if (!linkURI.startsWith(this.root)) {
            return null;
        }
        linkURI = linkURI.substring(root.length());
        
        Target target = new Target(this.type, this.root, linkURI, this.destURI);
        target.setOriginalURI(originalLinkURI);
        target.setParentURI(this.sourceURI);
        target.setConfirmExtension(this.confirmExtension);
        target.setFollowLinks(this.followLinks);
        target.setLogger(this.logger);
        return target;
    }

    /**
     * Sets the original URI. This is used to record the URI that
     * caused the creation of this Target, for example as a link
     * in another page. It is needed for doing link translation, as
     * this is the URI that must be replaced by the translated one.
     */
    public void setOriginalURI(String uri) {
        this.originalURI = uri;
    }
    
    /**
     * Sets the URI of the page that contained the link to this 
     * URI. Used for reporting purposes.
     */
    public void setParentURI(String uri) {
        this.parentURI = uri;
    }
    
    /**
     * Sets the mime type for the resource referenced by this target.
     * If a mime type is specified, the file extension of the 
     * destination URI will be checked to see that it matches the
     * default extension for the specified mime type. If it doesn't,
     * the default extension will be appended to the destination URI.
     *
     * This URI change will be taken into account in pages that link
     * to the current page.
     * 
     * If the mime type is not specified (and thus null), no extension
     * checking will take place. 
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
        this.finalDestinationURI = null;
    }
    
    /**
     * Sets a file extension to be appended to the end of the destination
     * URI. The main use of this is to create broken link error files that
     * stand out, within the file structure of the generated site, by, for
     * example, adding '.error' to the end of the filename.
     */
    public void setExtraExtension(String extension) {
        this.extension = extension;
        this.finalDestinationURI = null;
    }
    /**
     * Sets the default filename. This filename is appended to URIs
     * that refer to a directory, i.e. end with a slash, as resources
     * referred to by such a URI cannot be written to a file system
     * without a filename. 
     *
     * This URI change will be taken into account in pages that link
     * to the current page.
     * 
     * If no default is specified, the Cocoon constants value will 
     * be used.
     */
    public void setDefaultFilename(String filename) {
        this.defaultFilename = filename;
    }
    
    /**
     * Gets the filename from the source URI, without the path.
     * This is used to fill out relative URIs that have
     * parameters but no filename such as ?page=123
     */
    public String getPageURI() {
        String pageURI = this.getSourceURI();
        if (pageURI.indexOf("/") != -1) {
            pageURI = pageURI.substring(pageURI.lastIndexOf("/") + 1);
            if (pageURI.length() == 0) {
                pageURI = "./";
            }
        }
        return pageURI;
    }

    /**
     * Gets the path from the source URI, without the filename. 
     * This is used when absolutizing/relativizing link URIs.
     */
    public String getPath() {
        return NetUtils.getPath(this.getSourceURI());
    }

    /**
     * Gets the file extension for the source URI
     */
    public String getExtension() {
        return NetUtils.getExtension(this.getSourceURI());
    }
    
    /** 
     * Gets the parent URI (the URI of the page that contained
     * a link to this URI). null is returned if this page was
     * not referred to in a link.
     */
    public String getParentURI() {
        return this.parentURI;
    }
    
    /**
     * Calculates the destination URI - the URI to which the generated
     * page should be written. This will be a URI that, when resolved
     * by a SourceResolver, will return a modifiableSource.
     * 
     * This calculation is only done once per target. It is therefore
     * necessary to ensure that the mime type has been set (if required)
     * before this method is called.
     */
    public String getDestinationURI()
        throws ProcessingException {
        
        if (this.finalDestinationURI == null) {
            
            String actualSourceURI = this.sourceURI;
            if (!actualSourceURI.startsWith(root)) {
                throw new ProcessingException(
                    "Derived target does not share same root: "
                        + actualSourceURI);
            }
            actualSourceURI = actualSourceURI.substring(root.length());
            actualSourceURI = mangle(actualSourceURI);
            
            String destinationURI;
            if (APPEND_TYPE.equals(this.type)) {
                destinationURI = destURI + actualSourceURI;
            } else if (REPLACE_TYPE.equals(this.type)) {
                destinationURI = destURI;
            } else if (INSERT_TYPE.equals(this.type)) {
                int starPos = destURI.indexOf("*");
                if (starPos == -1) {
                    throw new ProcessingException("Missing * in replace mapper uri");
                } else if (starPos == destURI.length() - 1) {
                    destinationURI = destURI.substring(0, starPos) + actualSourceURI;
                } else {
                    destinationURI = destURI.substring(0, starPos)
                        + actualSourceURI
                        + destURI.substring(starPos + 1);
                }
            } else {
                throw new ProcessingException(
                    "Unknown mapper type: " + this.type);
            }
            if (mimeType != null) {
                final String ext = NetUtils.getExtension(destinationURI);
                final String defaultExt = MIMEUtils.getDefaultExtension(mimeType);
                if (defaultExt != null) {
                    if ((ext == null) || (!ext.equals(defaultExt))) {
                        destinationURI += defaultExt;
                    }
                }
            }
            if (this.extension != null) {
                destinationURI += this.extension; 
            }
            this.finalDestinationURI = destinationURI;
        }
        return this.finalDestinationURI;
    }

    /**
     * Gets a translated version of a link, ready for insertion
     * into another page as a link. This link needs to be
     * relative to the original page.
     */
    public String getTranslatedURI(String path)
        throws ProcessingException {
                    
        String actualSourceURI = this.sourceURI;
        if (!actualSourceURI.startsWith(root)) {
            return actualSourceURI;
        }
        actualSourceURI = mangle(actualSourceURI);
        
        if (mimeType != null) {
            final String ext = NetUtils.getExtension(actualSourceURI);
            final String defaultExt = MIMEUtils.getDefaultExtension(mimeType);
            if (defaultExt != null) {
                if ((ext == null) || (!ext.equals(defaultExt))) {
                    actualSourceURI += defaultExt;
                }
            }
        }
        return NetUtils.relativize(path, actualSourceURI);
    }

    /**
     * 
     * @return destination URI after all authentication details have been
     *         removed
     */
    public String getAuthlessDestURI() throws ProcessingException {
        return NetUtils.removeAuthorisation(this.getDestinationURI());
    }
    
    /**
     * Gets the original URI used to create this Target.
     * This URI is completely unprocessed.
     */
    public String getOriginalSourceURI() {
        return this.originalURI;
    }

    /**
     * Gets the source URI for this target, after
     * the URI has been 'prepared' by normalisation,
     * absolutization and deparameterization followed
     * by reparameterization. This final step is to 
     * ensure that all parameters appear in a consistent
     * order. For example page?a=1&b=2 and page?b=2&a=1
     * should be considered the same resource, and thus
     * have the same sourceURI. 
     */
    public String getSourceURI() {
        return this.sourceURI;
    }
    /**
     * Gets the source URI for this target, with 
     * parameters removed. This is the URI that is 
     * to be passed to Cocoon in order to generate 
     * the page.
     */
    public String getDeparameterizedSourceURI() {
        return this.deparameterizedSourceURI;
    }

    /**
     * Gets the parameters that have been removed from
     * the URI. These need to be passed to Cocoon when
     * generating a page.
     */
    public TreeMap getParameters() {
        return this.parameters;
    }

    /**
     * Mangle a URI.
     *
     * @param uri a URI to mangle
     * @return a mangled URI
     */
    private String mangle(String uri) {
        if (uri.length()==0 || uri.charAt(uri.length() - 1) == '/') {
            uri += defaultFilename;
        }
        uri = uri.replace('"', '\'');
        uri = uri.replace('?', '_');
        uri = uri.replace(':', '_');

        return uri;
    }

    public boolean equals(Object o) {
        return (o instanceof Target) && o.toString().equals(toString());
    }

    public int hashCode() {
        if (_hashCode == 0) {
            return _hashCode = toString().hashCode();
        }
        return _hashCode;
    }

    public String toString() {
        if (_toString == null) {
            return _toString =
                "<"
                    + type
                    + "|"
                    + root
                    + "|"
                    + sourceURI
                    + "|"
                    + destURI
                    + ">";
        }
        return _toString;
    }
    /**
     * @return boolean
     */
    public boolean confirmExtensions() {
        return confirmExtension;
    }

    public boolean followLinks() {
        return followLinks;
    }

    public String getLogger() {
        return logger;
    }

    public void setConfirmExtension(boolean b) {
        confirmExtension = b;
    }

    public void setFollowLinks(boolean b) {
        followLinks = b;
    }

    public void setLogger(String string) {
        logger = string;
    }
}