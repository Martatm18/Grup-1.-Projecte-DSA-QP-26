package edu.upc.dsa.db.util;

import edu.upc.dsa.models.Mission;
import edu.upc.dsa.models.Producto;
import edu.upc.dsa.models.User;

import java.util.LinkedHashMap;
import java.util.List;

public interface Session {
    void save(Object entity);
    void update(Object entity);
    void addProductToInventory(String username, Integer productId);
    boolean removeProductFromInventory(String username, Integer productId);
    List<Producto> getInventory(String username);
    List<User> getRanking();
    List<Mission> getMissionsWithObjectives();
    List<Object> findAll(Class theClass, LinkedHashMap<String, Object> params);
    <T> T get(Class<T> theClass, Object id);
    <T> T get(Class<T> theClass, String field, Object value);
    void close();
}
