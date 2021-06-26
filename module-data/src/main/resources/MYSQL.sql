create table user (
    id int primary key auto_increment,
    account varchar(20) not null unique,
    password varchar(80) not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table rule (
    id int primary key auto_increment,
    userId int,
    mac varchar(17) not null unique,
    permission int not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8;