package org.apache.cocoon.samples.castor;

/**
 *
 * @version CVS $Id: TestBean.java,v 1.2 2003/03/16 18:03:54 vgritsenko Exp $
 */
public class TestBean {
    private String name;
    private String scope;

    public TestBean() {
    }

    public TestBean(String newName, String newScope) {
        name = newName;
        scope = newScope;
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        name = newName;
    }

    public void setScope(String newScope) {
        scope = newScope;
    }

    public String getScope() {
        return scope;
    }
}
