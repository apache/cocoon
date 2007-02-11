function showString(parameter)
{
  print ("parameter = " + parameter);
  sendPage("page/showString", { "parameter" : parameter, "replaceme" : "@REPLACEME@" });
}
