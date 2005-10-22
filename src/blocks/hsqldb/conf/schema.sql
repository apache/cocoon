# Copyright 1999-2005 The Apache Software Foundation
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
# Samples Database Schema
#

#
# Table structure for table 'department'
#
CREATE TABLE department (
  id number(12) DEFAULT '0' NOT NULL,
  name varchar(64) DEFAULT '' NOT NULL,
  description varchar(255) DEFAULT '' NULL,
  PRIMARY KEY (id)
);

#
# Data for table 'department'
#
INSERT INTO department VALUES (1, 'Development', '<span>Department producing software packages, <i>on time</i>, and <i>bug free</i>.</span>');
INSERT INTO department VALUES (2, 'Management', '<span>Department spending all the budget on <i>meetings</i> and <i>donuts</i>.</span>');
INSERT INTO department VALUES (3, 'Testers', '<span>Department capable of finding issues in <i>bug free</i> software packages.</span>');

#
# Table structure for table 'employee'
#
CREATE TABLE employee (
  id number(12) DEFAULT '0' NOT NULL,
  department_id number(12) DEFAULT '0' NOT NULL,
  name varchar(64) DEFAULT '' NOT NULL,
  description varchar(255) DEFAULT '' NULL,
  PRIMARY KEY (id)
);

#
# Foreign Keys
#
ALTER TABLE employee ADD (
  CONSTRAINT fkdepartment FOREIGN KEY(DEPARTMENT_ID)
  REFERENCES DEPARTMENT(ID)
  ON DELETE CASCADE
);

#
# Data for table 'employee'
#
INSERT INTO EMPLOYEE VALUES(1,  1, 'Donald Ball')
INSERT INTO EMPLOYEE VALUES(2,  1, 'Sylvain Wallez ')
INSERT INTO EMPLOYEE VALUES(3,  1, 'Carsten Ziegeler ')
INSERT INTO EMPLOYEE VALUES(4,  1, 'Torsten Curdt')
INSERT INTO EMPLOYEE VALUES(5,  1, 'Marcus Crafter')
INSERT INTO EMPLOYEE VALUES(6,  1, 'Ovidiu Predescu')
INSERT INTO EMPLOYEE VALUES(7,  1, 'Christian Haul')
INSERT INTO EMPLOYEE VALUES(8,  2, 'Stefano Mazzocchi')
INSERT INTO EMPLOYEE VALUES(9,  3, 'Pierpaolo Fumagalli')
INSERT INTO EMPLOYEE VALUES(10, 3, 'Davanum Srinivas')
INSERT INTO EMPLOYEE VALUES(11, 3, 'Antonio Gallardo')
