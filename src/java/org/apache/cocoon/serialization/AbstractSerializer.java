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
package org.apache.cocoon.serialization;

import org.apache.cocoon.xml.AbstractXMLPipe;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation)
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Id: AbstractSerializer.java,v 1.4 2004/03/08 14:03:32 cziegeler Exp $
 */

public abstract class AbstractSerializer
extends AbstractXMLPipe implements Serializer {

    /**
     * The <code>OutputStream</code> used by this serializer.
     */
    protected OutputStream output;

    /**
     * Set the {@link OutputStream} where the requested resource should
     * be serialized.
     */
    public void setOutputStream(OutputStream out)
    throws IOException {
        this.output = out;
    }

    /**
     * Get the mime-type of the output of this <code>Serializer</code>
     * This default implementation returns null to indicate that the
     * mime-type specified in the sitemap is to be used
     */
    public String getMimeType() {
        return null;
    }

    /**
     * Recycle serializer by removing references
     */
    public void recycle() {
        super.recycle();
        this.output = null;
    }

    /**
     * Test if the component wants to set the content length
     */
    public boolean shouldSetContentLength() {
        return false;
    }

}
