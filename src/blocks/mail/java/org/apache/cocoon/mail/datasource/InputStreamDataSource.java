/*
 * Created by IntelliJ IDEA.
 * User: Vadim
 * Date: Oct 27, 2006
 * Time: 12:43:13 PM
 */
package org.apache.cocoon.mail.datasource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.excalibur.source.SourceUtil;

/**
 * The InputStreamDataSource class provides an object, that wraps an
 * {@link InputStream} object in a DataSource interface.
 *
 * @see javax.activation.DataSource
 * @version $Id$
 */
public class InputStreamDataSource extends AbstractDataSource {

    private byte[] data;

    /**
     * Creates a new instance of FilePartDataSource from an
     * {@link InputStream} object.
     *
     * @param in An {@link InputStream} object.
     */
    public InputStreamDataSource(InputStream in) throws IOException {
        this(in, null, null);
    }

    /**
     * Creates a new instance of FilePartDataSource from a byte array.
     */
    public InputStreamDataSource(byte[] data, String type, String name) {
        super(getName(name), getType(type));

        if (data == null) {
            this.data = new byte[0];
        } else {
            this.data = data;
        }
    }

    /**
     * Creates a new instance of FilePartDataSource from an
     * {@link InputStream} object.
     *
     * @param in An {@link InputStream} object.
     */
    public InputStreamDataSource(InputStream in, String type, String name) throws IOException {
        super(getName(name), getType(type));

        // Need to copy contents of InputStream into byte array since getInputStream
        // method is called more than once by JavaMail API.
        if (in == null) {
            data = new byte[0];
        } else {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            SourceUtil.copy(in, out);
            data = out.toByteArray();
        }
    }

    /**
     * Determines the name for this <code>DataSource</code> object.
     * It is first non empty value from the list:
     * <ul>
     * <li>The value of <code>name</code> argument.
     * <li>"attachment".
     * </ul>
     *
     * @return the name for this <code>DataSource</code>
     */
    private static String getName(String name) {
        if (isNullOrEmpty(name)) {
            name = "attachment";
        }

        return name;
    }

    /**
     * Determines the mime type for this <code>DataSource</code> object.
     * It is first non empty value from the list:
     * <ul>
     * <li>The value of <code>type</code> argument.
     * <li>"application/octet-stream".
     * </ul>
     *
     * @return The content type (mime type) of this <code>DataSource</code> object.
     */
    private static String getType(String type) {
        if (isNullOrEmpty(type)) {
            type = "application/octet-stream";
        }

        return type;
    }

    /**
     * The InputStream object passed into contructor.
     *
     * @return The InputStream object for this <code>DataSource</code> object.
     */
    public InputStream getInputStream() {
        return new ByteArrayInputStream(data);
    }
}
