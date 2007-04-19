package org.apache.cocoon.servletservice.postable;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.excalibur.source.Source;

public interface PostableSource extends Source {
	/**
     * Return an {@link OutputStream} to post to.
     *
     * The returned stream must be closed by the calling code.
     */
    OutputStream getOutputStream() throws IOException;
}
