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
package org.apache.cocoon.generation.asciiart;

import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Locale;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 *  A Simple testcase of the AsciiArtPad.
 *
 * @author huber.at.apache.org
 * @since 18. Dezember 2002
 * @version CVS $Id: AsciiArtPadTestCase.java,v 1.1 2003/04/09 13:46:03 stephan Exp $
 */
public class AsciiArtPadTestCase extends TestCase {

    private AsciiArtPad asciiArtPad;


    /**
     *Constructor for the AsciiArtPadTestCase object
     *
     *@param  name  Description of Parameter
     *@since
     */
    public AsciiArtPadTestCase(String name) {
        super(name);
    }


    /**
     *  The main program for the AsciiArtPadTestCase class
     *
     *@param  args           The command line arguments
     *@exception  Exception  Description of the Exception
     */
    public static void main(final String[] args) throws Exception {
        final String[] testCaseName = {AsciiArtPadTestCase.class.getName()};
        TestRunner.main(testCaseName);
    }


    /**
     *  A unit test for JUnit
     */
    public void test0() {
    }


    /**
     *  A unit test for JUnit
     *
     *@exception  Exception  Description of the Exception
     */
    public void test1() throws Exception {
        System.out.println("test1()");
        String[] asciiArt = new String[]{
        //          1
        //01234567890123456789
                "=                    =",
                " +--------------+ ",
                " | 1st quarter  | ",
                " +------------+-+ ",
                " | 2nd        |   ",
                " +-----+------+   ",
                " | 3rd |          ",
                " +-----+          ",
                "=                =",
                " +-------------+  ",
                " |  container  |  ",
                " |             |  ",
                " |  +-------+  |  ",
                " |  | part  |  |  ",
                " |  +-------+  |  ",
                " |             |  ",
                " +-------------+  ",
                "=                =",
                " +==============+  ",
                " |  Mail_Header |  ",
                " |  +========+  |  ",
                " |  | Body   |  |  ",
                " |  +========+  |  ",
                " |  |a |a |a |  |  ",
                " |  |1 |2 |3 |  |  ",
                " |  +========+  |  ",
                " +==============+  ",
                "=                =",
                " +----------------+",
                " | header         |",
                " +----------------+",
                " + c | col2   | c +",
                " + o |        | o +",
                " + l |        | l +",
                " + 1 |        | 3 +",
                " +----------------+",
                " |     footer     |",
                " +----------------+",
                "=                =",
                " +---stylesheets      ",
                " +---docs             ",
                " |   +---top_col_1    ",
                " |   +---mid_col_1    ",
                " |   +---mid_col_2    ",
                " |   +---mid_col_3    ",
                " |   +---mid_col_4    ",
                " |   \\---bottom_col_1",
                " \\---resources",
                "      |---styles",
                "      \\---images",
                };

        asciiArtPad = new AsciiArtPad();
        asciiArtPad.setXGrid(10);
        asciiArtPad.setYGrid(12);
        AsciiArtPad.AsciiArtPadBuilder aapb = new AsciiArtPad.AsciiArtPadBuilder(asciiArtPad);
        aapb.build(asciiArt);

        Iterator i = asciiArtPad.iterator();
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        StringBuffer svg_lines = new StringBuffer();
        StringBuffer svg_text = new StringBuffer();
        while (i.hasNext()) {
            Object o = i.next();
            double x;
            double y;
            if (o instanceof AsciiArtPad.AsciiArtLine) {
                AsciiArtPad.AsciiArtLine aal = (AsciiArtPad.AsciiArtLine) o;
                x = aal.getXStart();
                y = aal.getYStart();
                svg_lines.append("<path d=\"");
                svg_lines.append("M " + nf.format(x) + " " + nf.format(y));

                x = aal.getXEnd();
                y = aal.getYEnd();
                svg_lines.append("L " + nf.format(x) + " " + nf.format(y));
                svg_lines.append("\"/>\n");

            } else if (o instanceof AsciiArtPad.AsciiArtString) {
                AsciiArtPad.AsciiArtString aas = (AsciiArtPad.AsciiArtString) o;
                x = aas.getX();
                y = aas.getY();
                svg_text.append("<text ");
                svg_text.append("x=\"" + nf.format(x) + "\" y=\"" + nf.format(y) + "\">");
                svg_text.append("<![CDATA[" + aas.getS() + "]]>");
                svg_text.append("</text>\n");

            } else {
                System.out.println("o " + o.toString());
            }
        }
        System.out.println("<!-- lines --> ");
        System.out.println(svg_lines.toString());
        System.out.println("<!-- text --> ");
        System.out.println(svg_text.toString());
    }


    /**
     *The JUnit setup method
     *
     *@exception  Exception  Description of Exception
     *@since
     */
    protected void setUp() throws Exception {
        asciiArtPad = null;
    }


    /**
     *  The teardown method for JUnit
     *
     *@exception  Exception  Description of the Exception
     */
    protected void tearDown() throws Exception {
        asciiArtPad = null;
    }
}

