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
package org.apache.garbage;

import java.util.Map;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.garbage.parser.Parser;
import org.apache.garbage.tree.Tree;
import org.apache.garbage.serializer.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * 
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: Main.java,v 1.2 2004/03/05 10:07:22 bdelacretaz Exp $
 */
public class Main {

    private String args[] = null;
    private java.util.Map props = null;
  
    public String[] getArgs() {
        return(args);
    }
  
    public Map getProps() {
        return(props);
    }

    public static void main(String args[]) {
        try {
            Main main = new Main();
            main.args = args;
            main.props = System.getProperties();
            JXPathContext context = JXPathContext.newContext(main);

            InputSource source = new InputSource(args[0]);
            Parser parser = new Parser();
            Tree tree = parser.parse(source);
            String encoding = (args.length > 1? args[1]: "US-ASCII");

            XMLSerializer serializer = new XMLSerializer();
            serializer.setOutput(System.out, encoding);

            new Processor(serializer).process(tree, context);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
