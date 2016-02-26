UPDATE Settings SET value='3.1.0' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/feedback/mailServer/tls', 'false', 2, 644, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/xlinkResolver/ignore', 'operatesOn,featureCatalogueCitation,Anchor,source', 0, 2312, 'n');

DELETE FROM Settings WHERE name = 'system/removedMetadata/dir';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/tracking/enabled','false', 2, 10200, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/tracking/code', '', 0, 10210, 'n');
