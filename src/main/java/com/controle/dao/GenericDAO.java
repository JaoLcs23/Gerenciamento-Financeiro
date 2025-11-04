package com.controle.dao;

import java.util.List;

public interface GenericDAO<Tipo, ID> {
    void save(Tipo entity);

    Tipo findById(ID id);

    List<Tipo> findAll();

    void update(Tipo entity);

    void delete(ID id);
}