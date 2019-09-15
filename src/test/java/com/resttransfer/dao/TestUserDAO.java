package com.resttransfer.dao;


import com.resttransfer.exception.CustomException;
import com.resttransfer.model.User;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertTrue;

public class TestUserDAO {

    private static Logger log = Logger.getLogger(TestUserDAO.class);

    private static final DataBase h2DaoFactory = DataBase.getDataBase();

    @BeforeClass
    public static void setup() {
        // prepare test database and test data by executing sql script demo.sql
        log.debug("setting up test database and sample data....");
        h2DaoFactory.insertTestData();
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGetAllUsers() throws CustomException {
        List<User> allUsers = h2DaoFactory.getUserDAO().getAllUsers();
        assertTrue(allUsers.size() > 1);
    }

    @Test
    public void testGetUserById() throws CustomException {
        User u = h2DaoFactory.getUserDAO().getUserById(2L);
        assertTrue(u.getUserName().equals("petrov"));
    }

    @Test
    public void testGetNonExistingUserById() throws CustomException {
        User u = h2DaoFactory.getUserDAO().getUserById(500L);
        assertTrue(u == null);
    }

    @Test
    public void testGetNonExistingUserByName() throws CustomException {
        User u = h2DaoFactory.getUserDAO().getUserByName("test");
        assertTrue(u == null);
    }

    @Test
    public void testCreateUser() throws CustomException {
        final String userName = "test";
        User u = new User(userName);
        long id = h2DaoFactory.getUserDAO().insertUser(u);
        User uAfterInsert = h2DaoFactory.getUserDAO().getUserById(id);
        assertTrue(uAfterInsert.getUserName().equals(userName));
    }

}

