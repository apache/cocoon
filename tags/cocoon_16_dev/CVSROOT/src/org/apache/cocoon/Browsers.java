package org.apache.cocoon;

import java.util.*;
import org.apache.cocoon.framework.*;

/**
 * This inner class is used to store the mapping between browser names
 * and those signature fragments used for mapping.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:11 $
 */

public class Browsers extends Vector implements Configurable, Status {

    /**
     * Inner class that contains single browser information.
     */
    public class Browser {

        public String name;
        public String signature;

        public Browser(String name, String signature) {
            this.name = name;
            this.signature = signature;
        }

        public String toString() {
            return "( " + name + ", " + signature + " )";
        }
    }

    /**
     * Initialize the map with the mapping rules.
     */
    public void init(Configurations conf) throws InitializationException {
        for (int i = 0; ; i++) {
            String filter = (String) conf.get(Integer.toString(i));
            if (filter == null) break;
            int index = filter.indexOf('=');
            this.addElement(new Browser(filter.substring(0, index), filter.substring(index + 1)));
        }
    }

    /**
     * This method maps the "user-Agent" request parameter to a browser name
     * following the search rules defined in the servlet properties.
     */
    public String map(String agent) {
        try {
            for (int i = 0;; i++) {
                Browser b = (Browser) this.elementAt(i);
                int index = agent.indexOf(b.signature);
                if (index > -1) return b.name;
            }
        } catch (Exception e) {
            return "unknown";
        }
    }

    public String getStatus() {
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < this.size(); i++) {
            Browser b = (Browser) this.elementAt(i);
            buffer.append("<li>");
            buffer.append(b.toString());
            buffer.append("</li>");
        }

        return buffer.toString();
    }
}