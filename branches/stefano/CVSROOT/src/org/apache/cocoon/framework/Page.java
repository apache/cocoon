package org.apache.cocoon.framework;

import java.util.*;
import org.w3c.dom.*;

/**
 * The Page wrapper class.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:18 $
 */

public class Page implements java.io.Serializable {
	
	private String content = "text/xml";
	private String contentType;
    private boolean cached = false;
    private Vector changeables = new Vector(3);

	public String getContent() {
		return this.content;
	}
    
    public void setContent(String content) {
        this.content = content;
    }
	
	public String getContentType() {
		return this.contentType;
	}

    public void setContentType(String type) {
        if (type != null) this.contentType = type;
    }
    
    public boolean isText() {
    	return this.contentType.startsWith("text");
    }
    
    public Enumeration getChangeables() {
        return this.changeables.elements();
    }
    
    public void setChangeable(Changeable change) {
        this.changeables.addElement(change);
    }
    
    public boolean isCached() {
        return cached;
    }
    
    public void setCached(boolean cached) {
        this.cached = cached;
    }
}