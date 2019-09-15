# resttransaction
A Java RESTful API for money transfers

Jetty
H2

### Run 
exec:java

### Examples:

http://localhost:8080/user/1

http://localhost:8080/account/1

curl --header "Content-Type: application/json" --request POST --data '{"currencyCode":"USD","amount":1.00,"fromAccountId":2,"toAccountId":1}' http://localhost:8080/transaction

### Services see:

package com.resttransfer.service;
