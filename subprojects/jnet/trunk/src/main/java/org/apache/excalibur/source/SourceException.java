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

import java.io.IOException;

/**
 * This Exception is thrown every time there is a problem in processing
 * a source.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id$
 */
public class SourceException
    extends IOException
{
    /**
     * Construct a new <code>SourceException</code> instance.
     *
     * @param message the detail message for this exception.
     */
    public SourceException( final String message )
    {
        this( message, null );
    }

    /**
     * Construct a new <code>SourceException</code> instance.
     *
     * @param message the detail message for this exception.
     * @param throwable the root cause of the exception.
     */
    public SourceException( final String message, final Throwable throwable )
    {
        super( message  );
        this.initCause(throwable);
    }
}
