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
package org.apache.butterfly.components.pipeline;


/**
 * This exception can be thrown when executing a pipeline.
 * 
 * @version CVS $Id: PipelineProcessingException.java,v 1.1 2004/07/25 21:55:20 ugo Exp $
 */
public class PipelineProcessingException extends PipelineException {

    /**
     * 
     */
    public PipelineProcessingException() {
        super();
    }

    /**
     * @param arg0
     */
    public PipelineProcessingException(String arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     */
    public PipelineProcessingException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    /**
     * @param arg0
     */
    public PipelineProcessingException(Throwable arg0) {
        super(arg0);
    }

}
