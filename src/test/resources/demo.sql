DROP TABLE IF EXISTS User;
CREATE TABLE User (UserId LONG PRIMARY KEY AUTO_INCREMENT NOT NULL,
                   UserName VARCHAR(100) NOT NULL);

CREATE UNIQUE INDEX idx_ue on User(UserName);

INSERT INTO User (UserName) VALUES ('ivanov');--1
INSERT INTO User (UserName) VALUES ('petrov');--2
INSERT INTO User (UserName) VALUES ('sidorov');--3

DROP TABLE IF EXISTS Account;
CREATE TABLE Account (AccountId LONG PRIMARY KEY AUTO_INCREMENT NOT NULL,
                      --UserName VARCHAR(100),
                      UserId LONG,
                      Balance DECIMAL(19,2),
                      CurrencyCode VARCHAR(10)
);

INSERT INTO Account (UserId,Balance,CurrencyCode) VALUES (1,100.10,'USD');--1
INSERT INTO Account (UserId,Balance,CurrencyCode) VALUES (2,200.20,'USD');--2
INSERT INTO Account (UserId,Balance,CurrencyCode) VALUES (3,300.30,'USD');--3
INSERT INTO Account (UserId,Balance,CurrencyCode) VALUES (1,400.40,'EUR');--4
INSERT INTO Account (UserId,Balance,CurrencyCode) VALUES (2,500.50,'EUR');--5
INSERT INTO Account (UserId,Balance,CurrencyCode) VALUES (3,600.60,'EUR');--6