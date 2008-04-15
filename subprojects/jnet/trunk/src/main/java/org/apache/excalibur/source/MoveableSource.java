/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.excalibur.source;


/**
 * This class marks a source to be able to moved and copied to
 * serveral other locations. This class should only be used if
 * the implementations details should be hidden, otherwise
 * the class SourceUtils can be used.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id$
 */
public interface MoveableSource extends Source
{

    /**
     * Copy the current source to a specified destination.
     *
     * @param destination Destination of the source.
     *
     * @throws SourceException If an exception occurs during
     *                         the copy.
     */
    void copyTo(Source destination) throws SourceException;

    /**
     * Move the current source to a specified destination.
     *
     * @param destination Destination of the source.
     *
     * @throws SourceException If an exception occurs during
     *                         the move.
     */
    void moveTo(Source destination) throws SourceException;
}
