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

import org.apache.commons.jci.stores.ResourceStore;
import org.apache.commons.jci.stores.Transactional;

class JarListener extends org.apache.commons.jci.listeners.ReloadingListener {

    private final ResourceStore store;

    public JarListener(File file, ResourceStore store) {
        super(file, store);
        this.store = store;
    }

    public void onStop() {
        boolean reload = false;

        if (store instanceof Transactional) {
            ((Transactional) store).onStart();
        }

        if (deleted.size() > 0) {
            reload = true;
        }

        if (created.size() > 0) {
            // FIXME: not necessary
            // reload = true;
        }

        if (changed.size() > 0) {
            reload = true;
        }

        if (store instanceof Transactional) {
            ((Transactional) store).onStop();
        }

        // TODO: either clear the whole store or iterate the jar and remove
        // resource that are in there

        checked(reload);

    }
}
