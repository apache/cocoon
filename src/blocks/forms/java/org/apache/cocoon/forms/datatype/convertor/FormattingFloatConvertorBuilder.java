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
package org.apache.cocoon.forms.datatype.convertor;

import org.w3c.dom.Element;
import org.apache.cocoon.forms.Constants;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.i18n.I18nUtils;

import java.util.Locale;

/**
 * Builds {@link FormattingFloatConvertor}s.
 *
 * @version CVS $Id: FormattingFloatConvertorBuilder.java,v 1.3 2004/03/18 11:44:59 bruno Exp $
 */
public class FormattingFloatConvertorBuilder extends FormattingDecimalConvertorBuilder {
    protected FormattingDecimalConvertor createConvertor() {
        return new FormattingFloatConvertor();
    }
}
