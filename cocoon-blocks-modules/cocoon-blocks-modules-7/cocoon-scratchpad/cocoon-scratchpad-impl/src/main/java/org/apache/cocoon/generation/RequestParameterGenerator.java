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
package org.apache.cocoon.generation;

import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * <p>The {@link RequestParameterGenerator} is a simple generator producing as an
 * output a subset of what the {@link RequestGenerator} produces.</p>
 *
 * <p>This generator limits its output to the production of request parameters,
 * completely ignoring things like headers and configurations. An example:</p>
 *
 * <pre>
 * &lt;req:request xmlns:req="http://apache.org/cocoon/request/2.0"&gt;
 *   &lt;req:requestParameters&gt;
 *     &lt;req:parameter name="aParameter"&gt;
 *       &lt;req:value>itsValue</req:value&gt;
 *     &lt;/req:parameter&gt;
 *     &lt;req:parameter name="anotherParameter"&gt;
 *       &lt;req:value>itsFirstValue</req:value&gt;
 *       &lt;req:value>itsSecondValue</req:value&gt;
 *     &lt;/req:parameter&gt;
 *     <i>[...]</i>
 *   &lt;/req:requestParameters&gt;
 * &lt;/req:request&gt;
 * </pre>
 *
 * <p>The benefits of this simplified version of {@link RequestGenerator} is that
 * it is <b>cacheable</b>. The cacheability is achieved by crafting a very specific
 * {@link #getKey key} to be passed to Cocoon, so that the caching pipeline can
 * actually identify the differences in parameters.</p>
 *
 * <p><b>NOTE:</b> given the nature of this generator, and the strain it might put
 * onto the cache system, it is <i>strongly</i> suggested to limit its use to
 * internal pipelines only, with a controlled number of parameter and values passed
 * to it.</p>
 *
 * @version $Id$
 */
public class RequestParameterGenerator extends AbstractGenerator
implements CacheableProcessingComponent  {

    /* == CONSTANTS ============================================================== */

    /** <p>The namespace prefix of this generator.</p> */
    public static final String PREFIX = "req";
    /** <p>The namespace URI of this generator.</p> */
    private final static String URI = "http://apache.org/cocoon/request/2.0";
    /** <p>The local name of the root &lt;req:request/&gt; element.</p> */
    private final static String E_REQ_L = "request";
    /** <p>The qualified name of the root &lt;req:request/&gt; element.</p> */
    private final static String E_REQ_Q = PREFIX + ":" + E_REQ_L;
    /** <p>The local name of the &lt;req:requestParameters/&gt; element.</p> */
    private final static String E_PARAMS_L = "requestParameters";
    /** <p>The qualified name of the &lt;req:requestParameters/&gt; element.</p> */
    private final static String E_PARAMS_Q = PREFIX + ":" + E_PARAMS_L;
    /** <p>The local name of the &lt;req:parameter/&gt; element.</p> */
    private final static String E_PARAM_L = "parameter";
    /** <p>The qualified name of the &lt;req:parameter/&gt; element.</p> */
    private final static String E_PARAM_Q = PREFIX + ":" + E_PARAM_L;
    /** <p>The local name of the &lt;req:value/&gt; element.</p> */
    private final static String E_VALUE_L = "value";
    /** <p>The qualified name of the &lt;req:value/&gt; element.</p> */
    private final static String E_VALUE_Q = PREFIX + ":" + E_VALUE_L;

    /* == INSTANCE VARIABLES ===================================================== */

    /** <p>The current {@link Parameters} instance.</p> */
    private Parameters parameters = null;

    /* == CONSTRUCTORS =========================================================== */

    /**
     * <p>Create a new {@link RequestParameterGenerator} instance.</p>
     */
    public RequestParameterGenerator() {
        super();
    }

    /* == IMPLEMENTATION METHODS ================================================= */

    /**
     * <p>Recycle this instance by wiping all locally held references.</p>
     *
     * @see org.apache.avalon.excalibur.pool.Recyclable#recycle()
     */
    public void recycle() {
        this.parameters = null;
        super.recycle();
    }

    /**
     * <p>Generate the unique key.</p>
     *
     * @see org.apache.cocoon.caching.CacheableProcessingComponent#getKey()
     */
    public Serializable getKey() {
        return(this.getValidity().toString());
    }

    /**
     * <p>Generate (or return) the {@link SourceValidity} instance used to
     * possibly validate cached generations.</p>
     *
     * @return a <b>non null</b> {@link SourceValidity}.
     * @see org.apache.cocoon.caching.CacheableProcessingComponent#getKey()
     */
    public SourceValidity getValidity() {
        if (this.parameters != null) return(this.parameters);
        this.parameters = new Parameters(ObjectModelHelper.getRequest(this.objectModel));
        return(this.parameters);
    }

    /**
     * <p>Generate the content and send it down to the pipeline.</p>
     *
     * @see org.apache.cocoon.generation.Generator#generate()
     */
    public void generate()
    throws ProcessingException, SAXException, IOException {
        /* Start the document, associate prefix, and print core elements */
        AttributesImpl attributes = new AttributesImpl();
        this.xmlConsumer.startDocument();
        this.xmlConsumer.startPrefixMapping(PREFIX, URI);
        this.xmlConsumer.startElement(URI, E_REQ_L, E_REQ_Q, attributes);
        this.xmlConsumer.startElement(URI, E_PARAMS_L, E_PARAMS_Q, attributes);

        /* Retrieve the parameters object and "parse" it, sending elements */
        char parameters[] = ((Parameters)this.getValidity()).array;
        int offset = 0;
        int chunks = parameters[offset++];
        for (int chunk = 0; chunk < chunks; chunk++) {

            /* How many strings do we have in this chunk? */
            int strings = parameters[offset++];
            if (strings == 0) continue;

            /* Dump the parameter element with the name attribute */
            int length = parameters[offset++];
            String name = new String(parameters, offset, length);
            attributes.addAttribute("","name","name","CDATA", name);
            this.xmlConsumer.startElement(URI, E_PARAM_L, E_PARAM_Q, attributes);
            attributes.clear();
            offset += length;

            /* Dump out every remaining string in the chunk as a value element */
            for (int string = 1; string < strings; string++) {
                length = parameters[offset++];
                this.xmlConsumer.startElement(URI, E_VALUE_L, E_VALUE_Q, attributes);
                this.xmlConsumer.characters(parameters, offset, length);
                this.xmlConsumer.endElement(URI, E_VALUE_L, E_VALUE_Q);
                offset += length;
            }

            /* Close the parameter element */
            this.xmlConsumer.endElement(URI, E_PARAM_L, E_PARAM_Q);
        }

        /* Close the core elements and good bye */
        this.xmlConsumer.endElement(URI, E_PARAMS_L, E_PARAMS_Q);
        this.xmlConsumer.endElement(URI, E_REQ_L, E_REQ_Q);
        this.xmlConsumer.endPrefixMapping(PREFIX);
        this.xmlConsumer.endDocument();
    }

    /* == INNER CLASSES ========================================================== */

    /**
     * <p>This class encodes a varying number of request parameters into a characters
     * array, and gives this character array a {@link SourceValidity} view.</p>
     *
     * <p>Parameters are encoded using a count-prefix format (each array is prefixed
     * by its length), or roughly outlined as follows:</p>
     *
     * <p>
     *   <code>
     *     <b>count(parameters)</b><br>
     *     <br>
     *     <b>count(values[param0])+1</b><br>
     *     <b>length(name[param0])</b> <i>name[param0]</i><br>
     *     <b>length(value[param0][0])</b> <i>value[param0]</i><br>
     *     <b>length(value[param0][1])</b> <i>value[param0]</i><br>
     *     ...<br>
     *     <b>length(value[param0][N])</b> <i>value</i><br>
     *     ...<br>
     *     <b>count(values[paramK])+1</b><br>
     *     <b>length(name[paramK])</b> <i>name[paramK]</i><br>
     *     <b>length(value[paramK][0])</b> <i>value[paramK]</i><br>
     *     <b>length(value[paramK][1])</b> <i>value[paramK]</i><br>
     *     ...<br>
     *     <b>length(value[paramK][N])</b> <i>value</i><br>
     *   </code>
     * </p>
     */
    private static class Parameters implements SourceValidity {

        /** <p>An array of characters holding our NULL-separated parameters.</p> */
        private char array[] = null;
        /** <p>The {@link String} representation of this instance.</p> */
        private transient String string;
        /** <p>The hash code of this instance.</p> */
        private transient int hash = 0;

        /**
         * <p>Create a new {@link RequestParameterGenerator.Parameters} instance.</p>
         */
        private Parameters(Request request) {
            /* Sort the parameter names */
            StringBuffer buff = new StringBuffer();
            Set sort1 = new TreeSet();
            Set sort2 = new TreeSet();
            Enumeration enumeration = request.getParameterNames();
            while (enumeration.hasMoreElements()) sort1.add(enumeration.nextElement());

            /* Declare how many parameters we have */
            if (sort1.size() > Character.MAX_VALUE) {
                throw new IllegalArgumentException("Too many parameters");
            } else {
                buff.append((char)sort1.size());
            }

            /* Run through the sorted parameter names to get the values */
            Iterator iter1 = sort1.iterator();
            while (iter1.hasNext()) {
                /* Access the parameter name and values */
                String name = (String) iter1.next();
                String values[] = request.getParameterValues(name);

                /* Declare how many values (plus 1 for the name) we have */
                if (values.length >= Character.MAX_VALUE) {
                    throw new IllegalArgumentException("Too many parameter values");
                } else {
                    buff.append((char)(values.length + 1));
                }

                /* Encode the parameter name */
                if (name.length() > Character.MAX_VALUE) {
                    throw new IllegalArgumentException("Parameter name too long");
                } else {
                    buff.append((char)(name.length()));
                    buff.append(name);
                }

                /* Sort the parameter values */
                sort2.clear();
                for (int x = 0; x < values.length; x++) sort2.add(values[x]);

                /*  Run through the parameter values and encode them in the buffer */
                Iterator iter2 = sort2.iterator();
                while (iter2.hasNext()) {
                    /* Get the value and encode it */
                    String value = (String) iter2.next();
                    if (value.length() > Character.MAX_VALUE) {
                        throw new IllegalArgumentException("Parameter value too long");
                    } else {
                        buff.append((char)(value.length()));
                        buff.append(value);
                    }
                }
            }

            /* Save the array */
            this.array = new char[buff.length()];
            buff.getChars(0, buff.length(), array, 0);
        }

        /**
         * <p>Check the validity of this instance</p>
         *
         * <p>Given that the key returned by the {@link RequestParameterGenerator}
         * is the string representation of this object, we can safely assume that
         * Cocoon will select us only when the appropriate parameters are passed
         * to the pipeline.</p>
         *
         * <p>In this case, then the validity of this instance, by itself, is always
         * {@link SourceValidity#VALID}, as (in theory) Cocoon always accesses us
         * with the correct uniquely identifying key.</p>
         *
         * @see SourceValidity#isValid()
         */
        public int isValid() {
            return SourceValidity.VALID;
        }

        /**
         * <p>Compare the validity against another {@link SourceValidity}.</p>
         *
         * <p>This method will return {@link SourceValidity#VALID} if and only if
         * this instance {@link #equals equals} the specified validity, and
         * {@link SourceValidity#INVALID} in all other cases.</p>
         *
         * @see SourceValidity#isValid(SourceValidity)
         */
        public int isValid(SourceValidity validity) {
            return (this.equals(validity)? VALID: INVALID);
        }

        /**
         * <p>Check that this instance equals another object.</p>
         *
         * <p>This method will calculate the hash code in the same way the standard
         * {@link String} does, but operating on the encoded parameters.</p>
         *
         * @see String#hashCode()
         */
        public boolean equals(Object object) {
            /* Null check */
            if (object == null) return(false);

            /* Class check and conversion */
            if (!(object instanceof Parameters)) return(false);
            Parameters parameters = (Parameters) object;

            /* Longer full-string check */
            if (this.array.length != parameters.array.length) return(false);
            for (int x = 0; x < this.array.length; x++) {
                if (this.array[x] != parameters.array[x]) return(false);
            }

            /* Should be the same now! */
            return(true);
        }

        /**
         * <p>Return the hash code of this instance.</p>
         *
         * <p>This method will calculate the hash code in the same way the standard
         * {@link String} does, but operating on the encoded parameters.</p>
         *
         * @see String#hashCode()
         */
        public int hashCode() {
            /* This roughly does what String.hashCode() does (if I got it right) */
            int k = this.hash;
            if (k == 0) {
                for (int x = 0; x < array.length; x++) k = 31 * k + array[x];
                this.hash = k;
            }
            return(k);
        }

        /**
         * <p>Return the encoded string representing this group of parameters.</p>
         */
        public String toString() {
            if (this.string == null) this.string = new String(this.array);
            return(this.string);
        }
    }
}
