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

import java.util.Iterator;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

/**
 * Represents a service endpoint in a WSDL document.
 */
public class EndpointDefinition {

    private Definition wsdlDef;
    private String namespaceURI;
    private Service service;
    private Port port;

    /**
     * Creates a new EndpointDefinition. If a particular service is not
     * identified the endpoint is created using the first service definition
     * occurring in the WSDL with a port with a SOAP binding.
     *
     * @param wsdlURL
     * @param serviceName
     * @param portName
     * @throws WSDLException
     * @throws InvalidServiceException
     */
    public EndpointDefinition(String wsdlURL, String serviceName,
            String portName) throws WSDLException, InvalidServiceException {
        wsdlDef = loadWSDLDefinition(wsdlURL);
        namespaceURI = wsdlDef.getTargetNamespace();
        initialize(serviceName, portName);
    }

    /**
     * Returns the target namespace of the WSDL document in which this endpoint
     * is defined.
     *
     * @return String
     */
    public String getNamespaceURI() {
        return namespaceURI;
    }

    /**
     * Returns the name of the service that this endpoint belongs to.
     *
     * @return String
     */
    public String getServiceName() {
        return service.getQName().getLocalPart();
    }

    /**
     * Returns the name of the port that concretely defines the endpoint.
     *
     * @return String
     */
    public String getPortName() {
        return port.getName();
    }

    /**
     * Sets the service and port for this endpoint using the named entities if
     * provided.
     *
     * @param serviceName
     * @param portName
     * @throws InvalidServiceException
     */
    private void initialize(String serviceName, String portName)
            throws InvalidServiceException {
        if (serviceName != null) {
            service = getServiceNamed(serviceName);
        } else {
            service = getDefaultService();
        }
        // if using default service a port was already created in validating the
        // service.
        if (port == null) {
            if (portName != null) {
                port = getPortNamed(portName);
            } else {
                // returns 1st port with a soap binding for the given service or
                // throws an exception if none found
                port = getDefaultPort(service);
            }
        }
    }

    /**
     * Loads the WSDL document containing the definition of this endpoint.
     *
     * @param wsdlURL
     * @return @throws
     *         WSDLException
     */
    private Definition loadWSDLDefinition(String wsdlURL) throws WSDLException {
        WSDLFactory factory = WSDLFactory.newInstance();
        WSDLReader reader = factory.newWSDLReader();
        reader.setFeature("javax.wsdl.verbose", false);
        reader.setFeature("javax.wsdl.importDocuments", true);
        Definition wsdlDef = reader.readWSDL(wsdlURL);
        return wsdlDef;
    }

    /**
     * Returns an object representation of the named service.
     *
     * @param serviceName
     */
    private Service getServiceNamed(String serviceName) {
        return wsdlDef.getService(new QName(namespaceURI, serviceName));
    }

    /**
     * Returns an object representing the default service if none is specified
     * when this endpoint is created. The default service is determined by
     * selecting the 1st service that has a port with a SOAP binding.
     *
     * @return @throws
     *         InvalidServiceException
     */
    private Service getDefaultService() throws InvalidServiceException {
        Map services = wsdlDef.getServices();
        Iterator it = services.values().iterator();
        while (it.hasNext()) {
            Service service = (Service) it.next();
            Port port = getDefaultPort(service);
            if (port != null) {
                this.port = port;
                return service;
            }
        }
        String msg = "No services defined in WSDL document named: "
                + wsdlDef.getQName().getLocalPart();
        throw new InvalidServiceException(msg);
    }

    /**
     * Returns an object representation of the named port.
     *
     * @param portName
     */
    private Port getPortNamed(String portName) {
        Map serviceMap = wsdlDef.getServices();
        Iterator services = serviceMap.values().iterator();
        while (services.hasNext()) {
            Service service = (Service) services.next();
            Map portMap = service.getPorts();
            Iterator ports = portMap.values().iterator();
            while (ports.hasNext()) {
                Port port = (Port) ports.next();
                if (port.getName().equals(portName)) {
                    return port;
                }
            }
        }
        return null;
    }

    /**
     * Returns an object representing the default port if none is specified at
     * the time this endpoint is created. The default port is determined by
     * selecting the 1st port, for the given service, that has a SOAP binding.
     *
     * @param service
     * @throws InvalidServiceException
     */
    private Port getDefaultPort(Service service) throws InvalidServiceException {
        Map ports = service.getPorts();
        Iterator it = ports.values().iterator();
        while (it.hasNext()) {
            Port port = (Port) it.next();
            if (hasSoapBinding(port)) {
                return port;
            }
        }
        String msg = "No ports with SOAP binding for service named: "
                + service.getQName().getLocalPart();
        throw new InvalidServiceException(msg);
    }

    /**
     * Tests if a port has a SOAP binding.
     *
     * @param port
     */
    private boolean hasSoapBinding(Port port) {
        Iterator it = port.getExtensibilityElements().iterator();
        while (it.hasNext()) {
            Object element = it.next();
            if (element instanceof SOAPAddress) {
                return true;
            }
        }
        return false;
    }
}
