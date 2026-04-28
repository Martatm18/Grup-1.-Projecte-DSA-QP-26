package edu.upc.dsa;

import edu.upc.dsa.models.Producto;
import edu.upc.dsa.models.User;
import org.apache.log4j.Logger;

import java.util.List;

public class MainTest {
    private static final Logger logger = Logger.getLogger(MainTest.class);

    public static void main(String[] args) {
        ProductoManager pm = ProductoManagerImpl.getInstance();

        String userId = "estudiante_sigma";
        String password = "sigma123";

        int registerResult = pm.registerUser(userId, "Alumno Protocolo", password);
        User u = pm.loginUser(userId, password);
        User wrongLogin = pm.loginUser(userId, "password_mal");
        int duplicatedRegister = pm.registerUser(userId, "Alumno Repetido", password);

        logger.info("--- TEST INICIAL ---");
        logger.info("Registro nuevo usuario: " + registerResult + " (esperado 201)");
        logger.info("Login correcto: " + (u != null ? "OK" : "ERROR"));
        logger.info("Login password incorrecto: " + (wrongLogin == null ? "OK" : "ERROR"));
        logger.info("Registro usuario duplicado: " + duplicatedRegister + " (esperado 409)");

        if (u == null) {
            logger.error("No se puede continuar: el login ha fallado.");
            return;
        }

        logger.info("Usuario autenticado: " + u.getId());
        logger.info("Saldo inicial: " + u.getEcts() + " ECTS");

        logger.info("--- PRODUCTOS DISPONIBLES EN TIENDA ---");
        List<Producto> tienda = pm.getListaProductos();
        for (Producto p : tienda) {
            logger.info("ID: " + p.getId() + " | " + p.getNombre() + " | Coste: " + p.getPrecio() + " ECTS");
        }

        logger.info("--- COMPRANDO CARGA EMP (Coste 4) ---");
        int res = pm.comprarProducto("1", userId);

        if (res == 201) {
            logger.info("Resultado: COMPRA OK");
            logger.info("Nuevo saldo: " + u.getEcts() + " ECTS");

            logger.info("--- MOCHILA DEL JUGADOR ---");
            for (Producto item : u.getInventario()) {
                logger.info("Tienes: " + item.getNombre() + " (" + item.getDescripcion() + ")");
            }
        } else {
            logger.error("Resultado: ERROR (" + res + ")");
        }
    }
}
