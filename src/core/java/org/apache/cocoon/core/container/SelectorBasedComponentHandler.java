/* 
 * Copyright 2002-2004 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.container;

/**
 * @version CVS $Id: SelectorBasedComponentHandler.java 55144 2004-10-20 12:26:09Z ugo $
 */
public class SelectorBasedComponentHandler 
implements ComponentHandler {

    private final Object referenceSemaphore = new Object();
    private int references = 0;

    protected final CocoonServiceSelector selector;
    protected final String                key;
    
    public SelectorBasedComponentHandler(CocoonServiceSelector selector,
                                         String                key) {
        this.selector = selector;
        this.key = key;
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.core.container.ComponentHandler#canBeDisposed()
     */
    public boolean canBeDisposed() {
        return ( this.references == 0 );
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.core.container.ComponentHandler#dispose()
     */
    public void dispose() {
        // nothing to do here
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.core.container.ComponentHandler#get()
     */
    public Object get() throws Exception {
        Object c = this.selector.select(key);
        synchronized( this.referenceSemaphore ) {
            this.references++;
        }
        return c;        
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.core.container.ComponentHandler#initialize()
     */
    public void initialize() throws Exception {
        // nothing to do here
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.core.container.ComponentHandler#put(java.lang.Object)
     */
    public void put(Object component) throws Exception {
        synchronized( this.referenceSemaphore ) {
            this.references--;
        }
        this.selector.release(component);
    }
}
