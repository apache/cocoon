/*
 * Copyright 2004, Ugo Cei.
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.butterfly.transformation;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;

import org.apache.butterfly.source.Source;
import org.apache.butterfly.source.SourceResolver;
import org.apache.butterfly.xml.AbstractXMLPipe;
import org.apache.butterfly.xml.XMLConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Description of TraxTransformer.
 * 
 * @version CVS $Id: TraxTransformer.java,v 1.1 2004/07/23 08:47:20 ugo Exp $
 */
public class TraxTransformer extends AbstractXMLPipe implements Transformer {    
    private Source inputSource;
    protected static final Log logger = LogFactory.getLog(TraxTransformer.class);
    private SourceResolver sourceResolver;
    private TransformerHandler transformerHandler;
    
    /**
     * @param handler
     * @param resolver
     */
    public TraxTransformer(TransformerHandler handler, SourceResolver resolver) {
        this.transformerHandler = handler;
        this.sourceResolver = resolver;
    }

    public void setInputSource(String source) throws MalformedURLException, IOException {
        this.inputSource = sourceResolver.resolveURI(source);
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.xml.XMLProducer#setConsumer(org.apache.butterfly.xml.XMLConsumer)
     */
    public void setConsumer(XMLConsumer consumer) {
        setContentHandler(this.transformerHandler);
        setLexicalHandler(this.transformerHandler);
        // According to TrAX specs, all TransformerHandlers are LexicalHandlers
        final SAXResult result = new SAXResult(consumer);
        result.setLexicalHandler(consumer);
        this.transformerHandler.setResult(result);
    }
}
