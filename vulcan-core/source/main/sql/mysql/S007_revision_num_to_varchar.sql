alter table builds modify revision varchar(256);
alter table change_sets modify revision varchar(256);

update db_version set version_number=7;