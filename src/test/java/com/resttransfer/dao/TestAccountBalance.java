package com.resttransfer.dao;

import com.resttransfer.dao.impl.DataBaseImpl;
import com.resttransfer.exception.CustomException;
import com.resttransfer.model.Account;
import com.resttransfer.model.UserTransaction;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;

import static com.resttransfer.utils.Utils.getBigDecimalScale2Down;
import static junit.framework.TestCase.assertTrue;

public class TestAccountBalance {

    private static final Logger LOGGER = Logger.getLogger(TestAccountDAO.class);
    private static final DataBase DATA_BASE = DataBase.getDataBase();
    private static final int THREADS_COUNT = 80;

    @BeforeClass
    public static void setup() {
        DATA_BASE.insertTestData();
    }

    @After
    public void tearDown() {}

    @Test
    public void testAccountSingleThreadSameCcyTransfer() throws CustomException {

        final AccountDAO accountDAO = DATA_BASE.getAccountDAO();

        BigDecimal transferAmount = new BigDecimal(99.99);
        final Long fromAccount = 4L;
        final Long toAccount = 5L;

        UserTransaction transaction = new UserTransaction("EUR", transferAmount, fromAccount, toAccount);

        long startTime = System.currentTimeMillis();

        accountDAO.transferAccountBalance(transaction);
        long endTime = System.currentTimeMillis();

        LOGGER.info("TransferAccountBalance finished, time taken: " + (endTime - startTime) + "ms");

        Account accountFrom = accountDAO.getAccountById(fromAccount);
        Account accountTo = accountDAO.getAccountById(toAccount);

        LOGGER.debug("Account From: " + accountFrom);
        LOGGER.debug("Account To: " + accountTo);

        assertTrue(accountFrom.getBalance().equals(getBigDecimalScale2Down("300.41")));
        assertTrue(accountTo.getBalance().equals(getBigDecimalScale2Down("600.49")));

    }

    @Test
    public void testAccountMultiThreadedTransfer() throws InterruptedException, CustomException {
        final AccountDAO accountDAO = DATA_BASE.getAccountDAO();
        // transfer a total of 200USD from 100USD balance in multi-threaded
        // mode, expect half of the transaction fail
        final CountDownLatch latch = new CountDownLatch(THREADS_COUNT);
        final Long fromAccount = 1L;
        final Long toAccount = 2L;
        for (int i = 0; i < THREADS_COUNT; i++) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        final UserTransaction transaction = new UserTransaction("USD",
                                getBigDecimalScale2Down("1.01"), fromAccount, toAccount);
                        accountDAO.transferAccountBalance(transaction);
                    } catch (Exception e) {
                        LOGGER.error("Error occurred during transfer ", e);
                    } finally {
                        latch.countDown();
                    }
                }
            }).start();
        }
        latch.await();

        Account accountFrom = accountDAO.getAccountById(fromAccount);
        Account accountTo = accountDAO.getAccountById(toAccount);

        LOGGER.debug("Account From: " + accountFrom);
        LOGGER.debug("Account To: " + accountTo);

        assertTrue(accountFrom.getBalance().equals(getBigDecimalScale2Down("19.30")));
        assertTrue(accountTo.getBalance().equals(getBigDecimalScale2Down("281.00")));

    }

    @Test
    public void testTransferFailOnDBLock() throws CustomException, SQLException {
        final String SQL_LOCK_ACC = "SELECT * FROM Account WHERE AccountId = 5 FOR UPDATE";
        Connection conn = null;
        PreparedStatement lockStmt = null;
        ResultSet rs = null;
        Account fromAccount = null;

        try {
            conn = DataBaseImpl.getConnection();
            conn.setAutoCommit(false);
            // lock account for writing:
            lockStmt = conn.prepareStatement(SQL_LOCK_ACC);
            rs = lockStmt.executeQuery();
            if (rs.next()) {
                fromAccount = new Account(rs.getLong("AccountId"), rs.getLong("UserId"),
                        rs.getBigDecimal("Balance"), rs.getString("CurrencyCode"));
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("Locked Account: " + fromAccount);
            }

            if (fromAccount == null) {
                throw new CustomException("Locking error during test, SQL = " + SQL_LOCK_ACC);
            }
            // after lock account 5, try to transfer from account 6 to 5
            // default h2 timeout for acquire lock is 1sec
            BigDecimal transferAmount = getBigDecimalScale2Down("50");

            UserTransaction transaction = new UserTransaction("GBP", transferAmount, 6L, 5L);
            DATA_BASE.getAccountDAO().transferAccountBalance(transaction);
            conn.commit();
        } catch (Exception e) {
            LOGGER.error("Exception occurred, initiate a rollback");
            try {
                if (conn != null)
                    conn.rollback();
            } catch (SQLException re) {
                LOGGER.error("Fail to rollback transaction", re);
            }
        } finally {
            DbUtils.closeQuietly(conn);
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(lockStmt);
        }

        // now inspect account 3 and 4 to verify no transaction occurred
        BigDecimal originalBalance = getBigDecimalScale2Down("600.49");
        assertTrue(DATA_BASE.getAccountDAO().getAccountById(5).getBalance().equals(originalBalance));
    }

}