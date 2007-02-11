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
package org.apache.cocoon.serialization;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.cocoon.components.elementprocessor.ElementProcessorFactory;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.HSSFElementProcessorFactory;

/**
 *  Serializer to produce an HSSF stream.
 *
 * @author   Marc Johnson (marc_johnson27591@hotmail.com)
 * @author   Nicola Ken Barozzi (nicolaken@apache.org)
 * @version CVS $Id: HSSFSerializer.java,v 1.3 2003/04/02 04:22:14 crossley Exp $
 */
public class HSSFSerializer extends POIFSSerializer implements Initializable, Configurable {
    private ElementProcessorFactory _element_processor_factory;
    private final static String _mime_type = "application/vnd.ms-excel";
    String locale;

    /**
     *  Constructor
     */
    public HSSFSerializer() {
        super();
    }

    /**
     * Initialialize the component. Initialization includes allocating any
     * resources required throughout the components lifecycle.
     *
     * @exception Exception if an error occurs
     */
    public void initialize() throws Exception {
        _element_processor_factory = new HSSFElementProcessorFactory(locale);
        setupLogger(_element_processor_factory);
    }

    public void configure(Configuration conf) throws ConfigurationException {
        Configuration[] parameters = conf.getChildren("parameter");
        for (int i = 0; i < parameters.length; i++) {
            String name = parameters[i].getAttribute("name");
            if (name.trim().equals("locale")) {
                locale = parameters[i].getAttribute("value");
            }
        }
    }

    /**
     * get the mime type
     *
     *@return    application/vnd.ms-excel
     */
    public String getMimeType() {
        return _mime_type;
    }

    /**
     *  get the ElementProcessorFactory
     *
     *@return    the ElementProcessorFactory
     */
    protected ElementProcessorFactory getElementProcessorFactory() {
        return _element_processor_factory;
    }

    /**
     *  post-processing for endDocument
     */
    protected void doLocalPostEndDocument() {
    }

    /**
     *  pre-processing for endDocument
     */
    protected void doLocalPreEndDocument() {
    }

}
