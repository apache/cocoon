/*
* Copyright 1999-2004 The Apache Software Foundation
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
rolloverImagesOn=new Array();
rolloverImagesOff=new Array();

function rolloverOn(name) {
  if(document.images[name] && rolloverImagesOn[name]) document.images[name].src=rolloverImagesOn[name].src;
}

function rolloverOff(name) {
 if(document.images[name] && rolloverImagesOff[name]) document.images[name].src=rolloverImagesOff[name].src;
}


function rolloverLoad(name,on,off) {
  rolloverImagesOn[name]=new Image();
  rolloverImagesOn[name].src=mangle(on);
  rolloverImagesOff[name]=new Image();
  rolloverImagesOff[name].src=mangle(off);
}

function mangle(name) {

  name = name.replace(/:/g, "_");
  name = name.replace(/\?/g, "_");
  name = name.replace(/\"/g, "\'");

  return name;
}
