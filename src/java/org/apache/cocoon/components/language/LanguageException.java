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
package org.apache.cocoon.components.language;

import org.apache.avalon.framework.CascadingException;
import org.apache.cocoon.components.language.programming.CompilerError;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileReader;

/**
 * The language exception.
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @version CVS $Id: LanguageException.java,v 1.1 2003/03/09 00:08:52 pier Exp $
 */
public class LanguageException extends CascadingException {

    private CompilerError[] errors = null;
    private String filename = null;

    public LanguageException(String message, String filename, CompilerError[] errors) {
        super(message);
        this.filename = filename;
        this.errors = errors;
    }

    public LanguageException(String message) {
        super(message);
    }

    public LanguageException(String message, Throwable t) {
        super(message, t);
    }

    public String getMessage() {
        if (errors != null) {
            StringBuffer extendedMessage = new StringBuffer();
            extendedMessage.append(super.getMessage());

            if (errors != null && filename != null) {
                extendedMessage.append(getSource(filename));
            }

            for (int i = 0; i < errors.length; i++) {
                CompilerError error = errors[i];
                if (i > 0) extendedMessage.append("\n");
                extendedMessage.append("Line ");
                extendedMessage.append(error.getStartLine());
                extendedMessage.append(", column ");
                extendedMessage.append(error.getStartColumn());
                extendedMessage.append(": ");
                extendedMessage.append(error.getMessage());
            }
            return (extendedMessage.toString());
        }
        else {
            return(super.getMessage());
        }
    }

    // Stolen from ProcessingException...

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(super.toString());
        if(getCause()!=null) {
            s.append(": ");
            s.append(getCause().toString());
        }
        return s.toString();
    }

    public void printStackTrace() {
        super.printStackTrace();
        if(getCause()!=null)
            getCause().printStackTrace();
    }

    public void printStackTrace( PrintStream s ) {
        super.printStackTrace(s);
        if(getCause()!=null)
            getCause().printStackTrace(s);
    }

    public void printStackTrace( PrintWriter s ) {
        super.printStackTrace(s);
        if(getCause()!=null)
            getCause().printStackTrace(s);
    }

    private final static int linesBefore = 3;
    private final static int linesAfter = 3;


    private final static String getString( char[] buffer, int start, int end) {
        int currentLine = 1;
        int currentPos = 0;

        while(currentLine < start && currentPos < buffer.length) {
                 if (buffer[currentPos++] == '\n') {
                     currentLine++;
                 }
             }
        int startPos = currentPos;

        while(currentLine < (end+1) && currentPos < buffer.length) {
            if (buffer[currentPos++] == '\n') {
                currentLine++;
            }
        }
        int endPos = currentPos;

        return( new String( buffer, startPos, endPos-startPos ));
    }

    private String getSource( String filename ) {
        File file = new File(filename);
        long fileSize = file.length();
        if (file != null && file.exists() && file.isFile() && fileSize > 0) {
            // paranoid checking: nothing larger than ints can handle or 10MB
            if (fileSize < Integer.MAX_VALUE || fileSize < 10 * 1024 * 1024) {
                char[] buffer = new char[(int) fileSize];
                try {
                    FileReader fileReader = new FileReader(file);
                    fileReader.read(buffer, 0, (int) fileSize);

                    StringBuffer listing = new StringBuffer();

                    for (int i = 0; i < errors.length; i++) {
                        CompilerError error = errors[i];

                        int start = error.getStartLine();
                        int end = error.getEndLine();

                        if (start > 0 && end > 0) {
                            String before = getString(buffer, start - 1 - linesBefore, start - 1);
                            String itself = getString(buffer, start, end);
                            String after = getString(buffer, end + 1, end + 1 + linesAfter);

                            listing.append("ERROR ").append(i + 1).append(" (").append(error.getFile()).append("):\n");
                            listing.append("...\n");
                            listing.append(before);
                            listing.append("\n// start error (lines ").append(error.getStartLine()).append("-").append(error.getEndLine()).append(") \"").append(error.getMessage()).append("\"\n");
                            listing.append(itself);
                            listing.append("\n// end error\n");
                            listing.append(after);
                            listing.append("\n...\n");
                        }
                    }

                    fileReader.close();

                    return (listing.toString());
                }
                catch (Exception e) {
                }
            }
        }
        return (null);
    }

    /*
    public static void main(String[] args) {
        String s =
        "1 \n"+
        "2 System.out.println(\n"+
        "3 \n"+
        "4 this.contentHandler.startDocument();\n"+
        "5 AttributesImpl xspAttr = new AttributesImpl();\n"+
        "6 \n";

        char[] buffer = s.toCharArray();

        int start = 2;
        int end = 2;

        String before = getString(buffer, start - 1 - linesBefore, start - 1);
        String itself = getString(buffer, start, end);
        String after = getString(buffer, end + 1, end + 1 + linesAfter);


        System.out.print(before);
        System.out.println("--");
        System.out.print(itself);
        System.out.println("--");
        System.out.print(after);
    }
    */
}
