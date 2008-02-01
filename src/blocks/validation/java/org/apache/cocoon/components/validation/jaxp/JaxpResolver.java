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
package org.apache.cocoon.components.validation.jaxp;

import org.apache.cocoon.components.validation.impl.ValidationResolver;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.excalibur.source.SourceResolver;
import org.w3c.dom.DOMError;
import org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * <p>An implementation of the {@link LSResourceResolver} interface based on the
 * generic {@link ValidationResolver} to supply to JAXP schema factories.</p>
 *
 */
public class JaxpResolver extends ValidationResolver
implements LSResourceResolver {

    /**
     * <p>Create a new {@link JaxpResolver} instance.</p>
     */
    public JaxpResolver(SourceResolver sourceResolver,
                        EntityResolver entityResolver) {
        super(sourceResolver, entityResolver);
    }

    /**
     * <p>Resolve a resource into a {@link LSInput} from the provided location
     * information.</p>
     *
     * <p>This method will obtain a {@link InputSource} instance invoking the
     * {@link ValidationResolver#resolveEntity(String, String, String)} method
     * return it wrapped in a {@link JaxpInput} instance.</p>
     *
     * @param type the type of the resource being resolved.
     * @param namespace the namespace of the resource being resolved.
     * @param systemId the system identifier of the resource being resolved.
     * @param publicId the public identifier of the resource being resolved.
     * @param base the base uri against wich relative resolution should happen.
     * @return a <b>non null</b> {@link LSInput} instance.
     * @throws LSException wrapping another {@link Exception}.
     */
    public LSInput resolveResource(String type, String namespace, String publicId,
                                   String systemId, String base)
    throws LSException {
        try {
            final InputSource source = this.resolveEntity(base, publicId, systemId);
            return new JaxpInput(source);
        } catch (Exception exception) {
            String message = "Exception resolving resource " + systemId;
            Throwable err = new LSException(DOMError.SEVERITY_FATAL_ERROR, message);
            throw new NestableRuntimeException(message, err);
        }
    }
}
