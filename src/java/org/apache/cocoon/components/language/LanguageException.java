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
 * @version CVS $Id: LanguageException.java,v 1.2 2004/03/05 13:02:47 bdelacretaz Exp $
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
