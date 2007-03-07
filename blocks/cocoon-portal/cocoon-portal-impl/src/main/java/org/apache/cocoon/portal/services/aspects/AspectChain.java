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
package org.apache.cocoon.portal.services.aspects;

import java.util.Iterator;
import java.util.Properties;

import org.apache.cocoon.portal.PortalException;

/**
 * Reusable implementation of an aspect chain.
 *
 * @since 2.2
 * @version $Id$
 */
public interface AspectChain {

    Properties EMPTY_PROPERTIES = new Properties();

    Class getAspectClass();

    void addAspect(Object aspect, Properties config)
    throws PortalException;

    void addAspect(Object aspect, Properties config, int index)
    throws PortalException;

    boolean hasAspects();

    Iterator getAspectsIterator();

    Iterator getPropertiesIterator();
}
