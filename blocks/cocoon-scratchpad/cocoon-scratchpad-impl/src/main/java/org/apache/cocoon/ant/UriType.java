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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.cocoon.util.NetUtils;

/**
 *   Encapsulate URI arithmetic.
 * A simple class for encapsultating URI computations,
 * avoiding to use extensive String operation spread
 * over all classes.
 *
 * @version $Id$
 */
public class UriType {
    private String uri;
    private Map parameters;
    private String deparameterizedUri;
    private String sUri;
    private String mangledUri;
    private String path;

    /**
     * optionally set iff content type of a uri is determinable
     */
    private String contentType;
    /**
     * optionally set iff Uri has some destination file associated with
     */
    private File destFile;

    /**
     * optionally set iff links has been calculated for this Uri
     */
    private Set links;


    /**
     * Constructor for the UriType object
     *
     * @param  uri  String representation of a Uri
     */
    public UriType(String uri) {
        // normalize uri
        final String normalizedUri = NetUtils.normalize(uri);
        this.uri = normalizedUri;
        init();
    }


    /**
     * Constructor for the UriType object
     *
     * @param  parent  Parent context of a Uri
     * @param  uri     String representation of a Uri
     */
    public UriType(UriType parent, String uri) {
        if (parent != null) {
            // absolutize it relative to parent
            final String parentPath = parent.getPath();
            final String absolutizedUri = NetUtils.absolutize(parentPath, uri);
            // normalize
            this.uri = NetUtils.normalize(absolutizedUri);
        } else {
            // normalize
            final String normalizedUri = NetUtils.normalize(uri);
            this.uri = normalizedUri;
        }
        init();
    }


    /**
     *   Sets the destFile attribute of the UriType object
     *
     * @param  destFile   The new destFile value
     */
    public void setDestFile(File destFile) {
        this.destFile = destFile;
    }


    /**
     *   Sets the contentType attribute of the UriType object
     *
     * @param  contentType  The new contentType value
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }


    /**
     *   Gets the destFile attribute of the UriType object
     *
     * @return     The destFile value
     */
    public File getDestFile() {
        return this.destFile;
    }


    /**
     *   Gets the contentType attribute of the UriType object
     *
     * @return    The contentType value
     */
    public String getContentType() {
        return this.contentType;
    }


    /**
     *   Gets the links attribute of the UriType object
     *
     * @return    The links value
     */
    public Collection getLinks() {
        return this.links;
    }


    /**
     * Constructor for the getURI object
     *
     * @return    The uRI value
     */
    public String getUri() {
        return uri;
    }


    /**
     * Gets the parameters attribute of the UriType object
     *
     * @return    The parameters value
     */
    public Map getParameters() {
        return parameters;
    }


    /**
     * Gets the deparameterizedURI attribute of the UriType object
     *
     * @return    The deparameterizedURI value
     */
    public String getDeparameterizedUri() {
        return this.deparameterizedUri;
    }


    /**
     * Gets the sURI attribute of the UriType object
     *
     * @return    The sURI value
     */
    public String getSUri() {
        return this.sUri;
    }


    /**
     * Gets the mangledURI attribute of the UriType object
     *
     * @return    The mangledURI value
     */
    public String getMangledUri() {
        return this.mangledUri;
    }


    /**
     * Gets the path attribute of the UriType object
     *
     * @return    The path value
     */
    public String getPath() {
        return this.path;
    }


    /**
     * Gets the filename attribute of the UriType object
     *
     * @return    The filename value
     */
    public String getFilename() {
        return this.mangledUri;
    }


    /**
     * Gets the extension attribute of the UriType object
     *
     * @return    The extension value
     */
    public String getExtension() {
        final String filename = getFilename();
        return NetUtils.getExtension(filename);
    }


    /**
     * Gets the parameterizedURI attribute of the UriType object
     *
     * @param  addOriginalParameters    Description of Parameter
     * @param  addAdditionalParameters  Description of Parameter
     * @param  additionalParameters     Description of Parameter
     * @return                          The parameterizedURI value
     */
    public String getParameterizedUri(boolean addOriginalParameters,
                                      boolean addAdditionalParameters,
                                      Map additionalParameters) {
        Map mergedParameters = new HashMap();
        if (addOriginalParameters) {
            mergedParameters.putAll(parameters);
        }
        if (addAdditionalParameters && additionalParameters != null) {
            mergedParameters.putAll(additionalParameters);
        }

        final String parameterizedUri = NetUtils.parameterize(deparameterizedUri, mergedParameters);
        return parameterizedUri;
    }


    /**
     *   Gets the mergedParameterizedURI attribute of the UriType object
     *
     * @param  additionalParameters  Description of Parameter
     * @return                       The mergedParameterizedURI value
     */
    public String getMergedParameterizedUri(Map additionalParameters) {
        return getParameterizedUri(false, true, additionalParameters);
    }


    /**
     *   Gets the originalParameterizedURI attribute of the UriType object
     *
     * @return    The originalParameterizedURI value
     */
    public String getOriginalParameterizedUri() {
        return getParameterizedUri(true, false, null);
    }


    /**
     *   Adds a feature to the Link attribute of the UriType object
     *
     * @param  uriType  The feature to be added to the Link attribute
     */
    public void addLink(UriType uriType) {
        if (links == null) {
            links = new HashSet();
        }
        this.links.add(uriType);
    }


    /**
     *   Compute equality of Uri objects.
     *   Two uri objects are equal iff non-null, and
     *   uri member is non null, and uri member are equal.
     *
     * @param  o  Checked against this for equality
     * @return    boolean true if URIType objects are equal else false
     */
    public boolean equals(Object o) {
        if (o != null && o instanceof UriType) {
            UriType uriType = (UriType) o;
            if (uriType.uri != null && this.uri != null) {
                return uriType.uri.equals(this.uri);
            }
        }
        return false;
    }


    /**
     *   Compute hash code of this object
     *
     * @return    HashCode of uri member
     */
    public int hashCode() {
        return this.uri.hashCode();
    }


    /**
     * Mangle a URI.
     *
     * @param  uri  the Uri value
     * @return      mangled Uri
     */
    protected String mangledUri(String uri) {
        uri = uri.replace('"', '\'');
        uri = uri.replace('?', '_');
        uri = uri.replace(':', '_');

        return uri;
    }


    /**
     * Calculate all member values depending on the uri member value
     */
    protected void init() {
        if (this.uri != null) {
            this.parameters = new HashMap();
            this.deparameterizedUri = NetUtils.deparameterize(this.uri, this.parameters);
            this.sUri = NetUtils.parameterize(this.deparameterizedUri, this.parameters);
            this.mangledUri = mangledUri(this.sUri);
            this.path = NetUtils.getPath(this.uri);
            
            if (this.path.length() == 0) {
                this.path = "/";
            }
        }
    }
}

