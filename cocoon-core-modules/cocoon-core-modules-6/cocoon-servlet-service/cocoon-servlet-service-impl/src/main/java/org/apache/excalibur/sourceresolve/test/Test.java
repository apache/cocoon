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
package org.apache.excalibur.sourceresolve.test;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.excalibur.source.impl.FileSourceFactory;
import org.apache.excalibur.sourceresolve.jnet.Installer;
import org.apache.excalibur.sourceresolve.jnet.source.SourceFactoriesManager;
import org.apache.excalibur.sourceresolve.jnet.source.SourceURLStreamHandlerFactory;

public class Test {

    public static void main(String[] args) {
        try {
            Installer.setURLStreamHandlerFactory(new SourceURLStreamHandlerFactory());
            Installer.setURLStreamHandlerFactory(new SourceURLStreamHandlerFactory());
            final Map factories = new HashMap();
            factories.put("carsten", new FileSourceFactory());
            SourceFactoriesManager.setGlobalFactories(factories);
            final URL url = new URL("carsten:///Users/cziegeler/.m2/settings.xml");
            final InputStream is = (InputStream)url.getContent();
            final byte[] b = new byte[100000];
            int l = is.read(b);
            System.out.println(new String(b, 0, l));
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
