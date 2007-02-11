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
package org.apache.cocoon.util.test;

import java.io.File;
import junit.framework.TestCase;
import org.apache.cocoon.util.IOUtils;

/**
 * Test Cases for the IOUtils Class.
 * @see org.apache.cocoon.util.IOUtils
 *
 * @author <a href="mailto:stuart.roebuck@adolos.com">Stuart Roebuck</a>
 * @author <a href="mailto:berni_huber@a1.net">Bernhard Huber</a>
 * @version CVS $Id: IOUtilsTestCase.java,v 1.3 2003/05/31 12:51:53 jefft Exp $
 */
public class IOUtilsTestCase extends TestCase
{

    /**
     *Constructor for the IOUtilsTestCase object
     *
     * @param  name  Description of Parameter
     * @since
     */
    public IOUtilsTestCase(String name) {
        super(name);
    }


    /**
     *Description of the Method
     *
     * @param  args  Description of Parameter
     * @since
     */
    public static void main(String args[]) {
        junit.textui.TestRunner.run(IOUtilsTestCase.class);
    }


    /**
     * A unit test for <code>normalizedFilename()</code>
     *
     * @exception  Exception  Description of Exception
     * @since
     */
    public void testNormalizedFilename() throws Exception {
        Object[] test_values = {
                new String[]{".", "__"},
                new String[]{"", ""},
                new String[]{"file://", "file_" + File.separator + "_" + File.separator + "_"},
                new String[]{"/a/b/c", "a" + File.separator + "b" + File.separator + "c"},
                new String[]{"\\a\\b\\c", "a" + File.separator + "b" + File.separator + "c"},
                new String[]{"a/b/c", "a" + File.separator + "b" + File.separator + "c"},
                new String[]{"a\\b\\c", "a" + File.separator + "b" + File.separator + "c"},
                
                new String[]{"a/b/../c", "a" + File.separator + "c"},
                new String[]{"public/final.xml", "public_" + File.separator + "final_xml"},
                new String[]{"123", "_123"}
                };
        for (int i = 0; i < test_values.length; i++) {
            String tests[] = (String[]) test_values[i];
            String test = tests[0];
            String expected = tests[1];

            String result = IOUtils.normalizedFilename(test);
            String message = "Test " + "'" + test + "'";
            assertEquals(message, expected, result);
        }
    }


    /**
     * A unit test for <code>getContextFilePath()</code>
     *
     * @exception  Exception  Description of Exception
     * @since
     */
    public void testGetContextFilePath() throws Exception {
        Object[] test_values = {
                new String[]{"a", "a" + File.separator + "b", "b"},
                new String[]{"a\\b", "a\\b" + File.separator + "c/d", "c" + File.separator + "d"},
                new String[]{"a/b", "a/b" + File.separator + "c\\d", "c" + File.separator + "d"},
                };
        for (int i = 0; i < test_values.length; i++) {
            String tests[] = (String[]) test_values[i];
            String test_directory_path = tests[0];
            String test_file_path = tests[1];
            String expected = tests[2];

            String result = IOUtils.getContextFilePath(test_directory_path, test_file_path);
            String message = "Test " + "'" + test_directory_path + "'" + ", " +
                    "'" + test_file_path + "'";
            assertEquals(message, expected, result);
        }
    }


    /**
     * A unit test for <code>objectToBytes()</code>, and <code>bytesToObject()</code>
     *
     * @exception  Exception  Description of Exception
     * @since
     */
    public void testObjectToBytesBytesToObject() throws Exception {
        String test = "test";
        String expected = "test";

        String message = "Test " + "'" + test + "'";

        byte[] bytes = IOUtils.objectToBytes(test);
        String result = (String) IOUtils.bytesToObject(bytes);

        assertEquals(message, expected, result);
    }
}

