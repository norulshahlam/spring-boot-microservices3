create table  IF NOT EXISTS account (
	account_number VARCHAR(50) NOT NULL,
	account_type TEXT,
	balance DECIMAL(7,2),
	user_id INT,
  FOREIGN KEY (user_id) REFERENCES user(id)
);