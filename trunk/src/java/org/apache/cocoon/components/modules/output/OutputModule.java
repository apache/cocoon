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

package org.apache.cocoon.components.modules.output;

import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;

/**
 * Communicate results to other components. This could be done via
 * request attributes, session attribute etc. Implementors should obey
 * the transactional nature and e.g. queue values as request
 * attributes and do the real communication e.g. to a bean only when
 * the transaction completes successfully.
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: OutputModule.java,v 1.2 2004/02/06 23:34:32 joerg Exp $
 */
public interface OutputModule {

    String ROLE = OutputModule.class.getName();

    /**
     * communicate an attribute value to further processing logic. OutputModules
     * work in implicit transaction mode, thus setting an attribute starts an
     * transaction and sttributes are only visible after the transaction is
     * successfully completed with a call to commit
     * @param modeConf column's mode configuration from resource
     * description. This argument is optional.
     * @param objectModel The objectModel
     * @param name The attribute's label, consisting of "table.column"
     * or "table.column[index]" in case of multiple attributes of the
     * same spec.
     * @param value The attriute's value.
     * */
    void setAttribute( Configuration modeConf, Map objectModel, String name, Object value );


    /**
     * If a database transaction needs to rollback, this is called to
     * inform the further processing logic about this fact. All
     * already set attribute values are invalidated. <em>This is difficult
     * because only the request object can be used to synchronize this
     * and build some kind of transaction object. Beaware that sending
     * your data straight to some beans or other entities could result
     * in data corruption!</em>
     * */
    void rollback( Configuration modeConf, Map objectModel, Exception e );


    /**
     * Signal that the database transaction completed
     * successfully. See notes on {@link #rollback rollback}.
     * */
    void commit( Configuration modeConf, Map objectModel );


}
