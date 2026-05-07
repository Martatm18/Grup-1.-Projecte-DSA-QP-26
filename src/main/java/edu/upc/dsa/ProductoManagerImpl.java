package edu.upc.dsa;
import edu.upc.dsa.models.*;
import java.util.*;

public class ProductoManagerImpl implements ProductoManager
{
    private static ProductoManager instance;
    private List<Producto> tienda;
    private Map<String, User> usuarios;

    private ProductoManagerImpl() {
        this.tienda = new ArrayList<>();
        this.usuarios = new HashMap<>();
        this.registerUser("test", "Jugador de Pruebas", "test");

        // Inicializamos la tienda
        tienda.add(new Producto("1", "carga_EMP", "Desactiva drones temporalmente", 4)); //
        tienda.add(new Producto("2", "usb_amarillo", "Obtiene fragmento_codigo2", 4)); //
        tienda.add(new Producto("3", "tarjeta_temporal", "Acceso a despachos", 2)); //
        tienda.add(new Producto("4", "botiquin", "Sube 50% de vida", 2)); //
        tienda.add(new Producto("5", "bateria_seguridad", "Para abrir laboratorio", 3)); //
        tienda.add(new Producto("6", "mapa_ampliado", "Ver zonas ocultas", 1)); //
    }

    public static ProductoManager getInstance()
    {
        if (instance == null) instance = new ProductoManagerImpl();
        return instance;
    }

    @Override
    public List<Producto> getListaProductos()
    {
        return this.tienda;
    }

    @Override
    public int comprarProducto(String idProducto, String idUser)
    {
        User u = usuarios.get(idUser);
        Producto p = tienda.stream().filter(x -> x.getId().equals(idProducto)).findFirst().orElse(null);

        if (u == null || p == null)
        {
            return 404;
        }

        if (u.getEcts() >= p.getPrecio())
        {
            u.subtractEcts(p.getPrecio());
            u.addObjeto(p);
            return 201; // OK
        }

        return 402; // No hay saldo
    }

    @Override
    public void addUser(String id, String nombre)
    {
        usuarios.put(id, new User(id, nombre));
    }

    @Override
    public int registerUser(String id, String nombre, String password)
    {
        if (id == null || id.trim().isEmpty() || password == null || password.trim().isEmpty())
        {
            return 400;
        }

        String cleanId = id.trim();
        if (usuarios.containsKey(cleanId))
        {
            return 409;
        }

        String cleanName = nombre == null || nombre.trim().isEmpty() ? cleanId : nombre.trim();
        usuarios.put(cleanId, new User(cleanId, cleanName, password));
        return 201;
    }

    @Override
    public User loginUser(String id, String password)
    {
        if (id == null || password == null)
        {
            return null;
        }

        User user = usuarios.get(id.trim());
        if (user != null && password.equals(user.getPassword()))
        {
            return user;
        }

        return null;
    }

    @Override
    public User getUser(String idUser) {
        return this.usuarios.get(idUser);
    }
}

