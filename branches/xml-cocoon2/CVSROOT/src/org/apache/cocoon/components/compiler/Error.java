/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
 
package org.apache.cocoon.components.compiler;

/**
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.2.1 $ $Date: 1999-12-11 23:28:51 $
 * @since 2.0
 */

public class Error {
    
    private boolean error;
    private int startline;
    private int startcolumn;
    private int endline;
    private int endcolumn;
    private String file;
    private String message;
    
    public Error(String file, boolean error, int startline, int startcolumn, 
                 int endline, int endcolumn, String message) 
    {
        this.file = file;
        this.error = error;
        this.startline = startline;
        this.startcolumn = startcolumn;
        this.endline = endline;
        this.endcolumn = endcolumn;
        this.message = message;
    }

    public String getFile() {
        return file;
    }

    public boolean isError() {
        return error;
    }

    public int getStartLine() {
        return startline;
    }

    public int getStartColumn() {
        return startcolumn;
    }

    public int getEndLine() {
        return endline;
    }

    public int getEndColumn() {
        return endcolumn;
    }

    public String getMessage() {
        return message;
    }
}