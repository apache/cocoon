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
package org.apache.cocoon.components.validation.xerces;

import java.io.IOException;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.AggregatedValidity;
import org.apache.excalibur.xml.EntityResolver;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * <p>An implementation of Xerces' {@link XMLEntityResolver} resolving URIs using
 * Excalibur's {@link SourceResolver} and {@link EntityResolver}.</p>
 *
 * <p>Most of this code has been derived from the Xerces JAXP Validation interface
 * available in the <code>org.xml.xerces.jaxp.validation</code> package.</p>
 *
 * @author <a href="mailto:pier@betaversion.org">Pier Fumagalli</a>
 */
public class XercesEntityResolver implements XMLEntityResolver {
    
    private final AggregatedValidity sourceValidity = new AggregatedValidity();
    private final SourceResolver sourceResolver;
    private final EntityResolver entityResolver;

    public XercesEntityResolver() {
        this(null, null);
    }

    public XercesEntityResolver(SourceResolver sourceResolver,
                                EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
        this.sourceResolver = sourceResolver;
    }

    public SourceValidity getSourceValidity() {
        return this.sourceValidity;
    }

    public XMLInputSource resolveSource(Source source)
    throws XNIException, IOException {
        this.sourceValidity.add(source.getValidity());
        String location = source.getURI();
        XMLInputSource input = new XMLInputSource(null, location, location);
        input.setByteStream(source.getInputStream());
        return input;
    }

    public XMLInputSource resolveUri(String location)
    throws XNIException, IOException {
        if (this.sourceResolver == null) throw new IOException("Can't resolve now");

        /* Use Excalibur's SourceResolver to resolve the system id */
        Source source = this.sourceResolver.resolveURI(location);
        location = source.getURI();
        try {
            return this.resolveSource(source);
        } finally {
            this.sourceResolver.release(source);
        }
    }

    public XMLInputSource resolveEntity(XMLResourceIdentifier identifier)
    throws XNIException, IOException {
        if (this.sourceResolver == null) throw new IOException("Can't resolve now");

        String publicId = identifier.getPublicId();
        String systemId = identifier.getLiteralSystemId();
        String baseURI = identifier.getBaseSystemId();

        /* Try to resolve the public id if we don't have a system id */
        if ((systemId == null) && (this.entityResolver != null)) try {
            InputSource source = this.entityResolver.resolveEntity(publicId, null);
            if ((source == null) || (source.getSystemId() == null)) {
                throw new IOException("Can't resolve \"" + publicId + "\"");
            } else {
                systemId = source.getSystemId();
            }
        } catch (SAXException exception) {
            throw new XNIException("Error resolving public id", exception);
        }

        /* Use Cocoon's SourceResolver to resolve the system id */
        Source source = this.sourceResolver.resolveURI(systemId, baseURI, null);
        systemId = source.getURI();
        try {
            this.sourceValidity.add(source.getValidity());
            XMLInputSource input = new XMLInputSource(publicId, systemId, baseURI);
            input.setByteStream(source.getInputStream());
            return input;
        } finally {
            this.sourceResolver.release(source);
        }
    }
}
