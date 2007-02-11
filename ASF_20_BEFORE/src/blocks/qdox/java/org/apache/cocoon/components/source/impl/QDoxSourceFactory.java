/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Revision: 1.3 $ $Date: 2003/11/15 04:21:29 $
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
        String className = location.substring(location.indexOf(':') + 1);
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