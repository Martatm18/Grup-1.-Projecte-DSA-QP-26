package edu.upc.dsa.db.orm;

import java.util.LinkedHashMap;
import java.util.List;

public interface Session {
    void save(Object entity);
    List<Object> findAll(Class theClass, LinkedHashMap<String, Object> params);
    Object get(Class theClass, Object id);
    void close();
}