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

package org.apache.cocoon.transformation;

import java.util.HashMap;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.SitemapComponentTestCase;

/**
 *
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels </a>
 * @version CVS $Id: PatternTransformerTestCase.java,v 1.5 2004/03/05 13:01:48 bdelacretaz Exp $
 */
public class PatternTransformerTestCase extends SitemapComponentTestCase {

    public PatternTransformerTestCase(String name) {
        super(name);
    }

    public void testPatternTransformer() throws Exception {

        String src = "resource://org/apache/cocoon/transformation/patterntest-lexicon1.xml";
        Parameters parameters = new Parameters();
        String input = "resource://org/apache/cocoon/transformation/patterntest-input1.xml";
        String result = "resource://org/apache/cocoon/transformation/patterntest-result1.xml";

        assertEqual(load(result), transform("pattern", src, parameters, load(input)));
    }
}
