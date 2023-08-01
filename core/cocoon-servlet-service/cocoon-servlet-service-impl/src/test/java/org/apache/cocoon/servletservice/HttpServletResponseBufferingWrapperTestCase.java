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
package org.apache.cocoon.servletservice;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.easymock.MockControl;

public class HttpServletResponseBufferingWrapperTestCase extends TestCase {
    
    public void testHeadersPassing() {
        MockControl control = MockControl.createControl(HttpServletResponse.class);
        HttpServletResponse response = (HttpServletResponse)control.getMock();
        response.setHeader("test", "foo");
        response.isCommitted();
        control.setReturnValue(false, MockControl.ZERO_OR_MORE);
        control.replay();
        HttpServletResponseBufferingWrapper responseWrapper = new HttpServletResponseBufferingWrapper(response);
        responseWrapper.setHeader("test", "foo");
        control.verify();
    }
    
    public void testStatusCodePassing() {
        MockControl control = MockControl.createControl(HttpServletResponse.class);
        HttpServletResponse response = (HttpServletResponse)control.getMock();
        response.setStatus(HttpServletResponse.SC_OK);
        response.isCommitted();
        control.setReturnValue(false, MockControl.ZERO_OR_MORE);
        control.replay();
        HttpServletResponseBufferingWrapper responseWrapper = new HttpServletResponseBufferingWrapper(response);
        responseWrapper.setStatus(HttpServletResponse.SC_OK);
        control.verify();
    }
    
    public void testNoBuffering() throws IOException {
        CountingServletOutputStream countingStream = new CountingServletOutputStream();
        
        MockControl control = MockControl.createControl(HttpServletResponse.class);
        HttpServletResponse response = (HttpServletResponse)control.getMock();
        response.setStatus(HttpServletResponse.SC_OK);
        response.isCommitted();
        control.setReturnValue(false, MockControl.ZERO_OR_MORE);
        response.getOutputStream();
        control.setReturnValue(countingStream);
        control.replay();
        
        HttpServletResponseBufferingWrapper responseWrapper = new HttpServletResponseBufferingWrapper(response);
        responseWrapper.setStatus(HttpServletResponse.SC_OK);
        OutputStream outputStream = responseWrapper.getOutputStream();
        outputStream.write(0);
        
        assertEquals(1, countingStream.getCounter());
        control.verify();
    }
    
    public void testBuffering() throws IOException {
        CountingServletOutputStream countingStream = new CountingServletOutputStream();
        
        MockControl control = MockControl.createControl(HttpServletResponse.class);
        HttpServletResponse response = (HttpServletResponse)control.getMock();
        response.isCommitted();
        control.setReturnValue(false, MockControl.ZERO_OR_MORE);
        response.getOutputStream();
        control.setReturnValue(countingStream);
        control.replay();
        
        HttpServletResponseBufferingWrapper responseWrapper = new HttpServletResponseBufferingWrapper(response);
        responseWrapper.setStatus(HttpServletResponse.SC_NOT_FOUND);
        OutputStream outputStream = responseWrapper.getOutputStream();
        outputStream.write(0);
        
        assertEquals(0, countingStream.getCounter());
        control.verify();
    }
    
    public void testBufferOverflow() throws IOException {
        MockControl control = MockControl.createControl(HttpServletResponse.class);
        HttpServletResponse response = (HttpServletResponse)control.getMock();
        response.isCommitted();
        control.setReturnValue(false, MockControl.ZERO_OR_MORE);
        response.getOutputStream();
        control.setReturnValue(new CountingServletOutputStream());
        control.replay();
        HttpServletResponseBufferingWrapper responseWrapper = new HttpServletResponseBufferingWrapper(response);
        responseWrapper.setStatus(HttpServletResponse.SC_NOT_FOUND);
        OutputStream outputStream = responseWrapper.getOutputStream();
        boolean catchedException = false;
        byte[] b = new byte[1024*1024];
        for (int i = 0; i < 2; i++) {
            try {
                outputStream.write(b);
            } catch (RuntimeException e) {
                //here we check whether we caught right exception. Is there any better method?
                if (e.getMessage().indexOf("limit") != -1)
                    catchedException = true;
                else
                    throw e;
            }
        }
        control.verify();
        assertTrue("Did not catch exception of overflowed buffer.", catchedException);
    }
    
    public void testFlushBufferedResponseWhenBufferIsEmpty() throws IOException {
        MockControl control = MockControl.createControl(HttpServletResponse.class);
        HttpServletResponse response = (HttpServletResponse)control.getMock();
        response.isCommitted();
        control.setReturnValue(false, MockControl.ZERO_OR_MORE);
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.flushBuffer();
        control.replay();
        HttpServletResponseBufferingWrapper responseWrapper = new HttpServletResponseBufferingWrapper(response);
        responseWrapper.setStatus(HttpServletResponse.SC_NOT_FOUND);
        responseWrapper.flushBufferedResponse();
        control.verify();
    }
    
    /**
     * This method tests late setting of status code to 404. Late here means <b>after</b> getOutputStream method was called.
     * @throws IOException
     */
    public void testLateSettingStatusCodeTo404() throws IOException {
        CountingServletOutputStream countingStream = new CountingServletOutputStream();
        
        MockControl control = MockControl.createControl(HttpServletResponse.class);
        HttpServletResponse response = (HttpServletResponse)control.getMock();
        response.isCommitted();
        control.setReturnValue(false, MockControl.ZERO_OR_MORE);
        response.getOutputStream();
        control.setReturnValue(countingStream);
        control.replay();
        
        HttpServletResponseBufferingWrapper responseWrapper = new HttpServletResponseBufferingWrapper(response);
        responseWrapper.setStatus(HttpServletResponse.SC_NOT_FOUND);
        OutputStream outputStream = responseWrapper.getOutputStream();
        outputStream.write(0);
        
        assertEquals(0, countingStream.getCounter());
        responseWrapper.resetBufferedResponse();
        assertEquals(0, countingStream.getCounter());
        control.verify();
    }
    
    /**
     * This method tests if flushing of obtained writer is performed correctly in flushBufferedResponse.
     * @throws Exception
     */
    public void testWriterFlushing() throws Exception {
        CountingServletOutputStream countingStream = new CountingServletOutputStream();
        
        MockControl control = MockControl.createControl(HttpServletResponse.class);
        HttpServletResponse response = (HttpServletResponse)control.getMock();
        response.setStatus(HttpServletResponse.SC_OK);
        response.getCharacterEncoding();
        control.setReturnValue("ISO-8859-1");
        response.isCommitted();
        control.setReturnValue(false, MockControl.ZERO_OR_MORE);
        response.getOutputStream();
        control.setReturnValue(countingStream, MockControl.ONE_OR_MORE);
        response.flushBuffer();
        control.replay();
        
        HttpServletResponseBufferingWrapper responseWrapper = new HttpServletResponseBufferingWrapper(response);
        responseWrapper.setStatus(HttpServletResponse.SC_OK);
        PrintWriter writer = responseWrapper.getWriter();
        writer.write("0");
        responseWrapper.flushBufferedResponse();
        
        assertEquals(1, countingStream.getCounter());
    }
    
    /**
     * Simple ServletOutputStream that counts how many bytes were written.
     *
     */
    private class CountingServletOutputStream extends ServletOutputStream {
        
        private int counter = 0;

        public void write(int arg0) throws IOException {
            counter++;
        }
        
        public int getCounter() {
            return counter;
        }
    }
    
}
