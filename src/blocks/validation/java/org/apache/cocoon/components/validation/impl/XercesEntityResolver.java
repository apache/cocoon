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

import org.apache.excalibur.source.SourceValidity;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;

/**
 * <p>TODO: ...</p>
 *
 * @author <a href="mailto:pier@betaversion.org">Pier Fumagalli</a>
 */
public class XercesEntityResolver implements XMLEntityResolver {
    
    public SourceValidity getSourceValidity() {
        return null;
    }

    public XMLInputSource resolveEntity(String location)
    throws XNIException, IOException {
        /*
        try {
            URI base = new URI("file://" + System.getProperty("user.dir") + "/");
            System.err.println("BASE URI: " + base.toASCIIString());
            URI relative = new URI(location);
            System.err.println("RELATIVE: " + relative.toASCIIString());
            URI resolved = base.resolve(relative);
            System.err.println("RELATIVE: " + resolved.toASCIIString());
            location = resolved.toASCIIString();
            XMLInputSource source = new XMLInputSource(null, location, location);
            source.setByteStream(resolved.toURL().openStream());
            return source;
        } catch (URISyntaxException exception) {
            String message = "Cannot resolve " + location;
            Throwable throwable = new IOException(message);
            throw (IOException) throwable.initCause(exception);
        }
        */
        throw new IOException("Not implemented"); 
    }

    public XMLInputSource resolveEntity(XMLResourceIdentifier identifier)
    throws XNIException, IOException {
        /*
        System.err.println("Resolving Identifier: "
                           + identifier.getLiteralSystemId()
                           + " from " + identifier.getBaseSystemId()
                           + " [exp=" + identifier.getExpandedSystemId() + "]");
        try {
            URI base = new URI(identifier.getBaseSystemId());
            URI relative = new URI(identifier.getLiteralSystemId());
            URI resolved = base.resolve(relative);
            XMLInputSource source = new XMLInputSource(identifier.getPublicId(),
                    resolved.toASCIIString(),
                    base.toASCIIString());
            source.setByteStream(resolved.toURL().openStream());
            return source;
        } catch (URISyntaxException exception) {
            String message = "Cannot resolve " + identifier.getLiteralSystemId();
            Throwable throwable = new IOException(message);
            throw (IOException) throwable.initCause(exception);
        }
        */
        throw new IOException("Not implemented"); 
    }
}
