package org.apache.cocoon.compiler;

/**
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:15 $
 */

public class Error {
    
    private boolean error;
    private int startline;
    private int startcolumn;
    private int endline;
    private int endcolumn;
    private String file;
    private String message;
    
    public Error(String file, boolean error, int startline, int startcolumn, int endline, int endcolumn, String message) {
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