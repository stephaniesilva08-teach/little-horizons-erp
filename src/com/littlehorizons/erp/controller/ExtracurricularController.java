package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.ClubDao;
import com.littlehorizons.erp.dao.EventDao;
import com.littlehorizons.erp.dao.JdbcEventDao;
import com.littlehorizons.erp.dao.JdbcClubDao;
import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.model.Event;
import com.littlehorizons.erp.model.Membership;
import com.littlehorizons.erp.model.Participant;
import com.littlehorizons.erp.util.Validator;

import java.util.List;

/**
 * Business rules for clubs, memberships, events and participants.
 */
public class ExtracurricularController {

    private final ClubDao clubDao;
    private final EventDao eventDao;

    public ExtracurricularController() {
        this(new JdbcClubDao(), new JdbcEventDao());
    }

    public ExtracurricularController(ClubDao clubDao, EventDao eventDao) {
        this.clubDao = clubDao;
        this.eventDao = eventDao;
    }

    public int createClub(Club club) {
        validateClub(club);
        return clubDao.insert(club);
    }

    public void updateClub(Club club) {
        if (club.getIdClub() <= 0) {
            throw new IllegalArgumentException("Falta el identificador del club.");
        }
        validateClub(club);
        clubDao.update(club);
    }

    public void removeClub(int idClub) {
        if (!clubDao.delete(idClub)) {
            throw new IllegalArgumentException("No se encontró el club a eliminar.");
        }
    }

    public List<Club> listClubs() {
        return clubDao.findAll();
    }

    public List<Club> listActiveClubs() {
        return clubDao.findActive();
    }

    public Club findClub(int idClub) {
        return clubDao.findById(idClub);
    }

    public int enrollInClub(Membership membership) {
        if (membership.getIdStudent() <= 0 || membership.getIdClub() <= 0) {
            throw new IllegalArgumentException("Estudiante y club son obligatorios.");
        }
        if (!Validator.isValidDate(membership.getEntryDate())) {
            throw new IllegalArgumentException("La fecha de ingreso es inválida.");
        }
        Club club = clubDao.findById(membership.getIdClub());
        if (club == null) {
            throw new IllegalArgumentException("El club no existe.");
        }
        int current = clubDao.countActiveMembers(membership.getIdClub());
        if (current >= club.getMaxCapacity()) {
            throw new IllegalArgumentException("El club alcanzó su capacidad máxima ("
                    + club.getMaxCapacity() + ").");
        }
        membership.setState("Activa");
        return clubDao.insertMembership(membership);
    }

    public void withdrawFromClub(Membership membership) {
        if (membership.getIdMembership() <= 0) {
            throw new IllegalArgumentException("Falta el identificador de la membresía.");
        }
        membership.setState("Retirada");
        clubDao.updateMembership(membership);
    }

    public void removeMembership(int idMembership) {
        if (!clubDao.deleteMembership(idMembership)) {
            throw new IllegalArgumentException("No se encontró la membresía a eliminar.");
        }
    }

    public List<Membership> listAllMemberships() {
        return clubDao.findAllMemberships();
    }

    public List<Membership> listClubMembers(int idClub) {
        return clubDao.findMembershipsByClub(idClub);
    }

    public List<Membership> listStudentClubs(int idStudent) {
        return clubDao.findMembershipsByStudent(idStudent);
    }

    public int createEvent(Event event) {
        validateEvent(event);
        return eventDao.insert(event);
    }

    public void updateEvent(Event event) {
        if (event.getIdEvent() <= 0) {
            throw new IllegalArgumentException("Falta el identificador del evento.");
        }
        validateEvent(event);
        eventDao.update(event);
    }

    public void removeEvent(int idEvent) {
        if (!eventDao.delete(idEvent)) {
            throw new IllegalArgumentException("No se encontró el evento a eliminar.");
        }
    }

    public List<Event> listEvents() {
        return eventDao.findAll();
    }

    public List<Event> listActiveEvents() {
        return eventDao.findActive();
    }

    public Event findEvent(int idEvent) {
        return eventDao.findById(idEvent);
    }

    public int addParticipant(Participant participant) {
        if (participant.getIdEvent() <= 0 || participant.getIdStudent() <= 0) {
            throw new IllegalArgumentException("Evento y estudiante son obligatorios.");
        }
        if (!Validator.isOneOf(participant.getRole(), "Participante", "Competidor",
                "Organizador", "Asistente")) {
            throw new IllegalArgumentException("Rol de participación inválido.");
        }
        return eventDao.insertParticipant(participant);
    }

    public void removeParticipant(int idParticipation) {
        if (!eventDao.deleteParticipant(idParticipation)) {
            throw new IllegalArgumentException("No se encontró el participante a eliminar.");
        }
    }

    public List<Participant> listEventParticipants(int idEvent) {
        return eventDao.findParticipantsByEvent(idEvent);
    }

    public List<Participant> listStudentParticipations(int idStudent) {
        return eventDao.findParticipantsByStudent(idStudent);
    }

    private void validateClub(Club c) {
        if (!Validator.isValidName(c.getName())) {
            throw new IllegalArgumentException("El nombre del club es inválido.");
        }
        if (c.getMaxCapacity() <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor a cero.");
        }
        if (!Validator.isNotEmpty(c.getSchedule())) {
            throw new IllegalArgumentException("El horario de reunión es obligatorio.");
        }
    }

    private void validateEvent(Event e) {
        if (!Validator.isNotEmpty(e.getName())) {
            throw new IllegalArgumentException("El nombre del evento es obligatorio.");
        }
        if (!Validator.isOneOf(e.getEventType(), "Académico", "Cultural", "Deportivo",
                "Social", "Científico")) {
            throw new IllegalArgumentException("Tipo de evento inválido.");
        }
        if (!Validator.isValidDate(e.getEventDate())) {
            throw new IllegalArgumentException("La fecha del evento es inválida.");
        }
        if (!Validator.isNotEmpty(e.getPlace())) {
            throw new IllegalArgumentException("El lugar del evento es obligatorio.");
        }
    }
}
