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
package org.apache.cocoon.kernel.composition;

/**
 * <p>The {@link Lifecycle} interface defines a possible minimal life cycle
 * for a {@link Composer}.</p>
 *
 * <p>Note that not all {@link Composer}s <b>have</b> have implement this
 * interface by definition. This interface should be implemented <b>only</b>
 * by those {@link Composer}s requiring a minimal lifecycle management
 * (notification of initialization and destruction) as provided by this
 * framework.</p>
 *
 * <p>Note that this interface will be only considered for {@link Composer}s
 * and some objects intstances internal to the framework. If this interface
 * is implemented by a normal component, it will be silently ignored.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.5 $)
 */
public interface Lifecycle {

    /**
     * <p>Notify this instance of its initialization.</p>
     *
     * <p>This method is called <b>once and only once</b> by the framework
     * after this instance has been created or acquired from its associated
     * {@link Composer}</p>
     *
     * <p>Instances of this interface is guaranteed that all non lifecycle
     * aware methods (such as {@link Composer#contextualize(Wirings)})
     * will be called <b>before</b> this method is called.</p>
     *
     * <p>Any lifecycle-aware initialization or startup method specified by
     * extensions of this API <b>must</b> be invoked <b>after</b> this method
     * has been called, as this method defines the beginning of lifecycle
     * awareness.</p>
     *
     * @throws Exception if this instance cannot be initialized.
     */
    public void init()
    throws Exception;

    /**
     * <p>Notify this instance of its destruction.</p>
     *
     * <p>This method is called <b>once and only once</b> by the framework
     * before this instance is garbage collected or releaased to its associated
     * {@link Composer}.</p>
     *
     * <p>As this framework does not allow any post-lifecycle methods, instances
     * of this interface are guaranteed that this method will be the last one
     * called before garbage collection, release or disposal.</p>
     *
     * <p>If this method throws an {@link Exception} and this instance was
     * acquired from a {@link Composer}, its instance <b>will not</b> be
     * {@link Composer#release(Object) released} to it (which would be its
     * normal behavior), but rather {@link Composer#dispose(Object) disposed}
     * back through it.</p>
     *
     * @throws Exception if this instance cannot be destroyed.
     */
    public void destroy()
    throws Exception;
}
