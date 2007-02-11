#
# Tutorial DB
#

#
# Table structure for table 'department'
#
CREATE TABLE department (
  id number(12) DEFAULT '0' NOT NULL,
  name varchar(64) DEFAULT '' NOT NULL,
  PRIMARY KEY (id)
);

#
# Data for table 'department'
#

INSERT INTO department VALUES (1,'Development');
INSERT INTO department VALUES (2,'Management');
INSERT INTO department VALUES (3,'Testers');

#
# Table structure for table 'employee'
#
CREATE TABLE employee (
  id number(12) DEFAULT '0' NOT NULL,
  department_id number(12) DEFAULT '0' NOT NULL,
  name varchar(64) DEFAULT '' NOT NULL,
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

INSERT INTO EMPLOYEE VALUES(1,1,'Donald Ball')
INSERT INTO EMPLOYEE VALUES(2,1,'Sylvain Wallez ')
INSERT INTO EMPLOYEE VALUES(3,1,'Carsten Ziegeler ')
INSERT INTO EMPLOYEE VALUES(4,1,'Torsten Curdt')
INSERT INTO EMPLOYEE VALUES(5,1,'Marcus Crafter')
INSERT INTO EMPLOYEE VALUES(6,1,'Ovidiu Predescu')
INSERT INTO EMPLOYEE VALUES(7,1,'Christian Haul')
INSERT INTO EMPLOYEE VALUES(8,2,'Stefano Mazzocchi')
INSERT INTO EMPLOYEE VALUES(9,3,'Pierpaolo Fumagalli')
INSERT INTO EMPLOYEE VALUES(10,3,'Davanum Srinivas')
