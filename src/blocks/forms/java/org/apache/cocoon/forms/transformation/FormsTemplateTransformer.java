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
package org.apache.cocoon.forms.transformation;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.transformation.Transformer;

import org.apache.avalon.framework.parameters.Parameters;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Map;

/**
 * See description of {@link WidgetReplacingPipe}.
 * 
 * @version CVS $Id: FormsTemplateTransformer.java,v 1.1 2004/03/09 10:34:13 reinhard Exp $
 */
public class FormsTemplateTransformer extends EffectWidgetReplacingPipe implements Transformer {

    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters parameters)
    throws ProcessingException, SAXException, IOException {

        FormsPipelineConfig pipeContext = FormsPipelineConfig.createConfig(objectModel, parameters);
        super.init(null, pipeContext);
    }
}
