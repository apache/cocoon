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
package org.apache.cocoon.woody.datatype.convertor;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.apache.cocoon.woody.Constants;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.impl.ResourceSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test case for the {@link EnumConvertorBuilder} class.
 * 
 * @version CVS $Id: EnumConvertorBuilderTestCase.java,v 1.2 2003/11/17 16:17:17 ugo Exp $
 */
public class EnumConvertorBuilderTestCase extends TestCase {

    protected DocumentBuilder parser;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        parser = factory.newDocumentBuilder();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
    }

    public EnumConvertorBuilderTestCase(String name) {
        super(name);
    }

    /**
     * Test the {@link EnumConvertorBuilder#build(org.w3c.dom.Element)
     * build} method.
     * @throws Exception
     */
    public void testBuild() throws Exception {
        Source confSource = new ResourceSource("resource://org/apache/cocoon/woody/datatype/convertor/EnumConvertorTestCase.conf.xml");
        Document sample = parser.parse(confSource.getInputStream());
        Element convertorElement = (Element) sample.getElementsByTagNameNS(Constants.WD_NS, "convertor").item(0);
        String enumClassName = convertorElement.getElementsByTagNameNS(Constants.WD_NS, "enum").item(0).getFirstChild().getNodeValue();
        EnumConvertorBuilder builder = new EnumConvertorBuilder();
        Convertor convertor = builder.build(convertorElement);
        assertTrue("The returned convertor is not an EnumConvertor",
                convertor instanceof EnumConvertor);
        assertEquals("The convertor does not convert the expected class",
                Class.forName(enumClassName), convertor.getTypeClass());
    }
}
