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
package org.apache.cocoon;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DynamicConfigurator;
import org.apache.tools.ant.ExitException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Ant task for running Cocoon. Allows for the embedding of Cocoon into 
 *
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a>
 * @version CVS $Id: CocoonTask.java,v 1.4 2004/03/05 13:02:42 bdelacretaz Exp $
 */
public class CocoonTask extends Task implements DynamicConfigurator {

    private CommandlineJava cmdl = new CommandlineJava();
    private boolean failOnError = false;
    private Throwable caught = null;

    private String uriGroup = null;
    private Document xconf;
    private Element root;
    private ElementWrapper _wrapper;
    
    private static final String CLASS_DELEGATE = "org.apache.cocoon.bean.helpers.AntDelegate";
            
    public CocoonTask() {
        try {
            DocumentBuilder builder =
                DocumentBuilderFactory.newInstance().newDocumentBuilder();
            xconf = builder.newDocument();
            root = xconf.createElement("cocoon");
            xconf.appendChild(root);
            _wrapper = new ElementWrapper(root);
            cmdl.setClassname(CLASS_DELEGATE);
        }
        catch (ParserConfigurationException e) {
            throw new BuildException(e);
        }
    }
    
    /**
     * Adds a path to the classpath.
     *
     * @return created classpath
     */
    public Path createClasspath() {
        return cmdl.createClasspath(getProject()).createPath();
    }

    /**
     * Classpath to use, by reference.
     *
     * @param r a reference to an existing classpath
     */
    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }

    /**
     * Set the classpath to be used when running the Java class
     *
     * @param s an Ant Path object containing the classpath.
     */
    public void setClasspath(Path s) {
        createClasspath().append(s);
    }

    public void setUrigroup(String group) {
        this.uriGroup = group;
    }
 
    /**
     * A dynamic configurator for each element.
     */
    private static class ElementWrapper
                         implements DynamicConfigurator {

        private Node node;

        /** Instantiate a root wrapper */
        private ElementWrapper(Node node) {
            this.node = node;
        }

        /** Instantiate a child wrapper */
        private ElementWrapper(Node parent, String childName) {
            Document document = parent.getOwnerDocument();
            if (document == null) {
              document = (Document)parent; // Node is the document!
            }
            node = document.createElement(childName);
            parent.appendChild(node);
        }

        //
        // interface DynamicConfigurator
        public void setDynamicAttribute(String name, String value)
                    throws BuildException {
            // Never called for anything by Element wrappers
            Element element = (Element)node;
            element.setAttribute(name, value);
        }

        public Object createDynamicElement(String name)
                      throws BuildException {
            return new ElementWrapper(node, name);
        }
    }

    public File getLibDir() throws BuildException {
        Element root = xconf.getDocumentElement();
        String contextDir = null;
        if (root!=null) {
            if (hasAttribute(root, "context-dir")){
                contextDir = getAttributeValue(root, "context-dir");
            }
        }
        if (contextDir != null) {
            return new File(contextDir + "/WEB-INF/lib");
        } else {
            throw new BuildException("No context directory specified. Cannot find Cocoon");
        }
    }

    private static String getAttributeValue(Node node, String attr) throws IllegalArgumentException {
        NamedNodeMap nodes = node.getAttributes();
        if (nodes != null) {
            Node attribute = nodes.getNamedItem(attr);
            if (attribute != null && attribute.getNodeValue() != null) {
                return attribute.getNodeValue();
            }
        }
        throw new IllegalArgumentException("Missing " + attr + " attribute on <" + node.getNodeName() + "> node");
    }

    private static boolean hasAttribute(Node node, String attr) {
        NamedNodeMap nodes = node.getAttributes();
        if (nodes != null) {
            Node attribute = nodes.getNamedItem(attr);
            return (attribute != null);
        }
        return false;
    }

    //
    // interface DynamicConfigurator
    public void setDynamicAttribute(String name, String value)
                throws BuildException {
        root.setAttribute(name, value);
    }

    public Object createDynamicElement(String name)
                  throws BuildException {
        return _wrapper.createDynamicElement(name);
    }

    /**
     * Do the execution and return a return code.
     *
     * @return the return code from the execute java class if it was
     * executed in a separate VM (fork = "yes").
     *
     * @throws BuildException if required parameters are missing
     */
    public void execute() throws BuildException {
        // FIXME - This is never read
        int err= -1;

        if (cmdl.getClasspath() == null) {
            throw new BuildException("Could not find a classpath that points to the Cocoon classes");
        }
        try {
            try {
                execute(cmdl);
                err = 0;
            } catch (ExitException ex) {
                err = ex.getStatus();
            }
        } catch (BuildException e) {
            if (failOnError) {
                throw e;
            } else {
                log(e.getMessage(), Project.MSG_ERR);
                err = 0;
            }
        } catch (Throwable t) {
            if (failOnError) {
                throw new BuildException(t);
            } else {
                log(t.getMessage(), Project.MSG_ERR);
                err = 0;
            }
        }
    }
    
    public void execute(CommandlineJava command) throws BuildException {
        final String classname = command.getJavaCommand().getExecutable();

        AntClassLoader loader = null;
        try {
            if (command.getSystemProperties() != null) {
                command.getSystemProperties().setSystem();
            }

            final Class[] param = {Class.forName("org.w3c.dom.Document"), Class.forName("java.lang.String")};
            Class target = null;
            if (command.getClasspath() == null) {
                target = Class.forName(classname);
            } else {
                loader = new AntClassLoader(getProject().getCoreLoader(), project, 
                                            command.getClasspath(), false);
                loader.setIsolated(true);
                loader.setThreadContextLoader();
                target = loader.forceLoadClass(classname);
                AntClassLoader.initializeClass(target);
            }
            Method method = target.getMethod("process", param);
            if (method == null) {
                throw new BuildException("Could not find process() method in "
                                         + classname);
            }

            run(method);

            if (caught != null) {
                throw caught;
            }

        } catch (ClassNotFoundException e) {
            throw new BuildException("Could not find " + classname + "."
                                     + " Make sure you have it in your"
                                     + " classpath");
        } catch (SecurityException e) {
            throw e;
        } catch (Throwable e) {
            throw new BuildException(e);
        } finally {
            if (loader != null) {
                loader.resetThreadContextLoader();
                loader.cleanup();
            }
            if (command.getSystemProperties() != null) {
                command.getSystemProperties().restoreSystem();
            }
        }
    }

    public void run(Method method) {
        final Object[] argument = {xconf, uriGroup};
        try {
            method.invoke(null, argument);
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (!(t instanceof InterruptedException)) {
                caught = t;
            } /* else { swallow, probably due to timeout } */
        } catch (Throwable t) {
            caught = t;
        } finally {
            synchronized (this) {
                notifyAll();
            }
        }
    }
}
