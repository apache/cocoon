/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.transcoder;

import org.apache.batik.transcoder.Transcoder;

/**
 * Apache Batik Transcoder factory.
 * When given a MIME type, find a Transcoder which supports that MIME type.
 * @author <a href="mailto:rossb@apache.org">Ross Burton</a>
 * @version CVS $Id: TranscoderFactory.java,v 1.2 2004/03/05 13:01:45 bdelacretaz Exp $
 */
public interface TranscoderFactory {

  /**
   * Create a transcoder for a specified MIME type.
   * @param mimeType The MIME type of the destination format
   * @return A suitable transcoder, or <code>null> if one cannot be found
   */
  Transcoder createTranscoder(String mimeType) ;
}
