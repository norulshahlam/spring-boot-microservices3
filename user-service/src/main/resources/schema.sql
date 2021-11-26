create table IF NOT EXISTS user(
id int NOT NULL AUTO_INCREMENT,
name varchar(50),
email varchar(50),
password varchar(50),
PRIMARY KEY (id)
);