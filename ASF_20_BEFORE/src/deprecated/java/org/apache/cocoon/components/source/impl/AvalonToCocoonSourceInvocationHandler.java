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
package org.apache.cocoon.components.source.impl;

import org.apache.avalon.framework.component.ComponentManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ModifiableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * An <code>InvocationHandler</code> which acts as a proxy for excalibur
 * <code>Source</code> objects to make them compatible with the cocoon
 * <code>Source</code> interface.
 * Much of the code was taken from {@link AvalonToCocoonSource}.
 *
 * @author Stefan K&ouml;hler
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: AvalonToCocoonSourceInvocationHandler.java,v 1.2 2003/12/23 15:28:33 joerg Exp $
 */
public class AvalonToCocoonSourceInvocationHandler 
implements InvocationHandler {

    /** The real source */
    protected Source source;

    /** The source resolver */
    protected SourceResolver resolver;

    /** The environment */
    protected Environment environment;

    /** The manager */
    protected ComponentManager manager;
    
    /**
     * Constructor
     */
    public AvalonToCocoonSourceInvocationHandler(Source source,
                                                  SourceResolver resolver,
                                                  Environment environment,
                                                  ComponentManager manager) {
        this.source = source;
        this.resolver = resolver;
        this.environment = environment;
        this.manager = manager;
    }

    /**
     * Processes a method invocation on a proxy instance and returns the result.
     * It invokes the corresponding method of the wrapped excalibur source.
     *
     * @param proxy  the Cocoon source proxy instance that the method was invoked on
     * @param method the Method instance corresponding to the interface method
     *               invoked on the proxy instance.
     * @param args   the arguments for the interface method
     * @return       the result of the proxy method
     */
    public Object invoke( Object proxy, Method method, Object[] args ) 
    throws Throwable {
        try {
            if        (method.getName().equals("getInputStream")) { 
                return this.getInputStream(); 
            } else if (method.getName().equals("getInputSource")) { 
                return this.getInputSource(); 
            } else if (method.getName().equals("getSystemId")) {
                return this.getSystemId();
            } else if(method.getName().equals("recycle")) { 
                this.recycle(); 
                return null;  
            } else if(method.getName().equals("toSAX")) { 
                this.toSAX((ContentHandler) args[0]); 
                return null; 
            } else{
                return method.invoke(source, args);
            }
        }
        catch ( InvocationTargetException e ){
            throw e.getTargetException();
        }
    }

    /**
     * Return an <code>InputStream</code> object to read from the source.
     */
    public InputStream getInputStream()
    throws ProcessingException, IOException {
        try {
            return this.source.getInputStream();
        } catch (SourceException e) {
            throw SourceUtil.handle(e);
        }
    }

    /**
     * Return an <code>InputSource</code> object to read the XML
     * content.
     *
     * @return an <code>InputSource</code> value
     * @exception ProcessingException if an error occurs
     * @exception IOException if an error occurs
     */
    public InputSource getInputSource()
    throws ProcessingException, IOException {
        try {
            InputSource newObject = new InputSource(this.source.getInputStream());
            newObject.setSystemId(this.getSystemId());
            return newObject;
        } catch (SourceException se) {
            throw SourceUtil.handle(se);
        }
    }

    /**
     * Return the unique identifer for this source
     */
    public String getSystemId() {
        return this.source.getURI();
    }

    public void recycle() {
        this.resolver.release(this.source);
        this.source = null;
        this.environment = null;
    }

    public void refresh() {
        this.source.refresh();
    }

    /**
     * Stream content to a content handler or to an XMLConsumer.
     *
     * @throws SAXException if failed to parse source document.
     */
    public void toSAX(ContentHandler handler)
    throws SAXException {
        try {
            SourceUtil.parse(this.manager, this.source, handler);
        } catch (ProcessingException pe) {
            throw new SAXException("ProcessingException during streaming.", pe);
        } catch (IOException ioe) {
            throw new SAXException("IOException during streaming.", ioe);
        }
    }

    /**
     * Creates a dynamic proxy for an excalibur <code>Source</code> object to
     * make it behave like a cocoon <code>Source</code>.
     * @param  source the source object to be wrapped
     * @return a proxy object which implements the cocoon <code>Source</code>
     *         interface and all of the interfaces that the wrapped object
     *         implements
     * @throws SourceException in case of an error
     */
    public static org.apache.cocoon.environment.Source createProxy(Source source,
               SourceResolver resolver,
               Environment environment,
               ComponentManager manager)
    throws SourceException{
        Class[] sourceInterfaces = source.getClass().getInterfaces();
        Class[] proxyInterfaces = new Class[sourceInterfaces.length+2];

        for(int i=0; i < sourceInterfaces.length; i++) {
            proxyInterfaces[i] = sourceInterfaces[i];
        }
        
        proxyInterfaces[sourceInterfaces.length] = org.apache.cocoon.environment.Source.class;
        proxyInterfaces[sourceInterfaces.length+1]   = ModifiableSource.class;

        InvocationHandler invocationHandler = new AvalonToCocoonSourceInvocationHandler(
             source,
             resolver,
             environment,
             manager
        );

        try {
            org.apache.cocoon.environment.Source proxy;
            proxy = (org.apache.cocoon.environment.Source)Proxy.newProxyInstance(source.getClass().getClassLoader(),
                                                  proxyInterfaces, invocationHandler);
            return proxy;
        } catch(Exception e){
            throw new SourceException("Error creating proxy object", e);
        }

    }

}

