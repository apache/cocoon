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
package org.apache.butterfly.source;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;


/**
 * Description of SourceResolver.
 * 
 * @version CVS $Id: SourceResolver.java,v 1.2 2004/07/25 21:55:20 ugo Exp $
 */
public class SourceResolver {
    private Map factories;
    URL baseURL;
    
    public SourceResolver() {
        try {
            // TODO: contextualize it
            baseURL = new File(System.getProperty("user.dir")).toURL();
        } catch (MalformedURLException e) {
            throw new SourceException(e);
        }
    }
    
    /**
     * @param factories The factories to set.
     */
    public void setFactories(Map factories) {
        this.factories = factories;
    }
    
    /**
     * Get a <code>Source</code> object.
     * @throws org.apache.excalibur.source.SourceNotFoundException if the source cannot be found
     */
    public Source resolveURI(String location) {
        return this.resolveURI(location, null, null);
    }

    /**
     * Get a <code>Source</code> object.
     */
    public Source resolveURI(String location,
                             String baseURI,
                             Map parameters)
    {
        if (null != baseURI && SourceUtil.indexOfSchemeColon(baseURI) == -1) {
            throw new SourceException("BaseURI is not valid, it must contain a protocol: " + baseURI);
        }

        if( baseURI == null ) baseURI = baseURL.toExternalForm();

        String systemID = location;
        // special handling for windows file paths
        if( location.length() > 1 && location.charAt( 1 ) == ':' )
            systemID = "file:/" + location;
        else if( location.length() > 2 && location.charAt(0) == '/' && location.charAt(2) == ':' )
            systemID = "file:" + location;

        // determine protocol (scheme): first try to get the one of the systemID, if that fails, take the one of the baseURI
        String protocol;
        int protocolPos = SourceUtil.indexOfSchemeColon(systemID);
        if( protocolPos != -1 )
        {
            protocol = systemID.substring( 0, protocolPos );
        }
        else
        {
            protocolPos = SourceUtil.indexOfSchemeColon(baseURI);
            if( protocolPos != -1 )
                protocol = baseURI.substring( 0, protocolPos );
            else
                protocol = "*";
        }

        Source source = null;
        // search for a SourceFactory implementing the protocol
        SourceFactory factory = (SourceFactory) factories.get(protocol);
        if (factory == null) {
            factory = (SourceFactory) factories.get("*");
            if (factory == null) {
                throw new SourceException("Unable to select source factory for " + systemID);
            }
            systemID = absolutize(factory, baseURI, systemID);
            return factory.getSource(systemID, parameters);
        }
        systemID = absolutize(factory, baseURI, systemID);
        return factory.getSource(systemID, parameters);
    }

    /**
     * Makes an absolute URI based on a baseURI and a relative URI.
     */
    private String absolutize( SourceFactory factory, String baseURI, String systemID )
    {
        if( factory instanceof URIAbsolutizer )
            systemID = ((URIAbsolutizer) factory).absolutize(baseURI, systemID);
        else
            systemID = SourceUtil.absolutize(baseURI, systemID);
        return systemID;
    }
}
