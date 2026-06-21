package edu.upc.dsa.db.util;

import edu.upc.dsa.models.Dialogue;
import edu.upc.dsa.models.Producto;
import edu.upc.dsa.models.Mission;
import edu.upc.dsa.models.Objective;
import edu.upc.dsa.models.Puzzle;
import edu.upc.dsa.models.SigmaObject;
import edu.upc.dsa.models.Equipo;
import edu.upc.dsa.models.User;
import edu.upc.dsa.models.UserGameState;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SessionImpl implements Session {
    private final Connection conn;

    public SessionImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void save(Object entity) {
        if (entity instanceof User) {
            saveUser((User) entity);
        }
        else if (entity instanceof UserGameState) {
            saveUserGameState((UserGameState) entity);
        }
        else if (entity instanceof Equipo) {
            saveEquipo((Equipo) entity);
        }
    }

    @Override
    public void update(Object entity) {
        if (entity instanceof User) {
            updateUser((User) entity);
        }
    }

    private void saveUser(User user) {
        String sql = QueryHelper.createInsertUser();

        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, user.getId());
            pstm.setString(2, user.getNombre());
            pstm.setString(3, user.getPassword());
            pstm.setString(4, user.getEmail());
            pstm.setInt(5, user.getEcts());
            pstm.setString(6, user.getAvatar());

            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveUserGameState(UserGameState gameState) {
        String sql = QueryHelper.createInsertUserGameState();

        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, gameState.getUsername());
            pstm.setInt(2, gameState.getHealth());
            pstm.setInt(3, gameState.getMaxHealth());
            pstm.setObject(4, gameState.getCurrentMissionId());
            pstm.setObject(5, gameState.getCurrentObjectiveId());

            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveEquipo(Equipo equipo) {
        String sql = QueryHelper.createInsertEquipo();

        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, equipo.getId());
            pstm.setString(2, equipo.getNombre());
            pstm.setString(3, equipo.getDescripcion());
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateUser(User user) {
        String sql = QueryHelper.createUpdateUser();

        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, user.getNombre());
            pstm.setString(2, user.getEmail());
            pstm.setInt(3, user.getEcts());
            pstm.setString(4, user.getAvatar());
            pstm.setString(5, user.getId());

            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addProductToInventory(String username, Integer productId) {
        String sql = QueryHelper.createUpsertInventory();

        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, username);
            pstm.setInt(2, productId);

            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean removeProductFromInventory(String username, Integer productId) {
        int quantity = getInventoryQuantity(username, productId);

        if (quantity == 0) {
            return false;
        }

        String sql = quantity == 1
                ? QueryHelper.createDeleteInventoryProduct()
                : QueryHelper.createDecreaseInventoryQuantity();

        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, username);
            pstm.setInt(2, productId);
            pstm.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private int getInventoryQuantity(String username, Integer productId) {
        String sql = QueryHelper.createSelectInventoryQuantity();

        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, username);
            pstm.setInt(2, productId);

            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quantity");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return 0;
    }

    @Override
    public List<Producto> getInventory(String username) {
        List<Producto> inventory = new LinkedList<>();
        String sql = QueryHelper.createSelectInventory();

        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, username);

            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    Producto producto = buildProducto(rs);
                    int quantity = rs.getInt("quantity");

                    for (int i = 0; i < quantity; i++) {
                        inventory.add(producto);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return inventory;
    }

    @Override
    public List<SigmaObject> getMissionObjects(String username) {
        List<SigmaObject> objects = new LinkedList<>();
        boolean withUser = username != null && !username.trim().isEmpty();
        String sql = withUser
                ? QueryHelper.createSelectMissionObjectsByUser()
                : QueryHelper.createSelectMissionObjects();

        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            if (withUser) {
                pstm.setString(1, username.trim());
            }

            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    objects.add(buildSigmaObject(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return objects;
    }

    @Override
    public List<User> getRanking() {
        List<User> ranking = new ArrayList<>();
        String sql = QueryHelper.createSelectRanking();

        try (PreparedStatement pstm = conn.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery()) {

            while (rs.next()) {
                User user = buildUser(rs);
                UserGameState gameState = new UserGameState();
                gameState.setUsername(user.getId());
                gameState.setHealth(rs.getInt("health"));
                gameState.setMaxHealth(rs.getInt("max_health"));
                gameState.setCurrentMissionId(rs.getObject("current_mission_id") == null ? null : rs.getInt("current_mission_id"));
                gameState.setCurrentObjectiveId(rs.getObject("current_objetive_id") == null ? null : rs.getInt("current_objetive_id"));
                gameState.setCurrentMissionTitle(rs.getString("mission_title"));
                gameState.setCurrentObjectiveTitle(rs.getString("objective_title"));
                user.setGameState(gameState);
                ranking.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return ranking;
    }

    @Override
    public List<Mission> getMissionsWithObjectives() {
        Map<Integer, Mission> missions = new LinkedHashMap<>();
        String sql = QueryHelper.createSelectMissionsWithObjectives();

        try (PreparedStatement pstm = conn.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery()) {

            while (rs.next()) {
                Integer missionId = rs.getInt("mission_id");
                Mission mission = missions.get(missionId);

                if (mission == null) {
                    mission = new Mission(
                            missionId,
                            rs.getString("mission_title"),
                            rs.getString("mission_description"),
                            rs.getInt("mission_order"),
                            rs.getInt("is_active") == 1
                    );
                    mission.setObjectives(new ArrayList<Objective>());
                    missions.put(missionId, mission);
                }

                if (rs.getObject("objective_id") != null) {
                    mission.getObjectives().add(new Objective(
                            rs.getInt("objective_id"),
                            missionId,
                            rs.getString("objective_title"),
                            rs.getString("objective_description"),
                            rs.getInt("objective_order"),
                            rs.getString("type"),
                            rs.getString("reference"),
                            rs.getInt("reward")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return new ArrayList<>(missions.values());
    }

    @Override
    public List<Object> findAll(Class theClass, LinkedHashMap<String, Object> params) {
        List<Object> result = new LinkedList<>();
        String sql = QueryHelper.createSelectFindAll(theClass, params);

        System.out.println("QUERY ORM: " + sql);

        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            int i = 1;
            for (Object value : params.values()) {
                System.out.println("Param " + i + ": " + value);
                pstm.setObject(i++, value);
            }

            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                if (theClass == User.class) {
                    result.add(buildUser(rs));
                }
                else if (theClass == Producto.class) {
                    result.add(buildProducto(rs));
                }
                else if (theClass == Equipo.class) {
                    result.add(buildEquipo(rs));
                }
                else if (theClass == UserGameState.class) {
                    result.add(buildUserGameState(rs));
                }
                else if (theClass == Mission.class) {
                    result.add(buildMission(rs));
                }
                else if (theClass == Objective.class) {
                    result.add(buildObjective(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    @Override
    public <T> T get(Class<T> theClass, Object id) {
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put("id", id);

        List<Object> result = findAll(theClass, params);

        if (result.isEmpty()) {
            return null;
        }

        return theClass.cast(result.get(0));
    }

    @Override
    public <T> T get(Class<T> theClass, String field, Object value) {
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put(field, value);

        List<Object> result = findAll(theClass, params);

        if (result.isEmpty()) {
            return null;
        }

        return theClass.cast(result.get(0));
    }

    private User buildUser(ResultSet rs) throws SQLException {
        User user = new User(
                rs.getString("username"),
                rs.getString("name"),
                rs.getString("password_hash")
        );

        user.setEmail(rs.getString("email"));
        user.setEcts(rs.getInt("ects"));
        user.setAvatar(rs.getString("avatar"));

        return user;
    }

    private Producto buildProducto(ResultSet rs) throws SQLException {
        Producto p = new Producto(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getInt("price")
        );
        p.setSlug(rs.getString("slug"));
        return p;
    }

    private SigmaObject buildSigmaObject(ResultSet rs) throws SQLException {
        return new SigmaObject(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("type"),
                rs.getString("content"),
                rs.getInt("obtenido") == 1
        );
    }

    private UserGameState buildUserGameState(ResultSet rs) throws SQLException {
        UserGameState gameState = new UserGameState();
        gameState.setUsername(rs.getString("username"));
        gameState.setHealth(rs.getInt("health"));
        gameState.setMaxHealth(rs.getInt("max_health"));
        gameState.setCurrentMissionId(rs.getObject("current_mission_id") == null ? null : rs.getInt("current_mission_id"));
        gameState.setCurrentObjectiveId(rs.getObject("current_objetive_id") == null ? null : rs.getInt("current_objetive_id"));

        return gameState;
    }

    private Equipo buildEquipo(ResultSet rs) throws SQLException {
        return new Equipo(
                rs.getString("id"),
                rs.getString("nombre"),
                rs.getString("descripcion")
        );
    }

    private Mission buildMission(ResultSet rs) throws SQLException {
        return new Mission(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getInt("mission_order"),
                rs.getInt("is_active") == 1
        );
    }

    private Objective buildObjective(ResultSet rs) throws SQLException {
        return new Objective(
                rs.getInt("id"),
                rs.getInt("mission_id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getInt("objective_order"),
                rs.getString("type"),
                rs.getString("reference"),
                rs.getInt("reward")
        );
    }

    @Override
    public void marcarObjetivoCompletado(String username, int objectiveId) {
        try (PreparedStatement ps = conn.prepareStatement(QueryHelper.createUpsertUserObjective())) {
            ps.setString(1, username);
            ps.setInt(2, objectiveId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void marcarMisionCompletada(String username, int missionId) {
        try (PreparedStatement ps = conn.prepareStatement(QueryHelper.createUpsertUserMission())) {
            ps.setString(1, username);
            ps.setInt(2, missionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void darEcts(String username, int cantidad) {
        try (PreparedStatement ps = conn.prepareStatement(QueryHelper.createAddEcts())) {
            ps.setInt(1, cantidad);
            ps.setString(2, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateGameState(String username, Integer missionId, Integer objectiveId) {
        try (PreparedStatement ps = conn.prepareStatement(QueryHelper.createUpdateGameState())) {
            ps.setObject(1, missionId);
            ps.setObject(2, objectiveId);
            ps.setString(3, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateGameStateObjective(String username, Integer objectiveId) {
        try (PreparedStatement ps = conn.prepareStatement(QueryHelper.createUpdateGameStateObjective())) {
            ps.setObject(1, objectiveId);
            ps.setString(2, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Integer> getObjectiveRewardShopIds(int objectiveId) {
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(QueryHelper.createSelectObjectiveRewardNames())) {
            ps.setInt(1, objectiveId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("id"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return ids;
    }

    @Override
    public boolean isMisionCompleta(String username, int missionId) {
        try (PreparedStatement ps = conn.prepareStatement(QueryHelper.createSelectMissionCompletionCheck())) {
            ps.setString(1, username);
            ps.setInt(2, missionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int done = rs.getInt("done");
                    return total > 0 && total == done;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    @Override
    public Mission getSiguienteMision(int missionId) {
        try (PreparedStatement ps = conn.prepareStatement(QueryHelper.createSelectNextMission())) {
            ps.setInt(1, missionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return buildMission(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Objective getPrimerObjetivoMision(int missionId) {
        try (PreparedStatement ps = conn.prepareStatement(QueryHelper.createSelectFirstObjectiveOfMission())) {
            ps.setInt(1, missionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return buildObjective(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Objective getSiguienteObjetivoPendiente(String username, int missionId) {
        try (PreparedStatement ps = conn.prepareStatement(QueryHelper.createSelectNextPendingObjective())) {
            ps.setString(1, username);
            ps.setInt(2, missionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return buildObjective(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Puzzle getPuzzleById(int puzzleId) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM puzzles WHERE id=?")) {
            ps.setInt(1, puzzleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return buildPuzzle(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<Puzzle> getPuzzlesByMission(int missionId) {
        List<Puzzle> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM puzzles WHERE mission_id=? ORDER BY id ASC")) {
            ps.setInt(1, missionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(buildPuzzle(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public UserGameState getEstadoJugador(String username) {
        try (PreparedStatement ps = conn.prepareStatement(QueryHelper.createSelectGameState())) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserGameState state = buildUserGameState(rs);
                    state.setCurrentMissionTitle(rs.getString("mission_title"));
                    state.setCurrentObjectiveTitle(rs.getString("objective_title"));
                    return state;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<String> getCompletedRewardObjectNames(String username) {
        List<String> names = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(QueryHelper.createSelectCompletedRewardObjectNames())) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    names.add(rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return names;
    }

    @Override
    public void updateHealth(String username, int delta) {
        try (PreparedStatement ps = conn.prepareStatement(QueryHelper.createUpdateHealth())) {
            ps.setInt(1, delta);
            ps.setString(2, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Dialogue> getDialogues(String npcId, Integer missionId, String triggerCondition) {
        List<Dialogue> result = new ArrayList<>();
        boolean hasMission = missionId != null;
        boolean hasTrigger = triggerCondition != null && !triggerCondition.isEmpty();
        String sql = QueryHelper.createSelectDialogues(hasMission, hasTrigger);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setString(idx++, npcId);
            if (hasMission) ps.setInt(idx++, missionId);
            if (hasTrigger) ps.setString(idx, triggerCondition);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(buildDialogue(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private Dialogue buildDialogue(ResultSet rs) throws SQLException {
        Dialogue d = new Dialogue();
        d.setId(rs.getInt("id"));
        d.setNpcId(rs.getString("npc_id"));
        d.setMissionId(rs.getObject("mission_id") == null ? null : rs.getInt("mission_id"));
        d.setObjectiveId(rs.getObject("objective_id") == null ? null : rs.getInt("objective_id"));
        d.setTriggerCondition(rs.getString("trigger_condition"));
        d.setSequenceOrder(rs.getInt("sequence_order"));
        d.setText(rs.getString("text"));
        return d;
    }

    private Puzzle buildPuzzle(ResultSet rs) throws SQLException {
        Puzzle p = new Puzzle();
        p.setId(rs.getInt("id"));
        p.setMissionId(rs.getInt("mission_id"));
        p.setObjectiveId(rs.getObject("objective_id") == null ? null : rs.getInt("objective_id"));
        p.setTitle(rs.getString("title"));
        p.setDescription(rs.getString("description"));
        p.setSolution(rs.getString("solution"));
        return p;
    }

    @Override
    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
