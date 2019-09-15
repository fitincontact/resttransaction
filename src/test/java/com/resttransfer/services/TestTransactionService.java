package com.resttransfer.services;


import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import com.resttransfer.model.Account;
import com.resttransfer.model.UserTransaction;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;

import static com.resttransfer.utils.Utils.getBigDecimalScale2Down;
import static org.junit.Assert.assertTrue;


/**
 * Integration testing for RestAPI
 * Test data are initialised from src/test/resources/demo.sql
 * <p>
 * INSERT INTO Account (UserName,Balance,CurrencyCode) VALUES ('test2',100.0000,'USD'); --ID =1
 * INSERT INTO Account (UserName,Balance,CurrencyCode) VALUES ('test1',200.0000,'USD'); --ID =2
 * INSERT INTO Account (UserName,Balance,CurrencyCode) VALUES ('test2',500.0000,'EUR'); --ID =3
 * INSERT INTO Account (UserName,Balance,CurrencyCode) VALUES ('test1',500.0000,'EUR'); --ID =4
 */
public class TestTransactionService extends TestService {
    //test transaction related operations in the account

    /*
       TC B1 Positive Category = AccountService
       Scenario: test deposit money to given account number
                 return 200 OK
    */
    @Test
    public void testDeposit() throws IOException, URISyntaxException {
        URI uri = URIBUILDER.setPath("/account/1/deposit/100").build();
        HttpPut request = new HttpPut(uri);
        request.setHeader("Content-type", "application/json");
        HttpResponse response = HTTPCLIENT.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        assertTrue(statusCode == 200);
        String jsonString = EntityUtils.toString(response.getEntity());
        Account afterDeposit = OBJECTMAPPER.readValue(jsonString, Account.class);
        //check balance is increased from 100 to 200
        assertTrue(afterDeposit.getBalance().equals(getBigDecimalScale2Down("200.10")));

    }

    /*
      TC B2 Positive Category = AccountService
      Scenario: test withdraw money from account given account number, account has sufficient fund
                return 200 OK
    */
    @Test
    public void testWithDrawSufficientFund() throws IOException, URISyntaxException {
        URI uri = URIBUILDER.setPath("/account/2/withdraw/100").build();
        HttpPut request = new HttpPut(uri);
        request.setHeader("Content-type", "application/json");
        HttpResponse response = HTTPCLIENT.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        assertTrue(statusCode == 200);
        String jsonString = EntityUtils.toString(response.getEntity());
        Account afterDeposit = OBJECTMAPPER.readValue(jsonString, Account.class);
        //check balance is decreased from 200 to 100
        assertTrue(afterDeposit.getBalance().equals(getBigDecimalScale2Down("100.20")));

    }

    /*
       TC B3 Negative Category = AccountService
       Scenario: test withdraw money from account given account number, no sufficient fund in account
                 return 500 INTERNAL SERVER ERROR
    */
    @Test
    public void testWithDrawNonSufficientFund() throws IOException, URISyntaxException {
        URI uri = URIBUILDER.setPath("/account/2/withdraw/1000.23456").build();
        HttpPut request = new HttpPut(uri);
        request.setHeader("Content-type", "application/json");
        HttpResponse response = HTTPCLIENT.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        String responseBody = EntityUtils.toString(response.getEntity());
        assertTrue(statusCode == 500);
        assertTrue(responseBody.contains("Not sufficient Fund"));
    }

    /*
       TC B4 Positive Category = AccountService
       Scenario: test transaction from one account to another with source account has sufficient fund
                 return 200 OK
    */
    @Test
    public void testTransactionEnoughFund() throws IOException, URISyntaxException {
        URI uri = URIBUILDER.setPath("/transaction").build();
        BigDecimal amount = getBigDecimalScale2Down("10.00");
        UserTransaction transaction = new UserTransaction("EUR", amount, 4L, 5L);

        String jsonInString = OBJECTMAPPER.writeValueAsString(transaction);
        StringEntity entity = new StringEntity(jsonInString);
        HttpPost request = new HttpPost(uri);
        request.setHeader("Content-type", "application/json");
        request.setEntity(entity);
        HttpResponse response = HTTPCLIENT.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        assertTrue(statusCode == 200);
    }

    /*
        TC B5 Negative Category = AccountService
        Scenario: test transaction from one account to another with source account has no sufficient fund
                  return 500 INTERNAL SERVER ERROR
     */
    @Test
    public void testTransactionNotEnoughFund() throws IOException, URISyntaxException {
        URI uri = URIBUILDER.setPath("/transaction").build();
        BigDecimal amount = getBigDecimalScale2Down("10000.00");
        UserTransaction transaction = new UserTransaction("EUR", amount, 3L, 4L);

        String jsonInString = OBJECTMAPPER.writeValueAsString(transaction);
        StringEntity entity = new StringEntity(jsonInString);
        HttpPost request = new HttpPost(uri);
        request.setHeader("Content-type", "application/json");
        request.setEntity(entity);
        HttpResponse response = HTTPCLIENT.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        assertTrue(statusCode == 500);
    }

    /*
       TC C1 Negative Category = TransactionService
       Scenario: test transaction from one account to another with source/destination account with different currency code
                 return 500 INTERNAL SERVER ERROR
    */
    @Test
    public void testTransactionDifferentCcy() throws IOException, URISyntaxException {
        URI uri = URIBUILDER.setPath("/transaction").build();
        BigDecimal amount = getBigDecimalScale2Down("100.00");
        UserTransaction transaction = new UserTransaction("USD", amount, 3L, 4L);

        String jsonInString = OBJECTMAPPER.writeValueAsString(transaction);
        StringEntity entity = new StringEntity(jsonInString);
        HttpPost request = new HttpPost(uri);
        request.setHeader("Content-type", "application/json");
        request.setEntity(entity);
        HttpResponse response = HTTPCLIENT.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        assertTrue(statusCode == 500);

    }


}
