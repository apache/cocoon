/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.util;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.CharacterData;

/**
 * This class is an improved version of the Excalibur ConfigurationUtil class
 * to support namespaces in DOMs.
 * @since 2.1.10
 *
 * @version $Id$
 */
public class ConfigurationUtil {
    /**
     * Private constructor to block instantiation.
     */
    private ConfigurationUtil() {
    }

    /**
     * Convert a DOM Element tree into a configuration tree.
     *
     * @param element the DOM Element
     * @return the configuration object
     */
    public static Configuration toConfiguration( final Element element ) {
        final DefaultConfiguration configuration =
            new DefaultConfiguration( element.getLocalName(), element.getPrefix(), element.getNamespaceURI(), element.getPrefix() );
        final NamedNodeMap attributes = element.getAttributes();
        final int length = attributes.getLength();
        for( int i = 0; i < length; i++ ) {
            final Node node = attributes.item( i );
            final String name = node.getNodeName();
            final String value = node.getNodeValue();
            configuration.setAttribute( name, value );
        }

        boolean flag = false;
        String content = "";
        final NodeList nodes = element.getChildNodes();
        final int count = nodes.getLength();
        for( int i = 0; i < count; i++ ) {
            final Node node = nodes.item( i );
            if( node instanceof Element ) {
                final Configuration child = toConfiguration( (Element)node );
                configuration.addChild( child );
            } else if( node instanceof CharacterData ) {
                final CharacterData data = (CharacterData)node;
                content += data.getData();
                flag = true;
            }
        }

        if( flag ) {
            configuration.setValue( content );
        }

        return configuration;
    }

    /**
     * Convert a configuration tree into a DOM Element tree.
     *
     * @param configuration the configuration object
     * @return the DOM Element
     */
    public static Element toElement( final Configuration configuration )
    throws ConfigurationException {
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document document = builder.newDocument();

            return createElement( document, configuration );
        } catch( final ParserConfigurationException pce ) {
            throw new IllegalStateException( pce.toString() );
        }
    }

    /**
     * Create an DOM {@link Element} from a {@link Configuration}
     * object.
     *
     * @param document the DOM document
     * @param configuration the configuration to convert
     * @return the DOM Element
     */
    private static Element createElement( final Document document,
                                          final Configuration configuration )
    throws ConfigurationException {
        final Element element = document.createElementNS( configuration.getNamespace(), configuration.getName() );
        element.setPrefix( configuration.getLocation() );
        final String content = configuration.getValue( null );
        if( null != content )
        {
            final Text child = document.createTextNode( content );
            element.appendChild( child );
        }

        final String[] names = configuration.getAttributeNames();
        for( int i = 0; i < names.length; i++ )
        {
            final String name = names[ i ];
            final String value = configuration.getAttribute( name, null );
            element.setAttribute( name, value );
        }
        final Configuration[] children = configuration.getChildren();
        for( int i = 0; i < children.length; i++ )
        {
            final Element child = createElement( document, children[ i ] );
            element.appendChild( child );
        }
        return element;
    }
}
