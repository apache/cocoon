package org.apache.cocoon.components.flow.javascript;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import java.util.List;

/**
 * Implements a Rhino JavaScript {@link
 * org.mozilla.javascript.ErrorReporter}. This is used to explicitly
 * refer to the error in the original source files, rather than the
 * combined file as presented for parsing by {@link ListInputStream}
 * in {@link JavaScriptInterpreter#readScripts}.
 *
 * <p>When an error is reported, either during the parsing of the
 * JavaScript files, or at runtime, an instance of this class is
 * invoked. This class maintains a list of {@link SourceInfo} objects,
 * which contain the original {@link
 * org.apache.cocoon.environment.Source} object and the line numbers
 * in it. When the error happens, the reporter matches the aggregated
 * line number to the actual source of the error.
 *
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @since August 15, 2002
 */
public class JSErrorReporter implements ErrorReporter
{
  /**
   * List of {@link SourceInfo} objects.
   */
  protected List sourcesInfo;

  public JSErrorReporter(List sourcesInfo)
  {
    this.sourcesInfo = sourcesInfo;
  }

  public void error(String message,
                    String sourceName, int line,
                    String lineSrc, int column)
  {
    String errMsg = getErrorMessage("ERROR: ", message, line, lineSrc, column);
    System.out.print(errMsg);
  }

  public void warning(String message, String sourceName, int line,
                      String lineSrc, int column)
  {
    System.out.print(getErrorMessage("WARNING: ", message, line, lineSrc, column));
  }
    
  public EvaluatorException runtimeError(String message, String sourceName,
                                         int line, String lineSrc,
                                         int column)
  {
    String errMsg = getErrorMessage("", message, line, lineSrc, column);
    return new EvaluatorException(errMsg);
  }

  /**
   * Identifies the real location of the error in the file given the
   * information stored in <code>sourcesInfo</code>.
   *
   * @param type a <code>String</code> value, indicating the error
   * type (error or warning)
   * @param message a <code>String</code> value, the error or warning
   * message
   * @param line an <code>int</code> value, the original cummulative
   * line number
   * @param lineSource a <code>String</code> value, the text of the
   * line in the file
   * @param column an <code>int</code> value, the column in
   * <code>lineSource</code> where the error appeared
   * @return a <code>String</code> value, the aggregated error
   * message, with the source file and line number adjusted to the
   * real values
   */
  protected String getErrorMessage(String type,
                                   String message,
                                   int line,
                                   String lineSource, int column)
  {
    int i = 0, size = sourcesInfo.size();
    int accLines = 0;

    // Find the file which contains the line number indicated by the error
    SourceInfo source;
    do {
      source = (SourceInfo)sourcesInfo.get(i);
      accLines += source.getLineNumbers();
      i++;
    } while (accLines < line && i < size);

    String errorMsg;

    if (i == size && line > accLines) {
      errorMsg = "ERROR: Line number " + line + " out of bounds!";
      return errorMsg;
    }

    String systemId = source.getSystemId();
    int realLineNo = line - (accLines - source.getLineNumbers());

    errorMsg = systemId + ":" + realLineNo;

    // If line source information is provided, make use of that to
    // print a more descriptive error message.
    if (lineSource != null) {
      errorMsg += "\n\n" + lineSource + "\n";

      StringBuffer blanks = new StringBuffer(column);
      for (i = 1; i < column; i++)
        blanks.append(" ");

      errorMsg += blanks + "^" + "\n\n";
    }
    else
      errorMsg += ": ";

    errorMsg += type + message + "\n\n";

    return errorMsg;
  }
}
