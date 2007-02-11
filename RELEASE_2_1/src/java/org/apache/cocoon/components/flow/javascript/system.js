//
// CVS $Id: system.js,v 1.6 2003/05/08 00:05:04 vgritsenko Exp $
//
// JavaScript definitions
//
// Author: Ovidiu Predescu <ovidiu@apache.org>
// Date: March 19, 2002
//

var suicide;

var lastContinuation = null;

function callFunction(func, args)
{
  suicide = new Continuation();
  return func.apply(this, args);
}

function sendPageAndWait(uri, bizData, timeToLive)
{
  var kont = _sendPageAndWait(uri, bizData, timeToLive);
  lastContinuation = kont;
  return kont;
}

function _sendPageAndWait(uri, bizData, timeToLive)
{
  var k = new Continuation();
  var kont = new WebContinuation(cocoon, k, lastContinuation, timeToLive);
  cocoon.forwardTo("cocoon://" + cocoon.environment.getURIPrefix() + uri,
                   bizData, kont);
  suicide();
}

function sendPageAndContinue(uri, bizData)
{
    log.error("Deprecated: Please use sendPage instead");
}

function sendPage(uri, bizData)
{
  cocoon.forwardTo("cocoon://" + cocoon.environment.getURIPrefix() + uri,
                   bizData, null);
}

function process(uri, bizData, output)
{
  cocoon.process(uri, bizData, output);
}

// This function is called to restart a previously saved continuation
// passed as argument.
function handleContinuation(kont)
{
  kont.continuation(kont);
}

// Function called to handle an expired or invalid continuation
// identified by 'id'
function handleInvalidContinuation(id)
{
  // Throw an exception which can be handled in sitemap's handle-error section
  throw new Packages.org.apache.cocoon.components.flow.InvalidContinuationException("Continuation ID + " + id + " is invalid");
}

// Redirect Support
//
// redirect to the given URI
function redirect(uri)
{
  cocoon.redirect(uri);
}

// Action Support
//
// call an action from JS
function act(type, src, param)
{
  if (type == undefined || src == undefined || param == undefined) {
    log.error("Signature does not match act(type,src,param)");
    return undefined;
  }
  return  cocoon.callAction(type,src,param);
}

// InputModule Support
//
// obtain value form InputModule
function inputValue(type, name)
{
  if (type == undefined || name == undefined) {
    log.error("Signature does not match inputValue(type,name)");
    return undefined;
  }
  return cocoon.inputModuleGetAttribute(type, name);
}

// OutputModule Support
// 
// set an attribute (starts transaction, commit or rollback required!)
function outputSet(type, name, value)
{
  if (type == undefined || name == undefined || value == undefined) {
    log.error("Signature does not match outputSet(type,name,value)");
  } else {
    cocoon.outputModuleSetAttribute(type, name, value);
  }
}

// makes attributes permanent (ends transaction)
function outputCommit(type)
{
  if (type == undefined) {
    log.error("Signature does not match outputCommit(type)");
  } else {
    cocoon.outputModuleCommit(type);
  }
}

// deletes attributes (ends transaction)
function outputRollback(type)
{
  if (type == undefined) {
    log.error("Signature does not match outputCommit(type)");
  } else {
    cocoon.outputModuleRollback(type);
  }
}

