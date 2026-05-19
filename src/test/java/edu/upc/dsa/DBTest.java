package edu.upc.dsa;

import edu.upc.dsa.models.User;

public class DBTest {

    public static void main(String[] args) {
        ProductoManager pm = ProductoManagerImpl.getInstance();

        String username = "hectorgrau" + System.currentTimeMillis();
        String nombre = "Héctor";
        String password = "1234";
        String email = username + "@gmail.com";

        int registerStatus = pm.registerUser(username, nombre, password, email);
        System.out.println("Register status: " + registerStatus + " (esperado 201)");

        User loginOk = pm.loginUser(username, password);
        System.out.println("Login correcto: " + (loginOk != null ? "OK" : "ERROR"));

        User loginWrong = pm.loginUser(username, "password_mal");
        System.out.println("Login incorrecto: " + (loginWrong == null ? "OK" : "ERROR"));

        User userFromDb = pm.getUser(username);
        System.out.println("Get user: " + (userFromDb != null ? "OK" : "ERROR"));

        int duplicatedStatus = pm.registerUser(username, "Usuario Repetido", password, "otro_" + email);
        System.out.println("Register duplicado: " + duplicatedStatus + " (esperado 409)");

        if (userFromDb != null) {
            System.out.println("--- DATOS USUARIO ---");
            System.out.println("id: " + userFromDb.getId());
            System.out.println("nombre: " + userFromDb.getNombre());
            System.out.println("email: " + userFromDb.getEmail());
            System.out.println("ects: " + userFromDb.getEcts());
        }
    }
}