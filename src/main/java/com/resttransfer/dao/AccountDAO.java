package com.resttransfer.dao;

import com.resttransfer.exception.CustomException;
import com.resttransfer.model.Account;
import com.resttransfer.model.UserTransaction;

import java.math.BigDecimal;

public interface AccountDAO {

    Account getAccountById(long accountId) throws CustomException;
    long createAccount(Account account) throws CustomException;
    int deleteAccountById(long accountId) throws CustomException;
    int updateAccountBalance(long accountId, BigDecimal deltaAmount) throws CustomException;
    int transferAccountBalance(UserTransaction userTransaction) throws CustomException;
}

