/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

var calculator = Packages.org.apache.cocoon.samples.supersonic.shapes.Shape;

function public_startShape() {
    var hint = "Calculate shape's area and perimeter using logic in java. ";

    // let user select shape
    cocoon.sendPageAndWait("java-shapes/views/select", {"hint" : hint});
    var shapeId = cocoon.request.get("shape");

    // send shape-specific view
    cocoon.sendPageAndWait("java-shapes/views/" + shapeId, {"shapeId" : shapeId});

    // instantiate appropriate calculator
    switch (shapeId){
        case "square":
            var b      = parseInt( cocoon.request.get("b") );
            calculator = new Packages.org.apache.cocoon.samples.tour.shapes.Square(b);
            break;
        case "rectangular":
            var h      = parseInt( cocoon.request.get("h") );
            var b      = parseInt( cocoon.request.get("b") );
            calculator = new Packages.org.apache.cocoon.samples.tour.shapes.Rectangular(b,h);
            break;
        case "circle":
            var r      = parseInt( cocoon.request.get("r") );
            calculator = new Packages.org.apache.cocoon.samples.tour.shapes.Circle(r);
            break;
    }

    // compute results
    var a = calculator.area();
    var p = calculator.perimeter();

    cocoon.sendPage("java-shapes/views/results", {"area" : a, "perimeter" : p, "shape" : shapeId} );
}