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
package org.apache.butterfly.source.impl;

import java.io.InputStream;

import org.apache.butterfly.source.Source;
import org.apache.butterfly.source.SourceValidity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Description of URLSource.
 * 
 * @version CVS $Id: URLSource.java,v 1.1 2004/07/23 08:47:20 ugo Exp $
 */
public class URLSource implements Source {
    
    /** The logger */
    protected static final Log logger = LogFactory.getLog(URLSource.class);

    /* (non-Javadoc)
     * @see org.apache.butterfly.source.Source#exists()
     */
    public boolean exists() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.source.Source#getInputStream()
     */
    public InputStream getInputStream() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.source.Source#getURI()
     */
    public String getURI() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.source.Source#getScheme()
     */
    public String getScheme() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.source.Source#getValidity()
     */
    public SourceValidity getValidity() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.source.Source#refresh()
     */
    public void refresh() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.source.Source#getMimeType()
     */
    public String getMimeType() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.source.Source#getContentLength()
     */
    public long getContentLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.source.Source#getLastModified()
     */
    public long getLastModified() {
        // TODO Auto-generated method stub
        return 0;
    }

}
