/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.framework;

/**
 * Thrown when a <code>Configurable</code> component cannot be configured
 * properly.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-07 15:35:38 $
 * @since Cocoon 2.0
 */
public class ConfigurationException extends Exception {
    private Exception exception=null;
    private Class source=null;

    public ConfigurationException() {
        this(null,null,null);
    }

    public ConfigurationException(String msg) {
        this(msg,null,null);
    }

    public ConfigurationException(Exception exc) {
        this(null,exc);
    }

    public ConfigurationException(Class src) {
        this(null,null,src);
    }

    public ConfigurationException(String msg, Exception exc) {
        this(msg,exc,null);
    }
    
    public ConfigurationException(String msg, Class src) {
        this(msg,null,src);
    }
    
    public ConfigurationException(Exception exc, Class src) {
        this(null,exc,src);
    }
    
    public ConfigurationException(String msg, Exception exc, Class src) {
        super(msg);
        this.setException(exc);
        this.setSource(src);
    }

    public Exception getException() {
        return(this.exception);
    }

    public void setException(Exception exc) {
        this.exception=exc;
    }

    public Class getSource() {
        return(this.source);
    }

    public void setSource(Class src) {
        this.source=src;
    }
}
