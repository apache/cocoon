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
 * @version CVS $Id: IOUtilsTestCase.java,v 1.6 2004/05/05 22:28:22 ugo Exp $
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
                new String[]{"file://", "file_"},
                // was new String[]{"file://", "file_" + File.separator + "_" + File.separator + "_"},
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

