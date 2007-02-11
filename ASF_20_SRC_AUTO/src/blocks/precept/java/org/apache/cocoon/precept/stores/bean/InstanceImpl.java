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
package org.apache.cocoon.precept.stores.bean;

import java.util.Collection;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;

import org.apache.cocoon.components.classloader.ClassLoaderManager;
import org.apache.cocoon.precept.Context;
import org.apache.cocoon.precept.InvalidXPathSyntaxException;
import org.apache.cocoon.precept.Preceptor;
import org.apache.cocoon.precept.PreceptorViolationException;
import org.apache.cocoon.precept.stores.AbstractInstance;
import org.apache.cocoon.xml.DocumentHandlerAdapter;

import org.apache.commons.jxpath.JXPathContext;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * @author Torsten Curdt <tcurdt@dff.st>
 * @since Mar 15, 2002
 * @version CVS $Id: InstanceImpl.java,v 1.4 2004/03/05 13:02:20 bdelacretaz Exp $
 */
public class InstanceImpl extends AbstractInstance implements Configurable {

    private Preceptor preceptor;
    private Mapping mapping;
    private Object bean;
    private JXPathContext beanContext;

    public void setBean(Object bean) {
        this.bean = bean;
        this.beanContext = JXPathContext.newContext(bean);
    }

    public void configure(Configuration configuration) throws ConfigurationException {
        Configuration clazzConf = configuration.getChild("class", false);
        if (clazzConf != null) {
            ClassLoaderManager clazzLoader = null;
            try {
                String clazzName = clazzConf.getValue();
                String mappingURI = clazzConf.getAttribute("mapping");

                if (mappingURI != null) {
                    mapping = new Mapping();
                    // resolve
                    //mapping.loadMapping(getFile(resolver,mappingURI));
                    getLogger().debug("bean class = [" + clazzName + "] mapping [" + mappingURI + "]");
                } else {
                    getLogger().debug("bean class = [" + clazzName + "] using default mapping");
                }

                clazzLoader = (ClassLoaderManager) manager.lookup(ClassLoaderManager.ROLE);
                Class clazz = clazzLoader.loadClass(clazzName);
                setBean(clazz.newInstance());
            } catch (ServiceException e) {
                throw new ConfigurationException("", e);
            } catch (ClassNotFoundException e) {
                throw new ConfigurationException("", e);
            } catch (InstantiationException e) {
                throw new ConfigurationException("", e);
            } catch (IllegalAccessException e) {
                throw new ConfigurationException("", e);
            } finally {
                manager.release(clazzLoader);
            }
        }
    }

    public void setValue(String xpath, Object value) throws PreceptorViolationException, InvalidXPathSyntaxException {
        setValue(xpath, value, null);
    }

    public void setValue(String xpath, Object value, Context context) throws PreceptorViolationException, InvalidXPathSyntaxException {
        try {
            beanContext.setValue(xpath, value);
        }
        catch (Exception e) {
            throw new PreceptorViolationException(e);
        }
    }

    public Object getValue(String xpath) throws InvalidXPathSyntaxException {
        try {
            return (beanContext.getValue(xpath));
        }
        catch (Exception e) {
            throw new InvalidXPathSyntaxException(e);
        }
    }

    public void setPreceptor(Preceptor preceptor) {
        this.preceptor = preceptor;
        preceptor.buildInstance(this);
    }

    public Preceptor getPreceptor() {
        return (preceptor);
    }

    public long getLastModified() {
        //NYI
        return 0;
    }

    public void toSAX(ContentHandler handler, boolean withConstraints) throws SAXException {
        try {
            Marshaller marshaller = new Marshaller(new DocumentHandlerAdapter(handler));
            if (mapping != null) {
                marshaller.setMapping(mapping);
            }
            marshaller.marshal(bean);
        }
        catch (ValidationException e) {
            throw new SAXException(e);
        }
        catch (MappingException e) {
            throw new SAXException(e);
        }
        catch (MarshalException e) {
            throw new SAXException(e);
        }
    }

    public Collection getNodePaths() {
        return null;
    }
}
