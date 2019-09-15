package com.resttransfer.dao.impl;

import com.resttransfer.dao.AccountDAO;
import com.resttransfer.exception.CustomException;
import com.resttransfer.model.Account;
import com.resttransfer.model.UserTransaction;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.resttransfer.utils.Utils.getBigDecimalScale2Down;

public class AccountDAOImpl implements AccountDAO {

    private static final Logger LOGGER = Logger.getLogger(AccountDAOImpl.class);
    private final static String SQL_GET_ACC_BY_ID = "SELECT AccountId,UserId,Balance,CurrencyCode FROM Account WHERE AccountId = ? ";
    private final static String SQL_LOCK_ACC_BY_ID = "SELECT AccountId,UserId,Balance,CurrencyCode FROM Account WHERE AccountId = ? FOR UPDATE";
    private final static String SQL_CREATE_ACC = "INSERT INTO Account (Userid, Balance, CurrencyCode) VALUES (?, ?, ?)";
    private final static String SQL_UPDATE_ACC_BALANCE = "UPDATE Account SET Balance = ? WHERE AccountId = ? ";
    private final static String SQL_DELETE_ACC_BY_ID = "DELETE FROM Account WHERE AccountId = ?";

    public Account getAccountById(final long accountId) throws CustomException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Account account = null;
        try {
            connection = DataBaseImpl.getConnection();
            preparedStatement = connection.prepareStatement(SQL_GET_ACC_BY_ID);
            preparedStatement.setLong(1, accountId);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                account = new Account(resultSet.getLong("AccountId"), resultSet.getLong("UserId"), resultSet.getBigDecimal("Balance"),
                        resultSet.getString("CurrencyCode"));
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("Retrieve Account By Id: " + account);
            }
            return account;
        } catch (SQLException e) {
            throw new CustomException("getAccountById(): Error reading account data", e);
        } finally {
            DbUtils.closeQuietly(connection, preparedStatement, resultSet);
        }

    }

    public long createAccount(final Account account) throws CustomException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = DataBaseImpl.getConnection();
            preparedStatement = connection.prepareStatement(SQL_CREATE_ACC);
            preparedStatement.setLong(1, account.getUserId());
            preparedStatement.setBigDecimal(2, account.getBalance());
            preparedStatement.setString(3, account.getCurrencyCode());
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                final String error = "Creating account failed, no rows affected.";
                LOGGER.error(error);
                throw new CustomException(error);
            }
            resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                return resultSet.getLong(1);
            } else {
                final String error = "Creating account failed, no ID obtained.";
                LOGGER.error(error);
                throw new CustomException(error);
            }
        } catch (SQLException e) {
            final String error = "Error Inserting Account "+ account;
            LOGGER.error(error);
            throw new CustomException(error, e);
        } finally {
            DbUtils.closeQuietly(connection, preparedStatement, resultSet);
        }
    }

    public int deleteAccountById(long accountId) throws CustomException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = DataBaseImpl.getConnection();
            preparedStatement = connection.prepareStatement(SQL_DELETE_ACC_BY_ID);
            preparedStatement.setLong(1, accountId);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            final String error = "Error deleting user account Id "+ accountId;
            throw new CustomException(error, e);
        } finally {
            DbUtils.closeQuietly(connection);
            DbUtils.closeQuietly(preparedStatement);
        }
    }

    public int updateAccountBalance(long accountId, final BigDecimal deltaAmount) throws CustomException {
        Connection connection = null;
        PreparedStatement lockStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;
        Account targetAccount = null;
        int updateCount = -1;
        try {
            connection = DataBaseImpl.getConnection();
            connection.setAutoCommit(false);
            lockStmt = connection.prepareStatement(SQL_LOCK_ACC_BY_ID);
            lockStmt.setLong(1, accountId);
            rs = lockStmt.executeQuery();
            if (rs.next()) {
                targetAccount = new Account(rs.getLong("AccountId"), rs.getLong("UserId"),
                        rs.getBigDecimal("Balance"), rs.getString("CurrencyCode"));
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("updateAccountBalance from Account: " + targetAccount);
            }

            if (targetAccount == null) {
                throw new CustomException("updateAccountBalance(): fail to lock account : " + accountId);
            }
            // update account upon success locking
            final BigDecimal balance = targetAccount.getBalance().add(deltaAmount);
            if (balance.compareTo(getBigDecimalScale2Down("0")) < 0) {
                throw new CustomException("Not sufficient Fund for account: " + accountId);
            }

            updateStmt = connection.prepareStatement(SQL_UPDATE_ACC_BALANCE);
            updateStmt.setBigDecimal(1, balance);
            updateStmt.setLong(2, accountId);
            updateCount = updateStmt.executeUpdate();
            connection.commit();
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("New Balance after Update: " + targetAccount);
            return updateCount;
        } catch (SQLException se) {
            // rollback transaction if exception occurs
            LOGGER.error("updateAccountBalance(): User Transaction Failed, rollback initiated for: " + accountId, se);
            try {
                if (connection != null)
                    connection.rollback();
            } catch (SQLException re) {
                throw new CustomException("Fail to rollback transaction", re);
            }
        } finally {
            DbUtils.closeQuietly(connection);
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(lockStmt);
            DbUtils.closeQuietly(updateStmt);
        }
        return updateCount;
    }

    public int transferAccountBalance(UserTransaction userTransaction) throws CustomException {
        int result = -1;
        Connection connection = null;
        PreparedStatement lockStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet resultSet = null;
        Account fromAccount = null;
        Account toAccount = null;

        try {
            connection = DataBaseImpl.getConnection();
            connection.setAutoCommit(false);
            // lock the credit and debit account for writing:
            lockStmt = connection.prepareStatement(SQL_LOCK_ACC_BY_ID);
            lockStmt.setLong(1, userTransaction.getFromAccountId());
            resultSet = lockStmt.executeQuery();
            if (resultSet.next()) {
                fromAccount = new Account(resultSet.getLong("AccountId"), resultSet.getLong("UserId"),
                        resultSet.getBigDecimal("Balance"), resultSet.getString("CurrencyCode"));
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("transferAccountBalance from Account: " + fromAccount);
            }
            lockStmt = connection.prepareStatement(SQL_LOCK_ACC_BY_ID);
            lockStmt.setLong(1, userTransaction.getToAccountId());
            resultSet = lockStmt.executeQuery();
            if (resultSet.next()) {
                toAccount = new Account(resultSet.getLong("AccountId"),
                        resultSet.getLong("UserId"),
                        resultSet.getBigDecimal("Balance"),
                        resultSet.getString("CurrencyCode"));
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("transferAccountBalance to Account: " + toAccount);
            }

            if (fromAccount == null || toAccount == null) {
                throw new CustomException("Fail to lock both accounts for write");
            }

            if (!fromAccount.getCurrencyCode().equals(userTransaction.getCurrencyCode())) {
                throw new CustomException(
                        "Fail to transfer Fund, transaction ccy are different from source/destination");
            }

            if (!fromAccount.getCurrencyCode().equals(toAccount.getCurrencyCode())) {
                throw new CustomException(
                        "Fail to transfer Fund, the source and destination account are in different currency");
            }

            BigDecimal fromAccountLeftOver = fromAccount.getBalance().subtract(userTransaction.getAmount());
            if (fromAccountLeftOver.compareTo(getBigDecimalScale2Down("0")) < 0) {
                throw new CustomException("Not enough Fund from source Account ");
            }

            updateStmt = connection.prepareStatement(SQL_UPDATE_ACC_BALANCE);
            updateStmt.setBigDecimal(1, fromAccountLeftOver);
            updateStmt.setLong(2, userTransaction.getFromAccountId());
            updateStmt.addBatch();
            updateStmt.setBigDecimal(1, toAccount.getBalance().add(userTransaction.getAmount()));
            updateStmt.setLong(2, userTransaction.getToAccountId());
            updateStmt.addBatch();
            int[] rowsUpdated = updateStmt.executeBatch();
            result = rowsUpdated[0] + rowsUpdated[1];
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Number of rows updated for the transfer : " + result);
            }
            connection.commit();
        } catch (SQLException se) {
            LOGGER.error("transferAccountBalance(): User Transaction Failed, rollback initiated for: " + userTransaction,
                    se);
            try {
                if (connection != null)
                    connection.rollback();
            } catch (SQLException re) {
                throw new CustomException("Fail to rollback transaction", re);
            }
        } finally {
            DbUtils.closeQuietly(connection);
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(lockStmt);
            DbUtils.closeQuietly(updateStmt);
        }
        return result;
    }

}
