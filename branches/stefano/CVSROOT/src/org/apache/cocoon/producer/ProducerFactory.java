package org.apache.cocoon.producer;

import java.io.*;
import java.util.*;
import javax.servlet.http.*;
import org.apache.cocoon.framework.*;

/**
 * This class implements the production router by identifying a producer
 * associated to the requested XML resource.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:12 $
 */

public class ProducerFactory extends Router {

    private String parameter;
    private Factory factory;
    
    public void init(Configurations conf) {
        super.init(conf);
        
        parameter = (String) conf.get("parameter");
        if (parameter == null) parameter="parameter";
        factory = (Factory) director.getActor("factory");
    }
    
    /**
     * This methods returns the instance of the producer associated with
     * the given request.
     */
    public Producer getProducer(HttpServletRequest request) throws Exception {
        String type = getType(request);
        Producer producer = (Producer) objects.get(type);
        return (producer != null) ? producer : (Producer) factory.create(type);
    }

    /**
     * This method returns the producer type as indicated in the request
     * parameter.
     */
    private String getType(HttpServletRequest request) {
        String param = request.getParameter(parameter);
        return (param == null) ? defaultType : param;
    }
    
    public String getStatus() {
        return "<b>Cocoon Producers</b>" + super.getStatus();
    }
}