/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.python.util;

import java.util.Properties;

import org.python.core.PyObject;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version $Id$
 */
public class PythonInterpreter {

    public static void initialize(Properties preProperties,
                                  Properties postProperties,
                                  String[] argv) {
        throw new NoSuchMethodError("This is a mock object");
    }

    public void set(String string, Object obj) {
        throw new NoSuchMethodError("This is a mock object");
    }

    public void exec(PyObject code) {
        throw new NoSuchMethodError("This is a mock object");
    }
}
