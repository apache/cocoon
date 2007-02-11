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
package org.apache.cocoon.precept.stores.bean.test;

/**
 * @author Torsten Curdt <tcurdt@dff.st>
 * @since Mar 20, 2002
 * @version CVS $Id: SystemBean.java,v 1.3 2004/03/05 13:02:20 bdelacretaz Exp $
 */
public class SystemBean {

    private String os;
    private String processor;
    private int ram;
    private String servlet_engine;
    private String java_version;

    public SystemBean() {
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getProcessor() {
        return processor;
    }

    public void setProcessor(String processor) {
        this.processor = processor;
    }

    public int getRam() {
        return ram;
    }

    public void setRam(int ram) {
        this.ram = ram;
    }

    public String getServlet_engine() {
        return servlet_engine;
    }

    public void setServlet_engine(String servlet_engine) {
        this.servlet_engine = servlet_engine;
    }

    public String getJava_version() {
        return java_version;
    }

    public void setJava_version(String java_version) {
        this.java_version = java_version;
    }
}
