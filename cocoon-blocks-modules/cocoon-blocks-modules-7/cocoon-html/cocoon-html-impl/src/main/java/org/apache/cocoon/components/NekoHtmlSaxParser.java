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
package org.apache.cocoon.components;

import java.util.Iterator;
import java.util.Properties;

import org.apache.xerces.parsers.AbstractSAXParser;
import org.cyberneko.html.HTMLConfiguration;

/**
 * @version $Id$
 */
public class NekoHtmlSaxParser extends AbstractSAXParser {

    public NekoHtmlSaxParser(Properties properties) {
        super(getConfig(properties));
    }

    private static HTMLConfiguration getConfig(Properties properties) {
        HTMLConfiguration config = new HTMLConfiguration();
        config.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
        if (properties != null) {
            for (Iterator i = properties.keySet().iterator(); i.hasNext();) {
                String name = (String) i.next();
                if (name.indexOf("/features/") > -1) {
                    config.setFeature(name, Boolean.getBoolean(properties.getProperty(name)));
                } else if (name.indexOf("/properties/") > -1) {
                    config.setProperty(name, properties.getProperty(name));
                }
            }
        }
        return config;
    }

}
