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

*/

package org.apache.cocoon.components.flow.javascript;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.cocoon.ProcessingException;
import org.apache.excalibur.source.Source;

/**
 * Maintains a list of {@link org.apache.cocoon.environment.Source}
 * objects to read the input from. This class keeps track of the
 * number of lines in each <code>Source</code> object, so that in case
 * of errors the location is correctly reported.
 *
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @since August 15, 2002
 */
public class ListInputStream extends InputStream {
    /**
     * The <code>List</code> of {@link
     * org.apache.cocoon.environment.Source} objects.
     */
    List sourcesList;

    /**
     * Location information on each <code>Source</code> object part of
     * <code>sourcesList</code>.
     */
    List sourcesInfo;

    /**
     * Indicates whether we read all the streams.
     */
    boolean eof = false;

    /**
     * The total number of elements in the <code>sourcesList</code> list.
     */
    private int size;

    /**
     * What is the current <code>Source</code> object being read from.
     */
    private int currentIndex;

    /**
     * The current <code>Source</code> object being read from.
     */
    protected SourceInfo currentSourceInfo;

    /**
     * The current <code>InputStream</code> object being read from.
     */
    private InputStream currentStream;

    /**
     * Given a <code>List</code> of {@link
     * org.apache.cocoon.environment.Source} objects, creates a new
     * <code>ListInputStream</code> instance.
     *
     * @param sources a <code>List</code> of {@link
     * org.apache.cocoon.environment.Source}
     */
    public ListInputStream(List sources)
        throws ProcessingException, IOException {
        sourcesList = sources;
        currentIndex = 0;
        size = sources.size();

        sourcesInfo = new ArrayList(size);
        for (int i = 0; i < size; i++)
            sourcesInfo.add(new SourceInfo((Source) sources.get(i)));

        currentSourceInfo = (SourceInfo) sourcesInfo.get(0);
        currentStream = ((Source) sources.get(0)).getInputStream();
    }

    /**
     * Simplistic implementation: return the number of number of bytes
     * that can be read only from the current stream, without bothering
     * to check the remaining streams.
     *
     * @return an <code>int</code> value
     */
    public int available() throws IOException {
        return currentStream.available();
    }

    public void close() throws IOException {
        try {
            for (int i = 0; i < size; i++)
                 ((Source) sourcesList.get(i)).getInputStream().close();
        } finally {
            currentStream = null;
            eof = true;
        }
    }

    public int read() throws IOException {
        if (eof)
            return -1;

        // Read a character from the current stream
        int ch = currentStream.read();

        // If we reached the end of the stream, try moving to the next one
        if (ch == -1) {
            currentIndex++;

            // If there are no more streams to read, indicate that to our caller
            if (currentIndex == size) {
                eof = true;
                return -1;
            }

            currentSourceInfo = (SourceInfo) sourcesInfo.get(currentIndex);
            currentStream =
                ((Source) sourcesList.get(currentIndex)).getInputStream();
        }

        // FIXME: I18N
        if (ch == '\n')
            currentSourceInfo.lineNumbers++;

        return ch;
    }

    public int read(byte[] array) throws IOException {
        return read(array, 0, array.length);
    }

    public int read(byte[] array, int offset, int len) throws IOException {
        if (eof)
            return -1;

        if (len == 0)
            return 0;

        int numberOfCharsRead = 0;

        for (int i = offset;
            numberOfCharsRead < len;
            numberOfCharsRead++, i++) {
            int ch = read();
            if (ch == -1)
                return numberOfCharsRead;

            array[i] = (byte) ch;
        }

        return numberOfCharsRead;
    }

    public boolean markSupported() {
        return false;
    }

    /**
     * Return a <code>List</code> of {@link SourceInfo} objects which
     * maintains the line number information of a {@link
     * org.apache.cocoon.environment.Source} object.
     *
     * @return a <code>List</code> value
     */
    public List getSourceInfo() {
        return sourcesInfo;
    }
}
