create table users (
	id int generated by default as identity not null primary key,
	username varchar(256) not null	
)

alter table builds add column broken_by_user_id int before message_key;
alter table builds add column claimed_date timestamp before message_key;
alter table builds add constraint fk_broken_by foreign key (broken_by_user_id) references users (id);

update db_version set version_number=3;
