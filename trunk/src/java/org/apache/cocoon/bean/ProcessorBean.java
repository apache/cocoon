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

 4. The names "Jakarta", "Avalon", "Excalibur" and "Apache Software Foundation"
    must not be used to endorse or promote products derived from this  software
    without  prior written permission. For written permission, please contact
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
 on  behalf of the Apache Software  Foundation. For more  information on the
 Apache Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.bean;

import org.apache.cocoon.environment.commandline.FileSavingEnvironment;
import org.apache.cocoon.environment.commandline.LinkSamplingEnvironment;
import org.apache.cocoon.environment.commandline.CommandLineContext;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.util.IOUtils;

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
     * Allow subclasses to recursively precompile XSPs.
     */
    protected void precompile() {
        recursivelyPrecompile( new File(getContextURI()), new File(getContextURI()) );
    }

    /**
     * Recurse the directory hierarchy and process the XSP's.
     * @param contextDir a <code>File</code> value for the context directory
     * @param file a <code>File</code> value for a single XSP file or a directory to scan recursively
     */
    private void recursivelyPrecompile( File contextDir, File file ) {
        if ( file.isDirectory() ) {
            String entries[] = file.list();
            for ( int i = 0; i < entries.length; i++ ) {
                recursivelyPrecompile( contextDir, new File( file, entries[i] ) );
            }
        } else if ( file.getName().toLowerCase().endsWith( ".xmap" ) ) {
            try {
                this.processXMAP( IOUtils.getContextFilePath( contextDir.getCanonicalPath(), file.getCanonicalPath() ) );
            } catch ( Exception e ) {
                //Ignore for now.
            }
        } else if ( file.getName().toLowerCase().endsWith( ".xsp" ) ) {
            try {
                this.processXSP( IOUtils.getContextFilePath( contextDir.getCanonicalPath(), file.getCanonicalPath() ) );
            } catch ( Exception e ) {
                //Ignore for now.
            }
        }
    }

    /**
     * Process a single XSP file
     *
     * @param uri a <code>String</code> pointing to an xsp URI
     * @exception Exception if an error occurs
     */
    protected void processXSP( String uri ) throws Exception {
        String markupLanguage = "xsp";
        String programmingLanguage = "java";
        Environment env = new LinkSamplingEnvironment( "/", new File( getContextURI() ), m_attributes,
                null, m_cliContext, getInitializationLogger() );
        getRootProcessor().precompile( uri, env, markupLanguage, programmingLanguage );
    }

    /**
     * Process a single XMAP file
     *
     * @param uri a <code>String</code> pointing to an xmap URI
     * @exception Exception if an error occurs
     */
    protected void processXMAP( String uri ) throws Exception {
        String markupLanguage = "sitemap";
        String programmingLanguage = "java";
        Environment env = new LinkSamplingEnvironment( "/", new File(getContextURI()), m_attributes,
                null, m_cliContext, getInitializationLogger() );
        getRootProcessor().precompile( uri, env, markupLanguage, programmingLanguage );
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
