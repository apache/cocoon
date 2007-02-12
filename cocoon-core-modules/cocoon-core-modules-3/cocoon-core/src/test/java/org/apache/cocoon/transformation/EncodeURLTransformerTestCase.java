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
import org.apache.cocoon.environment.mock.MockRequest;
import org.apache.cocoon.environment.mock.MockResponse;
import org.apache.cocoon.environment.mock.MockSession;
import org.w3c.dom.Document;


/**
 * A simple testcase for FilterTransformer.
 *
 * @version $Id$
 */
public class EncodeURLTransformerTestCase extends SitemapComponentTestCase {
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.SitemapComponentTestCase#getSitemapComponentInfo()
     */
    protected String[] getSitemapComponentInfo() {
        return new String[] {Transformer.class.getName(),
                             EncodeURLTransformer.class.getName(),
                             "encodeurl"};
    }
    
    /** Testcase for encode url transformation
     *
     * @throws Exception if ServiceManager enterEnvironment fails
     */
    public void testEncodeURL1() throws Exception {
        getLogger().debug("testEncodeURL1");
        
        Parameters parameters = new Parameters();
        
        String input = "resource://org/apache/cocoon/transformation/encodeurl-input-1.xml";
        String result = "resource://org/apache/cocoon/transformation/encodeurl-result-1.xml";
        String src =  null;
        
        Document inputDocument = load(input);
        Document resultDocument = load(result);
        Document transformDocument = transform("encodeurl", src, parameters, inputDocument);
        
        printDocs( resultDocument, inputDocument, transformDocument );
        
        assertIdentical( resultDocument, transformDocument );
    }
    
    /** Testcase for encode url transformation
     *
     * @throws Exception if ServiceManager enterEnvironment fails
     */
    public void testEncodeURL2() throws Exception {
        getLogger().debug("testEncodeURL2");
        
        Parameters parameters = new Parameters();
        
        String input = "resource://org/apache/cocoon/transformation/encodeurl-input-2.xml";
        String result = "resource://org/apache/cocoon/transformation/encodeurl-result-2.xml";
        String src =  null;
        
        // force that sessionId is added to an URL
        MockRequest request = getRequest();
        MockSession session = (MockSession)request.getSession();
        MockResponse response = getResponse();
        
        response.setSession(session);
        getRequest().setIsRequestedSessionIdFromURL( true );

        session.setIsNew(true);
        
        Document inputDocument = load(input);
        Document resultDocument = load(result);
        Document transformDocument = transform("encodeurl", src, parameters, inputDocument);
        
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
