/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

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
package org.apache.cocoon.precept.stores.bean;

import org.apache.avalon.framework.component.ComponentException;

import org.apache.avalon.framework.configuration.Configurable;

import org.apache.avalon.framework.configuration.Configuration;

import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.cocoon.components.classloader.ClassLoaderManager;

import org.apache.cocoon.xml.DocumentHandlerAdapter;

import org.apache.commons.jxpath.JXPathContext;

import org.exolab.castor.mapping.Mapping;

import org.exolab.castor.mapping.MappingException;

import org.exolab.castor.xml.MarshalException;

import org.exolab.castor.xml.Marshaller;

import org.exolab.castor.xml.ValidationException;

import org.xml.sax.ContentHandler;

import org.xml.sax.SAXException;

import org.apache.cocoon.precept.*;

import org.apache.cocoon.precept.stores.AbstractInstance;


import java.util.Collection;

/**
 * @author Torsten Curdt <tcurdt@dff.st>
 * @since Mar 15, 2002
 * @version CVS $Id: InstanceImpl.java,v 1.2 2003/03/16 17:49:05 vgritsenko Exp $
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
            } catch (ComponentException e) {
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
