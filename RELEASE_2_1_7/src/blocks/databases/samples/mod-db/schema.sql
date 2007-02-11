

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

-- this is the hsqldb schema file
-- to adapt it to another RDBMS, replace column type identity
-- with appropriate autoincrement type, e.g. SERIAL for informix
-- you might want to add "on delete cascade" to foreign keys in
-- table user_groups

create table user (
	uid integer identity primary key,
	name varchar(50),
	firstname varchar(50),
	uname varchar(20),
	unique (uname)
);
create table groups (
	gid integer identity primary key,
	gname varchar(20),
	unique (gname)
);

create table user_groups (
	uid integer,
	gid integer,
	primary key (uid,gid),
	foreign key (uid) references user(uid),
	foreign key (gid) references groups(gid)
);

create table media (
	id integer identity primary key,
	image varbinary,
	mimetype varchar(50),
	primary key (id)
);

