/*

 ============================================================================
 The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 2004 The Apache Software Foundation. All rights reserved.

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

 */
package org.apache.cocoon.portal.pluto.service.log;

import org.apache.avalon.framework.logger.Logger;
import org.apache.pluto.services.log.LogService;

/**
 * Our own log service logging to an avalon logger
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: LogServiceImpl.java,v 1.1 2004/01/22 14:01:23 cziegeler Exp $
 */
public class LogServiceImpl 
implements LogService {

    /** The logger to use */
    protected Logger logger;
    
    /** Constructor */
    public LogServiceImpl(Logger logger) {
        this.logger = logger;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.log.LogService#debug(java.lang.String, java.lang.String, java.lang.Throwable)
     */
    public void debug(String aComponent, String aMessage, Throwable aThrowable) {
        this.logger.debug(aComponent + " : " + aMessage, aThrowable);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.log.LogService#debug(java.lang.String, java.lang.String)
     */
    public void debug(String aComponent, String aMessage) {
        this.logger.debug(aComponent + " : " + aMessage);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.log.LogService#error(java.lang.String, java.lang.String, java.lang.Throwable)
     */
    public void error(String aComponent, String aMessage, Throwable aThrowable) {
        this.logger.error(aComponent + " : " + aMessage, aThrowable);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.log.LogService#error(java.lang.String, java.lang.Throwable)
     */
    public void error(String aComponent, Throwable aThrowable) {
        this.error(aComponent, aThrowable);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.log.LogService#info(java.lang.String, java.lang.String)
     */
    public void info(String aComponent, String aMessage) {
        this.logger.info(aComponent + " : " + aMessage);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.log.LogService#isDebugEnabled(java.lang.String)
     */
    public boolean isDebugEnabled(String aComponent) {
        return this.logger.isDebugEnabled();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.log.LogService#isErrorEnabled(java.lang.String)
     */
    public boolean isErrorEnabled(String aComponent) {
        return this.logger.isErrorEnabled();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.log.LogService#isInfoEnabled(java.lang.String)
     */
    public boolean isInfoEnabled(String aComponent) {
        return this.logger.isInfoEnabled();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.log.LogService#isWarnEnabled(java.lang.String)
     */
    public boolean isWarnEnabled(String aComponent) {
        return this.logger.isWarnEnabled();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.log.LogService#warn(java.lang.String, java.lang.String)
     */
    public void warn(String aComponent, String aMessage) {
        this.logger.warn(aComponent + " : " + aMessage);
    }

}
