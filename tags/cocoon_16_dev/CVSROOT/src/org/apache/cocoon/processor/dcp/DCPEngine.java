package org.apache.cocoon.processor.dcp;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import javax.servlet.http.*;
import org.apache.cocoon.processor.*;
import org.apache.cocoon.framework.*;
import org.apache.cocoon.interpreter.*;

/**
 * This class implements a DOM processor that evaluates
 * <i><font color="green">&lt;?</font><font color="brown">dcp?</font>
 * <font color="green">&gt;</font></i> processing instructions to generate
 * dynamic content.
 * 
 * <p align="justify">
 * The following processing instructions are recognized:
 * </p>
 * <ul>
 * <li>
 * <pre><font color="green">&lt;?</font><font color="brown">dcp-object</font>
 * <font color="darkGreen">name</font>=<font color="magenta">"<i>objectName</i>"</font>
 * [<font color="darkGreen">language</font>=<font color="magenta">"<i>languageName</i>"</font>]
 * <font color="darkGreen">code</font>=<font color="magenta">"<i>codeLocation</i>"</font>
 * <font color="green">?&gt;</font></pre>
 * <p align="justify">
 * This instruction declares an external program (or <i>DCP script</i>) that contains
 * node-generation methods. These methods will be invoked during document processing
 * as dictated by the appearance of subsequent <i><font color="green">&lt;?</font>
 * <font color="brown">dcp-content?</font><font color="green">&gt;</font></i> 
 * directives (explained below).
 * </p>
 * <p align="justify">
 * Attribute <font color="darkGreen"><i>name</i></font> specifies an author-defined
 * <font color="magenta"><i>objectName</i></font> that will be used to qualify method
 * names in the DCP script. This name must be unique within the document.
 * </p>
 * <p align="justify">
 * Attribute <font color="darkGreen"><i>language</i></font> specifies the programming
 * language in which the DCP script is written. Supported values for this attribute
 * are <font color="magenta"><i>java</i></font>
 * and <font color="magenta"><i>javascript</i></font>
 * (also referred to as <font color="magenta"><i>ecmascript</i></font>).
 * This attribute is optional; its default value is
 * <font color="magenta"><i>java</i></font>. Other languages may be added in the future. It
 * is valid for the same XML document to use multiple DCP scripts written in different languages.
 * </p>
 * <p align="justify">
 * Attribute <font color="darkGreen"><i>code</i></font> specifies the actual DCP script
 * location. Interpretation of this mandatory attribute is language-dependent.
 * For Java, it is a fully qualified class name. For Javascript, it is a script
 * filename relative to the path of the
 * invoking XML document. The same code can be specified multiple times in
 * a given document, provided a different <font color="magenta"><i>objectName</i></font> is used in
 * each case.
 * </p>
 * </li>
 * <li>
 * <pre><font color="green">&lt;?</font><font color="brown">dcp-content</font>
 * <font color="darkGreen">method</font>=<font color="magenta">"<i>object.method</i>"</font>
 * [<font color="darkGreen">param1</font>=<font color="magenta">"<i>value</i>"</font> 
 * <font color="darkGreen">param2</font>=<font color="magenta">"<i>value</i>"</font> ...]
 * <font color="green">?&gt;</font></pre>
 * <p align="justify">
 * This instruction requests the substitution of its corresponding node by the
 * return value of a named <i>method</i> defined in a DCP script.
 * </p>
 * <p align="justify">
 * Single-valued, named parameters can be passed to node-generation methods by specifying
 * additional attributes in the
 * <font color="green">&lt;?</font><font color="brown"><i>dcp-content</i></font><font color="green">?&gt;</font>
 * processing instruction. These attributes are made available to the method through a
 * <code><i>Dictionary</i></code> argument.
 * </p>
 * <p align="justify">
 * Attribute <font color="darkGreen"><i>method</i></font> defines what
 * <font color="magenta"><i>method</i></font> to invoke on a given
 * <font color="magenta"><i>object</i></font>. The object name must have been
 * associated with a DCP script by means of a previous
 * <font color="green">&lt;?</font><font color="brown"><i>dcp-object</i></font><font color="green">?&gt;</font>
 * processing instruction. Node-generation methods must be public and conform to the following signature:
 * </p>
 * <blockquote>
 * <pre><font color="brown"><i>methodName</i></font>(
 * [<font color="blue">java.util.Dictionary parameters</font>],
 * [<font color="blue">org.w3c.Node source</font>]
 * )</pre>
 * </blockquote>
 * <p align="justify">
 * where the optional function arguments are:
 * </p>
 * <ul>
 * <li>
 * <font color="blue"><i>parameters</i></font>. A dictionary containing optional named parameters
 * specified as additional attributes to the
 * <font color="green">&lt;?</font><font color="brown"><i>dcp-content</i></font><font color="green">?&gt;</font>
 * processing instruction.
 * </li>
 * <li>
 * <font color="blue"><i>source</i></font>. The processing instruction node
 * corresponding to the
 * <font color="green">&lt;?</font><font color="brown"><i>dcp-content</i></font><font color="green">?&gt;</font>
 * directive itself. This is useful for methods that need access to siblings or ancestors in the
 * DOM tree.
 * </li>
 * </ul>
 * <p align="justify">
 * Methods can return any type of value, including primitive types, <i>void</i> and
 * <i>null</i>. <i>Void</i> and <i>null</i> are understood as a request to remove the
 * corresponding node. Returned values that are instances of <i>org.w3c.Node</i> are
 * simply inserted into the corresponding DOM tree position. Primitive types and
 * regular objects are wrapped as strings in <i>org.w3c.Text</i> nodes. Arrays are wrapped as
 * <i>org.w3c.DocumentFragment</i>'s containing as
 * many children as elements in the array; these elements are recursively wrapped
 * according to the above rules.
 * </p>
 * </li>
 * </ul>
 * 
 * @author <a href="mailto:rrocha@plenix.org">Ricardo Rocha</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:14 $
 */
 
public class DCPEngine {
    
    private static final String OBJECT_PI = "dcp-object";
    private static final String CONTENT_PI = "dcp-content";
    private static final String VARIABLE_PI = "dcp-var";
    private static final String NAME_ATTRIBUTE = "name";
    private static final String LANGUAGE_ATTRIBUTE = "language";
    private static final String CODE_ATTRIBUTE = "code";
    private static final String METHOD_ATTRIBUTE = "method";
    
    private InterpreterFactory interpreterFactory;
    private Document document;
    private Director director;
    private Dictionary parameters;
    private Hashtable instances;
    private Hashtable globalVariables;

    /**
     * Set the document being processed and any parameters passed by the invoking
     * environment.
     * <p align="justify">
     * This method sets the DOM tree to be scanned for dynamic content
     * <i><font color="green">&lt;?</font><font color="brown">dcp?</font><font color="green">&gt;</font></i>
     * processing instructions as well as the context parameters provided by the
     * invoking environment.
     * </p>
     * 
     * @param document The document to be processed for dynamic content
     * @param parameters The table of environment variables to be used during processing
     */
    public DCPEngine(Document document, InterpreterFactory factory, Dictionary parameters) {
        this.document = document;
        this.interpreterFactory = factory;
        this.parameters = parameters;
        this.instances = new Hashtable(10);
        this.globalVariables = new Hashtable(10);
    }

    /**
     * Process the document substituting
     * <i><font color="green">&lt;?</font><font color="brown">dcp?</font><font color="green">&gt;</font></i>
     * processing instructions by dynamic content.
     * <p align="justify">
     * This method carries out the actual expansion of dynamic content
     * processing instructions embedded in the document.
     * </p>
     * 
     * @throws Exception When any error occurs during processing
     */
    public void process() throws Exception {

        // Process document
        doProcess(document);

        // Destroy instances created during processing
        Enumeration e = this.instances.keys();

        while (e.hasMoreElements()) {
            String instanceName = (String) e.nextElement();
            Instance instance = (Instance) this.instances.get(instanceName);

            instance.destroy();
        } 
    } 

    private void doProcess(Node node) {
        short nodeType = node.getNodeType();

        switch (nodeType) {

            case Node.ELEMENT_NODE:

            case Node.DOCUMENT_NODE:
                Node[] children = getChildren(node);

                for (int i = 0; i < children.length; i++) {
                    doProcess(children[i]);
                } 

                break;

            case Node.PROCESSING_INSTRUCTION_NODE:
                Node parent = node.getParentNode();
                ProcessingInstruction pi = (ProcessingInstruction) node;

                try {
                    String target = pi.getTarget();

                    if (target.equals(OBJECT_PI)) {
                        processObject(pi);
                        parent.removeChild(pi);
                    } else if (target.equals(CONTENT_PI)) {
                        Node result = processContent(pi);

                        if (result == null) {
                            parent.removeChild(pi);
                        } else {
                            parent.replaceChild(result, pi);
                        } 
                    } else if (target.equals(VARIABLE_PI)) {
                        processVariable(pi);
                        parent.removeChild(pi);
                    } 
                } catch (Exception e) {
                    String message = e.getMessage();
                    String className = e.getClass().getName();
                    Text text = document.createTextNode("{DCP Error: " 
                                                        + className + ": " 
                                                        + message + "}");

                    parent.replaceChild(text, pi);
                } 

                break;
        }
    } 

    private void processObject(ProcessingInstruction pi) throws Exception {

        Hashtable attributes = new Hashtable();
        parseAttributes(pi.getData(), attributes);

        String objectName = (String) attributes.get(NAME_ATTRIBUTE);
        if (objectName == null) {
            // Object name is mandatory
            throw new DCPException("Missing name in object definition");
        } 

        // Verify object name uniqueness within document
        if (this.instances.containsKey(objectName)) {
            throw new DCPException("Duplicate object name: " + objectName);
        } 

        String codeLocation = (String) attributes.get(CODE_ATTRIBUTE);
        if (codeLocation == null) {     
            // Code location is mandatory
            throw new DCPException("Missing code location in object definition");
        } 

        String languageName = (String) attributes.get(LANGUAGE_ATTRIBUTE);

        Interpreter interpreter = interpreterFactory.getInterpreter(languageName);
        Module module = interpreter.createModule(codeLocation);
        Instance instance = module.createInstance(this.document, this.parameters);

        // Register object instance
        this.instances.put(objectName, instance);
    } 

    private Node processContent(ProcessingInstruction pi) throws Exception {

        // Add global variables
        Hashtable attributes = (Hashtable) this.globalVariables.clone();
        parseAttributes(pi.getData(), attributes);

        String methodReference = (String) attributes.get(METHOD_ATTRIBUTE);
        if (methodReference == null) {
            // Method reference is mandatory
            throw new DCPException("Missing method name in content generation");
        } 

        /* Extract object and method names */
        String objectName = null;
        String methodName = null;
        StringTokenizer st = new StringTokenizer(methodReference, ".");

        try {
            objectName = st.nextToken();
            methodName = st.nextToken();
        } catch (NoSuchElementException e) {
            throw new DCPException("Invalid method reference: " + methodReference);
        } 

        Instance instance = (Instance) this.instances.get(objectName);

        if (instance == null) {      
            // Reference to an object not previously defined
            throw new DCPException("Undefined object: " + objectName);
        } 

        // Invoke method
        return instance.invoke(methodName, attributes, pi);
    } 

    private void processVariable(ProcessingInstruction pi) throws Exception {
        // Add given attributes to global variable list
        parseAttributes(pi.getData(), this.globalVariables);
    } 

    private void parseAttributes(String data, Hashtable attributes) {
        int length = data.length();
        char[] chars = data.toCharArray();

        try {
            for (int index = 0; index < length; index++) {

                /* Skip leading blanks */
                while (index < length && chars[index] <= ' ') {
                    index++;
                } 

                /* Get variable name */
                StringBuffer nameBuffer = new StringBuffer();

                while (index < length 
                       &&!(chars[index] == '=' || chars[index] <= ' ')) {
                    nameBuffer.append(chars[index++]);
                } 

                String name = nameBuffer.toString();

                // Skip blanks
                while (index < length && chars[index] <= ' ') {
                    index++;
                } 

                /* Get variable value */
                if (chars[index++] != '=') {
                    throw new Exception("Invalid attribute name: '" + name 
                                        + "'");
                } 

                // Skip blanks
                while (index < length && chars[index] <= ' ') {
                    index++;
                } 

                if (chars[index++] != '"') {
                    throw new Exception("Invalid attribute value for '" 
                                        + name + "'");
                } 

                StringBuffer valueBuffer = new StringBuffer();

                while (index < length && chars[index] != '"') {
                    valueBuffer.append(chars[index++]);
                } 

                String value = valueBuffer.toString();

                if (index == length || chars[index] != '"') {
                    throw new Exception("Unterminated string '" + value 
                                        + "' in attribute '" + name + "'");
                } 

                // Store name/value pair
                attributes.put(name, value);
            } 
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } 
    } 

    private Node[] getChildren(Node node) {
        NodeList nodeList = node.getChildNodes();
        int childCount = nodeList.getLength();
        Node[] children = new Node[childCount];

        for (int i = 0; i < childCount; i++) {
            children[i] = nodeList.item(i);
        } 

        return children;
    } 
}