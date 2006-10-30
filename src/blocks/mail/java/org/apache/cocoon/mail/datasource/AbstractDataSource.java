/*
 * Created by IntelliJ IDEA.
 * User: Vadim
 * Date: Oct 27, 2006
 * Time: 12:29:43 PM
 */
package org.apache.cocoon.mail.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;

import org.apache.avalon.framework.logger.AbstractLogEnabled;

/**
 * The AbstractDataSource class is a base class for other {@link DataSource}
 * implementation.
 *
 * @version $Id$
 */
public abstract class AbstractDataSource extends AbstractLogEnabled
                                         implements DataSource {

    private String name;
    private String contentType;


    public AbstractDataSource() {
    }

    /**
     * @param name Name of the content part
     * @param type Mime type of the content
     */
    public AbstractDataSource(String name, String type) {
        this.name = name;
        this.contentType = type;
        if (isNullOrEmpty(this.name)) {
            this.name = null;
        }
        if (isNullOrEmpty(this.contentType)) {
            this.contentType = null;
        }
    }

    /**
     * Check String for null or empty.
     * @param str
     * @return true if str is null, empty string, or equals "null"
     */
    protected static boolean isNullOrEmpty(String str) {
        return str == null || "".equals(str) || "null".equals(str);
    }

    /**
     * Returns the name for this <code>DataSource</code> object.
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The content type (mime type) of this <code>DataSource</code> object.
     */
    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Get the <code>InputStream</code> for this DataSource.
     */
    public abstract InputStream getInputStream() throws IOException;

    /**
     * Not implemented. Throws <code>IOException</code>.
     * @throws java.io.IOException since unimplemented
     */
    public OutputStream getOutputStream() throws IOException {
        throw new IOException("no data sink available");
    }
}
