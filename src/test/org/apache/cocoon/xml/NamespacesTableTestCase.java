package org.apache.cocoon.xml;

import org.xml.sax.helpers.DefaultHandler;

import junit.framework.TestCase;

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
        
        assertNull(ns.getPrefix(ns.getPrefix("http://ns3")));
        assertNull(ns.getUri("ns3"));
        
        ns.leaveScope(false);
        assertNotNull(ns.removeDeclaration("ns1"));
        assertNotNull(ns.removeDeclaration("ns2"));
        assertNull(ns.removeDeclaration("ns3"));
    }
    
    public void testWrongUndeclare() {
        NamespacesTable ns = new NamespacesTable();
        
        ns.enterScope();
        ns.addDeclaration("ns1", "http://ns1");
        ns.addDeclaration("ns2", "http://ns2");
        
        ns.enterScope(); // increments closedScopes on ns2
        
        ns.addDeclaration("ns3", "http://ns3");
        
        try {
            ns.leaveScope(false);
        } catch(IllegalStateException e) {
            return;
        }
        
        fail();
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
        
        ns.leaveScope(true);
        ns.leaveScope(true);
        
        assertEquals("http://ns1", ns.getUri("ns1"));
        assertEquals(1, ns.getPrefixes("http://ns1").length);
        
        ns.leaveScope(true);
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
            };
        });
        
        // Enter and leave a nested scope
        ns.addDeclaration("ns3", "http://ns3");
        ns.enterScope();
        ns.leaveScope(true);
        
        ns.leaveScope(new DefaultHandler() {
            public void endPrefixMapping(String prefix) throws org.xml.sax.SAXException {
                assertEquals("ns2", prefix);
            };
        });
    }
}
