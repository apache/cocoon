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
package org.apache.cocoon.components.language.programming.java;

import net.sf.pizzacompiler.compiler.ClassReader;
import net.sf.pizzacompiler.compiler.FileCompilerOutput;
import net.sf.pizzacompiler.compiler.FileSourceReader;
import net.sf.pizzacompiler.compiler.Main;
import org.apache.cocoon.components.language.programming.CompilerError;
import org.apache.cocoon.util.ClassUtils;
import org.apache.log.Hierarchy;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * This class wraps the Pizza Java Compiler.
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id$
 * @deprecated Will be removed in 2.2
 */
public class Pizza extends AbstractJavaCompiler {

    public final static String PIZZA_CLASS = "net.sf.pizzacompiler.compiler.Main";

    public Pizza() {
        try {
            ClassUtils.loadClass(PIZZA_CLASS);
        } catch (ClassNotFoundException e) {
            Hierarchy.getDefaultHierarchy().getLoggerFor("cocoon").error("No Pizza Java compiler found in your classpath. Make sure you added 'pizza.jar'", e);
            throw new RuntimeException("No Pizza Java compiler found in your classpath. Make sure you added 'pizza.jar'");
        }
        net.sf.pizzacompiler.compiler.Main.init();
    }

    /**
     * Compile a source file yielding a loadable class file.
     */
    public boolean compile() {

        ByteArrayOutputStream err = new ByteArrayOutputStream();

        Main.init();
        Main.setClassReader(new ClassReader(this.classpath, null));
        Main.argument("-java");
        Main.argument("-O");
        Main.argument("-nowarn");
        Main.compile(new String[]{file},
                new FileSourceReader(),
                new FileCompilerOutput(new File(destDir)),
                new PrintStream(err));

        this.errors = new ByteArrayInputStream(err.toByteArray());
        return err.size() == 0;
    }

    /**
     * Parse the compiler error stream to produce a list of
     * <code>CompilerError</code>s
     *
     * @param input The error stream
     * @return The list of compiler error messages
     * @exception IOException If an error occurs during message collection
     */
    protected List<CompilerError> parseStream(BufferedReader input) throws IOException {
        List<CompilerError> errors = new ArrayList<CompilerError>();

        while (true) {
            StringBuilder buffer = new StringBuilder();

            // most errors terminate with the '^' char
            String line;
            do {
                if ((line = input.readLine()) == null) {
                    if (buffer.length() > 0) {
                        // There's an error which doesn't end with a '^'
                        errors.add(new CompilerError("\n" + buffer.toString()));
                    }
                    return errors;
                }
                buffer.append(line);
                buffer.append('\n');
            } while (!line.endsWith("^"));

            // add the error bean
            errors.add(parseModernError(buffer.toString()));
        }
    }

    /**
     * Parse an individual compiler error message with modern style.
     *
     * @param error The error text
     * @return A messaged <code>CompilerError</code>
     */
    private CompilerError parseModernError(String error) {
        StringTokenizer tokens = new StringTokenizer(error, ":");
        try {
            String file = tokens.nextToken();
            if (file.length() == 1)
                file = file + ":" + tokens.nextToken();
            int line = Integer.parseInt(tokens.nextToken());

            String message = tokens.nextToken("\n").substring(1);
            String context = tokens.nextToken("\n");
            String pointer = tokens.nextToken("\n");
            int startcolumn = pointer.indexOf("^");
            int endcolumn = context.indexOf(" ", startcolumn);
            if (endcolumn == -1) {
                endcolumn = context.length();
            }
            return new CompilerError(file, false, line, startcolumn, line, endcolumn, message);
        } catch(NoSuchElementException nse) {
            return new CompilerError("no more tokens - could not parse error message: " + error);
        } catch(Exception nse) {
            return new CompilerError("could not parse error message: " + error);
        }
    }

    public String toString() {
        return "Pizza Java Compiler";
    }
}
