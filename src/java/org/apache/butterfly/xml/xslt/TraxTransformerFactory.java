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
package org.apache.butterfly.xml.xslt;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.apache.butterfly.source.SourceResolver;
import org.apache.butterfly.transformation.TraxTransformer;


/**
 * Description of TraxTransformerFactory.
 * 
 * @version CVS $Id: TraxTransformerFactory.java,v 1.1 2004/07/23 08:47:20 ugo Exp $
 */
public class TraxTransformerFactory {
    private SourceResolver sourceResolver;
    private SAXTransformerFactory transformerFactory;
    
    /**
     * 
     */
    public TraxTransformerFactory() {
        this.transformerFactory = (SAXTransformerFactory) TransformerFactory.newInstance();
    }
    
    /**
     * @param sourceResolver The sourceResolver to set.
     */
    public void setSourceResolver(SourceResolver sourceResolver) {
        this.sourceResolver = sourceResolver;
    }

    public TraxTransformer getTransformer(String src) {
        Source source = new StreamSource(sourceResolver.resolveURI(src).getInputStream());
        synchronized (this.transformerFactory) {
            try {
                Templates stylesheet = this.transformerFactory.newTemplates(source);
                // TODO: cache templates?
                return new TraxTransformer(transformerFactory.newTransformerHandler(stylesheet),
                        this.sourceResolver);
            } catch (TransformerConfigurationException e) {
                // TODO log?
                throw new TraxException("Cannot create a new XSLT template.", e);
            }
        }
    }
    
    public TransformerHandler getTransformerHandler() {
        synchronized (this.transformerFactory) {
            try {
                return this.transformerFactory.newTransformerHandler();
            } catch (TransformerConfigurationException e) {
                // TODO log?
                throw new TraxException("Cannot create a new TransformerHandler.", e);
            }
        }        
    }
}
