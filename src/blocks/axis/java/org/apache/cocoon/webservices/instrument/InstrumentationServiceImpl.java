/*

============================================================================
                  The Apache Software License, Version 1.1
============================================================================

Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

Redistribution and use in source and binary forms, with or without modifica-
tion, are permitted provided that the following conditions are met:

1. Redistributions of  source code must  retain the above copyright  notice,
   this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

3. The end-user documentation included with the redistribution, if any, must
   include  the following  acknowledgment:  "This product includes  software
   developed  by the  Apache Software Foundation  (http://www.apache.org/)."
   Alternately, this  acknowledgment may  appear in the software itself,  if
   and wherever such third-party acknowledgments normally appear.

4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
   used to  endorse or promote  products derived from  this software without
   prior written permission. For written permission, please contact
   apache@apache.org.

5. Products  derived from this software may not  be called "Apache", nor may
   "Apache" appear  in their name,  without prior written permission  of the
   Apache Software Foundation.

THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
(INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

This software  consists of voluntary contributions made  by many individuals
on  behalf of the Apache Software  Foundation and was  originally created by
Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: InstrumentationServiceImpl.java,v 1.1 2003/03/09 00:02:30 pier Exp $
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
