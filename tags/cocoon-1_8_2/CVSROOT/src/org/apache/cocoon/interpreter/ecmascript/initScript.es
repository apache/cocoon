// Init Script

var id = 'DCP Initialization Script';

/* Initialization Internal Functions */
function getScriptFunctions(object) {
  var property;
  var functions = "";

  for (property in object) {
    if (
      property != "getScriptFunctions" &&
      typeof object[property] == "function"
    ) {
      functions += object[property];
    }
  }

  return functions;
}

function getScriptVariables(object) {
  var property;
  var variables = new java.util.Vector();

  for (property in object) {
    if (typeof object[property] != "function") {
      variables.addElement(property);
    }
  }

  return variables;
}

/* DOM Factory Functions */
function createAttribute(name) {
  return document.createAttribute(name);
}

function createCDATASection(data) {
  return document.createCDATASection(data);
}

function createComment(data) {
  return document.createComment(data);
}

function createDocumentFragment() {
  return document.createDocumentFragment();
}

function createElement(tagName) {
  return document.createElement(tagName);
}

function createEntityReference(name) {
  return document.createEntityReference(name);
}

function createProcessingInstruction(target, data) {
  return document.createProcessingInstruction(target, data);
}

function createTextNode(data) {
  return document.createTextNode(data);
}

/* Formatting Functions */
function formatCount(number) {
  if (number == null) {
    return "";
  }

  return java.text.NumberFormat.getNumberInstance().format(number);
}

function formatCurrency(number) {
  if (number == null) {
    return "";
  }

  return java.text.NumberFormat.getCurrencyInstance().format(new Number(number));
}

function formatPercentage(number) {
  if (number == null) {
    return "";
  }

  return (new java.text.DecimalFormat("###.##%")).format(new Number(number));
}

function formatDate(date, format) {
  if (date == null) {
    return "";
  }

  if (format == null) {
    format = "MM/dd/yyyy hh:mm:ss";
  }

  var formatter = new java.text.SimpleDateFormat(format);
  formatter.setTimeZone(java.util.TimeZone.getDefault());

  return formatter.format(date);
}

/* Database Management Functions */
var dbError = null;
var connectionManager = null;
tryEval("connectionManager = Packages.DBConnectionManager.getInstance();");

function sqlRowSet(connectionName, sqlStatement) {
  if (connectionManager == null) {
    return null;
  }

  dbError = null;
  var connection = connectionManager.getConnection(connectionName);

  if (connection == null) {
    dbError = "Failed to get requested connection: " + connectionName;

    return null;
  }

  var statement = connection.createStatement();

  var resultSet;
  var result = tryEval("resultSet = statement.executeQuery(sqlStatement);");

  if (result.error) {
    dbError = result.error.getLocalizedMessage();
    connectionManager.freeConnection(connectionName, connection);

    return null;
  }

  var metaData = resultSet.getMetaData();

  var array = new Array();

  for (var i = 0; resultSet.next(); i++) {
    array[i] = new SqlRow(resultSet, metaData);
  }

  statement.close();
  connectionManager.freeConnection(connectionName, connection);

  return array;
}

function SqlRow(resultSet, metaData) {
  var columnCount = metaData.getColumnCount();

  for (var i = 0; i < columnCount; i++) {
    var columnValue = resultSet.getObject(i + 1);
    var columnName = metaData.getColumnName(i + 1).toLowerCase();

    this[columnName] = columnValue;
  }
}

/* General-purpose Functions */
function sleep(milliSeconds) {
  java.lang.Thread.currentThread().sleep(milliSeconds);
}

function nvl(object) {
  return object == null ? "" : object;
}
