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
package org.apache.cocoon.components.xmlform;

import org.apache.avalon.framework.CascadingRuntimeException;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * A sample domain object used as a Form model.
 * Notice that it has mixed content:
 * JavaBean properties and
 * DOM Nodes, which are handled correctly by the
 * framework when referenced via XPath.
 *
 * @version CVS $Id: TestBean.java,v 1.3 2004/03/05 13:02:38 bdelacretaz Exp $
 */
public class TestBean {
    private int count = 1;
    private short numInstalls = 1;
    private String liveUrl = "http://";
    private boolean publish = true;
    private List favorites = new ArrayList();

    private boolean hidden = false;

    private Node system;

    public TestBean() {
        initSystem();
        initFavorites();
    }

    public String getLiveUrl() {
        return liveUrl;
    }

    public void setLiveUrl(String newUrl) {
        liveUrl = newUrl;
    }

    public short getNumber() {
        return numInstalls;
    }

    public void setNumber(short num) {
        numInstalls = num;
    }

    public boolean getPublish() {
        return publish;
    }

    public void setPublish(boolean newPublish) {
        publish = newPublish;
    }

    public Node getSystem() {
        return system;
    }

    public void setSystem(Node newSystem) {
        system = newSystem;
    }

    public boolean getHidden() {
        return hidden;
    }

    public void setHidden(boolean newHidden) {
        hidden = newHidden;
    }

    public int getCount() {
        return count;
    }

    public void incrementCount() {
        count++;
    }

    public List getFavorite() {
        return favorites;
    }

    public void setFavorite(List newFavorites) {
        favorites = newFavorites;
    }

    public void initSystem() {
        DOMImplementation impl;

        try {
            // Find the implementation
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            factory.setNamespaceAware(false);
            factory.setValidating(false);
            DocumentBuilder builder = factory.newDocumentBuilder();

            impl = builder.getDOMImplementation();
        } catch (Exception ex) {
            throw new CascadingRuntimeException("Failed to initialize DOM factory.",
                                                ex);
        }

        // initialize system as dom node
        Document doc = impl.createDocument(null,
                                           "XMLForm_Wizard_System_Node",
                                           null);
        Node rootElement = doc.getDocumentElement();

        Node os = doc.createElement("os");
        Text text = doc.createTextNode("Linux");

        os.appendChild(text);
        rootElement.appendChild(os);

        Node processor = doc.createElement("processor");

        text = doc.createTextNode("p4");
        processor.appendChild(text);
        rootElement.appendChild(processor);

        Attr ram = doc.createAttribute("ram");

        ram.setValue("512");
        NamedNodeMap nmap = rootElement.getAttributes();

        nmap.setNamedItem(ram);

        Node servletEngine = doc.createElement("servletEngine");

        text = doc.createTextNode("Tomcat");
        servletEngine.appendChild(text);
        rootElement.appendChild(servletEngine);

        Node javaVersion = doc.createElement("javaVersion");

        text = doc.createTextNode("1.3");
        javaVersion.appendChild(text);
        rootElement.appendChild(javaVersion);

        system = rootElement;

    }

    public void initFavorites() {
        favorites.add("http://cocoon.apache.org");
        favorites.add("http://jakarta.apache.org");
        favorites.add("http://www.google.com");
        favorites.add("http://www.slashdot.org");
        favorites.add("http://www.yahoo.com");
    }

}
