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
package org.apache.cocoon.components.source.impl;


import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;

import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.impl.AbstractSource;

import org.apache.cocoon.components.modules.input.InputModule;

import org.apache.commons.jxpath.JXPathContext;


/**
 * A <code>Source</code> that takes its content from a
 * module.
 * <p>The URI syntax is
 * "module:<input-module>:<attribute-name>[#XPath]",
 * where :
 * <ul>
 * <li>an input-module name is used for finding an input-module for reading data from</li>,
 * <li>"attribute-name" is the name of the attribute found in the module</li>,
 * <li>"XPath" is an XPath that is aplied on the object in the
 * attribute, by using JXPath.</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:danielf@nada.kth.se">Daniel Fagerstom</a>
 */

public class ModuleSource
    extends AbstractSource {

    private final static String SCHEME = "module";
    private String attributeType;
    private String attributeName;
    private String xPath;
    private ServiceManager manager;
    private Map objectModel;
    private Logger logger;
    
    /**
     * Create a module source from a 'module:' uri and a the object model.
     * <p>The uri is of the form "module:attribute-type:attribute-name#xpath</p>
     */
    public ModuleSource( Map objectModel, String uri,
                         ServiceManager manager, Logger logger )
        throws MalformedURLException {

        this.objectModel = objectModel;
        this.manager = manager;
        this.logger = logger;

        setSystemId( uri );

        // Scheme
        int start = 0;
        int end = uri.indexOf( ':' );
        if ( end == -1 )
            throw new MalformedURLException("Malformed uri for module source (cannot find scheme) : " + uri);

        String scheme = uri.substring( start, end );
        if ( !SCHEME.equals( scheme ) )
            throw new MalformedURLException("Malformed uri for a module source : " + uri);

        setScheme( scheme );

        // Attribute type
        start = end + 1;
        end = uri.indexOf( ':', start );
        if ( end == -1 ) {
            throw new MalformedURLException("Malformed uri for module source (cannot find attribute type) : " + uri);
        }
        this.attributeType = uri.substring( start, end );

        // Attribute name
        start = end + 1;
        end = uri.indexOf( '#', start );

        if ( end == -1 )
            end = uri.length();

        if ( end == start )
            throw new MalformedURLException("Malformed uri for module source (cannot find attribute name) : " + uri);

        this.attributeName = uri.substring( start, end );

        // xpath
        start = end + 1;
        this.xPath = start < uri.length() ? uri.substring( start ) : "";
    }
    
    /**
     * Return an <code>InputStream</code> object to read from the source.
     *
     * @throws IOException if I/O error occured.
     */
    public InputStream getInputStream() throws IOException, SourceException {
        if ( this.logger.isDebugEnabled() ) {
            this.logger.debug( "Getting InputStream for " + getURI() );
        }

        Object obj = getInputAttribute( this.attributeType, this.attributeName );
        if ( obj == null )
            throw new SourceException( " The attribute: " + this.attributeName +
                                       " is empty" );

        if ( !(this.xPath.length() == 0 || this.xPath.equals( "/" )) ) {
            JXPathContext context = JXPathContext.newContext( obj );
            obj = context.getValue( this.xPath );

            if ( obj == null )
                throw new SourceException( "the xpath: " + this.xPath +
                                           " applied on the attribute: " +
                                           this.attributeName +
                                           " returns null");
        }

        if ( obj instanceof InputStream ) {
            return (InputStream)obj;
        } else if ( obj instanceof String ) {
            return new ByteArrayInputStream( ((String)obj).getBytes() );
        } else {
            throw new SourceException( "The object type: " + obj.getClass() +
                                       " could not be serialized as a InputStream " + obj );
        }
    }

    /**
     * Does this source actually exist ?
     *
     * @return true if the resource exists.
     *
     */
    public boolean exists() {
        boolean exists = false;
        try {
            exists = getInputAttribute( this.attributeType, this.attributeName ) != null;
        } catch ( SourceException e ) {
            exists = false;
        }
        return exists;
    }

    private Object getInputAttribute( String inputModuleName, String attributeName )
        throws  SourceException {
        Object obj;
        ServiceSelector selector = null;
        InputModule inputModule = null;
        try {
            selector = (ServiceSelector) this.manager.lookup( InputModule.ROLE + "Selector" );
            inputModule = (InputModule) selector.select( inputModuleName );
            obj = inputModule.getAttribute( attributeName, null, this.objectModel );

        } catch ( ServiceException e ) {
            throw new SourceException( "Could not find an InputModule of the type " + 
                                       inputModuleName , e );
        } catch ( ConfigurationException e ) {
            throw new SourceException( "Could not find an attribute: " + attributeName +
                                       " from the InputModule " + inputModuleName, e );
        } finally {
            if ( inputModule != null ) selector.release( inputModule );
            this.manager.release( selector );
        }

        return obj;
    }
}
