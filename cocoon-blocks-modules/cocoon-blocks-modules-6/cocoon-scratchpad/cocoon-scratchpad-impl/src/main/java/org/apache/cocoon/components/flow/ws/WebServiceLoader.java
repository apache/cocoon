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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.rmi.Remote;
import javax.wsdl.WSDLException;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.axis.utils.JavaUtils;
import org.apache.axis.wsdl.toJava.Namespaces;
import org.apache.axis.wsdl.toJava.Utils;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.store.Store;

import org.apache.cocoon.Constants;
import org.apache.cocoon.util.AbstractLogEnabled;

/**
 * Given a WSDL URL generates and compiles client bindings for the requested
 * service endpoint.
 */
public class WebServiceLoader extends AbstractLogEnabled
                              implements Contextualizable, ThreadSafe, Serviceable, Disposable {

    protected ServiceManager serviceManager;
    // protected CompilingClassLoader classLoader;
    // protected MyClassRepository javaClassRepository = new MyClassRepository();
    protected String sourcePath;
    protected SourceResolver resolver;
    private Store endpointCache;


    /**
     * Loads a SOAP endpoint using the first service definition with a SOAP binding found in the referenced WSDL
     * document.
     *
     * @param wsdlURL
     * @return @throws
     *         Exception
     * @throws InvalidServiceException
     * @throws LoadException
     */
    public Remote load(String wsdlURL) throws Exception, LoadException {
        return load(wsdlURL, null, null);
    }

    /**
     * Loads a SOAP endpoint using the endpoint identified by the given
     * service/port combination.
     *
     * @param wsdlURL
     * @param serviceName
     * @param portName
     * @return @throws
     *         LoadException
     */
    public Remote load(String wsdlURL, String serviceName, String portName) throws LoadException {
        // return endpoint proxy from cache if we have it, should check every so
        // often to see if
        // WSDL has been modifed and regenerate bindings if required
        String key = null;
        if (this.endpointCache != null) {
            key = makeEndpointKey(wsdlURL, serviceName, portName);
            if (this.endpointCache.containsKey(key)) {
                return (Remote) this.endpointCache.get(key);
            }
        }

        // create the endpoint definition
        EndpointDefinition endpointDefn;
        try {
            endpointDefn = new EndpointDefinition(wsdlURL, serviceName,
                    portName);
        } catch (WSDLException e) {
            String msg = "Could not load web service, error reading WSDL";
            throw new LoadException(msg, e);
        } catch (InvalidServiceException e) {
            String msg = "Could not load web service, no valid service declared in WSDL";
            throw new LoadException(msg, e);
        }

        if (!bindingsAreGenerated(endpointDefn.getNamespaceURI(), endpointDefn
                .getServiceName())) {
            generateClientBindings(wsdlURL);
        }

        // instantiate service and return endpoint
        Remote endpoint = getServiceEndPoint(endpointDefn.getServiceName(),
                endpointDefn.getPortName(), endpointDefn.getNamespaceURI());

        // cache the proxy
        if (this.endpointCache != null) {
            try {
                this.endpointCache.store(key, endpoint);
            } catch (IOException e) {
                String msg = "Error storing proxy in endpoint cache";
                getLogger().error(msg);
            }
        }
        return endpoint;
    }

    /**
     * Creates a key that uniquely identifies a SOAP endpoint
     *
     * @param wsdlURL
     * @param serviceName
     * @param portName
     */
    private String makeEndpointKey(String wsdlURL, String serviceName, String portName) {
        String key = wsdlURL;
        if (serviceName != null) {
            key += serviceName;
        }
        if (portName != null) {
            key += portName;
        }
        return key;
    }

    /**
     * Dynamically instantiates a service locator and uses this to get the
     * endpoint stub
     *
     * @param serviceName
     * @param portName
     * @param namespaceURI
     * @throws LoadException
     */
    private Remote getServiceEndPoint(String serviceName, String portName,
            String namespaceURI) throws LoadException {
        try {
            String serviceLocatorClass = getServiceLocatorClassName(
                    namespaceURI, serviceName);
            Class c = Class
                    .forName(serviceLocatorClass, true, getClassLoader());
            String portAccessorName = getPortAccessorName(c, portName);
            Method m = c.getMethod(portAccessorName, null);
            Remote endpoint = (Remote) m.invoke(c.newInstance(), null);
            return endpoint;
        } catch (Exception e) {
            String msg = "Error loading web service: " + serviceName;
            getLogger().error(msg, e);
            throw new LoadException(msg, e);
        }
    }

    /**
     * Returns the locator class for the requested service
     *
     * @param targetNamespace
     * @param serviceName
     */
    private String getServiceLocatorClassName(String targetNamespace,
            String serviceName) {
        String serviceNameCaps = Utils.capitalizeFirstChar(serviceName);
        String packageName = Utils.makePackageName(targetNamespace);
        if (packageName == null) {
            packageName = "";
        } else {
            packageName = packageName + ".";
        }
        String serviceLocatorClassName = packageName + serviceNameCaps + "Locator";
        return serviceLocatorClassName;
    }

    /**
     * Returns the accessor method for the requested port
     *
     * @param serviceLocatorClass
     * @param portName
     */
    private String getPortAccessorName(Class serviceLocatorClass,
            String portName) throws SecurityException {
        String portAccessorName = "get"
                + Utils.capitalizeFirstChar(JavaUtils.xmlNameToJava(portName));
        return portAccessorName;
    }

    /**
     * @param wsdlURL
     */
    private void generateClientBindings(String wsdlURL) {
        ClientBindingGenerator cbg = new ClientBindingGenerator();
        cbg.generate(wsdlURL, this.sourcePath);
    }

    /**
     * Tests if client bindings have been generated for the WSDL.
     */
    private boolean bindingsAreGenerated(String namespace, String serviceName) {
        try {
            Namespaces namespaces = new Namespaces(this.sourcePath);
            String packageName = Utils.makePackageName(namespace);
            namespaces.put(namespace, packageName);
            String fileName = Utils.capitalizeFirstChar(serviceName) + ".java";
            return Utils.fileExists(fileName, namespace, namespaces);
        } catch (IOException e) {
            getLogger().error("Error checking for binding class for service: " + serviceName);
        }
        return false;
    }

    /**
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(final Context context) throws ContextException {
        final File workDir = (File) context.get(Constants.CONTEXT_WORK_DIR);
        this.sourcePath = workDir.getAbsolutePath() + File.separator + "axis";

    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager serviceManager) throws ServiceException {
        this.serviceManager = serviceManager;
        try {
            this.endpointCache = (Store) this.serviceManager
                    .lookup(Store.TRANSIENT_STORE);
        } catch (ServiceException e) {
            getLogger().error("Could not find component for role " + Store.TRANSIENT_STORE, e);
        }
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.serviceManager != null ) {
            //this.classLoader = null;
            this.serviceManager.release(this.endpointCache);
            this.endpointCache = null;
            this.serviceManager.release(this.resolver);
            this.resolver = null;
            this.serviceManager = null;
        }
    }

    private ClassLoader getClassLoader() throws Exception {
        return null;
    }
    
    /*
    private ClassLoader getClassLoader() throws Exception {
        synchronized (this.javaClassRepository) {
            if (this.classLoader == null) {
                resolver = (SourceResolver) this.serviceManager.lookup(SourceResolver.ROLE);
                this.classLoader = new CompilingClassLoader(Thread
                        .currentThread().getContextClassLoader(),
                        resolver,
                        this.javaClassRepository);
                this.classLoader
                        .addSourceListener(new CompilingClassLoader.SourceListener() {

                            public void sourceCompiled(Source src) {
                                // no action
                            }

                            public void sourceCompilationError(Source src,
                                    String errMsg) {

                                if (src != null) {
                                    WebServiceLoader.this.logger.error(errMsg);
                                }
                            }
                        });
                updateSourcePath();
            }
            return this.classLoader;
        }
    }

    private void updateSourcePath() {
        if (classLoader != null) {
            classLoader.setSourcePath(new String[] { this.sourcePath });
        }
    }

    class MyClassRepository implements CompilingClassLoader.ClassRepository {

        Map javaSource = new HashMap();
        Map javaClass = new HashMap();
        Map sourceToClass = new HashMap();
        Map classToSource = new HashMap();

        public synchronized void addCompiledClass(String className, Source src,
                byte[] contents) {
            javaSource.put(src.getURI(), src.getValidity());
            javaClass.put(className, contents);
            String uri = src.getURI();
            Set set = (Set) sourceToClass.get(uri);
            if (set == null) {
                set = new HashSet();
                sourceToClass.put(uri, set);
            }
            set.add(className);
            classToSource.put(className, src.getURI());
        }

        public synchronized byte[] getCompiledClass(String className) {
            return (byte[]) javaClass.get(className);
        }

        public synchronized boolean upToDateCheck() throws Exception {
            SourceResolver sourceResolver = (SourceResolver) WebServiceLoader.this.serviceManager.lookup(SourceResolver.ROLE);
            try {
                Iterator iter = javaSource.entrySet().iterator();
                List invalid = new LinkedList();
                while (iter.hasNext()) {
                    Map.Entry e = (Map.Entry) iter.next();
                    String uri = (String) e.getKey();
                    SourceValidity validity = (SourceValidity) e.getValue();
                    int valid = validity.isValid();
                    if (valid == SourceValidity.UNKNOWN) {
                        Source newSrc = null;
                        try {
                            newSrc = sourceResolver.resolveURI(uri);
                            valid = newSrc.getValidity().isValid(validity);
                        } catch (Exception ignored) {
                                // ignore
                        } finally {
                            if (newSrc != null) {
                                sourceResolver.release(newSrc);
                            }
                        }
                    }
                    if (valid != SourceValidity.VALID) {
                        invalid.add(uri);
                    }
                }
                iter = invalid.iterator();
                while (iter.hasNext()) {
                    String uri = (String) iter.next();
                    Set set = (Set) sourceToClass.get(uri);
                    Iterator ii = set.iterator();
                    while (ii.hasNext()) {
                        String className = (String) ii.next();
                        sourceToClass.remove(className);
                        javaClass.remove(className);
                        classToSource.remove(className);
                    }
                    set.clear();
                    javaSource.remove(uri);
                }
                return invalid.size() == 0;
            } finally {
                WebServiceLoader.this.serviceManager.release(sourceResolver);
            }
        }
    }
    */
}