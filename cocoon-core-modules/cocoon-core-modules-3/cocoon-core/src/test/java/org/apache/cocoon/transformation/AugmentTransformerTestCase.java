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
import org.w3c.dom.Document;


/**
 * A simple testcase for AugmentTransformer.
 *
 * @version $Id$
 */
public class AugmentTransformerTestCase extends SitemapComponentTestCase {
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.SitemapComponentTestCase#getSitemapComponentInfo()
     */
    protected String[] getSitemapComponentInfo() {
        return new String[] {Transformer.class.getName(),
                             AugmentTransformer.class.getName(),
                             "augment"};
    }

    /** Testcase for augment transformation
     *
     * @throws Exception if ServiceManager enterEnvironment fails
     */
    public void testAugment1() throws Exception {
        getLogger().debug("testAugment1");
        
        Parameters parameters = new Parameters();
        parameters.setParameter( "mount", "portal1/sect1/" );
        
        String input = "resource://org/apache/cocoon/transformation/augment-input-1.xml";
        String result = "resource://org/apache/cocoon/transformation/augment-result-1.xml";
        String src =  null;
        
        Document resultDocument = load(result);
        Document inputDocument = load(input);
        Document transformDocument = transform("augment", src, parameters, inputDocument );
        
        printDocs( resultDocument, inputDocument, transformDocument );
        
        assertIdentical( resultDocument, transformDocument );
    }
    
    /**
     * print documents to System.out
     *
     * @param resultDocument the expected result document
     * @param inputDocument the input document
     * @param transformDocument  the transformed input document
     */
    protected void printDocs( Document resultDocument, Document inputDocument, Document transformDocument ) {
        System.out.println( "resultDocument" );
        this.print( resultDocument );
        System.out.println( "inputDocument" );
        this.print( inputDocument );
        System.out.println( "transformDocument" );
        this.print( transformDocument );
    }
}
