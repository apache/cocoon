package org.apache.cocoon.dcp;

import java.util.*;
import org.w3c.dom.*;
import org.apache.cocoon.framework.*;

/**
 * The convenience class that all DCP objects should extend.
 *
 * @author <a href="mailto:rrocha@plenix.org">Ricardo Rocha</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:28 $
 */

public abstract class DefaultDCPProcessor implements Configurable {
  
  protected Document document;
  protected Dictionary parameters;

  public DefaultDCPProcessor() { }

  public void init(Configurations configurations) {
    this.document = (Document) configurations.get("document");
    this.parameters = (Dictionary) configurations.get("parameters");
  }

  protected Attr createAttribute(String name) {
    return document.createAttribute(name);
  }

  protected CDATASection createCDATASection(String data) {
    return document.createCDATASection(data);
  }

  protected Comment createComment(String data) {
    return document.createComment(data);
  }

  protected DocumentFragment createDocumentFragment() {
    return document.createDocumentFragment();
  }

  protected Element createElement(String tagName) {
    return document.createElement(tagName);
  }

  protected EntityReference createEntityReference(String name) {
    return document.createEntityReference(name);
  }

  protected ProcessingInstruction createProcessingInstruction(String target, String data) {
    return document.createProcessingInstruction(target, data);
  }

  protected Text createTextNode(String data) {
    return document.createTextNode(data);
  }
}