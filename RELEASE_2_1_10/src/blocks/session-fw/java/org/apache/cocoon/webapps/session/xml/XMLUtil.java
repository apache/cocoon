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
package org.apache.cocoon.webapps.session.xml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import javax.xml.transform.OutputKeys;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.CocoonComponentManager;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceParameters;
import org.apache.excalibur.source.SourceResolver;
import org.w3c.dom.DocumentFragment;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;



/**
 * A utility class which will soon be removed...
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @deprecated This block is deprecated and will be removed in future versions.
 * @version CVS $Id$
*/
public final class XMLUtil {

    /**
     * Convert umlaute to entities
     */
    public static String encode(String value) {
        StringBuffer buffer = new StringBuffer(value);
        for(int i = 0; i < buffer.length(); i++) {
            if (buffer.charAt(i) > 127) {
                buffer.replace(i, i+1, "__"+((int)buffer.charAt(i))+";");
            }
        }
        return buffer.toString();
    }

    /**
     * Convert entities to umlaute
     */
    public static String decode(String value) {
        StringBuffer buffer = new StringBuffer(value);
        int pos;
        boolean found;
        for(int i = 0; i < buffer.length(); i++) {
            if (buffer.charAt(i) == '_' &&
                buffer.charAt(i+1) == '_') {
                pos = i + 2;
                found = false;
                while (buffer.charAt(pos) >= '0'
                       && buffer.charAt(pos) <= '9') {
                    found = true;
                    pos++;
                }
                if (found == true
                    && pos > i + 2
                    && buffer.charAt(pos) == ';') {
                    int ent = new Integer(buffer.substring(i+2, pos)).intValue();
                    buffer.replace(i, pos+1, ""+ (char)ent);
                }
            }
        }
        return buffer.toString();
    }

    /**
     * Write a DOM Fragment to a source.
     * If the source is a ModifiableSource the interface is used.
     * If not, the source is invoked with an additional parameter named
     * "content" containing the XML.
     *
     * @param location URI of the Source
     * @param typeParameters Type of Source query.  Currently, only
     * <code>method</code> parameter (value typically <code>GET</code> or
     * <code>POST</code>) is recognized.  May be <code>null</code>.
     * @param parameters Parameters (e.g. URL params) of the source.
     * May be <code>null</code>
     * @param frag DOM fragment to serialize to the Source
     * @param resolver Resolver for the source.
     * @param serializerName The serializer to use
     * @throws ProcessingException
     */
    public static void writeDOM(String location,
                                Parameters typeParameters,
                                SourceParameters parameters,
                                DocumentFragment frag,
                                SourceResolver resolver,
                                String serializerName)
    throws ProcessingException {
        Source source = null;

        try {
            source = SourceUtil.getSource(location, typeParameters,
                                          parameters, resolver);
            if (source instanceof ModifiableSource) {
                ModifiableSource ws = (ModifiableSource) source;

                frag.normalize();

                if (null != serializerName) {
                    ComponentManager manager = CocoonComponentManager.getSitemapComponentManager();

                    ComponentSelector selector = null;
                    Serializer serializer = null;
                    OutputStream oStream = null;
                    try {
                        selector = (ComponentSelector)manager.lookup(Serializer.ROLE + "Selector");
                        serializer = (Serializer)selector.select(serializerName);
                        oStream = ws.getOutputStream();
                        serializer.setOutputStream(oStream);
                        serializer.startDocument();
                        DOMStreamer streamer = new DOMStreamer(serializer);
                        streamer.stream(frag);
                        serializer.endDocument();
                    } catch (ComponentException e) {
                        throw new ProcessingException("Unable to lookup serializer.", e);
                    } finally {
                        if (oStream != null) {
                            oStream.flush();
                            try {
                                oStream.close();
                            } catch (Exception ignore) {
                            }
                        }
                        if (selector != null) {
                            selector.release(serializer);
                            manager.release(selector);
                        }
                    }
                } else {
                    Properties props = XMLUtils.createPropertiesForXML(false);
                    props.put(OutputKeys.ENCODING, "ISO-8859-1");
                    final String content = XMLUtils.serializeNode(frag, props);
                    OutputStream oStream = ws.getOutputStream();

                    oStream.write(content.getBytes());
                    oStream.flush();
                    oStream.close();
                }
            } else {
                String content;
                if (null != serializerName) {
                    ComponentManager  manager = CocoonComponentManager.getSitemapComponentManager();

                    ComponentSelector selector = null;
                    Serializer serializer = null;
                    ByteArrayOutputStream oStream = new ByteArrayOutputStream();
                    try {
                        selector = (ComponentSelector)manager.lookup(Serializer.ROLE + "Selector");
                        serializer = (Serializer)selector.select(serializerName);
                        serializer.setOutputStream(oStream);
                        serializer.startDocument();
                        DOMStreamer streamer = new DOMStreamer(serializer);
                        streamer.stream(frag);
                        serializer.endDocument();
                    } catch (ComponentException e) {
                        throw new ProcessingException("Unable to lookup serializer.", e);
                    } finally {
                        oStream.flush();
                        try {
                            oStream.close();
                        } catch (Exception ignore) {
                            // do nothing
                        }
                        if (selector != null) {
                            selector.release(serializer);
                            manager.release(selector);
                        }
                    }
                    content = oStream.toString();
                } else {
                    Properties props = XMLUtils.createPropertiesForXML(false);
                    props.put(OutputKeys.ENCODING, "ISO-8859-1");
                    content = XMLUtils.serializeNode(frag, props);
                }

                if (parameters == null) {
                    parameters = new SourceParameters();
                } else {
                    parameters = (SourceParameters) parameters.clone();
                }
                parameters.setSingleParameterValue("content", content);

                source = SourceUtil.getSource(location, typeParameters,
                                              parameters, resolver);
                SourceUtil.toSAX(source, new DefaultHandler());
            }
        } catch (SourceException e) {
            throw SourceUtil.handle(e);
        } catch (IOException e) {
            throw new ProcessingException(e);
        } catch (SAXException e) {
            throw new ProcessingException(e);
        } finally {
            resolver.release(source);
        }
    }
}
