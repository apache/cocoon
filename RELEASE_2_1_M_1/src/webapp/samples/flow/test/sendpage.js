function showString(parameter)
{
  print ("parameter = " + parameter);
  sendPageAndContinue("showString", { "parameter" : parameter, "replaceme" : "@REPLACEME@" });
}
