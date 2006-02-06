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
import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.StringTokenizer;

/**
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Id$
 */
public class Loader {

    static boolean verbose = true;
    
    static final String REPOSITORIES = "loader.jar.repositories";
    static final String MAIN_CLASS = "loader.main.class";
    static final String VERBOSE_PROPERTY = "loader.verbose";
    static final String CLASSPATH_PROPERTY = "loader.class.path";
    
    class RepositoryClassLoader extends URLClassLoader {

        public RepositoryClassLoader(ClassLoader parent) {
            super(new URL[0], parent);
        }
            
        public void addRepository(File repository) {
            if (verbose) System.out.println("Processing repository: " + repository);

            if (repository.exists() && repository.isDirectory()) {
                File[] jars = repository.listFiles();
    
                for (int i = 0; i < jars.length; i++) {
                    if (jars[i].getAbsolutePath().endsWith(".jar")) {
                        try  {
                            URL url = jars[i].toURL();
                            if (verbose) System.out.println("Adding jar: " + jars[i]);
                            super.addURL(url);                
                        } catch (MalformedURLException e) {
                            throw new IllegalArgumentException(e.toString());
                        }
                    }
                }
            }
        }

        public void addFile(File file) throws MalformedURLException {
            if (verbose) System.out.println("Adding path: " + file);
            super.addURL(file.toURL());
        }
    }

    public static void main(String[] args) throws Exception {
        new Loader().run(args);
    }

    void run(String[] args) throws Exception 
    {
        String repositories = System.getProperty(REPOSITORIES);
        if (repositories == null) {
            System.out.println("Loader requires the '" + REPOSITORIES + "' property to be set");
            System.exit(1);
        }

        String mainClass = System.getProperty(MAIN_CLASS);
        if (mainClass == null) {
            System.out.println("Loader requires the '" + MAIN_CLASS + "' property to be set");
            System.exit(1);
        }

        String verboseProperty = System.getProperty(VERBOSE_PROPERTY);
        if (verboseProperty != null)
            verbose = Boolean.valueOf(verboseProperty).booleanValue();
        String classPath = System.getProperty(CLASSPATH_PROPERTY);

        if (verbose) System.out.println("-------------------- Loading --------------------");

        RepositoryClassLoader classLoader = new RepositoryClassLoader(this.getClass().getClassLoader());

        StringTokenizer st = new StringTokenizer(repositories, File.pathSeparator);
        while (st.hasMoreTokens()) {
            classLoader.addRepository(new File(st.nextToken()));        
        }

        if (classPath != null) {
            st = new StringTokenizer(classPath, File.pathSeparator);
            while (st.hasMoreTokens()) {
                classLoader.addFile(new File(st.nextToken()));
            }
        }

        Thread.currentThread().setContextClassLoader(classLoader);

        if (verbose) System.out.println("-------------------- Executing -----------------");
        if (verbose) System.out.println("Main Class: " + mainClass);
            
        invokeMain(classLoader, mainClass, args);            
    }
        
    void invokeMain(ClassLoader classloader, String classname, String[] args)
    throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException 
    {
        Class invokedClass = classloader.loadClass(classname);
        
        Class[] methodParamTypes = new Class[1];
        methodParamTypes[0] = args.getClass();
        
        Method main = invokedClass.getDeclaredMethod("main", methodParamTypes);
        
        Object[] methodParams = new Object[1];
        methodParams[0] = args;
        
        main.invoke(null, methodParams);
    }    
}
