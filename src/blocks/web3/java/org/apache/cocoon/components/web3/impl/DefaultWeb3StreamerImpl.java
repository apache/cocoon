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

package org.apache.cocoon.components.web3.impl;

import com.sap.mw.jco.JCO;

import org.apache.cocoon.components.web3.Web3Streamer;
import org.apache.cocoon.components.web3.Web3;

import org.apache.avalon.excalibur.pool.Poolable;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.ContentHandler;

/**
 * TBD
 *
 * @author <a href="mailto:michael.gerzabek@at.efp.cc">Michael Gerzabek</a>
 * @since 2.1
 * @version CVS $Id: DefaultWeb3StreamerImpl.java,v 1.6 2003/07/10 22:14:32 reinhard Exp $
 */
public class DefaultWeb3StreamerImpl implements Web3Streamer, Poolable {

    public void stream(JCO.Function function, 
        ContentHandler contentHandler) throws SAXException {

        AttributesImpl    attributes = new AttributesImpl();
        attributes.clear();
        attributes.addAttribute( Web3.URI, Web3.INCLUDE_NAME_ATTR,  
            Web3.INCLUDE_NAME_ATTR, "CDATA", function.getName().toUpperCase() );
        contentHandler.startElement( Web3.URI, Web3.INCLUDE_ELEM, 
            Web3.INCLUDE_ELEM, attributes );
        attributes.clear();
        contentHandler.startElement( Web3.URI, Web3.IMPORT_ELEM, 
            Web3.IMPORT_ELEM, attributes );
        streamParameterList( function.getImportParameterList(), contentHandler );
        contentHandler.endElement( Web3.URI, Web3.IMPORT_ELEM, Web3.IMPORT_ELEM );

        attributes.clear();
        contentHandler.startElement( Web3.URI, Web3.EXPORT_ELEM, 
            Web3.EXPORT_ELEM, attributes );
        streamParameterList( function.getExportParameterList(), contentHandler );
        contentHandler.endElement( Web3.URI, Web3.EXPORT_ELEM, Web3.EXPORT_ELEM );
        
        JCO.ParameterList tablesParameterList = function.getTableParameterList();
        attributes.clear();
        contentHandler.startElement( Web3.URI, Web3.TABLES_ELEM, 
            Web3.TABLES_ELEM, attributes );
        if (null != tablesParameterList) {
            for (int i = 0; i < tablesParameterList.getFieldCount(); i++) {
                attributes.clear();
                attributes.addAttribute( Web3.URI, Web3.TABLE_NAME_ATTR, 
                    Web3.TABLE_NAME_ATTR, "CDATA", 
                    tablesParameterList.getName(i).toUpperCase() );
                contentHandler.startElement( Web3.URI, Web3.TABLE_ELEM,
                    Web3.TABLE_ELEM, attributes );
                JCO.Table sapTable = tablesParameterList.getTable(i);
                if (null != sapTable) {
                    for (int j = 0; j < sapTable.getNumRows(); j++) {
                        sapTable.setRow(j);
                        attributes.clear();
                        attributes.addAttribute(Web3.URI, Web3.ROW_ID_ATTR, 
                            Web3.ROW_ID_ATTR, "CDATA", "" + (j + 1));
                        contentHandler.startElement(Web3.URI, Web3.ROW_ELEM, 
                            Web3.ROW_ELEM, attributes);
                        for (int k = 0; k < sapTable.getFieldCount(); k++) {
                            attributes.clear();
                            attributes.addAttribute(Web3.URI, 
                                Web3.FIELD_NAME_ATTR, Web3.FIELD_NAME_ATTR, 
                                "CDATA", sapTable.getName(k).toUpperCase());
                            contentHandler.startElement(Web3.URI, 
                                Web3.FIELD_ELEM, Web3.FIELD_ELEM, attributes);
                            String theValue = ( sapTable.getString(k) == null) 
                                ? "" : sapTable.getString(k).trim();
                            contentHandler.characters(theValue.toCharArray(), 0, 
                                theValue.length());
                            contentHandler.endElement(Web3.URI, Web3.FIELD_ELEM, 
                                Web3.FIELD_ELEM);
                        }
                        contentHandler.endElement(Web3.URI, Web3.ROW_ELEM, 
                            Web3.ROW_ELEM);
                    }
                    contentHandler.endElement(Web3.URI, Web3.TABLE_ELEM, 
                        Web3.TABLE_ELEM);
                }
            }           
        }
        contentHandler.endElement(Web3.URI, Web3.TABLES_ELEM, Web3.TABLES_ELEM);
        contentHandler.endElement( Web3.URI, Web3.INCLUDE_ELEM, 
            Web3.INCLUDE_ELEM );
    }
    
    protected void streamParameterList(JCO.ParameterList pList,
        ContentHandler contentHandler) throws SAXException {
        
        AttributesImpl attributes = new AttributesImpl();
        attributes.clear();
        if (pList != null) {
            for (int i = 0; i < pList.getFieldCount(); i++) {
                JCO.Field theField = pList.getField(i);
                if (theField.isStructure()) {
                    JCO.Structure sapStructure = 
                        pList.getStructure(pList.getName(i));
                    attributes.clear();
                    attributes.addAttribute(Web3.URI, Web3.STRUCTURE_NAME_ATTR, 
                        Web3.STRUCTURE_NAME_ATTR, "CDATA", 
                        pList.getName(i).toUpperCase());
                    contentHandler.startElement(Web3.URI, Web3.STRUCTURE_ELEM, 
                        Web3.STRUCTURE_ELEM, attributes);                
                    for (int j = 0; j < sapStructure.getFieldCount(); j++) {
                        attributes.clear();
                        attributes.addAttribute(Web3.URI, Web3.FIELD_NAME_ATTR, 
                            Web3.FIELD_NAME_ATTR, "CDATA", 
                            sapStructure.getName(j).toUpperCase());
                        contentHandler.startElement(Web3.URI, Web3.FIELD_ELEM, 
                            Web3.FIELD_ELEM, attributes);
                        String theValue = (sapStructure.getString(j) == null) 
                            ? "" : sapStructure.getString(j).trim();
                        contentHandler.characters(theValue.toCharArray(), 0, 
                            theValue.length());
                        contentHandler.endElement(Web3.URI, Web3.FIELD_ELEM, 
                            Web3.FIELD_ELEM);
                    }
                    contentHandler.endElement(Web3.URI, Web3.STRUCTURE_ELEM, 
                        Web3.STRUCTURE_ELEM);
                } 
                else {
                    attributes.clear();
                    attributes.addAttribute(Web3.URI, Web3.FIELD_NAME_ATTR, 
                        Web3.FIELD_NAME_ATTR, "CDATA", 
                        pList.getName(i).toUpperCase());
                    contentHandler.startElement(Web3.URI, Web3.FIELD_ELEM, 
                        Web3.FIELD_ELEM, attributes);
                    String theValue = (pList.getString(i) == null)
                        ? "" : pList.getString(i).trim();
                    contentHandler.characters(theValue.toCharArray(), 0, 
                        theValue.length());
                    contentHandler.endElement(Web3.URI, Web3.FIELD_ELEM, 
                        Web3.FIELD_ELEM);
                }
            }
        }        
    }

}

