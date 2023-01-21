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

package org.apache.cocoon.samples.tour.shapes;

/**
 *  Circle Shape for tour block java-shapes sample
 */
public class Circle implements Shape{
    double _r;

    public String getName() {
        return "Circle";
    }

    /** Creates a new instance of Circle */
    public Circle(double r) {
        _r = r;
    }
    
    public double getArea() {
        return 3.14 * _r * _r;
    }
    
    public double getPerimeter() {
        return 2 * 3.14 * _r;
    }
    
}
