package com.resttransfer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class Account {

    @JsonIgnore
    private long accountId;

    @JsonProperty(required = true)
    private long userId;

    @JsonProperty(required = true)
    private BigDecimal balance;

    @JsonProperty(required = true)
    private String currencyCode;

    public Account() { }

    public Account(long userId, BigDecimal balance, String currencyCode) {
        this.userId = userId;
        this.balance = balance;
        this.currencyCode = currencyCode;
    }

    public Account(long accountId, long userId, BigDecimal balance, String currencyCode) {
        this.accountId = accountId;
        this.userId = userId;
        this.balance = balance;
        this.currencyCode = currencyCode;
    }

    public long getAccountId() {
        return accountId;
    }

    public long getUserId() {
        return userId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Account account = (Account) o;

        if (accountId != account.accountId) return false;
        if (userId != account.userId) return false;
        if (!balance.equals(account.balance)) return false;
        return currencyCode.equals(account.currencyCode);

    }

    @Override
    public int hashCode() {
        int result = (int) (accountId ^ (accountId >>> 32));
        result = 31 * result +  (int) (userId ^ (userId >>> 32));
        result = 31 * result + balance.hashCode();
        result = 31 * result + currencyCode.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuffer strBuffer = new StringBuffer("");
        strBuffer.append("Account{accountId=");
        strBuffer.append(accountId);
        strBuffer.append(", userId='");
        strBuffer.append(userId);
        strBuffer.append("\\n, balance=");
        strBuffer.append(balance);
        strBuffer.append(", currencyCode='");
        strBuffer.append(currencyCode);
        strBuffer.append("\\n}");
        return strBuffer.toString();
    }
}

