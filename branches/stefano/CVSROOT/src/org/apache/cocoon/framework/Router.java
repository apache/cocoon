package org.apache.cocoon.framework;

import java.io.*;
import java.util.*;

/**
 * This class implements a basic Router implementation that is used
 * by those classes that must assign the execution of a particular
 * pluggable instance depending on some "type reaction".
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:18 $
 */

public abstract class Router extends AbstractActor implements Configurable, Status {

    protected Hashtable objects;
    protected String defaultType;

	public void init(Configurations conf) {
        Factory factory = (Factory) director.getActor("factory");
        this.defaultType = (String) conf.get("default");
        this.objects = new Hashtable();
        
        Configurations types = conf.getConfigurations("type");
        Enumeration e = types.keys();
        while (e.hasMoreElements()) {
            String type = (String) e.nextElement();
            String name = (String) types.get(type);
            objects.put(type, factory.create(name, conf));
        }
	}
    
	public String getStatus() {
        StringBuffer buffer = new StringBuffer();
        if (defaultType != null) buffer.append("<li><b>Default type</b> = " + defaultType);
        Enumeration e = objects.keys();
        while (e.hasMoreElements()) {
            String type = (String) e.nextElement();
	        Object o = objects.get(type);
            buffer.append("<li><b>" + type + "</b>");
            if (o instanceof Status) {
                buffer.append(": " + ((Status) o).getStatus());
            }
            buffer.append("</li>");
        }
        return buffer.toString();
	}
}