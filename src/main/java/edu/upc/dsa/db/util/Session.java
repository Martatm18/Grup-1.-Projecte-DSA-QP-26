package edu.upc.dsa.db.util;

import java.util.LinkedHashMap;
import java.util.List;

public interface Session {
    void save(Object entity);
    void update(Object entity);
    void addProductToInventory(String username, Integer productId);
    List<Object> findAll(Class theClass, LinkedHashMap<String, Object> params);
    Object get(Class theClass, Object id);
    void close();
}
