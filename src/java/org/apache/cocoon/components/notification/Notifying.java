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
package org.apache.cocoon.components.notification;

import java.util.Map;

/**
 *  Interface for Objects that can notify something.
 *
 * @author <a href="mailto:barozzi@nicolaken.com">Nicola Ken Barozzi</a>
 * @version CVS $Id: Notifying.java,v 1.2 2004/03/08 13:58:31 cziegeler Exp $
 */

public interface Notifying {

    /*
     * Proposed types of notifications
     */
    String UNKNOWN_NOTIFICATION = "unknown";
    String DEBUG_NOTIFICATION   = "debug";
    String INFO_NOTIFICATION    = "info" ;
    String WARN_NOTIFICATION    = "warn" ;
    String ERROR_NOTIFICATION   = "error";
    String FATAL_NOTIFICATION   = "fatal";

    /*
     * Proposed extra descriptions
     */
    String EXTRA_LOCATION   = "location";
    String EXTRA_LINE       = "line";
    String EXTRA_COLUMN     = "column" ;
    String EXTRA_REQUESTURI = "request-uri" ;
    String EXTRA_CAUSE      = "cause";
    String EXTRA_STACKTRACE = "stacktrace";
    String EXTRA_FULLTRACE  = "full exception chain stacktrace";

    /**
     * Gets the Type of the Notifying object
     */
    String getType();

    /**
     * Gets the Title of the Notifying object
     */
    String getTitle();

    /**
     * Gets the Source of the Notifying object
     */
    String getSource();

    /**
     * Gets the Sender of the Notifying object
     */
    String getSender();

    /**
     * Gets the Message of the Notifying object
     */
    String getMessage();

    /**
     * Gets the Description of the Notifying object
     */
    String getDescription();

    /**
     * Gets the ExtraDescriptions of the Notifying object
     */
    Map getExtraDescriptions();
}
