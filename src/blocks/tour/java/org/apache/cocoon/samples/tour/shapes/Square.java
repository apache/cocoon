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

package org.apache.cocoon.samples.tour.shapes;

/**
 *  Square Shape for tour block java-shapes sample
 */
public class Square implements Shape {
    float _b;
    /** Creates a new instance of Square */
    public Square(float b) {
        _b = b;
    }
    
    public String getName() {
        return "Square";
    }

    public double area() {
        return _b*_b;
    }
    
    public double perimeter() {
        return 4*_b;
    }
    
}
