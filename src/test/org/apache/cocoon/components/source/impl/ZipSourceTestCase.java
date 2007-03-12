package org.apache.cocoon.components.source.impl;

import org.apache.avalon.framework.service.ServiceException;

import org.apache.cocoon.core.container.ContainerTestCase;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;

public class ZipSourceTestCase extends ContainerTestCase {

    public void testURIHandling() throws Exception {
        String zipSourceUri = "zip:file://test.zip!/test.xml";
        Source zipSource;
        SourceResolver resolver = null;
        try {
            resolver = (SourceResolver) getManager().lookup(SourceResolver.ROLE);
            zipSource = resolver.resolveURI(zipSourceUri);
        } catch (ServiceException se) {
            throw new SourceException("SourceResolver is not available.", se);
        } finally {
            getManager().release(resolver);
        }
        assertTrue("Resolved Source is not an instance of ZipSource.", 
                   zipSource instanceof ZipSource);
        assertEquals("Scheme/protocol is wrong.", "zip", zipSource.getScheme());
        assertEquals("Uri is wrong.", zipSourceUri, zipSource.getURI());
    }
    
}
