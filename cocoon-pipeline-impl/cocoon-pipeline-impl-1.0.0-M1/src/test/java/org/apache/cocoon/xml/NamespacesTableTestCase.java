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
package org.apache.cocoon.xml;

import junit.framework.TestCase;

import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Test case for NamespacesTable
 * 
 * @version $Id$
 */
public class NamespacesTableTestCase extends TestCase {
    public NamespacesTableTestCase(String name) {
        super(name);
    }

    public void testSimple() {
        NamespacesTable ns = new NamespacesTable();
        
        ns.addDeclaration("ns1", "http://ns1");
        ns.addDeclaration("ns2", "http://ns2");
        
        ns.enterScope();
        
          assertEquals("http://ns1", ns.getUri("ns1"));
          assertEquals("ns1", ns.getPrefix("http://ns1"));
        
          assertEquals("http://ns2", ns.getUri("ns2"));
          assertEquals("ns2", ns.getPrefix("http://ns2"));
        
          ns.enterScope();
        
            ns.addDeclaration("ns3", "http://ns3");
            ns.enterScope();
        
              assertEquals("ns1", ns.getPrefix("http://ns1"));
              assertEquals("ns3", ns.getPrefix("http://ns3"));
              assertEquals(0, ns.getCurrentScopeDeclarations().length);
            ns.leaveScope();
            
            // Declarations in this scope are no more visible...
            assertNull(ns.getUri("ns3"));
            // ... but still listed in the declared mappings
            assertEquals(1, ns.getCurrentScopeDeclarations().length);
            assertEquals("ns3", ns.getCurrentScopeDeclarations()[0].getPrefix());
        
          ns.leaveScope();
        
          assertNull(ns.getPrefix(ns.getPrefix("http://ns3")));
          assertNull(ns.getUri("ns3"));
        
        ns.leaveScope();
        // Declarations that occured before this scope are no more visible...
        assertNull(ns.getUri("ns1"));
        assertNull(ns.getPrefix("http://ns1"));
        
        assertNull(ns.getUri("ns2"));
        assertNull(ns.getPrefix("http://ns2"));
        
        //... but are still available in getDeclaredPrefixes
        NamespacesTable.Declaration[] prefixes = ns.getCurrentScopeDeclarations();
        assertEquals(2, prefixes.length);

        assertEquals("ns2", prefixes[0].getPrefix());
        assertEquals("http://ns2", prefixes[0].getUri());
        assertEquals("ns1", prefixes[1].getPrefix());
        assertEquals("http://ns1", prefixes[1].getUri());
        
    }
    
    public void testOverride() {
        NamespacesTable ns = new NamespacesTable();
        
        ns.addDeclaration("ns1", "http://ns1");
        ns.enterScope();
        ns.addDeclaration("ns1", "http://otherns1");
        ns.enterScope();
        ns.addDeclaration("ns1", "http://yetanotherns1");
        ns.enterScope();
        
        assertEquals("http://yetanotherns1", ns.getUri("ns1"));
        assertEquals(0, ns.getPrefixes("http://ns1").length);
        
        ns.leaveScope();
        ns.leaveScope();
        
        assertEquals("http://ns1", ns.getUri("ns1"));
        assertEquals(1, ns.getPrefixes("http://ns1").length);
        
        ns.leaveScope();
        assertNull(ns.getUri("ns1"));
    }
    
    public void testMultiDeclaration() {
        NamespacesTable ns = new NamespacesTable();
        ns.addDeclaration("ns1", "http://ns1");
        ns.enterScope();
        // two in the same scope
        ns.addDeclaration("ns2", "http://ns1");
        ns.addDeclaration("ns3", "http://ns1");
        ns.enterScope();
        
        String[] prefixes = ns.getPrefixes("http://ns1");
        assertEquals(3, prefixes.length);
        assertEquals("ns3", prefixes[0]);
        assertEquals("ns2", prefixes[1]);
        assertEquals("ns1", prefixes[2]);
    }
    
    public void testStreamDeclarations() throws Exception {
        NamespacesTable ns = new NamespacesTable();
        ns.addDeclaration("ns1", "http://ns1");
        ns.enterScope();
        ns.addDeclaration("ns2", "http://ns2");
        ns.enterScope(new DefaultHandler() {
            public void startPrefixMapping(String prefix, String uri) throws org.xml.sax.SAXException {
                assertEquals("ns2", prefix);
                assertEquals("http://ns2", uri);
            }
        });
        
        // Enter and leave a nested scope
        ns.addDeclaration("ns3", "http://ns3");
        ns.enterScope();
        ns.leaveScope();
        
        ns.leaveScope(new DefaultHandler() {
            public void endPrefixMapping(String prefix) throws org.xml.sax.SAXException {
                assertEquals("ns2", prefix);
            }
        });
    }
    
    /**
     * A scenario that occurs in with jx:import where some prefixes are started but not used.
     * @throws Exception
     */
    public void testJXImport() throws Exception {
        NamespacesTable ns = new NamespacesTable();
        ContentHandler handler = new DefaultHandler();
        
        ns.addDeclaration("ft", "http://apache.org/cocoon/forms/1.0#template");
        ns.addDeclaration("fi", "http://apache.org/cocoon/forms/1.0#instance");
        ns.addDeclaration("jx", "http://apache.org/cocoon/templates/jx/1.0");
        ns.enterScope(handler);
          assertEquals("ft", ns.getPrefix("http://apache.org/cocoon/forms/1.0#template"));
          assertEquals("fi", ns.getPrefix("http://apache.org/cocoon/forms/1.0#instance"));
          assertEquals("jx", ns.getPrefix("http://apache.org/cocoon/templates/jx/1.0"));
          
          // Add declarations that won't be used
          ns.addDeclaration("jx", "http://apache.org/cocoon/templates/jx/1.0");
          ns.addDeclaration("fi", "http://apache.org/cocoon/forms/1.0#instance");
          ns.addDeclaration("bu", "http://apache.org/cocoon/browser-update/1.0");

        ns.leaveScope(handler);
        assertNull(ns.getPrefix("http://apache.org/cocoon/forms/1.0#template"));
        assertNull(ns.getPrefix("http://apache.org/cocoon/forms/1.0#instance"));
        assertNull(ns.getPrefix("http://apache.org/cocoon/templates/jx/1.0"));
        assertEquals(3, ns.getCurrentScopeDeclarations().length);

    }
    
    public void testDuplicate() throws Exception {
        NamespacesTable ns = new NamespacesTable();
        
        ns.addDeclaration("ns1", "http://ns1");
          ns.enterScope();
            ns.addDeclaration("ns1", "http://ns1");
              ns.enterScope();
              ns.leaveScope();
            ns.removeDeclaration("ns1");
            assertEquals("http://ns1", ns.getUri("ns1"));
          ns.leaveScope();
        ns.removeDeclaration("ns1");
        assertNull(ns.getUri("ns1"));
    }

}
