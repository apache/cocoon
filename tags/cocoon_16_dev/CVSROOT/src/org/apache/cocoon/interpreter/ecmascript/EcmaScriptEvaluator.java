package org.apache.cocoon.interpreter.ecmascript;

import java.io.*;
import java.util.*;
import FESI.jslib.*;

/**
 * @author <a href="mailto:rrocha@plenix.org">Ricardo Rocha</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:17 $
 */

public class EcmaScriptEvaluator {
    
  private boolean inUse;
  private JSGlobalObject evaluator;

  protected EcmaScriptEvaluator(JSGlobalObject prototype, String[] extensions)
    throws JSException
  {
    this.inUse = false;
    this.evaluator = JSUtil.makeEvaluator(extensions);

    // Copy Variables
    Vector variables = (Vector) prototype.evalAsFunction("getScriptVariables(this)");

    int variableCount = variables.size();
    for (int i = 0; i < variableCount; i++) {
      String variableName = (String) variables.elementAt(i);
      this.evaluator.setMember(variableName, prototype.eval(variableName));
    }

    // Copy functions
    this.evaluator.eval((String) prototype.evalAsFunction("getScriptFunctions(this)"));
  }

  protected JSGlobalObject getGlobalObject() {
    return this.evaluator;
  }

  protected synchronized boolean acquire() {
    if (this.inUse) {
      return false;
    }

    this.inUse = true;

    return true;
  }

  protected synchronized void release() {
    this.inUse = false;
  }
}