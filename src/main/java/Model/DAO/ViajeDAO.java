package Model.DAO;

import Model.Entity.Viaje;
import Model.Entity.Ruta;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;
import org.hibernate.exception.ConstraintViolationException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.*;

public class ViajeDAO extends GenericDAO{
    public ViajeDAO() {
        super();
    }
    private final HashMap<Integer, Viaje> viajes = new HashMap<>();

    public void crearViaje(Integer id, Viaje viaje) {
        viajes.put(id, viaje);
    }
    public boolean existeViaje(Integer id) {
        return viajes.containsKey(id);
    }
    public void eliminarViaje(Integer id) {
        viajes.remove(id);
    }
    public void actualizarViaje(Integer id, Viaje nuevoViaje) {
        viajes.put(id, nuevoViaje);
    }
    public Viaje obtenerViaje(Integer id) {
        return viajes.get(id);
    }


    public void crearViajeEnDB(Viaje viaje) {
        executeInTransaction(() -> em.persist(viaje));
    }

    private void executeInTransaction(Runnable action) {
        try {
            em.getTransaction().begin();
            action.run();
            em.getTransaction().commit();
        } catch (Exception e) {
            rollbackIfActive();
            e.printStackTrace();
        }
    }

    private void rollbackIfActive() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }


    public boolean existeViajeEnDB(Integer id) {
        return em.find(Viaje.class, id) != null;
    }

    public void eliminarViajeEnDB(Integer id)  {
        try {
            Viaje viaje = em.find(Viaje.class, id);
            if (viaje != null) {
                em.getTransaction().begin();
                em.remove(viaje);
                em.getTransaction().commit();
            }
        } catch (PersistenceException e) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("No se puede eliminar el viaje porque hay reservas asociadas.");
        }
        catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        }
    }

    public void actualizarViajeEnDB(Viaje nuevoViaje) {
        try {
            em.getTransaction().begin();
            em.merge(nuevoViaje);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        }
    }

    public Viaje obtenerViajeEnDB(Integer id) {
        return em.find(Viaje.class, id);
    }

    public List<Viaje> obtenerTodosLosViajes() {
        List<Viaje> viajes = new ArrayList<>();
        try {
            em.getTransaction().begin();
            Query query = em.createQuery("SELECT v FROM Viaje v", Viaje.class);
            viajes = query.getResultList();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        }
        return viajes;
    }

    public List<Object[]> listarViajesPorJornada(String jornada) {
        List<Object[]> resultList = new ArrayList<>();
        List<Object[]> processedList = new ArrayList<>();
        try {
            String sql = "SELECT DISTINCT v.horaDeSalida, r.origen, r.destino, v2.id " +
                    "FROM Viajes v " +
                    "JOIN Rutas r ON v.rutaId = r.id " +
                    "JOIN Viajes v2 ON v2.rutaId = v.rutaId " +
                    "               AND v2.jornada = :jornada " +
                    "               AND v2.horaDeSalida = v.horaDeSalida " +
                    "WHERE v.jornada = :jornada " +
                    "AND v.rutaId IN ( " +
                    "    SELECT DISTINCT rutaId " +
                    "    FROM Viajes " +
                    "    WHERE jornada = :jornada " +
                    ") " +
                    "ORDER BY r.origen, r.destino, v.horaDeSalida, v2.id";

            Query query = em.createNativeQuery(sql);
            query.setParameter("jornada", jornada);

            resultList = query.getResultList();

            // Proceso para concatenar IDs manualmente
            Map<String, List<String>> groupedData = new LinkedHashMap<>();
            for (Object[] row : resultList) {
                String key = row[0] + "|" + row[1] + "|" + row[2]; // horaDeSalida|origen|destino
                String idViaje = row[3].toString();

                groupedData.computeIfAbsent(key, k -> new ArrayList<>()).add(idViaje);
            }

            // Convertir el mapa agrupado en la lista final
            for (Map.Entry<String, List<String>> entry : groupedData.entrySet()) {
                String[] keyParts = entry.getKey().split("\\|");
                String horaDeSalida = keyParts[0];
                String origen = keyParts[1];
                String destino = keyParts[2];
                String concatenatedIds = String.join(",", entry.getValue());

                processedList.add(new Object[]{horaDeSalida, origen, destino, concatenatedIds});
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return processedList;
    }


    public Viaje obtenerViajePorCodigo(int codigo) {
        try {
            Viaje viaje = em.find(Viaje.class, codigo);
            if (viaje != null) {
                em.refresh(viaje);
            }
            return viaje;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<String> obtenerCorreosPasajerosPorViaje(int viajeId) {
        List<String> correosPasajeros = new ArrayList<>();
        try {
            String sql = "SELECT e.email FROM Reserva r JOIN r.estudiante e WHERE r.viaje.id = :viajeId";

            Query query = em.createQuery(sql);
            query.setParameter("viajeId", viajeId);

            correosPasajeros = query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return correosPasajeros;
    }

    public int[] convertirIdsAEnteros(String[] idsViajesSeleccionados) {
        if (idsViajesSeleccionados != null) {
            return Arrays.stream(idsViajesSeleccionados)
                    .mapToInt(Integer::parseInt)
                    .toArray();
        }
        return new int[0];
    }
    public List<Viaje> obtenerListaDeViajes(String[] idsViajes) {
        List<Viaje> listaViajes = new ArrayList<>();
        for (int id : convertirIdsAEnteros(idsViajes)) {
            listaViajes.add(obtenerViajePorCodigo(id));
        }
        return listaViajes;
    }


    public List<Integer> convertirCadenaAListaDeIds(String idsViajes) {
        List<Integer> viajesIdList = new ArrayList<>();

        if (idsViajes != null && !idsViajes.isEmpty()) {
            String[] idArray = idsViajes.split(",");
            for (String id : idArray) {
                try {
                    viajesIdList.add(Integer.parseInt(id.trim()));
                } catch (NumberFormatException e) {
                    System.out.println("ID inválido: " + id);
                }
            }
        }
        return viajesIdList;
    }

    public List<Viaje> obtenerViajesPorIds(String idsViajes) {
        List<Viaje> viajesList = new ArrayList<>();
        for (Integer idViaje : convertirCadenaAListaDeIds(idsViajes)) {
            viajesList.add(obtenerViajePorCodigo(idViaje));
        }

        return viajesList;
    }


    public List<Viaje> obtenerListaDeViajesPorConductor(int idConductor) {
        try {
            String sql = "SELECT v FROM Viaje v WHERE v.conductor.id = :idConductor";
            Query query = em.createQuery(sql, Viaje.class);
            query.setParameter("idConductor", idConductor);
            List<Viaje> viajes = query.getResultList();

            for (Viaje viaje : viajes) {
                em.refresh(viaje);
            }

            return viajes;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


    public List<Viaje> obtenerViajesPorIdsYFechas(String idsViajes, List<LocalDate> fechas) {
        List<Viaje> viajesList = new ArrayList<>();

        for (Integer idViaje : convertirCadenaAListaDeIds(idsViajes)) {
            Viaje viaje = obtenerViajePorCodigo(idViaje);
            System.out.println("Viaje encontrado POR FUERA - Fecha: " + viaje.getFecha() +"formato:" +viaje.getFecha().getClass().getName());
            if (viaje != null && fechas.contains(viaje.getFecha().toLocalDate())) {
                System.out.println("Viaje encontrado - Fecha: " + viaje.getFecha().toLocalDate());
                viajesList.add(viaje);
            }
        }

        return viajesList;
    }


}
