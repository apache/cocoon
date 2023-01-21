/*
 * Copyright 2004 Outerthought bvba and Schaubroeck nv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.forms.datatype.convertor;

import org.apache.cocoon.forms.datatype.convertor.Convertor;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.forms.CacheManager;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.Source;
import org.w3c.dom.Element;
import org.outerj.daisy.htmlcleaner.HtmlCleanerTemplate;
import org.outerj.daisy.htmlcleaner.HtmlCleanerFactory;
import org.xml.sax.InputSource;

/**
 * Builds {@link HtmlCleaningConvertor}s.
 */
public class HtmlCleaningConvertorBuilder implements StringConvertorBuilder {

    private CacheManager cacheManager;
    private SourceResolver sourceResolver;
    
    public Convertor build(Element element) throws Exception {
        String config = DomHelper.getAttribute(element, "config");

        Source source = null;
        try {
            source = sourceResolver.resolveURI(config);

            String prefix = HtmlCleanerTemplate.class.getName();
            HtmlCleanerTemplate template = (HtmlCleanerTemplate)cacheManager.get(source, prefix);
            if (template == null) {
                HtmlCleanerFactory factory = new HtmlCleanerFactory();
                InputSource is = SourceUtil.getInputSource(source);
                template = factory.buildTemplate(is);
                cacheManager.set(template, source, prefix);
            }

            return new HtmlCleaningConvertor(template);
        } finally {
            if (source != null)
                sourceResolver.release(source);
        }
    }
    public void setCacheManager( CacheManager cacheManager )
    {
        this.cacheManager = cacheManager;
    }
    public void setSourceResolver( SourceResolver sourceResolver )
    {
        this.sourceResolver = sourceResolver;
    }
}
