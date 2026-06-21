package edu.upc.dsa.db;

import edu.upc.dsa.db.util.FactorySession;
import edu.upc.dsa.db.util.Session;
import edu.upc.dsa.models.CanCompleteObjective;
import edu.upc.dsa.models.Mission;
import edu.upc.dsa.models.Objective;
import edu.upc.dsa.models.ObjectiveResult;
import edu.upc.dsa.models.Producto;
import edu.upc.dsa.models.Puzzle;
import edu.upc.dsa.models.UserGameState;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GameProgressDAO {

    public ObjectiveResult completarObjetivo(String username, int objectiveId) {
        Session session = null;
        try {
            session = FactorySession.openSession();

            Objective objetivo = session.get(Objective.class, objectiveId);
            if (objetivo == null) throw new RuntimeException("Objetivo no encontrado: " + objectiveId);

            CanCompleteObjective validation = puedeCompletar(session, username, objetivo);
            if (!validation.isCanComplete()) {
                throw new RuntimeException(validation.getCode() + ": " + validation.getMessage());
            }

            int missionId = objetivo.getMissionId();

            session.marcarObjetivoCompletado(username, objectiveId);

            if (objetivo.getReward() > 0) {
                session.darEcts(username, objetivo.getReward());
            }

            List<Integer> rewardShopIds = session.getObjectiveRewardShopIds(objectiveId);
            for (Integer shopId : rewardShopIds) {
                session.addProductToInventory(username, shopId);
            }

            ObjectiveResult result = new ObjectiveResult();
            result.setRewardEcts(objetivo.getReward());

            boolean misionCompleta = session.isMisionCompleta(username, missionId);
            result.setMissionCompleted(misionCompleta);

            if (misionCompleta) {
                session.marcarMisionCompletada(username, missionId);

                Mission siguienteMision = session.getSiguienteMision(missionId);
                if (siguienteMision != null) {
                    // Saltarse IR_ZONA al principio de la nueva misión
                    Objective primerObjetivo = primerObjetivoNoIrZona(session, username, siguienteMision.getId());
                    session.updateGameState(username, siguienteMision.getId(),
                            primerObjetivo != null ? primerObjetivo.getId() : null);

                    result.setNextMissionId(siguienteMision.getId());
                    result.setNextMissionTitle(siguienteMision.getTitle());
                    result.setNextObjectiveId(primerObjetivo != null ? primerObjetivo.getId() : null);
                    result.setNextObjectiveTitle(primerObjetivo != null ? primerObjetivo.getTitle() : null);
                }
            } else {
                Objective siguiente = session.getSiguienteObjetivoPendiente(username, missionId);
                if (siguiente != null) {
                    session.updateGameStateObjective(username, siguiente.getId());
                    result.setNextMissionId(missionId);
                    result.setNextObjectiveId(siguiente.getId());
                    result.setNextObjectiveTitle(siguiente.getTitle());
                }
            }

            return result;

        } finally {
            if (session != null) session.close();
        }
    }

    public ObjectiveResult autoCompletarObtencion(String username) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            UserGameState state = session.getEstadoJugador(username);
            if (state == null || state.getCurrentObjectiveId() == null) return null;
            Objective objetivo = session.get(Objective.class, state.getCurrentObjectiveId());
            if (objetivo == null || !"OBTENER_OBJETO".equals(objetivo.getType())) return null;
            return completarObjetivo(username, objetivo.getId());
        } finally {
            if (session != null) session.close();
        }
    }

    public ObjectiveResult autoCompletarUso(String username, String slug) {
        Session session = null;
        try {
            session = FactorySession.openSession();

            UserGameState state = session.getEstadoJugador(username);
            if (state == null || state.getCurrentObjectiveId() == null) return null;

            Objective objetivo = session.get(Objective.class, state.getCurrentObjectiveId());
            if (objetivo == null || !"OBTENER_OBJETO".equals(objetivo.getType())) return null;

            String ref = normalizeKey(objetivo.getReference());
            if (!ref.equals(normalizeKey(slug))) return null;

            return completarObjetivo(username, objetivo.getId());
        } finally {
            if (session != null) session.close();
        }
    }

    public ObjectiveResult autoCompletarCompra(String username, int productId) {
        Session session = null;
        try {
            session = FactorySession.openSession();

            Producto producto = session.get(Producto.class, productId);
            UserGameState state = session.getEstadoJugador(username);
            if (producto == null || state == null || state.getCurrentObjectiveId() == null) {
                return null;
            }

            Objective objetivo = session.get(Objective.class, state.getCurrentObjectiveId());
            if (objetivo == null || !"COMPRAR_OBJETO".equals(objetivo.getType())) {
                return null;
            }

            String objectiveKey = normalizeKey(objetivo.getReference());
            if (!matchesObject(objectiveKey, producto.getNombre())) {
                return null;
            }

            return completarObjetivo(username, objetivo.getId());
        } finally {
            if (session != null) session.close();
        }
    }

    public CanCompleteObjective puedeCompletarObjetivo(String username, int objectiveId) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            Objective objetivo = session.get(Objective.class, objectiveId);
            if (objetivo == null) {
                return cannot("OBJECTIVE_NOT_FOUND", "Objetivo no encontrado.", Collections.<String>emptyList());
            }
            return puedeCompletar(session, username, objetivo);
        } finally {
            if (session != null) session.close();
        }
    }

    public ObjectiveResult resolverPuzzle(String username, int puzzleId, String respuesta) {
        Session session = null;
        try {
            session = FactorySession.openSession();

            Puzzle puzzle = session.getPuzzleById(puzzleId);
            if (puzzle == null) throw new RuntimeException("Puzzle no encontrado: " + puzzleId);

            if (!isCorrectSolution(puzzle.getSolution(), respuesta)) {
                throw new RuntimeException("RESPUESTA_INCORRECTA");
            }

            return completarObjetivo(username, puzzle.getObjectiveId());

        } finally {
            if (session != null) session.close();
        }
    }

    public UserGameState getEstado(String username) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            return session.getEstadoJugador(username);
        } finally {
            if (session != null) session.close();
        }
    }

    private Objective primerObjetivoNoIrZona(Session session, String username, int missionId) {
        Objective obj = session.getPrimerObjetivoMision(missionId);
        int maxSaltos = 10;
        while (obj != null && "IR_ZONA".equals(obj.getType()) && maxSaltos-- > 0) {
            session.marcarObjetivoCompletado(username, obj.getId());
            obj = session.getSiguienteObjetivoPendiente(username, missionId);
        }
        return obj;
    }

    private CanCompleteObjective puedeCompletar(Session session, String username, Objective objetivo) {
        UserGameState state = session.getEstadoJugador(username);
        if (state == null) {
            return cannot("GAME_STATE_NOT_FOUND", "Estado de juego no encontrado.", Collections.<String>emptyList());
        }

        if (state.getCurrentObjectiveId() == null || !state.getCurrentObjectiveId().equals(objetivo.getId())) {
            return cannot("OBJECTIVE_NOT_ACTIVE", "Este no es el objetivo activo del jugador.", Collections.<String>emptyList());
        }

        List<String> required = requiredObjects(objetivo);
        List<String> missing = new ArrayList<>();
        for (String objectKey : required) {
            if (!hasObject(session, username, objectKey)) {
                missing.add(objectKey);
            }
        }

        if (!missing.isEmpty()) {
            return cannot("MISSING_REQUIREMENTS", "Faltan objetos necesarios para completar el objetivo.", missing);
        }

        return new CanCompleteObjective(true, "OK", "El objetivo se puede completar.", Collections.<String>emptyList());
    }

    private List<String> requiredObjects(Objective objetivo) {
        Integer id = objetivo.getId();
        if (id == null) {
            return Collections.emptyList();
        }
        if (id == 15) {
            return Arrays.asList("usb_amarillo");
        }
        if (id == 16) {
            return Arrays.asList("fragmento_codigo1", "fragmento_codigo2");
        }
        if (id == 17) {
            return Arrays.asList("bateria_seguridad", "tarjeta_acceso", "codigo_completo");
        }
        return Collections.emptyList();
    }

    private boolean hasObject(Session session, String username, String objectKey) {
        String required = normalizeKey(objectKey);

        for (Producto product : session.getInventory(username)) {
            String slug = product.getSlug();
            String match = (slug != null && !slug.isEmpty()) ? slug : normalizeKey(product.getNombre());
            if (required.equals(match)) return true;
        }

        for (String rewardObjectName : session.getCompletedRewardObjectNames(username)) {
            if (required.equals(normalizeKey(rewardObjectName))) return true;
        }

        return false;
    }

    private boolean matchesObject(String required, String actualName) {
        return required.equals(normalizeKey(actualName));
    }

    public boolean isCorrectSolutionPublic(String solution, String respuesta) {
        return isCorrectSolution(solution, respuesta);
    }

    private boolean isCorrectSolution(String solution, String respuesta) {
        if (solution == null || respuesta == null) return false;
        String resp = respuesta.trim();
        for (String valid : solution.split("\\|")) {
            if (valid.trim().equalsIgnoreCase(resp)) return true;
        }
        return false;
    }

    private CanCompleteObjective cannot(String code, String message, List<String> missingRequirements) {
        return new CanCompleteObjective(false, code, message, missingRequirements);
    }

    private String normalizeKey(String value) {
        if (value == null) {
            return "";
        }

        String normalized = Normalizer.normalize(value.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        return normalized;
    }
}
