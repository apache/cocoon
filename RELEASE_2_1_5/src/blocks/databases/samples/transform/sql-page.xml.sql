# MySQL dump 6.4

# Copyright 1999-2004 The Apache Software Foundation
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#
# Host: localhost    Database: test
#--------------------------------------------------------
# Server version	3.22.27

#
# Table structure for table 'department'
#
CREATE TABLE department (
  id int(11) DEFAULT '0' NOT NULL,
  name varchar(255) DEFAULT '' NOT NULL,
  PRIMARY KEY (id)
);

#
# Dumping data for table 'department'
#

INSERT INTO department VALUES (1,'Programmers');
INSERT INTO department VALUES (2,'Loungers');

#
# Table structure for table 'employee'
#
CREATE TABLE employee (
  id int(11) DEFAULT '0' NOT NULL,
  department_id int(11) DEFAULT '0' NOT NULL,
  name varchar(255) DEFAULT '' NOT NULL,
  PRIMARY KEY (id)
);

#
# Dumping data for table 'employee'
#

INSERT INTO employee VALUES (1,1,'Donald Ball');
INSERT INTO employee VALUES (2,1,'Stefano Mazzocchi');
INSERT INTO employee VALUES (3,2,'Pierpaolo Fumagalli');
