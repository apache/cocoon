# MySQL dump 6.4
#
# Host: localhost    Database: test
#--------------------------------------------------------
# Server version	3.22.27

#
# Table structure for table 'department_table'
#
CREATE TABLE department_table (
  id int(11) DEFAULT '0' NOT NULL,
  name varchar(255) DEFAULT '' NOT NULL,
  PRIMARY KEY (id)
);

#
# Dumping data for table 'department_table'
#

INSERT INTO department_table VALUES (1,'Programmers');
INSERT INTO department_table VALUES (2,'Loungers');

#
# Table structure for table 'employee_table'
#
CREATE TABLE employee_table (
  id int(11) DEFAULT '0' NOT NULL,
  department_id int(11) DEFAULT '0' NOT NULL,
  name varchar(255) DEFAULT '' NOT NULL,
  PRIMARY KEY (id)
);

#
# Dumping data for table 'employee_table'
#

INSERT INTO employee_table VALUES (1,1,'Donald Ball');
INSERT INTO employee_table VALUES (2,1,'Stefano Mazzochi');
INSERT INTO employee_table VALUES (3,2,'Pierpaolo Fumagalli');
