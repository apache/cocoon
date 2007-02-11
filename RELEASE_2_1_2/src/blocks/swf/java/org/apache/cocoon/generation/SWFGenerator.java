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

package org.apache.cocoon.generation;

import de.tivano.flash.swf.parser.SWFReader;
import de.tivano.flash.swf.parser.SWFVerboseDefineFont2Reader;
import de.tivano.flash.swf.parser.SWFVerboseDefineFontReader;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.SourceException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Map;

/**
 * uses the project http://developer.berlios.de/projects/spark-xml/
 *
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @version CVS $Id: SWFGenerator.java,v 1.3 2003/09/05 07:40:20 cziegeler Exp $
 */
public class SWFGenerator extends FileGenerator {
    
    private boolean pVerbose = false;
    private SWFReader parser;

    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);
        parser = new SWFReader();
        if (pVerbose) {
            parser.registerTagReader(48, new SWFVerboseDefineFont2Reader());
            parser.registerTagReader(10, new SWFVerboseDefineFontReader());
        }
    }

    public void generate()
    throws IOException, SAXException, ProcessingException {

        try {
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("processing file " + super.source);
                this.getLogger().debug("file resolved to " + this.inputSource.getURI());
            }

            this.parser.setContentHandler(super.xmlConsumer);
            this.parser.parse(new InputSource(this.inputSource.getInputStream()));

        } catch (SourceException e) {
            throw new ProcessingException("Could not read resource "
                                              + this.inputSource.getURI(), e);
        } catch (SAXException e) {
            final Exception cause = e.getException();
            if( cause != null ) {
                this.getLogger().debug("Got SAXException; Rethrowing cause exception", e);
                if ( cause instanceof ProcessingException )
                    throw (ProcessingException)cause;
                if ( cause instanceof IOException )
                    throw (IOException)cause;
                if ( cause instanceof SAXException )
                    throw (SAXException)cause;
                throw new ProcessingException("Could not read resource "
                                              + this.inputSource.getURI(), cause);
            }
            throw e;
        }
    }

}
