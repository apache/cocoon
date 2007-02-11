/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.cocoon.components.web3.impl;

import java.util.Properties;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.InputStream;
import java.io.IOException;

/**
 * Properties helper class.
 *
 * @author <a href="mailto:michael.gerzabek@at.efp.cc">Michael Gerzabek</a>
 * @since 2.1
 * @version CVS $Id: Web3Properties.java,v 1.5 2004/03/05 13:02:25 bdelacretaz Exp $
 */
public class Web3Properties extends Properties {

    ArrayList orderedKeys = new ArrayList();
    
    /** Creates new Properties */
    public Web3Properties() {
        super();
    }

    public Web3Properties(Properties defaults) {
	super(defaults);
    }

    public synchronized Iterator getKeysIterator() {
        return orderedKeys.iterator();
    }

    public static Web3Properties load(String name) throws Exception {
	Web3Properties props = null;
	InputStream is = Web3Properties.class.getResourceAsStream(name);
	props = new Web3Properties();
	if (null != is) {
	  props.load(is);
	  return props;
	} 
        else {
          throw new IOException("Properties could not be loaded."); 
        }
    }

    public synchronized Object put(Object key, Object value) {
	Object obj = super.put(key, value);
	orderedKeys.add(key);
	return obj;
    }

    public synchronized Object remove(Object key) {
	Object obj = super.remove(key);
	orderedKeys.remove(key);
	return obj;
    }
}
