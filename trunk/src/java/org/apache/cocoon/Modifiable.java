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
package org.apache.cocoon;

/**
 * This interface is implemented by those classes that change
 * their behavior/results over time (non-ergodic).
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
 * @version CVS $Id: Modifiable.java,v 1.2 2004/03/08 13:57:35 cziegeler Exp $
 */
public interface Modifiable
{
    /**
     * Queries the class to estimate its ergodic period termination.
     * <br>
     * This method is called to ensure the validity of a cached product. It
     * is the class responsibility to provide the fastest possible
     * implementation of this method or, whether this is not possible and the
     * costs of the change evaluation is comparable to the production costs,
     * to return <b>true</b> directly with no further delay, thus reducing
     * the evaluation overhead to a minimum.
     *
     * @return <b>true</b> if the class ergodic period is over and the class
     *         would behave differently if processed again, <b>false</b> if the
     *         resource is still ergodic so that it doesn't require
     *         reprocessing.
     */
    boolean modifiedSince( long date );
}
