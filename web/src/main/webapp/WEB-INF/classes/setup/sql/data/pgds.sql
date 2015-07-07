-- user pgds is used for pgds schema access
--CREATE ROLE pgds LOGIN
--  ENCRYPTED PASSWORD 'md500406785624cfc009235a08081167cdb'
--  NOSUPERUSER INHERIT CREATEDB NOCREATEROLE NOREPLICATION;

--ALTER ROLE pgds
--  SET search_path = pgds;

-- Schema pgds must either exist or be created
--CREATE SCHEMA pgds
--  AUTHORIZATION pgds;

DROP TABLE IF EXISTS pgds.groups;
DROP TABLE IF EXISTS pgds.users;
DROP TABLE IF EXISTS pgds.usergroups;

-- DROP VIEW pgds.groups;

CREATE OR REPLACE VIEW pgds.groups AS 
 SELECT groups.id, groups.name, groups.description, groups.email, groups.referrer
   FROM ngr.groups;

ALTER TABLE pgds.groups
  OWNER TO pgds;

-- DROP VIEW pgds.usergroups;

CREATE OR REPLACE VIEW pgds.usergroups AS 
 SELECT usergroups.userid, usergroups.groupid
   FROM ngr.usergroups;

ALTER TABLE pgds.usergroups
  OWNER TO pgds;


-- DROP VIEW pgds.users;

CREATE OR REPLACE VIEW pgds.users AS 
 SELECT users.id, users.username, users.password, users.surname, users.name, users.profile, users.address, users.city, users.state, users.zip, users.country, users.email, users.organisation, users.kind
   FROM ngr.users;

ALTER TABLE pgds.users
  OWNER TO pgds;
