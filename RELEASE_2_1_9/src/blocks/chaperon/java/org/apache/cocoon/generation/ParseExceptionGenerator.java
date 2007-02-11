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

package org.apache.cocoon.generation;

import net.sourceforge.chaperon.process.ParseException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;

import org.apache.commons.lang.exception.ExceptionUtils;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;

import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * Exception generator.
 *
 * @version CVS $Id: ParseExceptionGenerator.java,v 1.2 2004/03/05 13:01:47 bdelacretaz Exp $
 */
public class ParseExceptionGenerator extends AbstractGenerator
{
  public void generate() throws IOException, SAXException, ProcessingException
  {
    Throwable throwable = ObjectModelHelper.getThrowable(objectModel);

    while ((!(throwable instanceof ParseException)) && 
           (ExceptionUtils.getCause(throwable)!=null))
      throwable = ExceptionUtils.getCause(throwable);

    if (!(throwable instanceof ParseException))
    {
      ((ParseException)throwable).toXML(super.contentHandler);
      return;
    }
      
    Marshaller marshaller = new Marshaller(super.contentHandler);

    try
    {
      marshaller.marshal(throwable);
    }
    catch (MarshalException me)
    {
      throw new ProcessingException(me);
    }
    catch (ValidationException ve)
    {
      throw new ProcessingException(ve);
    }
  }
}
