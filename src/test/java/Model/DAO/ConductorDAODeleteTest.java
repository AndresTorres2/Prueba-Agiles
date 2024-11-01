package Model.DAO;

import Model.Entity.Conductor;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConductorDAODeleteTest {

    private ConductorDAO conductorDAO;
    private Conductor conductor;

    @Before
    public void setUp() {
        conductorDAO = new ConductorDAO();
        conductor = new Conductor(1, "Cristian", "Hernandez", "andres@gmail.com",
                "0991935087", "12345");
        conductorDAO.guardarConductor(conductor);

    }

    @Test
    public void given_Conductor_when_Delete_then_RemovedSuccessfully() {
        conductorDAO.eliminarConductor(String.valueOf(conductor.getId()));
        Conductor conductorEliminado = conductorDAO.buscarPorId(String.valueOf(conductor.getId()));

        assertNull("El conductor ha sido eliminado correctamente.", conductorEliminado);
    }

}