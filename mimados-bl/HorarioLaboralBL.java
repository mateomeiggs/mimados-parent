package pe.pucp.progra3.mimados-bl;

import pe.pucp.progra3.mimados-bl.exception.NegocioException;
import pe.pucp.progra3.mimados-model.HorarioLaboral;

import java.sql.SQLException;

public interface HorarioLaboralBL {
    void registrarHorario(HorarioLaboral horario) throws SQLException, NegocioException;
}
