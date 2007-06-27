/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

/*
* @version $Id$
*/

function countLines(str, cols) {
    var hard_lines = -2;
    var pos = 0;
    while (true) {
        pos = str.indexOf("\n", pos + 1);
        hard_lines ++;
        if (pos == -1) break;
    }
    var soft_lines = Math.round(str.length / (cols-1));
    if (hard_lines > soft_lines) soft_lines = hard_lines;
    return soft_lines;
}

function resizeTextareas() {
    var form = document.forms[0];
    for (var i in form) {
        if (!form[i]) continue;
        if(typeof form[i].rows != "number") continue;
        form[i].rows = countLines(form[i].value,form[i].cols) + 1;
    }
    setTimeout("resizeTextareas();", 300);
}

resizeTextareas();

