package org.apache.cocoon;

import java.io.*;
import java.net.*;
import java.util.*;
import org.w3c.dom.*;
import javax.servlet.http.*;

/**
 * Utility methods for Cocoon and its classes.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:12 $
 */

public final class Utils {

    /**
     * This method returns a vector of PI nodes based on the PI target name.
     */
    public static final Vector getAllPIs(Document document, String name) {
        return getAllPIs(document, name, false);
    }
	
    /**
     * This method returns a vector of PI nodes based on the PI target name
     * and removes the found PIs from the document if the remove flag is
     * true.
     */
    public static final Vector getAllPIs(Document document, String name, boolean remove) {
        Vector pis = new Vector();
        
        NodeList nodelist = document.getChildNodes();
        int i = nodelist.getLength();
        for (int j = 0; j < i; j++) {
            Node node = nodelist.item(j);
            if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
                if (((ProcessingInstruction) node).getTarget().equals(name)) {
                    pis.addElement(node);
                    if (remove) node.getParentNode().removeChild(node);
                }
            }
        }
        
        return pis;
    }
    
    /**
     * This method returns the first PI node based on the PI target name.
     */
    public static final ProcessingInstruction getFirstPI(Document document, String name) {
        return getFirstPI(document, name, false);
    }

    /**
     * This method returns the first PI node based on the PI target name and
     * removes it from the document if the remove flag is true.
     */
    public static final ProcessingInstruction getFirstPI(Document document, String name, boolean remove) {
        ProcessingInstruction pi = null;

        NodeList nodelist = document.getChildNodes();
        int i = nodelist.getLength();
        for (int j = 0; j < i; j++) {
            Node node = nodelist.item(j);
            if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
                if (((ProcessingInstruction) node).getTarget().equals(name)) {
                    pi = (ProcessingInstruction) node;
                    if (remove) node.getParentNode().removeChild(node);
                    break;
                }
            }
        }

        return pi;
    }
    
    /**
     * This method returns an hashtable with all the pseudo attributes collected
     * in the document. If more PI have the same target, the attributes are
     * all put in the same hashtable. If there are collisions, the last attribute
     * is insered.
     */
    public static final Hashtable getPIPseudoAttributes(Document document, String name) {
        Hashtable attributes = new Hashtable();
        Enumeration nodes = getAllPIs(document, name).elements();
        
        while (nodes.hasMoreElements()) {
            String data = ((ProcessingInstruction) nodes.nextElement()).getData();
            for (StringTokenizer st = new StringTokenizer(data, " \t="); st.hasMoreTokens();) {
                String key = st.nextToken();
                String token = st.nextToken();
                token = token.substring(1, token.length() - 1);
                attributes.put(key, token);
            }
        }
        
        return attributes;
    }
    
    /**
     * This method returns an hashtable of pseudo attributes found in the first
     * occurrence of the PI with the given name in the given document.
     * No validation is performed on the PI pseudo syntax
     */
    public static final Hashtable getPIPseudoAttributes(ProcessingInstruction pi) {
        Hashtable attributes = new Hashtable();
        
        String data = pi.getData();
        for (StringTokenizer st = new StringTokenizer(data, " \t="); st.hasMoreTokens();) {
            String key = st.nextToken();
            String token = st.nextToken();
            token = token.substring(1, token.length() - 1);
            attributes.put(key, token);
        }
        
        return attributes;
    }

    /**
     * Encodes the given request into a string using the format
     *   protocol://serverName:serverPort/requestURI?query
     */
    public static final String encode(HttpServletRequest req) {
        return encode(req, true, true);
    }

    /**
     * Encodes the given request into a string using the format
     *   userAgent:protocol://serverName:serverPort/requestURI?query
     * with the agent flag controlling the presence of the userAgent
     * field.
     */
    public static final String encode(HttpServletRequest req, boolean agent) {
        return encode(req, agent, true);
    }
    
    /**
     * Encodes the given request into a string using the format
     *   userAgent:protocol://serverName:serverPort/requestURI?query
     * with the agent flag controlling the presence of the userAgent
     * field and the query flag controlling the query field.
     */
    public static final String encode(HttpServletRequest req, boolean agent, boolean query) {
        StringBuffer url = new StringBuffer();
        if (agent) {
            url.append(req.getHeader("user-Agent"));
            url.append(':');
        }
        url.append(req.getScheme());
        url.append("://");
        url.append(req.getServerName());
        url.append(':');
        url.append(req.getServerPort());
        url.append(req.getRequestURI());
        if (query) {
            url.append('?');
            url.append(req.getQueryString());
        }
        return url.toString();
    }
}