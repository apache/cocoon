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
import org.apache.avalon.ComponentNotFoundException;
import org.apache.avalon.ComponentNotAccessibleException;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.12 $ $Date: 2000-12-08 20:39:34 $
 */
public class FileGenerator extends ComposerGenerator implements Poolable {

    /**
     * Generate XML data.
     */
    public void generate()
    throws IOException, SAXException, ProcessingException {
        try {
            log.debug("Looking up " + Roles.PARSER);
            Parser parser=(Parser)this.manager.lookup(Roles.PARSER);

            parser.setContentHandler(this.contentHandler);
            parser.setLexicalHandler(this.lexicalHandler);
            parser.parse(super.resolver.resolveEntity(null,this.source));
    } catch (IOException e) {
       log.error("FileGenerator.generate()", e);
       throw(e);
    } catch (SAXException e) {
       log.error("FileGenerator.generate()", e);
       throw(e);
    } catch (Exception e){
       log.error("Could not get parser", e);
       throw new ProcessingException(e.getMessage());
    }
    }
}
