/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.language.markup;

import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;

import org.apache.avalon.Component;

import org.apache.cocoon.components.language.programming.ProgrammingLanguage;

import org.xml.sax.EntityResolver;

/**
 * This interface defines a markup language whose SAX producer's instance are to
 * be translated into an executable program capable or transforming the original
 * document augmenting it with dynamic content
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.8 $ $Date: 2000-12-11 16:06:41 $
 */
public interface MarkupLanguage extends Component {
    /**
    * Return the input document's encoding or <code>null</code> if it is the
    * platform's default encoding.
    * This method should be called after <code>generateCode<code> method.
    *
    * @return The input document's encoding
    */
    String getEncoding();

    /**
    * Generate source code from the input source for the target
    * <code>ProgrammingLanguage</code>.
    *
    * @param input The input source document
    * @param filename The input document's original filename
    * @param programmingLanguage The target programming language
    * @return The generated source code
    * @exception Exception If an error occurs during code generation
    */
    String generateCode(
        InputSource input, String filename,
        ProgrammingLanguage programmingLanguage,
        EntityResolver resolver
    ) throws Exception;
}
