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
package org.apache.cocoon.precept.transformation;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.precept.Constraint;
import org.apache.cocoon.precept.Instance;
import org.apache.cocoon.precept.InvalidXPathSyntaxException;
import org.apache.cocoon.precept.NoSuchNodeException;
import org.apache.cocoon.precept.acting.AbstractPreceptorAction;
import org.apache.cocoon.transformation.AbstractTransformer;

import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author Torsten Curdt <tcurdt@dff.st>
 * @version CVS $Id: InstanceTransformer.java,v 1.1 2003/11/20 17:10:07 joerg Exp $
 */
public class InstanceTransformer extends AbstractTransformer {

    public final static String NS = "http://www.dff.st/ns/desire/instance/1.0";

    public final static Attributes NOATTR = new AttributesImpl();
    public final static String TAG_INSERTINSTANCE = "insert-instance";
    public final static String TAG_INSERTINSTANCE_ATTR_ID = "id";
    public final static String TAG_INSTANCE = "instance";
    public final static String TAG_INSTANCE_ATTR_ID = "id";
    public final static String TAG_INSERTVIOLATIONS = "insert-violations";
    public final static String TAG_BUTTON = "button";
    public final static String TAG_LABEL = "label";
    public final static String TAG_OUTPUT = "output";
    public final static String TAG_TEXTBOX = "textbox";
    public final static String TAG_PASSWORD = "password";
    public final static String TAG_SELECTBOOLEAN = "selectBoolean";
    public final static String TAG_SELECTONE = "selectOne";
    public final static String TAG_SELECTMANY = "selectMany";
    public final static String TAG_COMMON_ATTR_REF = "ref";
    public final static String TAG_COMMON_ATTR_INSTANCE = "instance";


    private Request request;
    private Session session;
    private Instance defaultInstance;
    private Object value;
    private String prefix;


    public void setup(SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws ProcessingException, SAXException, IOException {
        request = ObjectModelHelper.getRequest(objectModel);

        if (request == null) {
            getLogger().debug("no request object");
            throw new ProcessingException("no request object");
        }

        session = request.getSession(false);
        defaultInstance = null;
        prefix = null;
    }

    public void startElement(String uri, String name, String raw, Attributes attributes) throws SAXException {
        if (NS.equals(uri)) {
            if (prefix == null) {
                prefix = raw.substring(0, raw.length() - name.length() - 1);
                getLogger().debug("found prefix [" + String.valueOf(prefix) + "] for namespace [" + NS + "]");
            }

            if (TAG_INSERTINSTANCE.equals(name)) {
                if (session != null) {
                    String id = attributes.getValue(TAG_INSERTINSTANCE_ATTR_ID);
                    getLogger().debug("inserting instance [id=" + String.valueOf(id) + "]");

                    Instance instance = (Instance) session.getAttribute(id);
                    if (instance != null) {
                        instance.toSAX(this, true);
                    }
                    else {
                        getLogger().debug("could not find instance [id=" + String.valueOf(id) + "]");
                    }
                }
                else {
                    getLogger().debug("no session - no instance");
                }
            }
            else if (TAG_INSERTVIOLATIONS.equals(name)) {
                Collection violations = (Collection) request.getAttribute(AbstractPreceptorAction.PRECEPTORVIOLATIONS);
                if (violations != null) {
                    for (Iterator it = violations.iterator(); it.hasNext();) {
                        Constraint constraint = (Constraint) it.next();
                        super.startElement(uri, "constraint", prefix + ":" + "constraint", NOATTR);
                        String v = String.valueOf(constraint);
                        super.characters(v.toCharArray(), 0, v.length());
                        super.endElement(uri, "constraint", prefix + ":" + "constraint");
                    }
                }
            }
            else if (TAG_INSTANCE.equals(name)) {
                if (session != null) {
                    String id = attributes.getValue(TAG_INSTANCE_ATTR_ID);
                    defaultInstance = (Instance) session.getAttribute(id);
                    if (defaultInstance != null) {
                        getLogger().debug("using default instance [id=" + String.valueOf(id) + "]");
                    }
                    else {
                        getLogger().error("could not find instance [id=" + String.valueOf(id) + "]");
                    }
                }
            }
            else if (TAG_OUTPUT.equals(name) || TAG_TEXTBOX.equals(name) ||
                     TAG_PASSWORD.equals(name) || TAG_SELECTBOOLEAN.equals(name) ||
                     TAG_SELECTONE.equals(name)) {
                String ref = attributes.getValue(TAG_COMMON_ATTR_REF);
                String id = attributes.getValue(TAG_COMMON_ATTR_INSTANCE);

                getLogger().debug("[" + String.valueOf(name) + "] getting value from [" + String.valueOf(ref) + "]");

                if (ref != null) {
                    Instance instance = defaultInstance;

                    if (id != null) {
                        if (session != null) {
                            instance = (Instance) session.getAttribute(id);
                            if (instance != null) {
                                getLogger().debug("using instance [id=" + String.valueOf(id) + "]");
                            }
                            else {
                                getLogger().error("could not find instance [id=" + String.valueOf(id) + "]");
                            }
                        }
                    }

                    super.startElement(uri, name, raw, attributes);

                    try {
                        value = instance.getValue(ref);

                        getLogger().debug("[" + String.valueOf(ref) + "] = " + String.valueOf(value));

                        super.startElement(uri, "value", prefix + ":" + "value", NOATTR);
                        if (value != null) {
                            String v = String.valueOf(value);
                            super.characters(v.toCharArray(), 0, v.length());
                        }

                        super.endElement(uri, "value", prefix + ":" + "value");

                        if (instance.getPreceptor() != null) {
                            Collection constraints = instance.getPreceptor().getConstraintsFor(ref);
                            if (constraints != null) {
                                for (Iterator it = constraints.iterator(); it.hasNext();) {
                                    Constraint constraint = (Constraint) it.next();
                                    constraint.toSAX(this);
                                }
                            }
                        }
                    }
                    catch (InvalidXPathSyntaxException e) {
                        throw new SAXException(e);
                    }
                    catch (NoSuchNodeException e) {
                        //throw new SAXException(e);
                    }
                }
                else {
                    throw new SAXException("[" + String.valueOf(name) + "] needs an \"" + TAG_COMMON_ATTR_REF + "\" attribute");
                }
            }
            else if (TAG_SELECTMANY.equals(name)) {
                //NYI
                throw new SAXException("NYI");
            }
            else if (TAG_BUTTON.equals(name)) {
                //NYI
                super.startElement(uri, name, raw, attributes);
            }
            else {
                getLogger().error("unknown element [" + String.valueOf(name) + "]");
                super.startElement(uri, name, raw, attributes);
            }
        }
        else {
            super.startElement(uri, name, raw, attributes);
        }
    }


    public void endElement(String uri, String name, String raw) throws SAXException {
        if (NS.equals(uri)) {
            if (TAG_INSERTINSTANCE.equals(name)) {
            }
            else if (TAG_INSERTVIOLATIONS.equals(name)) {
            }
            else if (TAG_INSTANCE.equals(name)) {
            }
            else if (TAG_OUTPUT.equals(name) || TAG_TEXTBOX.equals(name) ||
                     TAG_PASSWORD.equals(name) || TAG_SELECTBOOLEAN.equals(name) ||
                     TAG_SELECTONE.equals(name)) {
                super.endElement(uri, name, raw);
            }
            else if (TAG_SELECTMANY.equals(name)) {
            }
            else if (TAG_BUTTON.equals(name)) {
                super.endElement(uri, name, raw);
            }
            else {
                getLogger().error("unknown element [" + String.valueOf(name) + "]");
                super.endElement(uri, name, raw);
            }
        }
        else {
            super.endElement(uri, name, raw);
        }
    }

    public void characters(char[] chars, int start, int len) throws SAXException {
        super.characters(chars, start, len);
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     * This method must be invoked before the generateValidity() method.
     *
     * @return The generated key or <code>null</code> if the component
     *              is currently not cacheable.
     */
    public Serializable generateKey() {
        return NOPValidity.SHARED_INSTANCE.toString();
    }

    /**
     * Generate the validity object.
     * Before this method can be invoked the generateKey() method
     * must be invoked.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public SourceValidity generateValidity() {
        return NOPValidity.SHARED_INSTANCE;
    }

}
