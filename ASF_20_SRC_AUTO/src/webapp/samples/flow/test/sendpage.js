function showString(parameter)
{
  print ("parameter = " + parameter);
  cocoon.sendPage("page/showString", { "parameter" : parameter, "replaceme" : "@REPLACEME@" });
}
