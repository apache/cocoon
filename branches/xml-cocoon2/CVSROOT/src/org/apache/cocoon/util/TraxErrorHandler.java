/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.util;

import javax.xml.transform.TransformerException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;

import org.apache.log.Logger;

public class TraxErrorHandler implements ErrorListener {

    private Logger logger = null;

    public TraxErrorHandler(Logger logger)
    {
        this.logger = logger;
    }

    public void warning(TransformerException exception)
        throws TransformerException
    {
        printLocation(exception);
    }

    public void error(TransformerException exception)
        throws TransformerException
    {
        printLocation(exception);
    }

    public void fatalError(TransformerException exception)
        throws TransformerException
    {
        printLocation(exception);
        throw exception;
    }

    private void printLocation(TransformerException exception)
    {
      SourceLocator locator = exception.getLocator();
  
      if(null != locator)
      {
        // System.out.println("Parser fatal error: "+exception.getMessage());
        String id = (locator.getPublicId() != locator.getPublicId())
                    ? locator.getPublicId()
                      : (null != locator.getSystemId())
                        ? locator.getSystemId() : "SystemId Unknown";
        if(logger != null)
            logger.error("Error in TraxTransformer: " + id + "; Line " + locator.getLineNumber()
                           + "; Column " + locator.getColumnNumber()+"; ", exception);
        else 
            System.out.println("Error in TraxTransformer: " + id + "; Line " + locator.getLineNumber()
                           + "; Column " + locator.getColumnNumber()+";" + exception);
      }
    }
}