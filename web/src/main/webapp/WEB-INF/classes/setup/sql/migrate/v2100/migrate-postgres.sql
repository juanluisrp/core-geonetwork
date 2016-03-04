-- Spring security
UPDATE Users SET security='update_hash_required';

-- Delete LDAP settings
DELETE FROM Settings WHERE parentid=86;
DELETE FROM Settings WHERE parentid=87;
DELETE FROM Settings WHERE parentid=89;
DELETE FROM Settings WHERE parentid=80;
DELETE FROM Settings WHERE id=80;

-- New settings
INSERT INTO Settings VALUES (24,20,'securePort','8443');
-- NGR 2 changed ids as already used by other settings
INSERT INTO Settings VALUES (960,1,'hidewithheldelements',NULL);
INSERT INTO Settings VALUES (961,960,'enable','false');
INSERT INTO Settings VALUES (962,960,'keepMarkedElement','true');
INSERT INTO Settings VALUES (963,960,'ignored','true');

-- Version update
UPDATE Settings SET value='2.10.0' WHERE name='version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='subVersion';

UPDATE HarvestHistory SET elapsedTime = 0 WHERE elapsedTime IS NULL;
