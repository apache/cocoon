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

import org.apache.cocoon.environment.commandline.FileSavingEnvironment;
import org.apache.cocoon.environment.commandline.LinkSamplingEnvironment;
import org.apache.cocoon.environment.commandline.CommandLineContext;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;
import java.io.File;
import java.io.OutputStream;
import java.io.IOException;

/**
 * ProcessorBean does XYZ
 *
 * @author <a href="bloritsch.at.apache.org">Berin Loritsch</a>
 * @version CVS $ Revision: 1.1 $
 */
public class ProcessorBean extends CocoonBean {
    
    protected static final String DEFAULT_USER_AGENT = Constants.COMPLETE_NAME;
    protected static final String DEFAULT_ACCEPT = "text/html, */*";
    private String m_userAgent = DEFAULT_USER_AGENT;
    private String m_accept = DEFAULT_ACCEPT;
    private Map m_attributes = new HashMap();
    private HashMap m_empty = new HashMap();
    private CommandLineContext m_cliContext;

    public void initialize() throws Exception {
        super.initialize();

        m_cliContext = new CommandLineContext( getContextURI() );
        m_cliContext.enableLogging( getInitializationLogger() );
    }

    /**
     * Samples an URI for its links.
     *
     * @param deparameterizedURI a <code>String</code> value of an URI to start sampling from
     * @param parameters a <code>Map</code> value containing request parameters
     * @return a <code>Collection</code> of links
     * @exception Exception if an error occurs
     */
    protected Collection getLinks( String deparameterizedURI, Map parameters )
            throws Exception {

        parameters.put( "user-agent", m_userAgent );
        parameters.put( "accept", m_accept );

        LinkSamplingEnvironment env =
                new LinkSamplingEnvironment( deparameterizedURI, new File(getContextURI()), m_attributes,
                        parameters, m_cliContext, getInitializationLogger() );
        processLenient( env );
        return env.getLinks();
    }

    /**
     * Processes an URI for its content.
     *
     * @param deparameterizedURI a <code>String</code> value of an URI to start sampling from
     * @param parameters a <code>Map</code> value containing request parameters
     * @param links a <code>Map</code> value
     * @param stream an <code>OutputStream</code> to write the content to
     * @return a <code>String</code> value for the content
     * @exception Exception if an error occurs
     */
    protected int getPage(
            String deparameterizedURI,
            long lastModified,
            Map parameters,
            Map links,
            List gatheredLinks,
            OutputStream stream )
            throws Exception {

        parameters.put( "user-agent", m_userAgent );
        parameters.put( "accept", m_accept );

        FileSavingEnvironment env =
                new FileSavingEnvironment( deparameterizedURI, lastModified, new File(getContextURI()),
                        m_attributes, parameters, links,
                        gatheredLinks, m_cliContext, stream, getInitializationLogger() );

        // Here Cocoon can throw an exception if there are errors in processing the page
        getRootProcessor().process( env );

        // if we get here, the page was created :-)
        int status = env.getStatus();
        if ( !env.isModified() ) {
            status = -1;
        }
        return status;
    }

    /** Class <code>NullOutputStream</code> here. */
    static class NullOutputStream extends OutputStream {
        public void write( int b ) throws IOException {}

        public void write( byte b[] ) throws IOException {}

        public void write( byte b[], int off, int len ) throws IOException {}
    }

    /**
     * Analyze the type of content for an URI.
     *
     * @param deparameterizedURI a <code>String</code> value to analyze
     * @param parameters a <code>Map</code> value for the request
     * @return a <code>String</code> value denoting the type of content
     * @exception Exception if an error occurs
     */
    protected String getType( String deparameterizedURI, Map parameters )
    throws Exception {

        parameters.put( "user-agent", m_userAgent );
        parameters.put( "accept", m_accept );

        FileSavingEnvironment env =
                new FileSavingEnvironment( deparameterizedURI, new File(getContextURI()), m_attributes,
                        parameters, m_empty, null, m_cliContext,
                        new OldCocoonWrapper.NullOutputStream(), getInitializationLogger() );
        processLenient( env );
        return env.getContentType();
    }

    /**
     * Try to process something but don't throw a ProcessingException.
     *
     * @param env the <code>Environment</code> to process
     * @return boolean true if no error were cast, false otherwise
     * @exception Exception if an error occurs, except RNFE
     */
    private boolean processLenient( Environment env ) throws Exception {
        try {
            getRootProcessor().process( env );
        } catch ( ProcessingException pe ) {
            return false;
        }
        return true;
    }
}
