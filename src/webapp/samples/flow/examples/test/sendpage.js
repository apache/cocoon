function showString(parameter)
{
  print ("parameter = " + parameter);
  
  sendPageAndContinue("showString.html",
                      { "parameter" : parameter, "replaceme" : "@REPLACEME@" });
}
