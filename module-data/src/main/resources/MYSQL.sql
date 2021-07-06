create table user (
    id int primary key auto_increment,
    account varchar(20) not null unique,
    password varchar(80) not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table pc (
    id int primary key auto_increment,
    mac varchar(17) not null unique,
    password varchar(80) not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


create table rule (
    id int primary key auto_increment,
    account varchar(20) not null,
    mac varchar(17) not null,
    permission char(1) not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into user values(null, 'admin','123');
insert into user values(null, 'test','123');

insert into pc values(null, '54:48:10:e2:a9:c5','123');
insert into pc values(null, 'b4:69:21:10:1d:6e','123');

insert into rule values(null, 'admin','54:48:10:e2:a9:c5','2');
insert into rule values(null, 'test','54:48:10:e2:a9:c5','1');