package com.resttransfer.services;

import static com.resttransfer.utils.Utils.getBigDecimalScale2Down;
import com.resttransfer.model.Account;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;

public class TestAccountService extends TestService {


    /*
    TC A1 Positive Category = AccountService
    Scenario: test get user account by user name
              return 200 OK
     */
    @Test
    public void testGetAccountByUserName() throws IOException, URISyntaxException {
        URI uri = URIBUILDER.setPath("/account/1").build();
        HttpGet request = new HttpGet(uri);
        HttpResponse response = HTTPCLIENT.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();

        assertTrue(statusCode == 200);
        //check the content
        String jsonString = EntityUtils.toString(response.getEntity());
        Account account = OBJECTMAPPER.readValue(jsonString, Account.class);
        assertTrue(account.getUserId() == 1);
    }

    /*
    TC A3 Positive Category = AccountService
    Scenario: test get account balance given account ID
              return 200 OK
    */
    @Test
    public void testGetAccountBalance() throws IOException, URISyntaxException {
        URI uri = URIBUILDER.setPath("/account/1/balance").build();
        HttpGet request = new HttpGet(uri);
        HttpResponse response = HTTPCLIENT.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        assertTrue(statusCode == 200);
        //check the content, assert user test2 have balance 100
        String balance = EntityUtils.toString(response.getEntity());
        BigDecimal res = getBigDecimalScale2Down(balance);
        BigDecimal db = getBigDecimalScale2Down("100.10");
        assertTrue(res.equals(db));
    }

    /*
    TC A4 Positive Category = AccountService
    Scenario: test create new user account
              return 200 OK
    */
    @Test
    public void testCreateAccount() throws IOException, URISyntaxException {
        URI uri = URIBUILDER.setPath("/account/create").build();
        BigDecimal balance = getBigDecimalScale2Down("10.00");
        Account acc = new Account(4, balance, "CNY");
        String jsonInString = OBJECTMAPPER.writeValueAsString(acc);
        StringEntity entity = new StringEntity(jsonInString);
        HttpPut request = new HttpPut(uri);
        request.setHeader("Content-type", "application/json");
        request.setEntity(entity);
        HttpResponse response = HTTPCLIENT.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        assertTrue(statusCode == 200);
        String jsonString = EntityUtils.toString(response.getEntity());
        Account aAfterCreation = OBJECTMAPPER.readValue(jsonString, Account.class);
        assertTrue(aAfterCreation.getUserId() == 4);
        assertTrue(aAfterCreation.getCurrencyCode().equals("CNY"));
    }


    /*
    TC A6 Positive Category = AccountService
    Scenario: delete valid user account
              return 200 OK
    */
    @Test
    public void testDeleteAccount() throws IOException, URISyntaxException {
        URI uri = URIBUILDER.setPath("/account/3").build();
        HttpDelete request = new HttpDelete(uri);
        request.setHeader("Content-type", "application/json");
        HttpResponse response = HTTPCLIENT.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        assertTrue(statusCode == 200);
    }


    /*
    TC A7 Negative Category = AccountService
    Scenario: test delete non-existent account. return 404 NOT FOUND
              return 404 NOT FOUND
    */
    @Test
    public void testDeleteNonExistingAccount() throws IOException, URISyntaxException {
        URI uri = URIBUILDER.setPath("/account/300").build();
        HttpDelete request = new HttpDelete(uri);
        request.setHeader("Content-type", "application/json");
        HttpResponse response = HTTPCLIENT.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        assertTrue(statusCode == 404);
    }


}

