package com.resttransfer.services;


import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import com.resttransfer.model.User;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertTrue;

public class TestUserService extends TestService {

    @Test
    public void testGetUser() throws IOException, URISyntaxException {
        URI uri = URIBUILDER.setPath("/user/notFound").build();
        HttpGet request = new HttpGet(uri);
        HttpResponse response = HTTPCLIENT.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        assertTrue(statusCode == 404);
    }

    /*
        TC D3 Positive Category = UserService
        Scenario: Create user using JSON
                  return 200 OK
     */
    @Test
    public void testCreateUser() throws IOException, URISyntaxException {
        URI uri = URIBUILDER.setPath("/user/create").build();
        final String userName = "test12";
        User user = new User(userName);
        String jsonInString = OBJECTMAPPER.writeValueAsString(user);
        StringEntity entity = new StringEntity(jsonInString);
        HttpPost request = new HttpPost(uri);
        request.setHeader("Content-type", "application/json");
        request.setEntity(entity);
        HttpResponse response = HTTPCLIENT.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        assertTrue(statusCode == 200);
        String jsonString = EntityUtils.toString(response.getEntity());
        User uAfterCreation = OBJECTMAPPER.readValue(jsonString, User.class);
        assertTrue(uAfterCreation.getUserName().equals(userName));
    }//POST http://localhost:8081/user/create HTTP/1.1

    /*
        TC D4 Negative Category = UserService
        Scenario: Create user already existed using JSON
                  return 400 BAD REQUEST
    */
    @Test
    public void testCreateExistingUser() throws IOException, URISyntaxException {
        URI uri = URIBUILDER.setPath("/user/create").build();
        User user = new User("test1");
        String jsonInString = OBJECTMAPPER.writeValueAsString(user);
        StringEntity entity = new StringEntity(jsonInString);
        HttpPost request = new HttpPost(uri);
        request.setHeader("Content-type", "application/json");
        request.setEntity(entity);
        HttpResponse response = HTTPCLIENT.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        assertTrue(statusCode == 400);

    }

    /*
     TC D5 Positive Category = UserService
     Scenario: Update Existing User using JSON provided from client
               return 200 OK
     */
    @Test
    public void testUpdateUser() throws IOException, URISyntaxException {
        URI uri = URIBUILDER.setPath("/user/1").build();
        User user = new User(1L, "test1");
        String jsonInString = OBJECTMAPPER.writeValueAsString(user);
        StringEntity entity = new StringEntity(jsonInString);
        HttpPut request = new HttpPut(uri);
        request.setHeader("Content-type", "application/json");
        request.setEntity(entity);
        HttpResponse response = HTTPCLIENT.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        assertTrue(statusCode == 200);
    }


    /*
    TC D6 Negative Category = UserService
    Scenario: Update non existed User using JSON provided from client
              return 404 NOT FOUND
    */
    @Test
    public void testUpdateNonExistingUser() throws IOException, URISyntaxException {
        URI uri = URIBUILDER.setPath("/user/100").build();
        User user = new User(2L, "test1");
        String jsonInString = OBJECTMAPPER.writeValueAsString(user);
        StringEntity entity = new StringEntity(jsonInString);
        HttpPut request = new HttpPut(uri);
        request.setHeader("Content-type", "application/json");
        request.setEntity(entity);
        HttpResponse response = HTTPCLIENT.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        assertTrue(statusCode == 404);
    }

    /*
     TC D7 Positive Category = UserService
     Scenario: test delete user
                return 200 OK
    */
    @Test
    public void testDeleteUser() throws IOException, URISyntaxException {
        URI uri = URIBUILDER.setPath("/user/3").build();
        HttpDelete request = new HttpDelete(uri);
        request.setHeader("Content-type", "application/json");
        HttpResponse response = HTTPCLIENT.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        assertTrue(statusCode == 200);
    }
//DELETE http://localhost:8081/user/3 HTTP/1.1

    /*
    TC D8 Negative Category = UserService
    Scenario: test delete non-existed user
              return 404 NOT FOUND
   */
    @Test
    public void testDeleteNonExistingUser() throws IOException, URISyntaxException {
        URI uri = URIBUILDER.setPath("/user/300").build();
        HttpDelete request = new HttpDelete(uri);
        request.setHeader("Content-type", "application/json");
        HttpResponse response = HTTPCLIENT.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        assertTrue(statusCode == 404);
    }


}

