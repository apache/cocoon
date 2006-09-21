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
package org.apache.cocoon.components.source.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.AbstractAggregatedValidity;

/**
 * <p>An aggregated {@link SourceValidity} for multiple sources.</p>
 *
 * @version $Id$
 */
public class MultiSourceValidity extends AbstractAggregatedValidity {

    /** <p>When validity expiration is performed.</p> */
    private long expiry;

    /** <p>The delay from <b>now</b> used to calculate the expiration time.</p> */
    private long delay;

    /** <p>An ordered list of URIs which should be checked.</p> */
    private List uris = new ArrayList();

    /** <p>Is this instance is closed (accepts modifications or is validable)? */
    private boolean isClosed = false;

    /** <p>The {@link SourceResolver} to use (transient not to be serialized). */
    private transient SourceResolver resolver;

    /**
     * <p>The delay value indicating to check always.</p>
     */
    public static final int CHECK_ALWAYS = -1;

    /**
     * <p>Create a new {@link MultiSourceValidity} instance.</p>
     *
     * <p>If the number of milliseconds is less than <b>zero</b>, or it's sum with
     * the number of <b>now</b> milliseconds is greater than the biggest long
     * representable, the expiration date will be set to {@link Long#MAX_VALUE}
     * milliseconds from the epoch.</p>
     *
     * @param resolver the {@link SourceResolver} used to access the sources.
     * @param delay the number of milliseconds from <b>now</b> defining for how long
     *              this instance will be valid.
     */
    public MultiSourceValidity(SourceResolver resolver, long delay) {
        /* Calculate the initial expiration time and calculate the delay */
        this.resolver = resolver;
        this.expiry = System.currentTimeMillis() + delay;
        this.delay = delay;
    }

    /**
     * <p>Add a {@link Source} to the list of {@link Source}s monitored by this
     * instance.</p>
     *
     * @param src a <b>non-null</b> {@link Source}.
     */
    public void addSource(Source src) {
        if (this.uris != null) {
            SourceValidity validity = src.getValidity();
            if (validity == null) {
                /* The source has no validity: this will be always be invalid. */
                this.uris = null;
            } else {
                /* Add the validity and URI to the list */
                super.add(validity);
                this.uris.add(src.getURI());
            }
        }
    }

    /**
     * <p>Close this instance, or in other words declare that no other sources will
     * be added to this {@link MultiSourceValidity} and that checkings can be now
     * performed.</p>
     */
    public void close() {
        this.isClosed = true;
        this.resolver = null;
    }

    /**
     * <p>Check the validity of this {@link SourceValidity} instance.</p>
     *
     * @see SourceValidity#isValid()
     */
    public int isValid() {
        if (System.currentTimeMillis() <= expiry) {
            /* Validity not expired, so, don't even check */
            return SourceValidity.VALID;
        }

        /* Re-calculate the expiry time based on the current time */
        expiry = System.currentTimeMillis() + delay;

        if (uris == null || !isClosed) {
            /* We have not been closed (yet) or we were forced to be invalid */
            return SourceValidity.INVALID;
        } else {
            /* Compute the status of all the sources listed in this instance */
            return computeStatus(null);
        }
    }

    /**
     * <p>Check the validity of this instance comparing it with a (recently acquired)
     * new {@link SourceValidity} object.</p>
     *
     * @see SourceValidity#isValid(SourceValidity)
     */
    public int isValid(SourceValidity newValidity) {
        if (uris == null || !isClosed) {
            /* We have not been closed (yet) or we were forced to be invalid */
            return SourceValidity.INVALID;
        }

        /* Perform a simple class check and compute the validity of the sources */
        if (newValidity instanceof MultiSourceValidity) {
            return computeStatus(((MultiSourceValidity)newValidity).resolver);
        } else {
            /* The supplied validity is not an instance of ourselves, forget it */
            return SourceValidity.INVALID;
        }
    }

    /**
     * <p>Compute the status of this instance by checking every source.</p>
     *
     * @param resolver The {@link SourceResolver} to use to access sources.
     * @return {@link SourceValidity.VALID}, {@link SourceValidity.INVALID} or
     *         {@link SourceValidity.UNKNOWN} depending on the status.
     */
    private int computeStatus(SourceResolver resolver) {
        /* Get the validities and analyse them one by one */
        List validities = super.getValidities();
        for (int i = 0; i < validities.size(); i++) {

            /* Check the validity status */
            SourceValidity validity = (SourceValidity) validities.get(i);
            switch (validity.isValid()) {

                /* The current source is valid: just continue to next source */
                case SourceValidity.VALID:
                    break;

                /* The current source is invalid: stop examining */
                case SourceValidity.INVALID:
                    return SourceValidity.INVALID;

                /* The source validity is not known: check with the new source */
                case SourceValidity.UNKNOWN:
                    /* We have no resolver: definitely don't know */
                    if (resolver == null) {
                        return SourceValidity.UNKNOWN;
                    }

                    /* Check the new source by asking to the resolver */
                    Source newSrc = null;
                    int newValidity = SourceValidity.INVALID;
                    try {
                        newSrc = resolver.resolveURI((String) this.uris.get(i));
                        newValidity = validity.isValid(newSrc.getValidity());
                    } catch(IOException ioe) {
                        /* Swallow the IOException, but set the new validity */
                        newValidity = SourceValidity.INVALID;
                    } finally {
                        /* Make sure that the source is released */
                        if (newSrc != null) {
                            resolver.release(newSrc);
                        }
                    }

                    /* If the source is still valid, go to the next one */
                    if (newValidity == SourceValidity.VALID) {
                        break;
                    }

                    /* The source is not valid (or unknown), we invalidate the lot */
                    return SourceValidity.INVALID;

                /* We got something _really_ odd out tof the validity, dunno. */
                default:
                    return SourceValidity.INVALID;
            }
        }

        /* All items checked successfully */
        return SourceValidity.VALID;
    }
}
