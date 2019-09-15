package com.resttransfer.dao.impl;

import com.resttransfer.dao.UserDAO;
import com.resttransfer.exception.CustomException;
import com.resttransfer.model.User;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {

    private static Logger log = Logger.getLogger(UserDAOImpl.class);
    private final static String SQL_GET_USER_BY_ID = "SELECT * FROM User WHERE UserId = ? ";
    private final static String SQL_GET_ALL_USERS = "SELECT * FROM User";
    private final static String SQL_GET_USER_BY_NAME = "SELECT * FROM User WHERE UserName = ? ";
    private final static String SQL_INSERT_USER = "INSERT INTO User (UserName) VALUES (?)";
    private final static String SQL_UPDATE_USER = "UPDATE User SET UserName = ? WHERE UserId = ? ";
    private final static String SQL_DELETE_USER_BY_ID = "DELETE FROM User WHERE UserId = ? ";

    public List<User> getAllUsers() throws CustomException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<User> users = new ArrayList<>();
        try {
            connection = DataBaseImpl.getConnection();
            preparedStatement = connection.prepareStatement(SQL_GET_ALL_USERS);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                User user = new User(resultSet.getLong("UserId"), resultSet.getString("UserName"));
                users.add(user);
                if (log.isDebugEnabled())
                    log.debug("Retrieve User: " + user);
            }
            return users;
        } catch (SQLException e) {
            throw new CustomException("Error reading user data", e);
        } finally {
            DbUtils.closeQuietly(connection, preparedStatement, resultSet);
        }
    }

    public User getUserById(long userId) throws CustomException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        User user = null;
        try {
            connection = DataBaseImpl.getConnection();
            preparedStatement = connection.prepareStatement(SQL_GET_USER_BY_ID);
            preparedStatement.setLong(1, userId);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                user = new User(resultSet.getLong("UserId"), resultSet.getString("UserName"));
                if (log.isDebugEnabled())
                    log.debug("Retrieve User: " + user);
            }
            return user;
        } catch (SQLException e) {
            throw new CustomException("Error reading user data", e);
        } finally {
            DbUtils.closeQuietly(connection, preparedStatement, resultSet);
        }
    }

    public User getUserByName(String userName) throws CustomException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        User user = null;
        try {
            connection = DataBaseImpl.getConnection();
            preparedStatement = connection.prepareStatement(SQL_GET_USER_BY_NAME);
            preparedStatement.setString(1, userName);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                user = new User(resultSet.getLong("UserId"), resultSet.getString("UserName"));
                if (log.isDebugEnabled())
                    log.debug("Retrieve User: " + user);
            }
            return user;
        } catch (SQLException e) {
            throw new CustomException("Error reading user data", e);
        } finally {
            DbUtils.closeQuietly(connection, preparedStatement, resultSet);
        }
    }

    public long insertUser(User user) throws CustomException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet generatedKeys = null;
        try {
            connection = DataBaseImpl.getConnection();
            preparedStatement = connection.prepareStatement(SQL_INSERT_USER, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, user.getUserName());
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                log.error("Creating user failed, no rows affected." + user);
                throw new CustomException("Users Cannot be created");
            }
            generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getLong(1);
            } else {
                log.error("Creating user failed, no ID obtained." + user);
                throw new CustomException("Users Cannot be created");
            }
        } catch (SQLException e) {
            log.error("Error Inserting User :" + user);
            throw new CustomException("Error creating user data", e);
        } finally {
            DbUtils.closeQuietly(connection,preparedStatement,generatedKeys);
        }

    }

    public int updateUser(Long userId,User user) throws CustomException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = DataBaseImpl.getConnection();
            preparedStatement = connection.prepareStatement(SQL_UPDATE_USER);
            preparedStatement.setString(1, user.getUserName());
            preparedStatement.setLong(2, userId);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error("Error Updating User :" + user);
            throw new CustomException("Error update user data", e);
        } finally {
            DbUtils.closeQuietly(connection);
            DbUtils.closeQuietly(preparedStatement);
        }
    }

    /**
     * Delete User
     */
    public int deleteUser(long userId) throws CustomException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = DataBaseImpl.getConnection();
            preparedStatement = connection.prepareStatement(SQL_DELETE_USER_BY_ID);
            preparedStatement.setLong(1, userId);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error("Error Deleting User :" + userId);
            throw new CustomException("Error Deleting User ID:"+ userId, e);
        } finally {
            DbUtils.closeQuietly(connection);
            DbUtils.closeQuietly(preparedStatement);
        }
    }

}

