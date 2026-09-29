package pe.pucp.progra3.mimados.bl.imp;

import pe.pucp.progra3.mimados-db-manager.TransactionContext;
import pe.pucp.progra3.mimados-bl.HorarioLaboralBL;
import pe.pucp.progra3.mimados-bl.exception.NegocioException;
import pe.pucp.progra3.mimados-dao.EmpleadoDAO;
import pe.pucp.progra3.mimados-dao.HorarioLaboralDAO;
import pe.pucp.progra3.mimados-dao.imp.EmpleadoDAOimp;
import pe.pucp.progra3.mimados-dao.imp.HorarioLaboralimp;
import pe.pucp.progra3.mimados-model.Empleado;
import pe.pucp.progra3.mimados-model.HorarioLaboral;

import java.sql.SQLException;
import java.util.List;

public class HorarioLaboralBLImpl implements HorarioLaboralBL {

    private final EmpleadoDAO empleadoDAO = new EmpleadoDAOimp();
    private final HorarioLaboralDAO horarioLaboralDAO = new HorarioLaboralimp();

    @Override
    public void registrarHorario(HorarioLaboral horario) throws SQLException, NegocioException {

        try {
            if (horario.getHoraInicio() == null || horario.getHoraFin() == null
                    || !horario.getHoraInicio().isBefore(horario.getHoraFin())) {
                throw new NegocioException("La hora de inicio debe ser menor a la hora de fin.");
            }

            int idEmpleado = horario.getEmpleado().getUsuario().getId();

            Empleado empleado = buscarEmpleadoActivo(idEmpleado);
            if (empleado == null) {
                throw new NegocioException("El empleado con ID " + idEmpleado + " no existe o no esta activo.");
            }

            List<HorarioLaboral> horariosActuales = horarioLaboralDAO.horarios_empleado(idEmpleado);
            for (HorarioLaboral existente : horariosActuales) {
                boolean seCruzan = existente.getActivo()
                        && existente.getDiaSemana() == horario.getDiaSemana()
                        && horario.getHoraInicio().isBefore(existente.getHoraFin())
                        && horario.getHoraFin().isAfter(existente.getHoraInicio());

                if (seCruzan) {
                    throw new NegocioException("El horario se cruza con uno ya registrado para ese dia.");
                }
            }

            horarioLaboralDAO.insertar_horario(horario);

            TransactionContext.commit();

        } catch (SQLException | NegocioException e) {
            TransactionContext.rollback();
            throw e;
        } finally {
            TransactionContext.close();
        }
    }

    private Empleado buscarEmpleadoActivo(int idEmpleado) throws SQLException {
        List<Empleado> empleados = empleadoDAO.listar_empleados();
        for (Empleado e : empleados) {
            if (e.getUsuario().getId() == idEmpleado && e.getActivo()) {
                return e;
            }
        }
        return null;
    }
}
