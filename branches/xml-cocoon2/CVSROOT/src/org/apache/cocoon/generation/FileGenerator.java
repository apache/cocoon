/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.generation;

import java.io.IOException;
import org.apache.cocoon.components.parser.Parser;
import org.apache.cocoon.ProcessingException;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.4 $ $Date: 2000-07-27 21:49:00 $
 */
public class FileGenerator extends ComposerGenerator {

    /**
     * Generate XML data.
     */
    public void generate()
    throws IOException, SAXException {
        Parser parser=(Parser)this.manager.getComponent("parser");
        parser.setContentHandler(this.contentHandler);
        parser.setLexicalHandler(this.lexicalHandler);
        parser.parse(super.environment.resolveEntity(this.source));
    }    
}
