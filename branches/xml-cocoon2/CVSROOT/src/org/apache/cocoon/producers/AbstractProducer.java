/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
 
package org.apache.cocoon.producer;

import org.apache.arch.*;
import org.apache.cocoon.*;

/**
 * This abstract class implements the Producer interface and provides
 * utitity methods to convert the generated stream into SAX events
 * that are used inside the processor pipeline. This class must be
 * seen as a transparent "mediator" between stream and SAX realms.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 1999-12-11 23:28:52 $
 */

public abstract class AbstractProducer implements Producer, Composer {
    
    private Parser parser;

    /**
     * Sets the component manager which is responsible to provide this
     * class with all the components it needs.
     */    
    public void setComponentManager(ComponentManager manager) {
    	this.parser = (Parser) manager.getComponent("parser");
    }

    /**
     * This method is always the one called by the Cocoon architecture, it is
     * this class responsibility to provide a way to parse and SAX-enable
     * the toWriter() method. This method should be overwritten by SAX-aware
     * producers to eliminate the overhead of stream parsing.
     */
    public void produce(EventListener listener) throws ProducerException {

        /* 
         * FIXME (SM) This code sucks. It's like having a cable with both
         * male/female ends. It would be awesome to have a SAX parser that
         * works as a Writer, so itself event driven, instead of reading
         * from a file. This shouldn't be so hard to implement. True, this 
         * method shouldn't be used at all, but if some legacy data generates
         * tons of stream, the Pipe class (which stores everything in memory to
         * provide both reading and writing capabilities) might well fail.
         *
         * The solution is to provide an event based SAX parser, which is
         * impossible to create given current SAX API. This is the ultimate
         * reason to drop JSP/Servlets alltogether for XML production.
         */

    	Pipe pipe = new Pipe();
    	this.parser.setEventListener(listener);
    	this.produce(new PrintWriter(pipe.getWriter()), request);
    	this.parser.parse(pipe.getReader());
    }
    
    /**
     * This method should be overwritten by SAX-unaware producer instances.
     */
    public void produce(PrintWriter writer) throws ProducerException {
        throw new RuntimeException(
          "Method Producer.produce(PrintWriter) not implemented."
        );
    }
    
    /**
     * This method always returns true to reduce the evaluation overhead to
     * a minimum. Producer are highly encouradged to overwrite this method
     * if they can provide a fast way to evaluate the response change.
     */
    public boolean hasChanged(Object request) {
        return true;
    }
}