package edu.upc.dsa;

import edu.upc.dsa.models.Producto;
import edu.upc.dsa.models.User;
import edu.upc.dsa.ProductoManager;
import edu.upc.dsa.ProductoManagerImpl;
import java.util.List;

public class MainTest {
    public static void main(String[] args) {
        ProductoManager pm = ProductoManagerImpl.getInstance();

        // 1. Registro de usuario
        String userId = "estudiante_sigma";
        pm.addUser(userId, "Alumno Protocolo");
        User u = pm.getUser(userId);

        System.out.println("--- TEST INICIAL ---");
        System.out.println("Usuario creado: " + u.getId());
        System.out.println("Saldo inicial: " + u.getEcts() + " ECTS");

        // 2. Listado de la tienda
        System.out.println("\n--- PRODUCTOS DISPONIBLES EN TIENDA ---");
        List<Producto> tienda = pm.getListaProductos();
        for (Producto p : tienda) {
            System.out.println("ID: " + p.getId() + " | " + p.getNombre() + " | Coste: " + p.getPrecio() + " ECTS");
        }

        // 3. Proceso de Compra (Carga EMP - ID 1 - Coste 4)
        System.out.println("\n--- COMPRANDO CARGA EMP (Coste 4) ---");
        int res = pm.comprarProducto("1", userId);

        if (res == 201) {
            System.out.println("Resultado: ¡COMPRA OK!");
            System.out.println("Nuevo saldo: " + u.getEcts() + " ECTS");

            // 4. Verificar Inventario
            System.out.println("\n--- MOCHILA DEL JUGADOR ---");
            for (Producto item : u.getInventario()) {
                System.out.println("Tienes: " + item.getNombre() + " (" + item.getDescripcion() + ")");
            }
        } else {
            System.out.println("Resultado: ERROR (" + res + ")");
        }
    }
}
