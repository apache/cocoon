package org.apache.cocoon.framework;

import java.util.*;
import java.io.*;

/**
 * This class encapsulates all the configurations needed by a Configurable
 * class to work.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:18 $
 */

public class Configurations extends Properties {

    private String baseName;
    
    public Configurations() {
        super();
    }
    
    /**
     * Create the class from a the file
     */
    public Configurations(String file) throws Exception {
        this(file, null);
    }
    
    /**
     * Create the class with given defaults and from the file
     */
    public Configurations(String file, Configurations defaults) throws Exception {
        this(defaults);
        InputStream input = new FileInputStream(file);
        load(input);
        input.close();
    }

    /**
     * Create the class with given defaults.
     */
    public Configurations(Configurations c) {
        super(c);
    }

    /**
     * Set the configuration.
     */
    public void set(String key, Object value) {
        super.put(key, value);
    }
    
    /**
     * Get the configuration.
     */
    public Object get(String key) {
        return super.get(key);
    }

    /**
     * Get the configuration and use the given default value if not found.
     */
    public Object get(String key, Object def) {
        Object o = super.get(key);
        return (o == null) ? def : o;
    }

    /**
     * Get the configuration, throw an exception if not present.
     */
    public Object getNotNull(String key) {
        Object o = super.get(key);
        if (o == null) {
            throw new RuntimeException("Cocoon configuration item '" + ((baseName == null) ? "" : baseName + "." + key) + "' is not set");
        } else {
            return o;
        }
    }
    
    /**
     * Get a vector of configurations when the syntax is incremental
     */
    public Vector getVector(String key) {
        Vector v = new Vector();
        
        for (int i = 0; ; i++) {
            Object n = get(key + "." + i);
            if (n == null) break;
            v.addElement(n);
        }
        
        return v;
    }

    /**
     * Create a subconfiguration starting from the base node.
     */
    public Configurations getConfigurations(String base) {
        Configurations c = new Configurations();
        c.setBasename((baseName == null) ? base : baseName + "." + base);
    	String prefix = base + ".";
    	
        Enumeration keys = this.propertyNames();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            
			if (key.startsWith(prefix)) {
				c.set(key.substring(prefix.length()), this.get(key));
			} else if (key.equals(base)) {
				c.set("", this.get(key));
			}
        }
        
    	return c;
    }
    
    public void setBasename(String baseName) {
        this.baseName = baseName;
    }
}