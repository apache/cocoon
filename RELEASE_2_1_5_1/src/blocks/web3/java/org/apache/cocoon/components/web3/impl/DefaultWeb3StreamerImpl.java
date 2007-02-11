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
 * @version CVS $Id: DefaultWeb3StreamerImpl.java,v 1.7 2004/03/05 13:02:25 bdelacretaz Exp $
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

