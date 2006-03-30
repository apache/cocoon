package org.apache.cocoon.webapps.session;

import org.w3c.dom.DocumentFragment;


public interface SessionManager {
    String ROLE = SessionManager.class.getClass().getName();
    DocumentFragment getContextFragment (String s1, String s2);
}
