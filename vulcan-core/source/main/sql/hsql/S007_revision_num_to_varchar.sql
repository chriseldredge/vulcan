alter table builds alter column revision varchar(256);
alter table change_sets alter column revision varchar(256);

update db_version set version_number=7;