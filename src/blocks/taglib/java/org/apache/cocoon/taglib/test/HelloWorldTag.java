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

import org.apache.cocoon.taglib.XMLProducerTagSupport;
import org.apache.cocoon.taglib.TagSupport;
import org.apache.cocoon.taglib.i18n.LocaleTag;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @version CVS $Id: HelloWorldTag.java,v 1.3 2004/03/05 13:02:25 bdelacretaz Exp $
 */
public class HelloWorldTag extends XMLProducerTagSupport {

    private static char[] charArrayEN = "Hello World".toCharArray();
    private static char[] charArrayDE = "Hallo Welt".toCharArray();

    /*
     * @see Tag#doStartTag(String, String, String, Attributes)
     */
    public int doStartTag(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        LocaleTag localeTag = (LocaleTag) TagSupport.findAncestorWithClass(this, LocaleTag.class);
        if (localeTag == null) {
            this.xmlConsumer.characters(charArrayEN, 0, charArrayEN.length);
        } else {
            String language = localeTag.getLocale().getLanguage();
            if ("de".equals(language))
                this.xmlConsumer.characters(charArrayDE, 0, charArrayDE.length);
            else
                this.xmlConsumer.characters(charArrayEN, 0, charArrayEN.length);
        }
        return EVAL_BODY;
    }

}
