package edu.upc.dsa.db.util;

import edu.upc.dsa.models.Mission;
import edu.upc.dsa.models.Objective;
import edu.upc.dsa.models.Producto;
import edu.upc.dsa.models.Puzzle;
import edu.upc.dsa.models.User;
import edu.upc.dsa.models.UserGameState;

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

    // Progreso de misiones
    void marcarObjetivoCompletado(String username, int objectiveId);
    void marcarMisionCompletada(String username, int missionId);
    void darEcts(String username, int cantidad);
    void updateGameState(String username, Integer missionId, Integer objectiveId);
    void updateGameStateObjective(String username, Integer objectiveId);
    List<Integer> getObjectiveRewardShopIds(int objectiveId);
    boolean isMisionCompleta(String username, int missionId);
    Mission getSiguienteMision(int missionId);
    Objective getPrimerObjetivoMision(int missionId);
    Objective getSiguienteObjetivoPendiente(String username, int missionId);
    Puzzle getPuzzleById(int puzzleId);
    UserGameState getEstadoJugador(String username);
    List<String> getCompletedRewardObjectNames(String username);
}
