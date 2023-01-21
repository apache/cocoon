package org.apache.cocoon.portal;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.processing.ProcessInfoProvider;

public class MockProcessInfoProvider implements ProcessInfoProvider {

    protected Map objectModel = new HashMap();

    /**
     * @see org.apache.cocoon.processing.ProcessInfoProvider#getObjectModel()
     */
    public Map getObjectModel() {
        return this.objectModel;
    }

    /**
     * @see org.apache.cocoon.processing.ProcessInfoProvider#getRequest()
     */
    public HttpServletRequest getRequest() {
        return null;
    }

    /**
     * @see org.apache.cocoon.processing.ProcessInfoProvider#getResponse()
     */
    public HttpServletResponse getResponse() {
        return null;
    }

    /**
     * @see org.apache.cocoon.processing.ProcessInfoProvider#getServletContext()
     */
    public ServletContext getServletContext() {
        return null;
    }

}
