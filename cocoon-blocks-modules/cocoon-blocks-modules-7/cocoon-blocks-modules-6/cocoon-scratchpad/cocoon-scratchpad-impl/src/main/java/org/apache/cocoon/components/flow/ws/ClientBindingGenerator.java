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
package org.apache.cocoon.components.flow.ws;

import java.util.List;

import org.apache.axis.utils.CLArgsParser;
import org.apache.axis.utils.CLOption;
import org.apache.axis.wsdl.WSDL2Java;

/**
 * The ClientBindingGenerator uses the WSDL2Java utility from the Axis project
 * to generate Java client bindings from a WSDL document.
 */
public class ClientBindingGenerator extends WSDL2Java {

    /**
     * Generates Java client bindings from a WSDL document identified by a URI.
     *
     * @param wsdlURI
     * @param outputDir
     */
    public void generate(String wsdlURI, String outputDir) {
        String[] args = new String[3];
        args[0] = "-o";
        args[1] = outputDir;
        args[2] = wsdlURI;
        run(args);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.axis.wsdl.gen.WSDL2#run(java.lang.String[])
     */
    protected void run(String[] args) {
        // Parse the arguments
        CLArgsParser argsParser = new CLArgsParser(args, options);

        // Get a list of parsed options
        List clOptions = argsParser.getArguments();
        int size = clOptions.size();

        try {
            // Parse the options and configure the emitter as appropriate.
            for (int i = 0; i < size; i++) {
                parseOption((CLOption) clOptions.get(i));
            }

            // validate argument combinations
            validateOptions();

            parser.run(wsdlURI);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}