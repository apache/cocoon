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
package org.apache.cocoon.components.store;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.cocoon.Constants;
import org.apache.cocoon.util.IOUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.BitSet;
import java.util.Enumeration;

/**
 * Stores objects on the filesystem: String objects as text files,
 * all other objects are serialized.
 *
 * @deprecated Use the {@link org.apache.cocoon.components.store.impl.FilesystemStore}
 *
 * @author ?
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: FilesystemStore.java,v 1.2 2003/09/24 22:34:52 cziegeler Exp $
 */
public final class FilesystemStore extends AbstractLogEnabled
    implements Store, Contextualizable, Parameterizable, ThreadSafe {

    protected File workDir;
    protected File cacheDir;

    /** The directory repository */
    protected File directoryFile;
    protected volatile String directoryPath;

    /**
     * Sets the repository's location
     */
    public void setDirectory(final String directory)
    throws IOException {
        this.setDirectory(new File(directory));
    }

    public void contextualize(final Context context)
    throws ContextException {
        this.workDir = (File)context.get(Constants.CONTEXT_WORK_DIR);
        this.cacheDir = (File)context.get(Constants.CONTEXT_CACHE_DIR);
    }

    public void parameterize(Parameters params)
    throws ParameterException {
        try {
            if (params.getParameterAsBoolean("use-cache-directory", false)) {
                if (this.getLogger().isDebugEnabled())
                    getLogger().debug("Using cache directory: " + cacheDir);
                setDirectory(cacheDir);
            } else if (params.getParameterAsBoolean("use-work-directory", false)) {
                if (this.getLogger().isDebugEnabled())
                    getLogger().debug("Using work directory: " + workDir);
                setDirectory(workDir);
            } else if (params.getParameter("directory", null) != null) {
                String dir = params.getParameter("directory");
                dir = IOUtils.getContextFilePath(workDir.getPath(), dir);
                if (this.getLogger().isDebugEnabled())
                    getLogger().debug("Using directory: " + dir);
                setDirectory(new File(dir));
            } else {
                try {
                    // Legacy: use working directory by default
                    setDirectory(workDir);
                } catch (IOException e) {
                    // Legacy: Always was ignored
                }
            }
        } catch (IOException e) {
            throw new ParameterException("Unable to set directory", e);
        }
    }

    /**
     * Sets the repository's location
     */
    public void setDirectory(final File directory)
    throws IOException {
        this.directoryFile = directory;

        /* Save directory path prefix */
        this.directoryPath = IOUtils.getFullFilename(this.directoryFile);
        this.directoryPath += File.separator;

        /* Does directory exist? */
        if (!this.directoryFile.exists()) {
            /* Create it anew */
            if (!this.directoryFile.mkdir()) {
                throw new IOException(
                "Error creating store directory '" + this.directoryPath + "': ");
            }
        }

        /* Is given file actually a directory? */
        if (!this.directoryFile.isDirectory()) {
            throw new IOException("'" + this.directoryPath + "' is not a directory");
        }

        /* Is directory readable and writable? */
        if (!(this.directoryFile.canRead() && this.directoryFile.canWrite())) {
            throw new IOException(
                "Directory '" + this.directoryPath + "' is not readable/writable"
            );
        }
    }

    /**
     * Returns the repository's full pathname
     */
    public String getDirectoryPath() {
        return this.directoryPath;
    }

    /**
     * Get the File object associated with the given unique key name.
     */
    public synchronized Object get(final Object key) {
        final File file = fileFromKey(key);

        if (file != null && file.exists()) {
            if (this.getLogger().isDebugEnabled()) {
                getLogger().debug("Found file: " + key);
            }
            try {
                return IOUtils.deserializeObject(file);
            } catch (Exception any) {
                getLogger().error("Error during deseralization.", any);
            }
        } else {
            if (this.getLogger().isDebugEnabled()) {
                getLogger().debug("NOT Found file: " + key);
            }
        }

        return null;
    }

    /**
     * Store the given object in a persistent state.
     * 1) Null values generate empty directories.
     * 2) String values are dumped to text files
     * 3) Object values are serialized
     */
    public synchronized void store(final Object key, final Object value)
    throws IOException {
        final File file = fileFromKey(key);

        /* Create subdirectories as needed */
        final File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }

        /* Store object as file */
        if (value == null) { /* Directory */
            if (file.exists()) {
                if (!file.delete()) { /* FAILURE */
                    getLogger().error("File cannot be deleted: " + file.toString());
                    return;
                }
            }

            file.mkdir();
        } else if (value instanceof String) {
            /* Text file */
            IOUtils.serializeString(file, (String) value);
        } else {
            /* Serialized Object */
            IOUtils.serializeObject(file, value);
        }
    }

    /**
     * Holds the given object in a volatile state.
     */
    public synchronized void hold(final Object key, final Object value)
    throws IOException {
        this.store(key, value);
        final File file = this.fileFromKey(key);
        if (file != null) {
          file.deleteOnExit();
        }
    }

    /**
     * Remove the object associated to the given key.
     */
    public synchronized void remove(final Object key) {
        final File file = fileFromKey(key);
        if (file != null) {
            file.delete();
        }
    }

    /**
     * Indicates if the given key is associated to a contained object.
     */
    public synchronized boolean containsKey(final Object key) {
        final File file = fileFromKey(key);
        if (file == null) {
            return false;
        }
        return file.exists();
    }

    /**
     * Returns the list of stored files as an Enumeration of Files
     */
    public synchronized Enumeration keys() {
        final FSEnumeration enum = new FSEnumeration();
        this.addKeys(enum, this.directoryFile);
        return enum;
    }

    /**
     * Returns count of the objects in the store, or -1 if could not be
     * obtained.
     */
    public synchronized int size() {
        return countKeys(this.directoryFile);
    }

    protected void addKeys(FSEnumeration enum, File directory) {
        final int subStringBegin = this.directoryFile.getAbsolutePath().length() + 1;
        final File[] files = directory.listFiles();
        for (int i=0; i<files.length; i++) {
            if (files[i].isDirectory()) {
                this.addKeys(enum, files[i]);
            } else {
                enum.add(this.decode(files[i].getAbsolutePath().substring(subStringBegin)));
            }
        }
    }

    protected int countKeys(File directory) {
        int count = 0;
        final File[] files = directory.listFiles();
        for (int i=0; i<files.length; i++) {
            if (files[i].isDirectory()) {
                count += this.countKeys(files[i]);
            } else {
                count ++;
            }
        }
        return count;
    }

    final class FSEnumeration implements Enumeration {
        private String[] array;
        private int      index;
        private int      length;

        FSEnumeration() {
            this.array = new String[16];
            this.length = 0;
            this.index = 0;
        }

        public void add(String key) {
            if (this.length == array.length) {
                String[] newarray = new String[this.length + 16];
                System.arraycopy(this.array, 0, newarray, 0, this.array.length);
                this.array = newarray;
            }
            this.array[this.length] = key;
            this.length++;
        }

        public boolean hasMoreElements() {
            return (this.index < this.length);
        }

        public Object nextElement() {
            if (this.hasMoreElements()) {
                this.index++;
                return this.array[index-1];
            }
            return null;
        }
    }

    /* Utility Methods*/
    protected File fileFromKey(final Object key) {
        return IOUtils.createFile(this.directoryFile, this.encode(key.toString()));
    }

    public String getString(final Object key)
    throws IOException {
        final File file = this.fileFromKey(key);
        if (file != null) {
            return IOUtils.deserializeString(file);
        }

        return null;
    }

    public synchronized void free() {}

    public synchronized Object getObject(final Object key)
    throws IOException, ClassNotFoundException
    {
        final File file = this.fileFromKey(key);
        if (file != null) {
            return IOUtils.deserializeObject(file);
        }

        return null;
    }

    /**
     * Inverse of encode exept it do not use path.
     * So decode(encode(s) - m_path) = s.
     * In other words it returns a String that can be used as key to retive
     * the record contained in the 'filename' file.
     */
    protected String decode( String filename )
    {
        try {
            return java.net.URLDecoder.decode( filename );
        } catch (Exception local) {
            throw new CascadingRuntimeException("Exception in decode", local);
        }
    }

    /** A BitSet defining the characters which don't need encoding */
    static BitSet charactersDontNeedingEncoding;
    static final int characterCaseDiff = ('a' - 'A');

    /** Initialize the BitSet */
    static
    {
        charactersDontNeedingEncoding = new BitSet(256);
        int i;
        for (i = 'a'; i <= 'z'; i++)
        {
            charactersDontNeedingEncoding.set(i);
        }
        for (i = 'A'; i <= 'Z'; i++)
        {
            charactersDontNeedingEncoding.set(i);
        }
        for (i = '0'; i <= '9'; i++)
        {
            charactersDontNeedingEncoding.set(i);
        }
        charactersDontNeedingEncoding.set('-');
        charactersDontNeedingEncoding.set('_');
        charactersDontNeedingEncoding.set('(');
        charactersDontNeedingEncoding.set(')');
    }

    /**
     * Returns a String that uniquely identifies the object.
     * <b>Note:</b> since this method uses the Object.toString()
     * method, it's up to the caller to make sure that this method
     * doesn't change between different JVM executions (like
     * it may normally happen). For this reason, it's highly recommended
     * (even if not mandated) that Strings be used as keys.
     */
    public String encode(String s) {
        final StringBuffer out = new StringBuffer( s.length() );
        final ByteArrayOutputStream buf = new ByteArrayOutputStream( 32 );
        final OutputStreamWriter writer = new OutputStreamWriter( buf );
        for (int i = 0; i < s.length(); i++)
        {
            int c = s.charAt(i);
            if (charactersDontNeedingEncoding.get(c))
            {
                out.append((char)c);
            }
            else
            {
                try
                {
                    writer.write(c);
                    writer.flush();
                }
                catch(IOException e)
                {
                    buf.reset();
                    continue;
                }
                byte[] ba = buf.toByteArray();
                for (int j = 0; j < ba.length; j++)
                {
                    out.append('%');
                    char ch = Character.forDigit((ba[j] >> 4) & 0xF, 16);
                    // converting to use uppercase letter as part of
                    // the hex value if ch is a letter.
                    if (Character.isLetter(ch))
                    {
                        ch -= characterCaseDiff;
                    }
                    out.append(ch);
                    ch = Character.forDigit(ba[j] & 0xF, 16);
                    if (Character.isLetter(ch))
                    {
                        ch -= characterCaseDiff;
                    }
                    out.append(ch);
                }
                buf.reset();
            }
        }

        return out.toString();
    }
}
