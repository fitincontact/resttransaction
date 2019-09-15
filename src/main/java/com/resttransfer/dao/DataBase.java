package com.resttransfer.dao;

import com.resttransfer.dao.impl.DataBaseImpl;

public interface DataBase {

    UserDAO getUserDAO();
    AccountDAO getAccountDAO();
    void insertTestData();
    static DataBase getDataBase() {
        return new DataBaseImpl();
    }
}
