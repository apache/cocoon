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

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
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
 * @deprecated This block is deprecated and will be removed in future versions.
 * @version $Id$
*/
public final class XMLUtil {

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
                    ServiceManager manager = EnvironmentHelper.getSitemapServiceManager();

                    Serializer serializer = null;
                    OutputStream oStream = null;
                    try {
                        serializer = (Serializer)manager.lookup(Serializer.ROLE + '/' + serializerName);
                        oStream = ws.getOutputStream();
                        serializer.setOutputStream(oStream);
                        serializer.startDocument();
                        DOMStreamer streamer = new DOMStreamer(serializer);
                        streamer.stream(frag);
                        serializer.endDocument();
                    } catch (ServiceException e) {
                        throw new ProcessingException("Unable to lookup serializer.", e);
                    } finally {
                        if (oStream != null) {
                            oStream.flush();
                            try {
                                oStream.close();
                            } catch (Exception ignore) {
                            }
                        }
                        manager.release(serializer);
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
                    ServiceManager manager = EnvironmentHelper.getSitemapServiceManager();

                    Serializer serializer = null;
                    ByteArrayOutputStream oStream = new ByteArrayOutputStream();
                    try {
                        serializer = (Serializer)manager.lookup(Serializer.ROLE + '/' + serializerName);
                        serializer.setOutputStream(oStream);
                        serializer.startDocument();
                        DOMStreamer streamer = new DOMStreamer(serializer);
                        streamer.stream(frag);
                        serializer.endDocument();
                    } catch (ServiceException e) {
                        throw new ProcessingException("Unable to lookup serializer.", e);
                    } finally {
                        oStream.flush();
                        try {
                            oStream.close();
                        } catch (Exception ignore) {
                            // do nothing
                        }
                        manager.release(serializer);
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
