package com.resttransfer.dao;

import com.resttransfer.exception.CustomException;
import com.resttransfer.model.Account;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;

import static com.resttransfer.utils.Utils.getBigDecimalScale2Down;
import static junit.framework.TestCase.assertTrue;

public class TestAccountDAO {

    private static final DataBase h2DaoFactory = DataBase.getDataBase();

    @BeforeClass
    public static void setup() {
        // prepare test database and test data. Test data are initialised from
        // src/test/resources/demo.sql
        h2DaoFactory.insertTestData();
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGetAccountById() throws CustomException {
        Account account = h2DaoFactory.getAccountDAO().getAccountById(1L);
        assertTrue(account.getUserId() == 1);
    }

    @Test
    public void testGetNonExistingAccById() throws CustomException {
        Account account = h2DaoFactory.getAccountDAO().getAccountById(100L);
        assertTrue(account == null);
    }

    @Test
    public void testCreateAccount() throws CustomException {
        BigDecimal balance = getBigDecimalScale2Down("10.00");
        Account a = new Account(7, balance, "CNY");
        long aid = h2DaoFactory.getAccountDAO().createAccount(a);
        Account afterCreation = h2DaoFactory.getAccountDAO().getAccountById(aid);
        assertTrue(afterCreation.getUserId() == 7);
        assertTrue(afterCreation.getCurrencyCode().equals("CNY"));
        assertTrue(afterCreation.getBalance().equals(balance));
    }

    @Test
    public void testDeleteAccount() throws CustomException {
        int rowCount = h2DaoFactory.getAccountDAO().deleteAccountById(2L);
        // assert one row(user) deleted
        assertTrue(rowCount == 1);
        // assert user no longer there
        assertTrue(h2DaoFactory.getAccountDAO().getAccountById(2L) == null);
    }

    @Test
    public void testDeleteNonExistingAccount() throws CustomException {
        int rowCount = h2DaoFactory.getAccountDAO().deleteAccountById(500L);
        // assert no row(user) deleted
        assertTrue(rowCount == 0);

    }

    @Test
    public void testUpdateAccountBalanceSufficientFund() throws CustomException {

        BigDecimal deltaDeposit = getBigDecimalScale2Down("50");
        BigDecimal afterDeposit = getBigDecimalScale2Down("150.10");
        int rowsUpdated = h2DaoFactory.getAccountDAO().updateAccountBalance(1L, deltaDeposit);
        assertTrue(rowsUpdated == 1);
        assertTrue(h2DaoFactory.getAccountDAO().getAccountById(1L).getBalance().equals(afterDeposit));
        BigDecimal deltaWithDraw = getBigDecimalScale2Down("-50");
        BigDecimal afterWithDraw = getBigDecimalScale2Down("100.10");
        int rowsUpdatedW = h2DaoFactory.getAccountDAO().updateAccountBalance(1L, deltaWithDraw);
        assertTrue(rowsUpdatedW == 1);
        assertTrue(h2DaoFactory.getAccountDAO().getAccountById(1L).getBalance().equals(afterWithDraw));

    }

    @Test(expected = CustomException.class)
    public void testUpdateAccountBalanceNotEnoughFund() throws CustomException {
        BigDecimal deltaWithDraw = getBigDecimalScale2Down("-5000");
        int rowsUpdatedW = h2DaoFactory.getAccountDAO().updateAccountBalance(1L, deltaWithDraw);
        assertTrue(rowsUpdatedW == 0);

    }

}
