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

package org.apache.cocoon.components.flow.javascript;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceUtil;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.tools.ToolErrorReporter;

public class JavaScriptCompilingClassLoader extends ClassLoader implements Serviceable {

    private Map sources = new HashMap();

    private static ToolErrorReporter reporter;

    private SourceResolver resolver;

    public JavaScriptCompilingClassLoader(ClassLoader classloader) {
        super(classloader);
    }

    public void addSource(String uri) {
        try {
            Source source = resolver.resolveURI(uri);

            String classname = getClassName(uri);
            String filename = System.getProperty("java.io.tmpdir") + File.separator + classname;

            System.out.println("source=" + uri);
            System.out.println("filename=" + filename);
            System.out.println("classname=" + classname);
            
            System.out.println("resource="+getResource("org/apache/cocoon/components/flow/javascript/calc.js"));

            SourceUtil.copy(source.getInputStream(), new FileOutputStream(filename+".js"));

            sources.put(classname, filename);

        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    protected Class findClass(String classname) throws ClassNotFoundException {

        System.out.println("js: find class " + classname);

        //byte[] bytes = compile(className);
        String filename = (String) sources.get(classname);
        compile(classname);

        try {
            FileInputStream in = new FileInputStream(filename+".class");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            SourceUtil.copy(in, out);
            byte[] bytes = out.toByteArray();

            return defineClass(classname, bytes, 0, bytes.length);

        } catch (IOException e) {
            throw new ClassNotFoundException("Could not load class: " + e.getMessage());
        }
    }

    private void compile(String classname) throws ClassNotFoundException {

        System.out.println("js: compile " + classname);

        Context cx = Context.enter();
        reporter = new ToolErrorReporter(true);
        cx.setErrorReporter(reporter);
        
        cx.setGeneratingSource(true);
        cx.setTargetPackage("");

        //ClassNameHelper nameHelper = ClassNameHelper.get(cx);
        String filename = ((String) sources.get(classname))+".js";
        
        System.out.println("filename="+filename);

        File f = new File(filename);

        if (!f.exists()) { throw new ClassNotFoundException("File '" + filename + "' not found"); }
        /*if (!filename.endsWith(".js")) { throw new ClassNotFoundException("File '" + filename
                + "' doesn't have the extention *.js"); }*/

        String out = ((String) sources.get(classname))+".class";
        /*String name = f.getName();
        String nojs = name.substring(0, name.length() - 3);
        String className = getClassName(nojs) + ".class";
        //String out = f.getParent() == null ? className : f.getParent() + File.separator + className;
        String out = System.getProperty("java.io.tmpdir") + File.separator + className;
        //nameHelper.setTargetClassFileName(out);*/
        cx.setTargetClassFileName(out);

        System.out.println("out=" + out);

        try {
            Reader in = new FileReader(filename);
            cx.compileReader(null, in, filename, 1, null);
        } catch (FileNotFoundException ex) {
            throw new ClassNotFoundException("File '" + filename + "' cannot open");
        } catch (IOException ioe) {
            Context.reportError(ioe.toString());
        }
        Context.exit();
    }

    /**
     * Verify that class file names are legal Java identifiers.  Substitute
     * illegal characters with underscores, and prepend the name with an
     * underscore if the file name does not begin with a JavaLetter.
     */
    public static String getClassName(String name) {
        char[] s = new char[name.length() + 1];
        char c;
        int j = 0;

        if (!Character.isJavaIdentifierStart(name.charAt(0))) {
            s[j++] = '_';
        }
        for (int i = 0; i < name.length(); i++, j++) {
            c = name.charAt(i);
            if (Character.isJavaIdentifierPart(c)) {
                s[j] = c;
            } else {
                s[j] = '_';
            }
        }
        return (new String(s)).trim();
    }

    public void service(ServiceManager manager) throws ServiceException {
        resolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
    }
}

