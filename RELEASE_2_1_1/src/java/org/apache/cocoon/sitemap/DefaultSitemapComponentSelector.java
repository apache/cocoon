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
package org.apache.cocoon.sitemap;

import org.apache.avalon.excalibur.component.ExcaliburComponentSelector;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Default component manager for Cocoon's sitemap components.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @version CVS $Id: DefaultSitemapComponentSelector.java,v 1.2 2003/08/12 15:48:02 sylvain Exp $
 */
public class DefaultSitemapComponentSelector extends ExcaliburComponentSelector
  implements SitemapComponentSelector {

    private Map hintLabels;
    private Map pipelineHints;
    private Map mime_types;
    private SitemapComponentSelector parentSelector;

    /** Dynamic component handlers mapping. */
    private Map componentMapping;

    /** The constructors (same as the Avalon ComponentManager)
     */
    public DefaultSitemapComponentSelector() {
        super();
        this.hintLabels = new HashMap();
        this.mime_types = new HashMap();
        this.pipelineHints = new HashMap();
        componentMapping = Collections.synchronizedMap(new HashMap());
    }

    public void setParentSelector(SitemapComponentSelector newSelector) {
        if (this.parentSelector == null) {
            this.parentSelector = newSelector;
        }
    }

    /**
     * Get the parent selector.
     * This is mainly used for releasing the parent selector
     */
    public SitemapComponentSelector getParentSelector() {
        return this.parentSelector;
    }

    public Component select(Object hint) throws ComponentException {
        Component component = null;

        try {
            component = super.select(hint);
        } catch (ComponentException ce) {
            if (this.parentSelector != null) {
                component = this.parentSelector.select(hint);
                componentMapping.put(component, this.parentSelector);
            } else {
                throw ce;
            }
        }

        return component;
    }

    public void release(Component component) {
        SitemapComponentSelector selector = (SitemapComponentSelector)componentMapping.get(component);
        if(selector != null) {
            componentMapping.remove(component);
            selector.release(component);
        } else {
            super.release(component);
        }
    }

    public boolean hasComponent(Object hint) {
        boolean exists = super.hasComponent( hint );
        if ( !exists && this.parentSelector != null ) {
            exists = this.parentSelector.hasComponent( hint );
        }
        return exists;
    }

    public void initialize() {
        super.initialize();
        this.mime_types = Collections.unmodifiableMap(this.mime_types);
    }

    public String getMimeTypeForHint(Object hint) {
        String mimeType = (String)this.mime_types.get(hint);
        if (mimeType != null) {
            return mimeType;
        }
        if (this.parentSelector != null) {
            return this.parentSelector.getMimeTypeForHint(hint);
        }
        return null;
    }

    public boolean hasLabel(Object hint, String label) {
        String[] labels = this.getLabels(hint);
        if (labels != null) {
            for (int i = 0; i < labels.length; i++) {
                if (labels[i].equals(label))
                    return true;
            }
        }
        return false;
    }

    public String[] getLabels(Object hint) {
        // If this hint is declared locally, use its labels (if any), otherwise inherit
        // those of the parent.
        if (super.hasComponent(hint)) {
            return (String[])this.hintLabels.get(hint);
        } else {
            return parentSelector.getLabels(hint);
        }   
    }

    public String getPipelineHint(Object hint) {
        // If this hint is declared locally, use its hints (if any), otherwise inherit
        // those of the parent.
        if (super.hasComponent(hint)) {
            return (String)this.pipelineHints.get(hint);
        } else {
            return parentSelector.getPipelineHint(hint);
        }   
    }

    public void addComponent(Object hint, Class component, Configuration conf)
            throws ComponentException {

        String mimeType = conf.getAttribute("mime-type", null);
        if (mimeType != null)
            this.mime_types.put(hint, mimeType);

        String label = conf.getAttribute("label", null);
        if (label != null) {
            // Empty '' attribute will result in empty array,
            // overriding all labels on the component declared in the parent.
            StringTokenizer st = new StringTokenizer(label, " ,", false);
            String[] labels = new String[st.countTokens()];
            for (int i = 0; i < labels.length; i++) {
                labels[i] = st.nextToken();
            }
            this.hintLabels.put(hint, labels);
        }

        String pipelineHint = conf.getAttribute("hint", null);
        this.pipelineHints.put(hint, pipelineHint);

        super.addComponent(hint, component, conf);
    }

    public void addSitemapComponent(Object hint, Class component,
                                    Configuration conf, String mimeType)
            throws ComponentException, ConfigurationException {

        this.addComponent(hint, component, conf);
        this.mime_types.put(hint, mimeType);
    }
}
