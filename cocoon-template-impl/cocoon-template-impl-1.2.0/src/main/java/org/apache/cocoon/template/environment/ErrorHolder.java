/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.template.environment;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * @version SVN $Id$
 */
public class ErrorHolder extends Exception {

    private Error err;

    public ErrorHolder(Error err) {
        super(err.getMessage());
        this.err = err;
    }

    public void printStackTrace(PrintStream ps) {
        err.printStackTrace(ps);
    }

    public void printStackTrace(PrintWriter pw) {
        err.printStackTrace(pw);
    }

    public void printStackTrace() {
        err.printStackTrace();
    }

    public Error getError() {
        return err;
    }

}