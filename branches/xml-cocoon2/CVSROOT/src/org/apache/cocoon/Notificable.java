/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon;

import java.util.HashMap;

/**
 *  Interface for Objects that can notify something.
 *
 * @author <a href="mailto:nicolaken@supereva.it">Nicola Ken Barozzi</a> Aisa
 * @created 24 August 2000
 */

public interface Notificable {

    /**
     *  Gets the Type attribute of the Notificable object
     */
    public String getType();

    /**
     *  Gets the Title attribute of the Notificable object
     */
    public String getTitle();

    /**
     *  Gets the Source attribute of the Notificable object
     */
    public String getSource();

    /**
     *  Gets the Sender attribute of the Notificable object
     */
    public String getSender();

    /**
     *  Gets the Message attribute of the Notificable object
     */
    public String getMessage();

    /**
     *  Gets the Description attribute of the Notificable object
     */
    public String getDescription();

    /**
     *  Gets the ExtraDescriptions attribute of the Notificable object
     */
    public HashMap getExtraDescriptions();
}
