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
package org.apache.butterfly.generation;

import org.apache.butterfly.source.Source;
import org.apache.butterfly.source.SourceResolver;
import org.apache.butterfly.xml.Parser;
import org.apache.butterfly.xml.XMLConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Description of FileGenerator.
 * 
 * @version CVS $Id: FileGenerator.java,v 1.2 2004/07/25 21:55:20 ugo Exp $
 */
public class FileGenerator implements Generator {
    private Source inputSource;
    private SourceResolver sourceResolver;
    private Parser parser;
    protected static final Log logger = LogFactory.getLog(FileGenerator.class);
    
    public void setParser(Parser parser) {
        this.parser = parser;
    }
    
    public void setInputSource(String source) {
        this.inputSource = sourceResolver.resolveURI(source);
    }
    
    /**
     * @param sourceResolver The sourceResolver to set.
     */
    public void setSourceResolver(SourceResolver sourceResolver) {
        this.sourceResolver = sourceResolver;
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.generation.Generator#setContentHandler(org.xml.sax.ContentHandler)
     */
    public void setConsumer(XMLConsumer consumer) {
        parser.setContentHandler(consumer);
    }
    
    public void generate() {
        parser.parse(this.inputSource);
    }

}
