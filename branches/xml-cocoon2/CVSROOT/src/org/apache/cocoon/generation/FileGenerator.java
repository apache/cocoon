/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.generation;

import org.apache.avalon.Poolable;
import java.io.IOException;
import org.apache.cocoon.components.parser.Parser;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Roles;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.8 $ $Date: 2000-10-19 14:43:48 $
 */
public class FileGenerator extends ComposerGenerator implements Poolable {

    /**
     * Generate XML data.
     */
    public void generate()
    throws IOException, SAXException {
        Parser parser=(Parser)this.manager.lookup(Roles.PARSER);
        parser.setContentHandler(this.contentHandler);
        parser.setLexicalHandler(this.lexicalHandler);
        parser.parse(super.resolver.resolveEntity(null,this.source));
    }    
}
