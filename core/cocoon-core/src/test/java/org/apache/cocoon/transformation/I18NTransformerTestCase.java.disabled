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

package org.apache.cocoon.transformation;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.SitemapComponentTestCase;

/**
 * A simple testcase for I18nTransformer.
 *
 * @version $Id$
 */
public class I18NTransformerTestCase extends SitemapComponentTestCase {
    
    /** Testcase for i18n
     *
     * @throws Exception if ServiceManager enterEnvironment fails
     */
    public void testI18n1() throws Exception {
        getLogger().debug("testI18n1");
        
        Parameters parameters = new Parameters();
        parameters.setParameter( "support-caching", "false" );
        
        String input = "resource://org/apache/cocoon/transformation/i18n-input-1.xml";
        String result = "resource://org/apache/cocoon/transformation/i18n-result-1.xml";
        String src =  null;
        
        assertEqual( load(result),
        transform("i18n", src, parameters, load(input)));
    }
    
    /** Testcase for i18n
     *
     * @throws Exception if ServiceManager enterEnvironment fails
     */
    public void testI18n2() throws Exception {
        getLogger().debug("testI18n2");
        
        Parameters parameters = new Parameters();
        parameters.setParameter( "support-caching", "false" );
        
        String input = "resource://org/apache/cocoon/transformation/i18n-input-2.xml";
        String result = "resource://org/apache/cocoon/transformation/i18n-result-2.xml";
        String src =  null;
        
        assertEqual( load(result),
        transform("i18n", src, parameters, load(input)));
    }
}
