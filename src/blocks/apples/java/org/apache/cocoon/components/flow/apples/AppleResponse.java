/*
 * File AppleResponse.java 
 * created by mpo
 * on Jul 21, 2003 | 10:52:10 AM
 * 
 * (c) 2003 - Outerthought BVBA
 */
package org.apache.cocoon.components.flow.apples;



/**
 * AppleResponse defines the parts of the 'response' an AppleController can set.
 */
public interface AppleResponse {
    

    /**
     * Sets the uri of the selected cocoon pipeline for publication of the result.
     * @param uri the uri that selects an (internal) publication pipe. 
     * @see ApplesProcessor#forwardTo
     */
    public void setURI(String uri);
    
    /**
     * Sets the 'bizdata' object to be sent as the flow's 'context-object' through 
     * the selected publication pipe.
     * @param data the 'bizdata' object
     */
    public void setData(Object data);

}
