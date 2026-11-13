package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Event;
import com.littlehorizons.erp.model.Participant;
import com.littlehorizons.erp.util.ConnectionProvider;
import com.littlehorizons.erp.util.Database;
import com.littlehorizons.erp.util.DataAccessException;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for events and participants.
 */
public class JdbcEventDao implements EventDao {

    private final ConnectionProvider connectionProvider;

    public JdbcEventDao() {
        this(Database.provider());
    }

    public JdbcEventDao(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    private static final String EVENT_SELECT =
            "SELECT ev.id_evento, ev.nombre_evento, ev.descripcion, ev.tipo_evento, "
            + "ev.fecha_evento, ev.lugar, ev.tiene_competencia, ev.id_organizador, "
            + "ev.esta_activo, ev.fecha_creacion, CONCAT_WS(' ', NULLIF(TRIM(p.primer_nombre), ''), "
            + "NULLIF(TRIM(p.segundo_nombre), ''), NULLIF(TRIM(p.primer_apellido), ''), NULLIF(TRIM(p.segundo_apellido), '')) AS organizer_name "
            + "FROM eventos ev LEFT JOIN profesores p ON p.id_profesor = ev.id_organizador ";

    private static final String PARTICIPANT_SELECT =
            "SELECT pe.id_participacion, pe.id_evento, pe.id_estudiante, "
            + "pe.rol_participacion, pe.posicion_obtenida, pe.observaciones, "
            + "pe.fecha_creacion, CONCAT_WS(' ', NULLIF(TRIM(e.primer_nombre), ''), NULLIF(TRIM(e.segundo_nombre), ''), "
            + "NULLIF(TRIM(e.primer_apellido), ''), NULLIF(TRIM(e.segundo_apellido), '')) AS student_name, ev.nombre_evento "
            + "FROM participantes_evento pe "
            + "INNER JOIN estudiantes e ON e.id_estudiante = pe.id_estudiante "
            + "INNER JOIN eventos ev ON ev.id_evento = pe.id_evento ";

    public int insert(Event event) {
        String sql = "INSERT INTO eventos (nombre_evento, descripcion, tipo_evento, "
                + "fecha_evento, lugar, tiene_competencia, id_organizador, esta_activo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, event.getName());
            ps.setString(2, event.getDescription());
            ps.setString(3, event.getEventType());
            DaoSupport.setDate(ps, 4, event.getEventDate());
            ps.setString(5, event.getPlace());
            ps.setBoolean(6, event.isHasCompetition());
            if (event.getOrganizerId() > 0) {
                ps.setInt(7, event.getOrganizerId());
            } else {
                ps.setNull(7, java.sql.Types.INTEGER);
            }
            ps.setBoolean(8, event.isActive());
            ps.executeUpdate();
            int id = DaoSupport.generatedKey(ps);
            event.setIdEvent(id);
            return id;
        } catch (SQLException e) {
            LoggerUtil.error("Could not insert event", e);
            throw new DataAccessException("No se pudo registrar el evento.", e);
        }
    }

    public void update(Event event) {
        String sql = "UPDATE eventos SET nombre_evento = ?, descripcion = ?, "
                + "tipo_evento = ?, fecha_evento = ?, lugar = ?, tiene_competencia = ?, "
                + "id_organizador = ?, esta_activo = ? WHERE id_evento = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, event.getName());
            ps.setString(2, event.getDescription());
            ps.setString(3, event.getEventType());
            DaoSupport.setDate(ps, 4, event.getEventDate());
            ps.setString(5, event.getPlace());
            ps.setBoolean(6, event.isHasCompetition());
            if (event.getOrganizerId() > 0) {
                ps.setInt(7, event.getOrganizerId());
            } else {
                ps.setNull(7, java.sql.Types.INTEGER);
            }
            ps.setBoolean(8, event.isActive());
            ps.setInt(9, event.getIdEvent());
            DaoSupport.requireUpdated(ps.executeUpdate(), "el evento");
        } catch (SQLException e) {
            LoggerUtil.error("Could not update event", e);
            throw new DataAccessException("No se pudo actualizar el evento.", e);
        }
    }

    public boolean delete(int idEvent) {
        String sql = "DELETE FROM eventos WHERE id_evento = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idEvent);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Could not delete event", e);
            throw new DataAccessException("No se pudo eliminar el evento.", e);
        }
    }

    public Event findById(int idEvent) {
        String sql = EVENT_SELECT + "WHERE ev.id_evento = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idEvent);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapEvent(rs) : null;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not find event", e);
            throw new DataAccessException("No se pudo consultar el evento.", e);
        }
    }

    public List<Event> findAll() {
        return queryEvents(EVENT_SELECT + "ORDER BY ev.fecha_evento DESC");
    }

    public List<Event> findActive() {
        return queryEvents(EVENT_SELECT + "WHERE ev.esta_activo = TRUE "
                + "ORDER BY ev.fecha_evento");
    }

    public int insertParticipant(Participant participant) {
        String sql = "INSERT INTO participantes_evento (id_evento, id_estudiante, "
                + "rol_participacion, posicion_obtenida, observaciones) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, participant.getIdEvent());
            ps.setInt(2, participant.getIdStudent());
            ps.setString(3, participant.getRole());
            ps.setString(4, participant.getPosition());
            ps.setString(5, participant.getObservations());
            ps.executeUpdate();
            int id = DaoSupport.generatedKey(ps);
            participant.setIdParticipation(id);
            return id;
        } catch (SQLException e) {
            LoggerUtil.error("Could not insert participant", e);
            throw new DataAccessException("No se pudo registrar el participante.", e);
        }
    }

    public boolean deleteParticipant(int idParticipation) {
        String sql = "DELETE FROM participantes_evento WHERE id_participacion = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idParticipation);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Could not delete participant", e);
            throw new DataAccessException("No se pudo eliminar el participante.", e);
        }
    }

    public List<Participant> findParticipantsByEvent(int idEvent) {
        return queryParticipants(PARTICIPANT_SELECT + "WHERE pe.id_evento = ? "
                + "ORDER BY student_name", idEvent);
    }

    public List<Participant> findParticipantsByStudent(int idStudent) {
        return queryParticipants(PARTICIPANT_SELECT + "WHERE pe.id_estudiante = ? "
                + "ORDER BY ev.fecha_evento DESC", idStudent);
    }

    private List<Event> queryEvents(String sql) {
        List<Event> events = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                events.add(mapEvent(rs));
            }
            return events;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list events", e);
            throw new DataAccessException("No se pudieron listar los eventos.", e);
        }
    }

    private List<Participant> queryParticipants(String sql, int parameter) {
        List<Participant> list = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, parameter);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapParticipant(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list participants", e);
            throw new DataAccessException("No se pudieron listar los participantes.", e);
        }
    }

    private Event mapEvent(ResultSet rs) throws SQLException {
        Event e = new Event();
        e.setIdEvent(rs.getInt("id_evento"));
        e.setName(rs.getString("nombre_evento"));
        e.setDescription(rs.getString("descripcion"));
        e.setEventType(rs.getString("tipo_evento"));
        e.setEventDate(DaoSupport.getDate(rs, "fecha_evento"));
        e.setPlace(rs.getString("lugar"));
        e.setHasCompetition(rs.getBoolean("tiene_competencia"));
        e.setOrganizerId(DaoSupport.getInt(rs, "id_organizador"));
        e.setActive(rs.getBoolean("esta_activo"));
        e.setCreationDate(DaoSupport.getTimestamp(rs, "fecha_creacion"));
        e.setOrganizerName(rs.getString("organizer_name"));
        return e;
    }

    private Participant mapParticipant(ResultSet rs) throws SQLException {
        Participant p = new Participant();
        p.setIdParticipation(rs.getInt("id_participacion"));
        p.setIdEvent(rs.getInt("id_evento"));
        p.setIdStudent(rs.getInt("id_estudiante"));
        p.setRole(rs.getString("rol_participacion"));
        p.setPosition(rs.getString("posicion_obtenida"));
        p.setObservations(rs.getString("observaciones"));
        p.setCreationDate(DaoSupport.getTimestamp(rs, "fecha_creacion"));
        p.setStudentName(rs.getString("student_name"));
        p.setEventName(rs.getString("nombre_evento"));
        return p;
    }
}
