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
package org.apache.cocoon.components.language.markup.xsp;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;

import org.apache.cocoon.components.modules.input.InputModule;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Helper class that caches references to InputModules for use in
 * XSPs. Works in conjunction with the input.xsl
 * logicsheet. References are obtained the first time a module is
 * accessed and kept until the page is completely displayed.
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: XSPModuleHelper.java,v 1.7 2004/02/19 22:13:27 joerg Exp $
 */
public class XSPModuleHelper {

    protected final static String INPUT_MODULE_SELECTOR = InputModule.ROLE+"Selector";

    private static final String PREFIX = "input";
    private static final String URI = "http://apache.org/cocoon/xsp/input/1.0";

    /* Operation codes */
    private final static int OP_GET = 0;
    private final static int OP_VALUES = 1;
    private final static int OP_NAMES = 2;

    private Map inputModules;
    private ComponentManager componentManager;
    private ComponentSelector componentInputSelector;
    private ServiceManager serviceManager;
    private ServiceSelector serviceInputSelector;
    

    /**
     * Get the input module
     */
    private InputModule getInputModule(String name)
    throws CascadingRuntimeException {
        if ( this.inputModules == null ) {
            throw new RuntimeException("ModuleHelper is not setup correctly.");
        }
        InputModule module = (InputModule) this.inputModules.get(name);
        if ( module == null ) {
            try {
                if ( this.componentManager != null ) {
                    if (this.componentInputSelector.hasComponent(name)) {
                        module = (InputModule) this.componentInputSelector.select(name);
                    }                
                } else {
                    if (this.serviceInputSelector.isSelectable(name)) {
                        module = (InputModule) this.serviceInputSelector.select(name);
                    }                            
                }
            } catch (Exception e) {
                throw new CascadingRuntimeException("Unable to lookup input module " + name, e);
            }
            if ( module == null ) {
                throw new RuntimeException("No such InputModule: "+name);
            }
            this.inputModules.put(name, module);
        }
        return module;
    }
    
    /**
     * Capsules use of an InputModule. Does all the lookups and so
     * on. Returns either an Object, an Object[], or an Iterator,
     * depending on the method called i.e. the op specified. The
     * second module is preferred and has an non null name. If an
     * exception is encountered, a warn message is printed and null is
     * returned.
     * @param op an <code>int</code> value encoding the desired operation
     * @param name a <code>String</code> value holding the name of the
     * InputModule
     * @param attr a <code>String</code> value holding the name of the
     * attribute to return. Is disregarded when attribute names is
     * requested.
     * @param objectModel a <code>Map</code> value holding the current
     * ObjectModel
     * @return an <code>Object</code> value
     * @exception CascadingRuntimeException if an error occurs. The real
     * exception can be obtained with <code>getCause</code>.
     */
    private Object get(int op, String name, String attr, Map objectModel, Configuration conf) throws CascadingRuntimeException {

        Object value = null;
        final InputModule input = this.getInputModule(name);

        try {

            switch (op) {
            case OP_GET:    
                value = input.getAttribute(attr, conf, objectModel);
                break;
            case OP_VALUES:
                value = input.getAttributeValues(attr, conf, objectModel);
                break;
            case OP_NAMES:
                value = input.getAttributeNames(conf, objectModel);
                break;
            }

        } catch (Exception e) {
            throw new CascadingRuntimeException("Error accessing attribute '"+attr+"' from input module '"+name+"'. "+e.getMessage(), e);
        }

        return value;
    }

    private Object get(int op, String name, String attr, Map objectModel) throws RuntimeException {
        return get(op, name, attr, objectModel, null);
    }



    /**
     * Initializes the instance for first use. Stores references to
     * component manager and component selector in instance 
     *
     * @param manager a <code>ComponentManager</code> value
     * @exception RuntimeException if an error occurs
     * @deprecated Use the {@link #setup(ServiceManager)} method instead
     */
    public void setup(ComponentManager manager) throws RuntimeException {

        this.inputModules = new HashMap();
        this.componentManager = manager;
        try {
            this.componentInputSelector = (ComponentSelector) this.componentManager.lookup(INPUT_MODULE_SELECTOR); 
        } catch (Exception e) {
            throw new CascadingRuntimeException("Could not obtain selector for InputModule.",e);
        }
    }

    /**
     * Initializes the instance for first use. Stores references to
     * service manager and service selector in instance 
     * 
     * @param manager a <code>ServiceManager</code> value
     * @exception RuntimeException if an error occurs
     */
    public void setup(ServiceManager manager) throws RuntimeException {

        this.inputModules = new HashMap();
        this.serviceManager = manager;
        try {
            this.serviceInputSelector = (ServiceSelector) this.serviceManager.lookup(INPUT_MODULE_SELECTOR); 
        } catch (Exception e) {
            throw new CascadingRuntimeException("Could not obtain selector for InputModule.",e);
        }
    }


    /**
     * Get a single attribute value from a module. Uses cached
     * reference if existing.
     *
     * @param objectModel a <code>Map</code> value
     * @param conf a <code>Configuration</code> containing the module dynamic configuration (aka modeConf)
     * @param module a <code>String</code> value holding the module name
     * @param name a <code>String</code> value holding the attribute name
     * @param deflt an <code>Object</code> value holding a default value
     * @return an <code>Object</code> value
     * @exception RuntimeException if an error occurs
     */
    public Object getAttribute(Map objectModel, Configuration conf, String module, String name, Object deflt) throws RuntimeException {

        Object result = this.get(OP_GET, module, name, objectModel, conf);
        if (result == null) result = deflt;
        return result;
    }

    /**
     * Get a single attribute value from a module.  Same as {@link
     * #getAttribute(Map, Configuration, String, String, Object)} with a
     * <code>null</code> configuration.
     */
    public Object getAttribute(Map objectModel, String module, String name, Object deflt) throws RuntimeException {
        return getAttribute(objectModel, null, module, name, deflt);
    }


    /**
     * Get an array of values from a module. Uses cached reference if
     * existing.
     *
     * @param objectModel a <code>Map</code> value
     * @param conf a <code>Configuration</code> containing the module dynamic configuration (aka modeConf)
     * @param module a <code>String</code> value holding the module name
     * @param name a <code>String</code> value holding the attribute name
     * @param deflt an <code>Object[]</code> value holding a default value
     * @return an <code>Object[]</code> value
     * @exception RuntimeException if an error occurs
     */
    public Object[] getAttributeValues(Map objectModel, Configuration conf, String module, String name, Object[] deflt) throws RuntimeException {

        Object[] result = (Object[]) this.get(OP_VALUES, module, name, objectModel, conf);
        if (result == null) result = deflt;
        return result;
    }

    /**
     * Get an array of values from a module. Same as
     * {@link #getAttributeValues(Map, Configuration, String, String, Object[])}
     * with a <code>null</code> configuration.
     */
    public Object[] getAttributeValues(Map objectModel, String module, String name, Object[] deflt) throws RuntimeException {
        return getAttributeValues(objectModel, null, module, name, deflt);
    }

    /**
     * Output the request attribute values for a given name to the
     * content handler.
     *
     * @param objectModel The Map objectModel
     * @param contentHandler The SAX content handler
     * @param module a <code>String</code> value holding the module name
     * @param name a <code>String</code> value holding the attribute name
     * @exception SAXException If a SAX error occurs
     * @exception RuntimeException if an error occurs
     */
    public void getAttributeValues(Map objectModel, ContentHandler contentHandler, String module, String name )
        throws SAXException, RuntimeException {

        AttributesImpl attr = new AttributesImpl();
        XSPObjectHelper.addAttribute(attr, "name", name);

        XSPObjectHelper.start(URI, PREFIX, contentHandler,
            "attribute-values", attr);

        Object[] values = this.getAttributeValues(objectModel, module, name, null);

        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                XSPObjectHelper.elementData(URI, PREFIX, contentHandler,
                    "value", String.valueOf(values[i]));
            }
        }

        XSPObjectHelper.end(URI, PREFIX, contentHandler, "attribute-values");
    }


    /**
     * Get an iterator to a collection of attribute names from a
     * module.
     *
     * @param objectModel a <code>Map</code> value
     * @param module the module's name
     * @return an <code>Iterator</code> value
     * @exception RuntimeException if an error occurs
     */
    public Iterator getAttributeNames(Map objectModel, Configuration conf, String module) throws RuntimeException {

        return (Iterator) this.get(OP_NAMES, module, null, objectModel);
    }

    /**  Get an iterator to a collection of attribute names from a module. Same
     * as {@link #getAttributeNames(Map, Configuration, String)} with a
     * <code>null</code> configuration.
     */
    public Iterator getAttributeNames(Map objectModel, String module) throws RuntimeException {
        return getAttributeNames(objectModel, (Configuration)null, module);
    }

    /**
     * Output attribute names for a given request
     *
     * @param objectModel The Map objectModel
     * @param contentHandler The SAX content handler
     * @param module the module's name
     * @exception SAXException If a SAX error occurs
     * @exception RuntimeException if an error occurs
     */
    public  void getAttributeNames(Map objectModel, ContentHandler contentHandler, String module)
        throws SAXException, RuntimeException {

        XSPObjectHelper.start(URI, PREFIX, contentHandler, "attribute-names");

        Iterator iter = this.getAttributeNames(objectModel, module);
        while (iter.hasNext()) {
            String name = (String) iter.next();
            XSPObjectHelper.elementData(URI, PREFIX, contentHandler, "name", name);
        }

        XSPObjectHelper.end(URI, PREFIX, contentHandler, "attribute-names");
    }



    /**
     * Releases all obtained module references.
     *
     * @exception RuntimeException if an error occurs
     */
    public void releaseAll() throws RuntimeException {

        if ( this.inputModules != null ) {
            // test for component manager
            if ( this.componentManager != null ) {
                try {
                    Iterator iter = this.inputModules.keySet().iterator();
                    while (iter.hasNext()) {
                        this.componentInputSelector.release((InputModule) this.inputModules.get(iter.next()));
                    }
                    this.inputModules = null;
                    this.componentManager.release(this.componentInputSelector);
                    this.componentManager = null;
                    this.inputModules = null;
                } catch (Exception e) {
                    throw new CascadingRuntimeException("Could not release InputModules.",e);
                }
                
            }
            if ( this.serviceManager != null ) {
                try {
                    Iterator iter = this.inputModules.keySet().iterator();
                    while (iter.hasNext()) {
                        this.serviceInputSelector.release(this.inputModules.get(iter.next()));
                    }
                    this.inputModules = null;
                    this.serviceManager.release(this.serviceInputSelector);
                    this.serviceManager = null;
                    this.inputModules = null;
                } catch (Exception e) {
                    throw new CascadingRuntimeException("Could not release InputModules.",e);
                }
                
            }
        }
    }
}
