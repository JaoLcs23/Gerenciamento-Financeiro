package com.controle.dao;

import com.controle.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractDAO<T, ID> implements GenericDAO<T, ID> {

    public AbstractDAO() {
    }
}