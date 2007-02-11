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
 * @version CVS $Id: SWFGenerator.java,v 1.4 2004/03/05 13:02:24 bdelacretaz Exp $
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
