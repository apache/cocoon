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
        throws MalformedURLException, SourceException {

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
