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

import org.apache.cocoon.kernel.configuration.Configurable;

/**
 * <p>The {@link Composer} interface is the core interface outlining the
 * contract between component instances and the framework itself.</p>
 *
 * <p>It is left to solid implementations of this interface to choose their
 * optimal way to supply object instances: objects can be for example
 * instantiated every time or pooled.</p>
 *
 * <p>The only contract this framework defines with a {@link Composer} is that
 * every object acquired from it <b>will</b> be returned either with a call to
 * {@link #release(Object)} if the framework did not detect any problem with
 * the instance, or with a call to {@link #dispose(Object)} if the framework
 * encountered any problem with it.</p>
 *
 * <p>In a &quot;block-aware&quot; environment, {@link Composer}s will be
 * instantiated, configured, and contextualized in the context of the block
 * they appear in (and therefore benefit from the block's required wirings and
 * parameters), while the {@link Object} instances they create will be
 * contextualized in the context of the <b>caller</b> block.</p>
 *
 * <p>This is why instances of {@link Wirings} are available only
 * to {@link Composer}s and <b>never</b> to the objects they create, while,
 * on the other hand, components can have access to the caller block resources
 * by implementing the {@link Component} interface.</p>
 *
 * <p>Therefore, if an object created by a {@link Composer} requires components
 * or parameters available to the block they reside in, the {@link Composer}
 * itself <b>must provide those</b> before returning their instance in the
 * {@link #acquire()} method.</p>
 *
 * <p>Every {@link Composer} implementation <b>must</b> provide a public void
 * (requiring no parameters) constructor.</p>
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.5 $)
 */
public interface Composer extends Configurable {

    /**
     * <p>Acquire an instance of an {@link Object} created (or recycled) by
     * this {@link Composer}.</p>
     *
     * @return a <b>non null</b> {@link Object} instance.
     * @throws Throwable if the {@link Object} can not be (for example)
     *                   instantiated or returned for whatever reason.
     */
    public Object acquire()
    throws Throwable;
    
    /**
     * <p>Normally release an {@link Object} instance previously acquired from
     * this {@link Composer}.</p>
     *
     * <p>Instances released by the framework using this method <b>may</b> be
     * recycled and re-acquired in the future if the implementation of this
     * factory is able and/or configured to do so.</b>
     *
     * @param object a <b>non null</b> {@link Object} instance as previously
     *               acquired from this {@link Composer} to release.
     */
    public void release(Object object);
    
    /**
     * <p>Abnormally dispose of an {@link Object} instance previously acquired
     * from this {@link Composer}.</p>
     *
     * <p>This method will be called when the instance previously acquired
     * failed in any of the non-lifecycle or lifecycle methods provided by
     * this API, or in other cases when disposition (and therefore garbage
     * collection of components) should be preferred to their simple release
     * (for example, not enough memory available in the Java&trade; Virtual
     * Machine).</p>
     *
     * <p>In other words, disposed object instances <b>should never</b> be
     * returned by following calls to the {@link #acquire()} method.</b>
     *
     * @param object a <b>non null</b> {@link Object} instance as previously
     *               acquired from this {@link Composer} to dispose.
     */
    public void dispose(Object object);

    /**
     * <p>Contextualize this {@link Composer} with the {@link Wirings}
     * associated to the block where it resides.</p>
     *
     * @param wirings the {@link Wirings} instance associated with
     *                this {@link Composer}'s block.
     * @throws Exception if there was an error performing operations on the
     *                   supplied {@link Wirings} instance.
     */
    public void contextualize(Wirings wirings)
    throws Exception;

}
