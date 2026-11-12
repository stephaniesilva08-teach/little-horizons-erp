package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Event;
import com.littlehorizons.erp.model.Participant;
import java.util.List;

/**
 * Persistence contract for events.
 */
public interface EventDao {

    int insert(Event event);

    void update(Event event);

    boolean delete(int idEvent);

    Event findById(int idEvent);

    List<Event> findAll();

    List<Event> findActive();

    int insertParticipant(Participant participant);

    boolean deleteParticipant(int idParticipation);

    List<Participant> findParticipantsByEvent(int idEvent);

    List<Participant> findParticipantsByStudent(int idStudent);
}
