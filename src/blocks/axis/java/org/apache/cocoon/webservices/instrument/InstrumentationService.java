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
package org.apache.cocoon.webservices.instrument;

import org.apache.avalon.framework.component.Component;
import org.apache.excalibur.instrument.InstrumentManageable;

/**
 * Component interface for retrieving sample information from the 
 * InstrumentManager.
 *
 * @author <a href="mailto:crafterm@apache.org">Marcus Crafter</a>
 * @version CVS $Id: InstrumentationService.java,v 1.3 2004/03/05 13:01:43 bdelacretaz Exp $
 */
public interface InstrumentationService extends InstrumentManageable, Component {
	
    /**
     * Component ROLE name
     */
    String ROLE = InstrumentationService.class.getName();

    /**
     * Obtain an array of samples from a specified sample name.
     *
     * <p>
     *  The specified path parameter identifies a sample, hierarchically from
     *  Instrumentable name to Instrument name, to Instrument sample name
     *  (including any child Instrumentables) using the '.' character as a
     *  separator.
     * </p>
     *
     * <pre>
     *  eg: instrument-manager.active-thread-count.maximum_1000_600
     * </pre>
     *
     * <p>
     *  The above example identifies the sample 'maximum_1000_600' on instrument
     *  'active-thread-count', on instrumentable 'instrument-manager'.
     * </p>
     *
     * <p>
     *  The length of the returned array is dependant on the configuration of the
     *  sample being accessed. Check instrumentation.xconf for the length of pre-
     *  defined samples that operate constantly, when instrumentation is enabled.
     * </p>
     *
     * @param path path value
     * @return an <code>int[]</code> array of samples
     * @exception Exception if an error occurs
     */
    int[] getSampleSnapshot(String path)
        throws Exception;

    /**
     * Obtains an array of instrumentable sample names
     *
     * @return a {@link String}[] array of sample names
     */
    String[] getSampleNames();
}
