/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.StringTokenizer;

/**
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.3 $ $Date: 2004/03/05 02:24:26 $
 */
public class Loader {

    static final boolean VERBOSE = true;
    
    static final String REPOSITORIES = "loader.jar.repositories";
    static final String MAIN_CLASS = "loader.main.class";
    
    class RepositoryClassLoader extends URLClassLoader {

        public RepositoryClassLoader(ClassLoader parent) {
            super(new URL[0], parent);
        }
            
        public void addRepository(File repository) {
            if (VERBOSE) System.out.println("Processing repository: " + repository);

            if (repository.exists() && repository.isDirectory()) {
                File[] jars = repository.listFiles();
    
                for (int i = 0; i < jars.length; i++) {
                    if (jars[i].getAbsolutePath().endsWith(".jar")) {
                        try  {
                            URL url = jars[i].toURL();
                            if (VERBOSE) System.out.println("Adding jar: " + jars[i]);
                            super.addURL(url);                
                        } catch (MalformedURLException e) {
                            throw new IllegalArgumentException(e.toString());
                        }
                    }
                }
            }
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

        if (VERBOSE) System.out.println("-------------------- Loading --------------------");

        RepositoryClassLoader classLoader = new RepositoryClassLoader(this.getClass().getClassLoader());

        StringTokenizer st = new StringTokenizer(repositories, File.pathSeparator);
        while (st.hasMoreTokens()) {
            classLoader.addRepository(new File(st.nextToken()));        
        }        

        Thread.currentThread().setContextClassLoader(classLoader);

        if (VERBOSE) System.out.println("-------------------- Executing -----------------");
        if (VERBOSE) System.out.println("Main Class: " + mainClass);
            
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