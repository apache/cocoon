/* For global variables to be treated as static, prepend "global." */
var count = 0;

/* Node Generation Functions */
function getCount() {
  return ++global.count;
}

function getSystemDate(parameters) {
  var now = new Date();
  var format = parameters.get("format");

  if (format != null) {
    return formatDate(now, format);
  }

  return now;
}

function getRequestParameters() {
  var parameterNames = request.getParameterNames();

  if (!parameterNames.hasMoreElements()) {
    return null;
  }

  var parameterList = createElement("parameters");

  while (parameterNames.hasMoreElements()) {
    var parameterName = parameterNames.nextElement();

    var parameterElement = createElement("parameter");
    parameterElement.setAttribute("name", parameterName);

    var parameterValues = request.getParameterValues(parameterName);

    for (var i = 0; i < parameterValues.length; i++) {
      var valueElement = createElement("parameter-value");
      valueElement.appendChild(createTextNode(parameterValues[i]));
      parameterElement.appendChild(valueElement);
    }

    parameterList.appendChild(parameterElement);
  }

  return parameterList;
}
