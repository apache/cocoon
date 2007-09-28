/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.forms.generation;

import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.xml.AbstractXMLPipe;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A filter for selection lists, that keeps only those items that start with a given filter value.
 *
 * @since 2.1.8
 * @version $Id$
 */
public class SelectionListFilter extends AbstractXMLPipe {
    
    private ContentHandler next;
    private int filterDepth = 0;
    private int depth = 0;
    private String filterValue;
    private static final ContentHandler NULL_HANDLER = new DefaultHandler();

    public SelectionListFilter(String filterValue, ContentHandler next) {
        this.next = next;
        this.setContentHandler(next);
        this.filterValue = filterValue;
    }
    
    public void startElement(String uri, String loc, String raw, Attributes a) throws SAXException {
        depth++;

        if (uri.equals(FormsConstants.INSTANCE_NS) && loc.equals("item")) {
            String value = a.getValue("value");
            if (!value.startsWith(this.filterValue)) {
                filterDepth = depth;
                setContentHandler(NULL_HANDLER);
            }
        }

        super.startElement(uri, loc, raw, a);
    }
    
    public void endElement(String uri, String loc, String raw) throws SAXException {
        super.endElement(uri, loc, raw);
        
        if (depth == filterDepth) {
            filterDepth = 0;
            setContentHandler(this.next);
        }

        depth--;
    }
}
