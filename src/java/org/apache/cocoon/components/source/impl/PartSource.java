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
package org.apache.cocoon.components.source.impl;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.SourceNotFoundException;

import org.apache.cocoon.servlet.multipart.Part;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import java.net.MalformedURLException;
import java.util.Map;
import java.io.IOException;
import java.io.InputStream;


/**
 * Implementation of a {@link Source} that gets its content
 * from a PartOnDisk or PartInMemory held in the Request when
 * a file is uploaded.
 *
 * @author <a href="mailto:paul.crabtree@dna.co.uk">Paul Crabtree</a>
 * @version CVS $Id: PartSource.java,v 1.5 2004/03/05 13:02:50 bdelacretaz Exp $
 */
public class PartSource implements Source
{
    /* hold a private ref to the protocol used to call the Source */
    private String protocol;

    /* hold a private ref to the full uri */
    private String uri;

    /* hold a private ref to the Part which has been uploaded. */
    private Part part;

    /**
     * Builds a PartSource given an URI.
     * @param uri e.g., upload://formField1
     * @throws SourceException
     * @throws MalformedURLException
     */
    public PartSource(String uri, Map objectModel) throws MalformedURLException, SourceException
    {
        // set the uri for use in getURI()
        this.uri = uri;

        int position = uri.indexOf(':') + 1;
        if (position != 0)
        {
            // set the protocol for use in getScheme()
            this.protocol = uri.substring(0, position-1);
        }
        else
        {
            // if the URI is not correctly formatted then throw an excpetion
            throw new MalformedURLException("No protocol found for part source in " + uri);
        }

        // get the request parameter name: the bit after ://
        String location = uri.substring(position + 2);

        // get the cocoon request from the object model.
        Request request = ObjectModelHelper.getRequest(objectModel);

        // try and cast the request object to a Part
        Object obj = request.get(location);
        if (obj instanceof Part)
        {
             part = (Part) obj;
        }
        else
        {
             throw new SourceException("Request object " + location + " is not an uploaded Part");
        }
    }

    /**
     * @see org.apache.excalibur.source.Source#getInputStream()
     */
    public InputStream getInputStream() throws IOException, SourceNotFoundException
    {
        try
        {
            return part.getInputStream();
        }
        catch (Exception ex)
        {
            throw new SourceNotFoundException("The part source can not be found.");
        }
    }

    /**
     * @see org.apache.excalibur.source.Source#getMimeType()
     */
    public String getMimeType()
    {
        return part.getMimeType();
    }

    /**
      * @return true if the resource exists.
      */
    public boolean exists()
    {
        return part != null;
    }

    /*
     * @see org.apache.excalibur.source.Source#getURI()
     */
    public String getURI()
    {
        return uri;
    }

    /*
     * @see org.apache.excalibur.source.Source#getScheme()
     */
    public String getScheme()
    {
        return this.protocol;
    }

    /*
     * Not used, Parts are not cacheable.
     */
    public SourceValidity getValidity()
    {
        // not sure what happens here.
        return null;
    }

    /**
      * @see org.apache.excalibur.source.Source#refresh()
      */
    public void refresh()
    {
    }

    /**
     * @see org.apache.excalibur.source.Source#getContentLength()
     */
    public long getContentLength()
    {
        return part.getSize();
    }

    /**
     * @see org.apache.excalibur.source.Source#getLastModified()
     */
    public long getLastModified()
    {
        return 0;
    }
    
    public Part getPart() {
        return this.part;
    }
}
