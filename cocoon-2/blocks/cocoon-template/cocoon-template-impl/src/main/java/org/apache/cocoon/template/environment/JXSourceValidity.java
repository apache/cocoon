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

import java.io.Serializable;

import org.apache.excalibur.source.SourceValidity;

/**
 * @version SVN $Id$
 */
public final class JXSourceValidity implements SourceValidity, Serializable {
    private final SourceValidity sourceValidity;
    private final SourceValidity templateValidity;

    public JXSourceValidity(SourceValidity sourceValidity,
            SourceValidity templateValidity) {
        this.sourceValidity = sourceValidity;
        this.templateValidity = templateValidity;
    }

    public int isValid() {
        switch (sourceValidity.isValid()) {
        case SourceValidity.INVALID:
            return SourceValidity.INVALID;
        case SourceValidity.UNKNOWN: {
            if (templateValidity.isValid() == SourceValidity.INVALID) {
                return SourceValidity.INVALID;
            } else {
                return SourceValidity.UNKNOWN;
            }
        }
        case SourceValidity.VALID:
            return templateValidity.isValid();
        }
        return SourceValidity.UNKNOWN;
    }

    public int isValid(SourceValidity otherValidity) {
        if (otherValidity instanceof JXSourceValidity) {
            JXSourceValidity otherJXValidity = (JXSourceValidity) otherValidity;
            switch (sourceValidity.isValid(otherJXValidity.sourceValidity)) {
            case SourceValidity.INVALID:
                return SourceValidity.INVALID;
            case SourceValidity.UNKNOWN: {
                if (templateValidity.isValid(otherJXValidity.templateValidity) == SourceValidity.INVALID) {
                    return SourceValidity.INVALID;
                } else {
                    return SourceValidity.UNKNOWN;
                }
            }
            case SourceValidity.VALID:
                return templateValidity
                        .isValid(otherJXValidity.templateValidity);
            }
        }
        return 0;
    }

}