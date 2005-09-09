/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.components.validation;

import org.apache.avalon.framework.service.ServiceSelector;

/**
 * <p>The {@link Validator} interface defines a {@link ServiceSelector} selecting
 * between different {@link SchemaParser}.</p>
 * 
 * <p>Selection can occur either normally, based on a component name specified in
 * the configuration files, or on the {@link SchemaParser#getSupportedGrammars()
 * supported grammars} of the configured {@link SchemaParser}s.</p>
 *
 * @author <a href="mailto:pier@betaversion.org">Pier Fumagalli</a>
 */
public interface Validator extends ServiceSelector {

    /** <p>Role name to use when this component is accessed via a selector.</p> */
    public static final String ROLE = Validator.class.getName();

    /** 
     * <p>An grammar identifer specifying tht the {@link SchemaParser} can
     * automatically detect the grammar while parsing.</p>
     */
    public static final String GRAMMAR_AUTOMATIC = "http://apache.org/cocoon/validation/grammars/automatic/1.0";

    /** <p>The <a href="http://www.relaxng.org/">RELAX NG</a/> grammar identifer.</p> */
    public static final String GRAMMAR_RELAX_NG = "http://relaxng.org/ns/structure/0.9";
    /** <p>The <a href="http://www.xml.gr.jp/relax">RELAX CORE</a/> grammar identifer.</p> */
    public static final String GRAMMAR_RELAX_CORE = "http://www.xml.gr.jp/xmlns/relaxCore";
    /** <p>The <a href="http://www.xml.gr.jp/relax">RELAX NAMESPACE</a/> grammar identifer.</p> */
    public static final String GRAMMAR_RELAX_NS = "http://www.xml.gr.jp/xmlns/relaxNamespace";
    /** <p>The <a href="http://www.thaiopensource.com/trex/">TREX</a/> grammar identifer.</p> */
    public static final String GRAMMAR_TREX = "http://www.thaiopensource.com/trex";
    /** <p>The <a href="http://www.w3.org/XML/Schema">XML SCHEMA</a/> grammar identifer.</p> */
    public static final String GRAMMAR_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    /** <p>The <a href="http://www.w3.org/TR/REC-xml">XML DTD</a/> grammar identifer.</p> */
    public static final String GRAMMAR_XML_DTD = "http://www.w3.org/XML/1998/namespace";

}
