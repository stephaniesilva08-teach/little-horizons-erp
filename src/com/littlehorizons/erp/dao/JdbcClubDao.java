package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.model.Membership;
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
 * Data access object for clubs and their memberships.
 */
public class JdbcClubDao implements ClubDao {

    private final ConnectionProvider connectionProvider;

    public JdbcClubDao() {
        this(Database.provider());
    }

    public JdbcClubDao(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    private static final String CLUB_SELECT =
            "SELECT c.id_club, c.nombre_club, c.descripcion, c.id_profesor_supervisor, "
            + "c.capacidad_maxima, c.horario_reunion, c.esta_activo, c.fecha_creacion, "
            + "c.fecha_actualizacion, CONCAT_WS(' ', p.primer_nombre, p.segundo_nombre, "
            + "p.primer_apellido, p.segundo_apellido) AS supervisor_name "
            + "FROM clubes c LEFT JOIN profesores p "
            + "ON p.id_profesor = c.id_profesor_supervisor ";

    private static final String MEMBERSHIP_SELECT =
            "SELECT mc.id_membresia, mc.id_estudiante, mc.id_club, mc.fecha_ingreso, "
            + "mc.estado_membresia, mc.fecha_retiro, mc.motivo_retiro, mc.fecha_creacion, "
            + "CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, "
            + "e.segundo_apellido) AS student_name, c.nombre_club "
            + "FROM membresias_club mc "
            + "INNER JOIN estudiantes e ON e.id_estudiante = mc.id_estudiante "
            + "INNER JOIN clubes c ON c.id_club = mc.id_club ";

    public int insert(Club club) {
        String sql = "INSERT INTO clubes (nombre_club, descripcion, "
                + "id_profesor_supervisor, capacidad_maxima, horario_reunion, esta_activo) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, club.getName());
            ps.setString(2, club.getDescription());
            if (club.getIdTeacher() > 0) {
                ps.setInt(3, club.getIdTeacher());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setInt(4, club.getMaxCapacity());
            ps.setString(5, club.getSchedule());
            ps.setBoolean(6, club.isActive());
            ps.executeUpdate();
            int id = DaoSupport.generatedKey(ps);
            club.setIdClub(id);
            return id;
        } catch (SQLException e) {
            LoggerUtil.error("Could not insert club", e);
            throw new DataAccessException("No se pudo registrar el club.", e);
        }
    }

    public void update(Club club) {
        String sql = "UPDATE clubes SET nombre_club = ?, descripcion = ?, "
                + "id_profesor_supervisor = ?, capacidad_maxima = ?, horario_reunion = ?, "
                + "esta_activo = ? WHERE id_club = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, club.getName());
            ps.setString(2, club.getDescription());
            if (club.getIdTeacher() > 0) {
                ps.setInt(3, club.getIdTeacher());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setInt(4, club.getMaxCapacity());
            ps.setString(5, club.getSchedule());
            ps.setBoolean(6, club.isActive());
            ps.setInt(7, club.getIdClub());
            ps.executeUpdate();
        } catch (SQLException e) {
            LoggerUtil.error("Could not update club", e);
            throw new DataAccessException("No se pudo actualizar el club.", e);
        }
    }

    public boolean delete(int idClub) {
        String sql = "DELETE FROM clubes WHERE id_club = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idClub);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Could not delete club", e);
            throw new DataAccessException("No se pudo eliminar el club.", e);
        }
    }

    public Club findById(int idClub) {
        String sql = CLUB_SELECT + "WHERE c.id_club = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idClub);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapClub(rs) : null;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not find club", e);
            throw new DataAccessException("No se pudo consultar el club.", e);
        }
    }

    public List<Club> findAll() {
        return queryClubList(CLUB_SELECT + "ORDER BY c.nombre_club");
    }

    public List<Club> findActive() {
        return queryClubList(CLUB_SELECT + "WHERE c.esta_activo = TRUE "
                + "ORDER BY c.nombre_club");
    }

    public int insertMembership(Membership membership) {
        String sql = "INSERT INTO membresias_club (id_estudiante, id_club, "
                + "fecha_ingreso, estado_membresia) VALUES (?, ?, ?, ?)";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, membership.getIdStudent());
            ps.setInt(2, membership.getIdClub());
            DaoSupport.setDate(ps, 3, membership.getEntryDate());
            ps.setString(4, membership.getState());
            ps.executeUpdate();
            int id = DaoSupport.generatedKey(ps);
            membership.setIdMembership(id);
            return id;
        } catch (SQLException e) {
            LoggerUtil.error("Could not insert membership", e);
            throw new DataAccessException("No se pudo registrar la membresía.", e);
        }
    }

    public void updateMembership(Membership membership) {
        String sql = "UPDATE membresias_club SET estado_membresia = ?, "
                + "fecha_retiro = ?, motivo_retiro = ? WHERE id_membresia = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, membership.getState());
            if ("Retirada".equalsIgnoreCase(membership.getState())) {
                ps.setDate(2, java.sql.Date.valueOf(java.time.LocalDate.now()));
            } else {
                ps.setNull(2, java.sql.Types.DATE);
            }
            ps.setString(3, membership.getWithdrawalReason());
            ps.setInt(4, membership.getIdMembership());
            ps.executeUpdate();
        } catch (SQLException e) {
            LoggerUtil.error("Could not update membership", e);
            throw new DataAccessException("No se pudo actualizar la membresía.", e);
        }
    }

    public boolean deleteMembership(int idMembership) {
        String sql = "DELETE FROM membresias_club WHERE id_membresia = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idMembership);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Could not delete membership", e);
            throw new DataAccessException("No se pudo eliminar la membresía.", e);
        }
    }

    public Membership findMembershipById(int idMembership) {
        String sql = MEMBERSHIP_SELECT + "WHERE mc.id_membresia = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idMembership);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapMembership(rs) : null;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not find membership", e);
            throw new DataAccessException("No se pudo consultar la membresía.", e);
        }
    }

    public List<Membership> findMembershipsByClub(int idClub) {
        return queryMembershipList(MEMBERSHIP_SELECT + "WHERE mc.id_club = ? "
                + "ORDER BY student_name", idClub);
    }

    public List<Membership> findMembershipsByStudent(int idStudent) {
        return queryMembershipList(MEMBERSHIP_SELECT + "WHERE mc.id_estudiante = ? "
                + "ORDER BY c.nombre_club", idStudent);
    }

    public List<Membership> findAllMemberships() {
        return queryMembershipList(MEMBERSHIP_SELECT + "ORDER BY c.nombre_club, student_name", 0);
    }

    public int countActiveMembers(int idClub) {
        String sql = "SELECT COUNT(*) FROM membresias_club "
                + "WHERE id_club = ? AND estado_membresia = 'Activa'";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idClub);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not count club members", e);
            throw new DataAccessException("No se pudo contar los miembros del club.", e);
        }
    }

    private List<Club> queryClubList(String sql) {
        List<Club> clubs = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                clubs.add(mapClub(rs));
            }
            return clubs;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list clubs", e);
            throw new DataAccessException("No se pudieron listar los clubes.", e);
        }
    }

    private List<Membership> queryMembershipList(String sql, int parameter) {
        List<Membership> list = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            if (parameter > 0) {
                ps.setInt(1, parameter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapMembership(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list memberships", e);
            throw new DataAccessException("No se pudieron listar las membresías.", e);
        }
    }

    private Club mapClub(ResultSet rs) throws SQLException {
        Club c = new Club();
        c.setIdClub(rs.getInt("id_club"));
        c.setName(rs.getString("nombre_club"));
        c.setDescription(rs.getString("descripcion"));
        c.setIdTeacher(DaoSupport.getInt(rs, "id_profesor_supervisor"));
        c.setMaxCapacity(rs.getInt("capacidad_maxima"));
        c.setSchedule(rs.getString("horario_reunion"));
        c.setActive(rs.getBoolean("esta_activo"));
        c.setCreationDate(DaoSupport.getTimestamp(rs, "fecha_creacion"));
        c.setUpdateDate(DaoSupport.getTimestamp(rs, "fecha_actualizacion"));
        c.setSupervisorName(rs.getString("supervisor_name"));
        return c;
    }

    private Membership mapMembership(ResultSet rs) throws SQLException {
        Membership m = new Membership();
        m.setIdMembership(rs.getInt("id_membresia"));
        m.setIdStudent(rs.getInt("id_estudiante"));
        m.setIdClub(rs.getInt("id_club"));
        m.setEntryDate(DaoSupport.getDate(rs, "fecha_ingreso"));
        m.setState(rs.getString("estado_membresia"));
        m.setWithdrawalReason(rs.getString("motivo_retiro"));
        m.setCreationDate(DaoSupport.getTimestamp(rs, "fecha_creacion"));
        m.setStudentName(rs.getString("student_name"));
        m.setClubName(rs.getString("nombre_club"));
        return m;
    }
}
