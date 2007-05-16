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
package org.apache.cocoon.sitemap;


/**
 * If a sitemap component needs to be cleaned up after it is used, it should
 * implement this additional interface. The pipeline implementation calls
 * the dispose method after the pipeline has been processed or when an error
 * during processing occured.
 *
 * Please note that you should only implement this interface for Spring managed
 * beans. In the case of Avalon components, use the Recyclable interface instead!
 *
 * @version $Id$
 */
public interface DisposableSitemapComponent {

    /**
     * Clean up the component.
     */
    void dispose();
}
