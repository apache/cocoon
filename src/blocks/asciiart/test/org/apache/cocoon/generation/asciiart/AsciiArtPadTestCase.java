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
 * @version CVS $Id: AsciiArtPadTestCase.java,v 1.2 2004/03/05 13:01:40 bdelacretaz Exp $
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

