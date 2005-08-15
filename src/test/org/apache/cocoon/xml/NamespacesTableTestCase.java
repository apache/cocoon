package org.apache.cocoon.xml;

import junit.framework.TestCase;

import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;

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
        
        ns.leaveScope();
        // Declarations that occured before this scope are no more visible
        assertNull(ns.removeDeclaration("ns1"));
        assertNull(ns.removeDeclaration("ns2"));
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
            ns.leaveScope();
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
            };
        });
        
        // Enter and leave a nested scope
        ns.addDeclaration("ns3", "http://ns3");
        ns.enterScope();
        ns.leaveScope();
        
        ns.leaveScope(new DefaultHandler() {
            public void endPrefixMapping(String prefix) throws org.xml.sax.SAXException {
                assertEquals("ns2", prefix);
            };
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
//        ns.enterScope(handler);
          ns.addDeclaration("jx", "http://apache.org/cocoon/templates/jx/1.0");
          ns.addDeclaration("fi", "http://apache.org/cocoon/forms/1.0#instance");
          ns.addDeclaration("bu", "http://apache.org/cocoon/browser-update/1.0");
          ns.removeDeclaration("jx");
          ns.removeDeclaration("fi");
          ns.removeDeclaration("bu");
//        ns.leaveScope(handler);
        assertEquals("ft", ns.getPrefix("http://apache.org/cocoon/forms/1.0#template"));
        assertEquals("fi", ns.getPrefix("http://apache.org/cocoon/forms/1.0#instance"));
        assertEquals("jx", ns.getPrefix("http://apache.org/cocoon/templates/jx/1.0"));
        ns.removeDeclaration("ft");
        ns.removeDeclaration("fi");
        ns.removeDeclaration("jx");

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
