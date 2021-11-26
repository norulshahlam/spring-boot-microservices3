create table  IF NOT EXISTS account (
	id INT NOT NULL AUTO_INCREMENT,
	account_number VARCHAR(50),
	account_type TEXT,
	balance DECIMAL(7,2),
	user_id INT,
  PRIMARY KEY (id),
  FOREIGN KEY (user_id) REFERENCES user(id)
);