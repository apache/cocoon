/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: PartSource.java,v 1.4 2003/12/20 14:28:34 sylvain Exp $
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
