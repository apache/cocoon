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

import java.util.ArrayList;
import java.util.List;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.excalibur.instrument.InstrumentManager;
import org.apache.excalibur.instrument.manager.DefaultInstrumentManager;
import org.apache.excalibur.instrument.manager.interfaces.InstrumentableDescriptor;
import org.apache.excalibur.instrument.manager.interfaces.InstrumentDescriptor;
import org.apache.excalibur.instrument.manager.interfaces.InstrumentSampleDescriptor;

/**
 * Implementation of {@link InstrumentationService} component. This component
 * allows you to access sample information from the InstrumentManager.
 *
 * @author <a href="mailto:crafterm@apache.org">Marcus Crafter</a>
 * @version CVS $Id: InstrumentationServiceImpl.java,v 1.2 2004/03/05 13:01:43 bdelacretaz Exp $
 */
public final class InstrumentationServiceImpl extends AbstractLogEnabled
    implements InstrumentationService {

    private static final int[] EMPTY_INT_ARRAY = {};
    private static final String[] EMPTY_STRING_ARRAY = {};

    // instrument manager reference
	private DefaultInstrumentManager m_iManager;

	/**
     * Sets the {@link InstrumentManager} for this service object.
     *
     * @param iManager an {@link InstrumentManager} instance
     */
    public void setInstrumentManager(final InstrumentManager iManager) {

        if (iManager == null) {
            if (getLogger().isWarnEnabled())
                getLogger().warn(
                    "No instrument manager available," +
                    "please enable instrumentation in your web.xml"
                );
        }

        // we require a DefaultInstrumentManager, attempt a cast.
        if (iManager instanceof DefaultInstrumentManager) {
            m_iManager = (DefaultInstrumentManager) iManager;
        } else {
            throw new UnsupportedOperationException(
                "InstrumentationService only supports DefaultInstrumentManager"
            );
        }
	}

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
    public int[] getSampleSnapshot(final String path)
        throws Exception {

        // ensure we have an instrument manager available
        if (!haveInstrumentManager()) {
            getLogger().warn(
               "No instrument manager available," +
               "please enable instrumentation in your web.xml"
            );
            return EMPTY_INT_ARRAY;
        }

        // return the samples
        return m_iManager.locateInstrumentSampleDescriptor(path)
            .getSnapshot().getSamples();
    }

    /**
     * Obtain a list of available samples, useful for browsing
     * available samples.
     *
     * @return an {@link String}[] array of sample names
     */
    public String[] getSampleNames() {

        // ensure we have an instrument manager available
        if (!haveInstrumentManager()) {
            getLogger().warn(
                "No instrument manager available," +
                "please enable instrumentation in your web.xml"
            );
            return EMPTY_STRING_ARRAY;
        }

        // list all instrumentables
        final InstrumentableDescriptor[] descriptors =
            m_iManager.getInstrumentableDescriptors();
        final List names = new ArrayList();

        for (int i = 0; i < descriptors.length; ++i) {

            // list all instruments 
            InstrumentDescriptor[] insts =
                descriptors[i].getInstrumentDescriptors();

            for (int k = 0; k < insts.length; ++k) {

                // list all samples
                InstrumentSampleDescriptor[] samples =
                    insts[k].getInstrumentSampleDescriptors();

                for (int j = 0; j < samples.length; ++j) {
                    names.add(samples[j].getName());
                }
            }
        }

        return (String[])names.toArray(new String[]{});
    }

    /**
     * Helper method to determine if a valid instrument manager is available
     *
     * @return true if an instrument manager is present, false otherwise
     */
    private boolean haveInstrumentManager() {
        return (m_iManager != null);
    }
}
