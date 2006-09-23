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
package org.apache.cocoon.forms.formmodel.algorithms;

import java.util.StringTokenizer;

import org.apache.cocoon.forms.formmodel.CalculatedFieldAlgorithm;
import org.apache.cocoon.forms.formmodel.CalculatedFieldAlgorithmBuilder;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.forms.util.JavaScriptHelper;
import org.mozilla.javascript.Function;
import org.w3c.dom.Element;

/**
 * Javascript based calculated field algorithm builder.
 *
 * <p>With this algorithm the user can implement it's own algorithm directly in
 * javascript and directly inside the form definition.</p>
 *
 * <p>The syntax is as follows:
 * <pre>
 *   &lt;fd:value type="javascript" triggers="items,price,discount"&gt;
 *     var price = parent.lookupWidget('price').getValue();
 *     var items = parent.lookupWidget('items').getValue();
 *     var discount = parent.lookupWidget('discount').getValue();
 *
 *     if (discount == 'completelyfree') return 0;
 *     var total = price * items;
 *     if (discount == 'halfprice') return total / 2;
 *       return total;
 *   &lt;/fd:value&gt;
 * </pre>
 * </p>
 *
 * <p>From inside the javascript function the following objects are accessible:
 *   <dl>
 *     <dt>form</dt>
 *     <dd>The form object.</dd>
 *     <dt>parent</dt>
 *     <dd>The parent widget. This is very useful in repeaters, since the parent is the repeater row.</dd>
 *     <dt>cocoon and other FOM objects</dt>
 *     <dd>This are accessible only when flowscript is in use (see bug COCOON-1804)</dd>
 *   </dl>
 * </p>
 *
 * <p>As you can see, the function must return the calculated value, and not set this
 * directly in the widget. This way the value can be converted correctly if needed.</p>
 * 
 * @version $Id$
 */
public class JavaScriptBuilder implements CalculatedFieldAlgorithmBuilder {

    public CalculatedFieldAlgorithm build(Element algorithmElement) throws Exception {
        JavaScript ret = new JavaScript();

        String fields = DomHelper.getAttribute(algorithmElement, "triggers");
        StringTokenizer stok = new StringTokenizer(fields, ", ");
        while (stok.hasMoreTokens()) {
            String fname = stok.nextToken();
            ret.addTriggerWidget(fname);
        }

        Function func = JavaScriptHelper.buildFunction(algorithmElement, "calculate", new String[]{"form", "parent"});
        ret.setJsfunction(func);

        return ret;
    }

}
