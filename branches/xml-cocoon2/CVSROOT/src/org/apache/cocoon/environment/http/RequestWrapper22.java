/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.environment.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.File;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.RequestDispatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import uk.co.weft.maybeupload.MaybeUploadRequestWrapper;

import org.apache.cocoon.environment.Request;

/**
 *
 * Implements the {@link javax.servlet.http.HttpServletRequest} interface
 * to provide request information for HTTP servlets.
 */

public class RequestWrapper extends MaybeUploadRequestWrapper {

    public RequestWrapper(HttpServletRequest httpservletrequest, File file, boolean flag, boolean flag1)
        throws Exception {
        super(httpservletrequest, file, flag, flag1);
    }

}
