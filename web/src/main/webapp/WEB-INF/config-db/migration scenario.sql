INSERT INTO ngr.users (id, kind, lastlogindate, name, organisation, profile, authtype, nodeid, password, security, surname, username, enabled)
 SELECT users.id, users.kind, ''::character varying(255) AS lastlogindate, users.name::character varying(255) AS name, users.organisation::character varying(255) AS organisation, 
        CASE
            WHEN users.profile::text = 'Administrator'::text THEN 0
            WHEN users.profile::text = 'UserAdmin'::text THEN 1
            WHEN users.profile::text = 'Reviewer'::text THEN 2
            WHEN users.profile::text = 'Editor'::text THEN 3
            ELSE 4
        END AS profile, ''::character varying(32) AS authtype, 'srv'::character varying(255) AS nodeid, users.password::character varying(120) AS password, 'UPDATE_HASH_REQUIRED'::character varying(128) AS security, users.surname::character varying(255) AS surname, users.username::character varying(255) AS username, true AS enabled
   FROM ngr_old.users WHERE id > 1;
   
 INSERT INTO ngr.groups (id, name, description, email, referrer, logo, website, enableallowedcategories, defaultcategory_id)
 SELECT groups.id, groups.name, groups.description, groups.email::character varying(128) AS email, groups.referrer, ''::character varying(255) AS logo, ''::character varying(255) AS website, FALSE AS enableallowedcategories, NULL::integer AS defaultcategory_id
   FROM ngr_old.groups WHERE id > 2;

  INSERT INTO ngr.usergroups (userid, profile, groupid)
 SELECT usergroups.userid, 2 AS profile, usergroups.groupid
   FROM ngr_old.usergroups;
   
 INSERT INTO ngr.metadata (id, data, changedate, createdate, displayorder, doctype, extra, popularity, rating, root, schemaid, title, istemplate, isharvested, harvesturi, harvestuuid, groupowner, owner, source, uuid)
	SELECT id, data, changedate, createdate, displayorder, doctype, extra, popularity, rating, root, schemaid, title, istemplate, isharvested, harvesturi, harvestuuid, groupowner, owner, source, uuid
		FROM ngr_old.metadata;
		
INSERT INTO ngr.operationallowed (groupid, metadataid, operationid)
	SELECT groupid, metadataid, operationid
		FROM ngr_old.operationallowed