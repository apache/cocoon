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
package org.apache.cocoon.transformation.helpers;

import java.util.Map;
import java.util.StringTokenizer;

import org.apache.avalon.framework.configuration.SAXConfigurationHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * A SAX ContentHandler that builds Avalon <code>Configuration</code> objects,
 * but also replaces variables of the form {var} with values from a map.
 *
 * @see VariableConfiguration
 * @author <a href="jefft@apache.org">Jeff Turner</a>
 * @version CVS $Id: InterpolatingConfigurationHandler.java,v 1.5 2004/03/05 13:01:59 bdelacretaz Exp $
 */
public class InterpolatingConfigurationHandler extends SAXConfigurationHandler {
    final private Map vars;
    final private String location;

    /** Constructor.
     * @param vars The mappings from variable name to value.
     */
    public InterpolatingConfigurationHandler(Map vars) {
        this.vars = vars;
        this.location = "Unknown";
    }

    /** Constructor.
     * @param vars The mappings from variable name to value.
     * @param location The origin of this configuration info.
     */
    public InterpolatingConfigurationHandler(Map vars, String location) {
        this.vars = vars;
        this.location = location;
    }

    /** Replace {vars} in attributes.  */
    public void startElement(String uri, String localName, String qName, Attributes attr) throws SAXException {
        AttributesImpl newAttr = new AttributesImpl(attr);
        for (int i=0; i<attr.getLength(); i++) {
            newAttr.setValue(i, interp(attr.getValue(i)));
        }
        super.startElement(uri, localName, qName, newAttr);
    }

    /** Replace {vars} in element bodies.  */
    public void characters( final char[] ch, int start, int len )
        throws SAXException
    {
        StringBuffer buf = new StringBuffer();
        if (start!=0) buf.append(ch, 0, start-1);
        String newVal = interp(new String(ch,start, len));
        buf.append(newVal);
        buf.append(ch, start+len, ch.length-(start+len));
        super.characters(buf.toString().toCharArray(), start, newVal.length());
    }

    protected String getLocationString() {
        return this.location;
    }


    /**
     * Interpolate variable values into a string.
     *
     * @param str String with {var} tokens
     * @return <code>str</code>, with {variables} replaced by values.  If an
     * unknown variable token is encountered it is ignored.
     */
    private String interp(String str) {
        StringBuffer buf = new StringBuffer(str.length()*2);
        StringTokenizer tok = new StringTokenizer(str, "{}", true);
        int state = 0; // 0=outside, 1=inside
        while (tok.hasMoreTokens()) {
            String token = tok.nextToken();
            if (state == 0 && "{".equals(token)) {
                state = 1;
            } else if (state == 1 && "}".equals(token)) {
                state = 0;
            } else if (state == 0) {
                buf.append(token);
            } else if (state == 1) {
                //System.out.println("Replacing "+token+" with "+vars.get(token));
                String val = (String)vars.get(token);
                if (val == null) {
                    buf.append("{").append(token).append("}");
                } else{ buf.append(val); }
            }
        }
        return buf.toString();
    }

}
