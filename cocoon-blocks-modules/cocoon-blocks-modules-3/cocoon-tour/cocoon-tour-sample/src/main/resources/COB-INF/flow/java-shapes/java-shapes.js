/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Shape's area and perimeter calculation example.

var calculator = null;

function public_startShape() {
    var hint = "Calculate shape's area and perimeter using logic in java. ";

    // let user select shape
    cocoon.sendPageAndWait("java-shapes/views/select", {"hint" : hint});
    var shapeId = cocoon.request.get("shape");

    // send shape-specific view
    cocoon.sendPageAndWait("java-shapes/views/" + shapeId, {"shapeId" : shapeId});

    // get request parameters (of which some are null depending on shape, that's not a problem)
    var h = parseInt( cocoon.request.get("h") );
    var b = parseInt( cocoon.request.get("b") );
    var r = parseInt( cocoon.request.get("r") );

    // instantiate appropriate calculator
    if(shapeId == "square") {
        calculator = new Packages.org.apache.cocoon.samples.tour.shapes.Square(b);
    } else if(shapeId=="rectangular") {
        calculator = new Packages.org.apache.cocoon.samples.tour.shapes.Rectangular(b,h);
    } else if(shapeId=="circle") {
        calculator = new Packages.org.apache.cocoon.samples.tour.shapes.Circle(r);
    } else {
        throw new java.lang.Exception("No calculator found for shape '" + shapeId + "'");
    }

    // compute results
    // (accessing bean-like properties like "getArea()" using property names like "area")
    var a = calculator.area;
    var p = calculator.perimeter;

    cocoon.sendPage("java-shapes/views/results", {"area" : a, "perimeter" : p, "shape" : shapeId} );
}
