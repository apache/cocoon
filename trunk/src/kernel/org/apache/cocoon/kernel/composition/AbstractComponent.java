/* ========================================================================== *
 * Copyright (C) 1996-2004 VNU Business Publications LTD. All rights reserved *
 * ========================================================================== */
package org.apache.cocoon.kernel.composition;

import org.apache.cocoon.kernel.resolution.Resolver;


/**
 * <p></p> 
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.1 $)
 */
public class AbstractComponent implements Component {
 
    /** <p>The {@link Wire} instance contextualized in this instance.</p> */
    protected Wire wire = null;

    /** <p>The {@link Resolver} instance contextualized in this instance.</p> */
    protected Resolver resolver = null;

    /**
     * <p>Create a new {@link AbstractComposer} instance.</p>
     */
    public AbstractComponent() {
        super();
    }

    /**
     * <p>Contextualize this {@link Component} instance.</p>
     *
     * <p>The contextualized {@link Wire} and {@link Resolver} instances will
     * be available in the {@link #wire} and {@link #resolver} fields
     * respectively.</p>
     *
     * @param wire the {@link Wire} instance through which the block requesting
     *             this instance is accessing it.
     * @param resolver the {@link Resolver} instance resolving resources in the
     *                 calling block instance.
     */
    public void contextualize(Wire wire, Resolver resolver) {
        this.resolver = resolver;
        this.wire = wire;
    }
}
