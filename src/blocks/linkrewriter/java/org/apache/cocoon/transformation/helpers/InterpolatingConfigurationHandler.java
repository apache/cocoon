/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: InterpolatingConfigurationHandler.java,v 1.4 2004/03/01 03:50:58 antonio Exp $
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
