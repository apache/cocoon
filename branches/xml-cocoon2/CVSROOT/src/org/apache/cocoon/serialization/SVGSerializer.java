/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
 
package org.apache.cocoon.serialization;

/**
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.6 $ $Date: 2000-08-21 17:36:26 $
 */

public class SVGSerializer extends DOMBuilder implements Serializer, Composer {
   
    /** The component manager instance */
    private ComponentManager manager = null;

    /** The current <code>OutputStream</code>. */
    private OutputStream output = null;
    
    /**
     * Set the current <code>ComponentManager</code> instance used by this
     * <code>Composer</code>.
     */
    public void setComponentManager(ComponentManager manager) {
        this.manager = manager;
    }

    /**
     * Set the <code>OutputStream</code> where the XML should be serialized.
     */
    public void setOutputStream(OutputStream out) {
        this.output = new BufferedOutputStream(out);
        super.factory = (Parser) this.manager.getComponent("parser");
    }

    /**
     * Receive notification of a successfully completed DOM tree generation.
     */
    public void notify(Document doc) throws SAXException {
        try {
            // (FIXME) (SM) we have to use an SVG rendere to create the image now.
        } catch(IOException e) {
            throw new SAXException("IOException writing image ", e);
        }
    }

    /**
     * Get the mime-type of the output of this <code>Serializer</code>
     * This default implementation returns null to indicate that the 
     * mime-type specified in the sitemap is to be used
     */
    public String getMimeType() {
        return null;
    }
}
