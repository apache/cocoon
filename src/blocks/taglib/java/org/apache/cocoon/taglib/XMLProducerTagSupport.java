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
package org.apache.cocoon.taglib;

import org.apache.cocoon.xml.XMLConsumer;

/**
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @version CVS $Id: XMLProducerTagSupport.java,v 1.3 2004/03/05 13:02:24 bdelacretaz Exp $
 */
public abstract class XMLProducerTagSupport extends TagSupport implements XMLProducerTag {
    
    protected XMLConsumer xmlConsumer;

    /*
     * @see XMLProducer#setConsumer(XMLConsumer)
     */
    public void setConsumer(XMLConsumer xmlConsumer) {
        this.xmlConsumer = xmlConsumer;
    }
    
    /*
     * @see Recyclable#recycle()
     */
    public void recycle() {
        xmlConsumer = null;
        super.recycle();
    }
}
