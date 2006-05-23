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
package org.apache.cocoon.components.validation.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.AggregatedValidity;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * <p>An internal {@link InputSource} resolver that can be used while parsing
 * schemas.</p>
 *
 * <p>This instance will track all resolved external sources and will store their
 * validity information. An aggregated {@link SourceValidity} for all resolved
 * sources can be retrieved when {@link #close() closing} this instance.</p>
 *
 */
public class ValidationResolver implements EntityResolver {

    /** <p>The {@link SourceResolver} to access {@link Source}s.</p> */
    private final SourceResolver sourceResolver;
    /** <p>The {@link EntityResolver} to resolve public IDs against catalogs.</p> */
    private final EntityResolver entityResolver;
    /** <p>The global {@link SourceValidity} of all resolved sources.</p> */
    private final AggregatedValidity sourceValidity;

    /** <p>A {@link List} of {@link Source} to be released when closing.</p> */ 
    private final List sources = new ArrayList();

    /** <p>A flag indicating whether this instance has been closed.</p> */ 
    private boolean closed = false;

    /**
     * <p>Create a new {@link ValidationResolver} instance.</p>
     * 
     * @throws NullPointerException if one of the specified {@link SourceResolver}
     *                              or {@link EntityResolver} was <b>null</b>. 
     */
    public ValidationResolver(SourceResolver sourceResolver,
                              EntityResolver entityResolver) {
        if (sourceResolver == null) throw new NullPointerException("Null source");
        if (entityResolver == null) throw new NullPointerException("Null entity");
        this.sourceValidity = new AggregatedValidity();
        this.sourceResolver = sourceResolver;
        this.entityResolver = entityResolver;
    }

    /**
     * <p>Resolve a {@link Source} into an {@link InputSource}.</p>
     */
    public InputSource resolveSource(Source source)
    throws IOException, SAXException {
        return this.resolveSource(source, null, null);
    }

    /**
     * <p>Resolve a {@link Source} into an {@link InputSource}, specifying a
     * specific system identifier.</p>
     */
    public InputSource resolveSource(Source source, String systemId)
    throws IOException, SAXException {
        return this.resolveSource(source, systemId, null);
    }

    /**
     * <p>Resolve a {@link Source} into an {@link InputSource}, specifying both
     * a specific system identifier and a public identifier.</p>
     * 
     * <p>If the specified system identifier was <b>null</b> the returned
     * {@link InputSource}'s {@link InputSource#getSystemId() system identifier}
     * will be obtained calling the {@link Source#getURI()} method.</p>
     */
    public InputSource resolveSource(Source source, String systemId, String publicId)
    throws IOException, SAXException {
        if (this.closed) throw new IllegalStateException("Resolver closed");

        /* Validate what we've been passed */
        if (source == null) throw new NullPointerException("Null source specified");

        /* Record the current source in the validities to return */
        this.sourceValidity.add(source.getValidity());

        /* Ensure that we have a proper system id */
        if (systemId == null) systemId = source.getURI();

        /* Create a new input source and return it filled out entirely */
        InputSource input = new InputSource(systemId);
        input.setByteStream(source.getInputStream());
        if (publicId != null) input.setPublicId(publicId);
        return input;
    }

    /**
     * <p>Resolve an entity identified by a specific system identifier as an
     * {@link InputSource}.</p>
     */
    public InputSource resolveEntity(String systemId)
    throws IOException, SAXException {
        return this.resolveEntity(null, null, systemId);
    }

    /**
     * <p>Resolve an entity identified by a specific system and public identifier
     * as an {@link InputSource}.</p>
     */
    public InputSource resolveEntity(String publicId, String systemId)
    throws IOException, SAXException {
        return this.resolveEntity(null, publicId, systemId);
    }

    /**
     * <p>Resolve an entity identified by a specific system and public identifier
     * and relative to a specified base location as an {@link InputSource}.</p>
     */
    public InputSource resolveEntity(String base, String publicId, String systemId)
    throws IOException, SAXException {
        if (this.closed) throw new IllegalStateException("Resolver closed");

        /* If the specified system id was null use the global entity resolver */
        if (systemId == null) {
            InputSource source = this.entityResolver.resolveEntity(publicId, null);
            if ((source == null) || (source.getSystemId() == null)) {
                throw new IOException("Can't resolve \"" + publicId + "\"");
            }
            systemId = source.getSystemId();
        }

        /* Now that we have a valid system id, attempt to resolve it as a source */
        final Source source;
        if (base == null) {
            source = this.sourceResolver.resolveURI(systemId);
        } else {
            source = this.sourceResolver.resolveURI(systemId, base, null);
        }

        /* Record this source as a source to release back to the resolver */
        this.sources.add(source);

        /* Return the resolved input source back to the caller */
        return this.resolveSource(source, systemId, publicId);
    }

    /**
     * <p>Close this {@link ValidationResolver} instance, releasing all created
     * {@link Source}s back to the {@link SourceResolver} and returning an
     * aggregated {@link SourceValidity}.</p>
     */
    public SourceValidity close() {

        /* Release all the sources that were opened using this source resolver */
        Iterator iterator = this.sources.iterator();
        while (iterator.hasNext()) {
            this.sourceResolver.release((Source) iterator.next());
        }

        /* Mark this instance as closed */
        this.closed = true;

        /* Return the source validity associated with this instance */
        return this.sourceValidity;
    }

    /**
     * <p>Ensure that when this object is garbage collected, the {@link #close()}
     * method is executed.</p>
     */
    protected void finalize()
    throws Throwable {
        try {
            super.finalize();
        } finally {
            if (this.closed) return;
            this.close();
        }
    }
}
