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
package org.apache.cocoon.taglib.test;

import org.apache.cocoon.taglib.IterationTag;
import org.apache.cocoon.taglib.TagSupport;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @version CVS $Id: IterationTestTag.java,v 1.3 2004/03/05 13:02:25 bdelacretaz Exp $
 */
public class IterationTestTag extends TagSupport implements IterationTag {

    private int i = 0;
    private int count = 2;
    
    /*
     * @see Tag#doStartTag(String, String, String, Attributes)
     */
    public int doStartTag(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        if (count > 0)
        	return EVAL_BODY;
        return SKIP_BODY;
    }
    
    /*
     * @see IterationTag#doAfterBody()
     */
    public int doAfterBody() throws SAXException {
        if (++i >= count)
            return SKIP_BODY;
        return EVAL_BODY_AGAIN;
    }
    
    public void setCount(String count) {
        this.count = Integer.parseInt(count);
    }

    /*
     * @see Recyclable#recycle()
     */
    public void recycle() {
        i = 0;
        count = 2;
        super.recycle();
    }
}
