/*-- $Id: LotusXSLProcessor.java,v 1.3 1999-11-09 02:30:51 dirkx Exp $ -- 

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
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
 
 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
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
package org.apache.cocoon.processor.xslt;

import java.io.*;
import java.net.*;
import java.util.*;
import org.w3c.dom.*;
import com.lotus.xml.*;
import com.lotus.xsl.*;
import org.apache.cocoon.*;
import org.apache.cocoon.parser.*;
import org.apache.cocoon.processor.*;
import org.apache.cocoon.framework.*;

/**
 * This class implements the processor interface for the Lotus XSL processor.
 *
 * <p>NOTE: some of this code was taken from the internals of the LotusXSL
 * processors to allow bugfixing and/or loose coupling on versions</p>
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.3 $ $Date: 1999-11-09 02:30:51 $
 */

public class LotusXSLProcessor extends AbstractXSLTProcessor {

    XSLProcessor styler;

    public void init(Director director) {
        super.init(director);
        this.styler = new XSLProcessor(new XMLParser(this.parser));
    }

    public Document process(Document document, Dictionary parameters) throws Exception {
        try {
            return styler.process(document, getStylesheet(document, parameters), null);
        } catch (PINotFoundException e) {
            return document;
        }
    }

    public String getStatus() {
        return "Lotus XSLT Processor";
    }

    class XMLParser extends XMLParserLiaisonDefault {
        Parser parser;

        public XMLParser(Parser parser) {
            this.parser = parser;
        }

        public Document createDocument() {
            return this.parser.createEmptyDocument();
        }

        public boolean isIgnorableWhitespace(Text text) {
            return false;
        }

        public Document parseXMLStream(Reader in, String filename) {
            try {
                return this.parser.parse(in, null);
            } catch (IOException e) {
                return null;
            }
        }

        public Document parseXMLStream(URL url) {
            try {
                return parser.parse(new InputStreamReader(url.openStream()), null);
            } catch (IOException e) {
                return null;
            }
        }
    }
}