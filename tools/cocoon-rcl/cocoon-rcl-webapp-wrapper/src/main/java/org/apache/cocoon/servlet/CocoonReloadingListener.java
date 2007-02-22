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
package org.apache.cocoon.servlet;

import java.io.File;

import org.apache.commons.jci.listeners.ReloadingListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CocoonReloadingListener extends ReloadingListener {

    private final Log log = LogFactory.getLog(CocoonReloadingListener.class);

    private static boolean reload = false;

    public CocoonReloadingListener(File file) {
        super(file);
    }

    public void onChangeFile(File changedFile) {
        super.onChangeFile(changedFile);
        String changedFileParent = changedFile.getParent().replace('\\', '/');

        if (changedFileParent.endsWith("META-INF/cocoon/spring") || changedFileParent.endsWith("config/avalon")
                || changedFileParent.endsWith("config/spring")) {
            log.info("File change detected: " + changedFile);
            reload = true;
        }
    }

    public static synchronized boolean isReload() {
        if (reload == true) {
            reload = false;
            return true;
        }
        return reload;
    }

}
