package com.resttransfer.dao.impl;

import com.resttransfer.dao.AccountDAO;
import com.resttransfer.dao.DataBase;
import com.resttransfer.dao.UserDAO;
import com.resttransfer.utils.Utils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import org.h2.tools.RunScript;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBaseImpl implements DataBase {
    private static final String H2_DRIVER = Utils.getStringProperty("h2_driver");
    private static final String H2_CONNECTION_URL = Utils.getStringProperty("h2_connection_url");
    private static final String H2_USER = Utils.getStringProperty("h2_user");
    private static final String H2_PASSWORD = Utils.getStringProperty("h2_password");
    private static Logger LOGGER = Logger.getLogger(DataBaseImpl.class);

    private final UserDAOImpl userDAO = new UserDAOImpl();
    private final AccountDAOImpl accountDAO = new AccountDAOImpl();

    public DataBaseImpl() {
        // init: load driver
        DbUtils.loadDriver(H2_DRIVER);
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(H2_CONNECTION_URL, H2_USER, H2_PASSWORD);

    }

    public UserDAO getUserDAO() {
        return userDAO;
    }

    public AccountDAO getAccountDAO() {
        return accountDAO;
    }

    @Override
    public void insertTestData() {
        LOGGER.info("Inserting test data");
        Connection connection = null;
        try {
            connection = DataBaseImpl.getConnection();
            RunScript.execute(connection, new FileReader("src/test/resources/demo.sql"));
        } catch (SQLException e) {
            LOGGER.error("Error inserting data: ", e);
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            LOGGER.error("Error finding test script file ", e);
            throw new RuntimeException(e);
        } finally {
            DbUtils.closeQuietly(connection);
        }
    }

}
