package com.sun.jdori.common;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id: PersistenceManagerFactoryImpl.java,v 1.1 2003/09/28 04:31:11 antonio Exp $
 */

import javax.jdo.PersistenceManagerFactory;
import javax.jdo.PersistenceManager;

public class PersistenceManagerFactoryImpl implements PersistenceManagerFactory {

    public PersistenceManager getPersistenceManager() {
        return null;
    }

}
 
