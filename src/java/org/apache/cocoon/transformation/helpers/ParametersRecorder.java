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
package org.apache.cocoon.transformation.helpers;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.excalibur.source.SourceParameters;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Iterator;


/**
 * This class records SAX Events and generates Parameters from them
 * The xml is flat and consists of elements which all have exactly one text node:
 * <parone>value_one<parone>
 * <partwo>value_two<partwo>
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: ParametersRecorder.java,v 1.2 2004/03/05 13:03:00 bdelacretaz Exp $
*/
public final class ParametersRecorder
extends NOPRecorder {

    private SourceParameters parameters;
    private String     key;
    private StringBuffer buffer;

    /**
     * If source is null a new Parameters object is created
     * Otherwise they are joined.
     */
    public ParametersRecorder() {
        super();
        this.parameters = new SourceParameters();
    }

    public SourceParameters getParameters(Parameters source) {
        if (source != null) {
            String[] names = source.getNames();
//            Iterator names = source.getParameterNames();
            if (names != null) {
                String currentParameterName;
                for(int i=0; i<names.length; i++) {
                    currentParameterName = names[i];
//                while (names.hasNext() == true) {
//                    currentParameterName = (String)names.next();
                    this.parameters.setParameter(currentParameterName, source.getParameter(currentParameterName, ""));
                }
            }
        }
        return parameters;
    }

    public SourceParameters getParameters(SourceParameters source) {
        if (source != null) {
            Iterator iter = source.getParameterNames();
            Iterator valuesIter;
            String value, parName;
            while (iter.hasNext() == true) {
                parName = (String)iter.next();
                valuesIter = source.getParameterValues(parName);
                while (valuesIter.hasNext() == true) {
                    value = (String)valuesIter.next();
                    this.parameters.setParameter(parName, value);
                }
            }
        }
        return parameters;
    }

    public void startElement(String namespace, String name, String raw,
                         Attributes attr)
    throws SAXException {
        if (this.key == null) {
            this.key = name;
            this.buffer = new StringBuffer();
        }
    }

    public void endElement(String namespace, String name, String raw)
    throws SAXException {
        if (this.key != null && this.key.equals(name) == true) {
            String value = this.buffer.toString().trim();
            if (value.length() > 0) {
                this.parameters.setParameter(this.key, value);
            }
            this.buffer = null;
            this.key = null;
        }
    }

    public void characters(char ary[], int start, int length)
    throws SAXException {
        if (this.key != null && this.buffer != null) {
            String value = new String(ary, start, length).trim();
            if (value.length() > 0) {
                buffer.append(value);
            } else {
                buffer.append(' ');
            }
        }
    }

}
