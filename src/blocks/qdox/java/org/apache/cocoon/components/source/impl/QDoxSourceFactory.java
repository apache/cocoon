/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.cocoon.components.source.impl;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.commons.lang.StringUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceResolver;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;


/**
 * This {@link org.apache.excalibur.source.SourceFactory SourceFactory} creates {@link
 * org.apache.excalibur.source.Source Source}s for the <code>javadoc:</code> URI scheme.
 * 
 * <p>The goal for this <code>SourceFactory</code> is to provide a <code>Source</code>
 * for a Java sourcefile containing as much information as possible to mimic the
 * standard Javadoc output.</p>
 * 
 * <p>The Source provides the following content:
 * <ul>
 *   <li>Classname</li>
 *   <li>Superclass</li>
 *   <li>Imports, including <code>java.lang</code> and the class' package</li>
 *   <li>Implemented interfaces</li>
 *   <li>Inner classes/interfaces, including superclass, implemented interfaces and
 * Javadoc (inner classes can be requested separately)</li>
 *   <li>Fields, including type, name and Javadoc</li>
 *   <li>Constructors, including parameters with their types and names, signature,
 * Javadoc and thrown exceptions</li>
 *   <li>Methods, including returntype, parameters, signature, Javadoc and thrown
 * exceptions</li>
 *   <li>Inheritance tree for each Class member, if needed</li>
 *   <li>Private members, if needed</li>
 * </ul>
 * </p>
 * 
 * <p>With this <code>SourceFactory</code>, you create Doclets with XSLT stylesheets
 * instead of Java code.</p>
 * 
 * <p>The <code>QDoxSourceFactory</code> uses <a href="http://qdox.sf.net/">QDox</a>
 * to parse the Java sourcefiles.
 * </p>
 * 
 * @author <a href="mailto:b.guijt1@chello.nl">Bart Guijt</a>
 * @version CVS $Revision: 1.5 $ $Date: 2004/04/03 00:46:33 $
 */
public final class QDoxSourceFactory
    extends AbstractLogEnabled
    implements SourceFactory, Serviceable, Configurable, ThreadSafe {
    
    protected final static String INCLUDE_INHERITANCE_ELEMENT = "include-inheritance";
    protected final static String VALUE_ATTRIBUTE = "value";
    protected final static String SOURCE_GROUP_ELEMENT = "source-roots";
    protected final static String GROUP_ATTRIBUTE = "group";
    protected final static String SOURCE_ROOT_ELEMENT = "source-root";
    protected final static String URI_ATTRIBUTE = "uri";
    
    protected ServiceManager manager;
    protected List sourceRootUris;
    
    /**
     * RegExp matcher for Java classnames: distinguishes package and classname.
     */
    protected RE rePackageClass;
    
    /**
     * RegExp matcher for Java classnames: distinguishes package, classname and innerclassname.
     */
    protected RE rePackageClassInnerclass;
    
    
    /**
     * Represents an URI and which packages it contains.
     * 
     * <p>Using this class, the QDoxSourceFactory can quickly find the right SourceRoot URI given a specified
     * package.</p>
     */
    protected final class SourceRoot {
        private List packages;
        private String sourceRootUri;
        
        protected SourceRoot(String uri) {
            if (!uri.endsWith(File.separator)) {
                uri += '/';
            }
            sourceRootUri = uri;
            packages = new ArrayList();
        }
        
        protected void addPackage(String packageName) {
            packages.add(packageName);
        }
        
        protected boolean hasPackage(String packageName) {
            return packages.contains(packageName);
        }
        
        protected String getUri() {
            return sourceRootUri;
        }
    }
    
    
    /**
     * @see org.apache.excalibur.source.SourceFactory#getSource(java.lang.String, java.util.Map)
     */
    public Source getSource(String location, Map parameters) throws MalformedURLException, IOException, SourceException {
        String className = StringUtils.substringAfter(location, ":");
        Source javaSource = null;
        if (className.length() > 0) {
            try {
                if(getLogger().isDebugEnabled()) {
                    getLogger().debug("getSource called with className=" + className);
                }
                javaSource = getSource(className);
            } catch (ServiceException se) {
                throw new SourceException("SourceResolver not found", se);
            }
        } else {
            throw new MalformedURLException();
        }

        QDoxSource result = null;
        if (javaSource != null) {
            return new QDoxSource(location, javaSource, getLogger(), manager);
        }

        if(getLogger().isDebugEnabled()) {
            getLogger().debug("returning source=" + result + " for className=" + className);
        }

        return result;
    }
    
    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Composing the QDoxSourceFactory...");
        }
        this.manager = manager;
        
        try {
            rePackageClass = new RE("([$\\w.]+)\\.([$\\w]+)");
            rePackageClassInnerclass = new RE("([$\\w.]+)\\.([$\\w]+)\\.([$\\w]+)");
        } catch (RESyntaxException e) {
            getLogger().error("RegExp syntax error!", e);
        }
    }
    
    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config) throws ConfigurationException {
        Configuration[] sourceRootGroups = config.getChildren(SOURCE_GROUP_ELEMENT);
        sourceRootUris = new ArrayList();
        
        for (int i=0; i<sourceRootGroups.length; i++) {
            Configuration[] sourceRootConfigs = sourceRootGroups[i].getChildren(SOURCE_ROOT_ELEMENT);
            
            for (int j=0; j<sourceRootConfigs.length; j++) {
                String uri = sourceRootConfigs[j].getAttribute(URI_ATTRIBUTE);
                sourceRootUris.add(new SourceRoot(uri));
            }
        }
        
        if (sourceRootUris.size() == 0 && getLogger().isErrorEnabled()) {
            getLogger().error("No source roots configured!");
        }
    }
    
    /**
     * Releases the specified Source.
     * 
     * @see org.apache.excalibur.source.SourceFactory#release(org.apache.excalibur.source.Source)
     */
    public void release(Source source) {
        // ??? What to do here?
    }
    
    /**
     * Method getSource.
     * 
     * @param className
     * @return File
     */
    private Source getSource(String className) throws ServiceException {
        String classFileName = className;
        String packageName;
        
        if (rePackageClass.match(className)) {
            packageName = rePackageClass.getParen(1);
        } else {
            packageName = "";
        }
        
        classFileName = classFileName.replace('.', '/') + ".java";
        SourceResolver resolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
        
        Source source = getSource(classFileName, packageName, resolver); 
        if (source == null && rePackageClassInnerclass.match(className)) {
            // Inner class?
            
            packageName = rePackageClassInnerclass.getParen(1);
            classFileName = className.substring(0, className.lastIndexOf('.')).replace('.', '/') + ".java";
            source = getSource(classFileName, packageName, resolver);
        }
        manager.release(resolver);
            
        if (source == null && getLogger().isWarnEnabled()) {
            getLogger().warn("No source found for class '" + className + "'!");
        }

        return source;
    }
    
    private Source getSource(String classFileName, String packageName, SourceResolver resolver) {
        // First, test whether there are configured packages to speed things up:
        for (Iterator i = sourceRootUris.iterator(); i.hasNext();) {
            SourceRoot sourceRoot = (SourceRoot) i.next();
            
            if (sourceRoot.hasPackage(packageName)) {
                String uri = sourceRoot.getUri() + classFileName;
                Source source = getSource(uri, resolver);
                if (source != null) {
                    return source;
                }
            }
        }
        
        // No suitable package found, iterate all source roots:
        for (Iterator i = sourceRootUris.iterator(); i.hasNext();) {
            SourceRoot sourceRoot = (SourceRoot) i.next();
            String uri = sourceRoot.getUri() + classFileName;
            
            Source source = getSource(uri, resolver);
            if (source != null) {
                sourceRoot.addPackage(packageName);
                return source;
            }
        }
        return null;
    }
    
    /**
     * Method getSource.
     * 
     * @param uri
     * @param resolver
     * @return Source
     */
    private Source getSource(String uri, SourceResolver resolver) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Testing uri <" + uri + ">...");
        }
        try {
            Source source = resolver.resolveURI(uri);
                
            if (source != null && source.getInputStream() != null) {
                return source;
            } else {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("uri <" + uri + "> is invalid.");
                }
            }
        } catch (Exception e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("uri <" + uri + "> is invalid: " + e.getClass().getName() + " says " + e.getMessage());
            }
        }
        return null;
    }
}
