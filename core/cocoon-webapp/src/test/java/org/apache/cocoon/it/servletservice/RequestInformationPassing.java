package org.apache.cocoon.it.servletservice;

import junit.framework.Assert;

import org.apache.cocoon.tools.it.HtmlUnitTestCase;

public class RequestInformationPassing extends HtmlUnitTestCase {

    public void testAttributes() throws Exception {
        this.loadResponse("test1/test1");
        Assert.assertTrue(this.response.getStatusCode() == 200);
    }

}
