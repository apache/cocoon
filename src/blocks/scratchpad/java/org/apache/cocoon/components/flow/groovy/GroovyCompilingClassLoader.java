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

package org.apache.cocoon.components.flow.groovy;

import groovy.lang.GroovyClassLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.commons.lang.StringUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.tools.GroovyClass;

/**
 * Class Loader for Groovy source files.
 * Compiles the scripts and store the resulting java class and the original Groovy scripts.
 *
 * @version CVS $Id: GroovyCompilingClassLoader.java,v 1.1 2004/06/07 01:04:18 antonio Exp $ 
 * 
 */
public class GroovyCompilingClassLoader extends ClassLoader implements Serviceable {

    private Map sources = new HashMap();			// Store the Groovy source file
    private Map compiledClasses = new HashMap();	// Store the compiled classes of the Groovy script
    private SourceResolver resolver;
    private GroovyClassLoader groovyClassLoader;	// A groovyClassLoader

    /**
     * Compile groovy scripts and load classes as needed.
     * @param classloader
     */
    public GroovyCompilingClassLoader(ClassLoader classloader) {
        super(classloader);
        groovyClassLoader = new GroovyClassLoader(classloader);
    }

    /* 
     * Intercept the Class Loader for Groovy compiled script.
     * (non-Javadoc)
     * @see java.lang.ClassLoader#findClass(java.lang.String)
     */
    protected Class findClass(String classname) throws ClassNotFoundException {
        System.out.println("groovy: find class " + classname);
        Class groovyClass = (Class) sources.get(classname);
        if (groovyClass == null) {
            return super.findClass(classname);
        }
        return groovyClass;
    }

    /**
     * Parse the Groovy script into a Java class.
     * @param uri - The location of the groovy script file to be compiled
     * @return The java class compiled from the Groovy script source file at the given uri
     */
    public Class getGroovyClass(String uri) {
        try {
	        Source source = resolver.resolveURI(uri);
            Class clazz = groovyClassLoader.parseClass(source.getInputStream());
	        sources.put(clazz.getName(), clazz);
	        // Prepare the byte code to be returned for bcel (the *.class")
            String resouceFileName = StringUtils.replaceChars(clazz.getName(), '.', '/') + ".class";
	        CompilationUnit c = new CompilationUnit();
            c.addSource(null, source.getInputStream());
            c.compile(Phases.CLASS_GENERATION); 
            Iterator classiterator = c.getClasses().iterator();
            GroovyClass groovyClass = (GroovyClass)classiterator.next();
            compiledClasses.put(resouceFileName, groovyClass);
	        return clazz;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (CompilationFailedException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /* 
     * This method is need to intercept the calls done by BCEL.
     * BCEL use it to get the ".class" file of the compiled Groovy script.
     * (non-Javadoc)
     * @see java.lang.ClassLoader#getResourceAsStream(java.lang.String)
     */
    public InputStream getResourceAsStream(String name) {
        GroovyClass groovyClass = (GroovyClass)compiledClasses.get(name);
        if (groovyClass == null) {
            return super.getResourceAsStream(name);
        }
	    return new ByteArrayInputStream(groovyClass.getBytes());
    }

    public void service(ServiceManager manager) throws ServiceException {
        resolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
    }
}
