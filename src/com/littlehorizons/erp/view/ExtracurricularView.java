package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.ExtracurricularController;
import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.model.Event;
import com.littlehorizons.erp.model.Membership;
import com.littlehorizons.erp.model.Participant;
import com.littlehorizons.erp.util.ConsoleUtils;

import java.util.List;

/**
 * Console view for clubs, memberships, events and participants.
 */
public class ExtracurricularView {

    private final ExtracurricularController controller =
            new ExtracurricularController();

    public void show() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("ACTIVIDADES EXTRACURRICULARES");
            System.out.println("  1. Clubes");
            System.out.println("  2. Membresías");
            System.out.println("  3. Eventos");
            System.out.println("  4. Participantes en eventos");
            System.out.println("  0. Volver al menú principal");
            int option = ConsoleUtils.readInt("Seleccione una opción: ", 0, 4);
            switch (option) {
                case 1 -> clubsMenu();
                case 2 -> membershipsMenu();
                case 3 -> eventsMenu();
                case 4 -> participantsMenu();
                case 0 -> back = true;
                default -> { }
            }
        }
    }

    private void clubsMenu() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("CLUBES");
            System.out.println("  1. Listar clubes");
            System.out.println("  2. Listar clubes activos");
            System.out.println("  3. Crear club");
            System.out.println("  4. Actualizar club");
            System.out.println("  5. Eliminar club");
            System.out.println("  0. Volver");
            int option = ConsoleUtils.readInt("Seleccione una opción: ", 0, 5);
            switch (option) {
                case 1 -> printClubs(ViewUtils.query("list_clubs", controller::listClubs));
                case 2 -> printClubs(ViewUtils.query("list_active_clubs",
                        controller::listActiveClubs));
                case 3 -> createClub();
                case 4 -> updateClub();
                case 5 -> deleteClub();
                case 0 -> back = true;
                default -> { }
            }
        }
    }

    private void printClubs(List<Club> clubs) {
        ConsoleUtils.printSubHeader("LISTADO DE CLUBES");
        if (clubs.isEmpty()) {
            ConsoleUtils.printInfo("No hay clubes registrados.");
        } else {
            System.out.printf("%-4s %-26s %-26s %-6s %-8s%n",
                    "ID", "CLUB", "SUPERVISOR", "CUPO", "ACTIVO");
            ViewUtils.printSeparator();
            clubs.forEach(c -> System.out.printf("%-4d %-26s %-26s %-6d %-8s%n",
                    c.getIdClub(), truncate(c.getName(), 26),
                    truncate(c.getSupervisorName(), 26), c.getMaxCapacity(),
                    c.isActive() ? "Sí" : "No"));
        }
        ConsoleUtils.pause();
    }

    private void createClub() {
        ConsoleUtils.printSubHeader("CREAR CLUB");
        Club club = readClub(new Club());
        ViewUtils.executeWithId(() -> controller.createClub(club),
                "Club creado con");
        ConsoleUtils.pause();
    }

    private void updateClub() {
        int id = ConsoleUtils.readInt("ID del club: ");
        Club club = ViewUtils.query("find_club", id, () -> controller.findClub(id));
        if (club == null) {
            ConsoleUtils.printError("El club no existe.");
            ConsoleUtils.pause();
            return;
        }
        readClub(club);
        ViewUtils.execute(() -> controller.updateClub(club),
                "Club actualizado correctamente.", club.getIdClub());
        ConsoleUtils.pause();
    }

    private Club readClub(Club c) {
        c.setName(ViewUtils.readWithDefault("Nombre del club", c.getName()));
        c.setDescription(ViewUtils.readWithDefault("Descripción", c.getDescription()));
        c.setIdTeacher(ConsoleUtils.readInt("ID del profesor supervisor: "));
        c.setMaxCapacity(ConsoleUtils.readInt("Capacidad máxima: ", 1, 500));
        c.setSchedule(ViewUtils.readWithDefault("Horario de reunión", c.getSchedule()));
        c.setActive(ConsoleUtils.readYesNo("¿Club activo?"));
        return c;
    }

    private void deleteClub() {
        int id = ConsoleUtils.readInt("ID del club: ");
        if (ConsoleUtils.readYesNo("¿Confirma la eliminación?")) {
            ViewUtils.execute(() -> controller.removeClub(id),
                    "Club eliminado correctamente.", id);
        }
        ConsoleUtils.pause();
    }

    private void membershipsMenu() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("MEMBRESÍAS");
            System.out.println("  1. Listar todas las membresías");
            System.out.println("  2. Listar miembros de un club");
            System.out.println("  3. Listar clubes de un estudiante");
            System.out.println("  4. Inscribir estudiante en club");
            System.out.println("  5. Retirar estudiante de club");
            System.out.println("  6. Reactivar membresía");
            System.out.println("  7. Eliminar membresía");
            System.out.println("  0. Volver");
            int option = ConsoleUtils.readInt("Seleccione una opción: ", 0, 7);
            switch (option) {
                case 1 -> printMemberships(ViewUtils.query("list_club_memberships",
                        controller::listAllMemberships));
                case 2 -> { }
                case 3 -> { }
                case 4 -> enrollInClub();
                case 5 -> withdrawFromClub();
                case 6 -> reactivateMembership();
                case 7 -> deleteMembership();
                case 0 -> back = true;
                default -> { }
            }
            if (option == 2) {
                int idClub = ConsoleUtils.readInt("ID del club: ");
                ViewUtils.execute(() -> printMemberships(
                        controller.listClubMembers(idClub)), null);
                ConsoleUtils.pause();
            } else if (option == 3) {
                int idStudent = ConsoleUtils.readInt("ID del estudiante: ");
                ViewUtils.execute(() -> printMemberships(
                        controller.listStudentClubs(idStudent)), null);
                ConsoleUtils.pause();
            }
        }
    }

    private void printMemberships(List<Membership> list) {
        ConsoleUtils.printSubHeader("MEMBRESÍAS");
        if (list.isEmpty()) {
            ConsoleUtils.printInfo("No hay membresías para mostrar.");
            return;
        }
        System.out.printf("%-4s %-28s %-26s %-12s %-10s%n",
                "ID", "ESTUDIANTE", "CLUB", "INGRESO", "ESTADO");
        ViewUtils.printSeparator();
        list.forEach(m -> System.out.printf("%-4d %-28s %-26s %-12s %-10s%n",
                m.getIdMembership(), truncate(m.getStudentName(), 28),
                truncate(m.getClubName(), 26), m.getEntryDate(), m.getState()));
    }

    private void enrollInClub() {
        Membership m = new Membership();
        m.setIdStudent(ConsoleUtils.readInt("ID del estudiante: "));
        m.setIdClub(ConsoleUtils.readInt("ID del club: "));
        m.setEntryDate(ConsoleUtils.readDate("Fecha de ingreso (YYYY-MM-DD): "));
        ViewUtils.executeWithId(() -> controller.enrollInClub(m),
                "Membresía registrada con");
        ConsoleUtils.pause();
    }

    private void withdrawFromClub() {
        Membership m = new Membership();
        m.setIdMembership(ConsoleUtils.readInt("ID de la membresía: "));
        m.setWithdrawalReason(ConsoleUtils.readNonEmpty("Motivo del retiro: "));
        ViewUtils.execute(() -> controller.withdrawFromClub(m),
                "Retiro registrado correctamente.", m.getIdMembership());
        ConsoleUtils.pause();
    }

    private void reactivateMembership() {
        Membership m = new Membership();
        m.setIdMembership(ConsoleUtils.readInt("ID de la membresía: "));
        m.setEntryDate(ConsoleUtils.readDate("Nueva fecha de ingreso (YYYY-MM-DD): "));
        ViewUtils.execute(() -> controller.reactivateMembership(m),
                "Membresía reactivada correctamente.", m.getIdMembership());
        ConsoleUtils.pause();
    }

    private void deleteMembership() {
        int id = ConsoleUtils.readInt("ID de la membresía: ");
        if (ConsoleUtils.readYesNo("¿Confirma la eliminación?")) {
            ViewUtils.execute(() -> controller.removeMembership(id),
                    "Membresía eliminada correctamente.", id);
        }
        ConsoleUtils.pause();
    }

    private void eventsMenu() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("EVENTOS");
            System.out.println("  1. Listar eventos");
            System.out.println("  2. Listar eventos activos");
            System.out.println("  3. Crear evento");
            System.out.println("  4. Actualizar evento");
            System.out.println("  5. Eliminar evento");
            System.out.println("  0. Volver");
            int option = ConsoleUtils.readInt("Seleccione una opción: ", 0, 5);
            switch (option) {
                case 1 -> printEvents(ViewUtils.query("list_events", controller::listEvents));
                case 2 -> printEvents(ViewUtils.query("list_active_events",
                        controller::listActiveEvents));
                case 3 -> createEvent();
                case 4 -> updateEvent();
                case 5 -> deleteEvent();
                case 0 -> back = true;
                default -> { }
            }
        }
    }

    private void printEvents(List<Event> events) {
        ConsoleUtils.printSubHeader("LISTADO DE EVENTOS");
        if (events.isEmpty()) {
            ConsoleUtils.printInfo("No hay eventos registrados.");
        } else {
            System.out.printf("%-4s %-30s %-12s %-12s %-20s%n",
                    "ID", "EVENTO", "TIPO", "FECHA", "LUGAR");
            ViewUtils.printSeparator();
            events.forEach(e -> System.out.printf("%-4d %-30s %-12s %-12s %-20s%n",
                    e.getIdEvent(), truncate(e.getName(), 30), e.getEventType(),
                    e.getEventDate(), truncate(e.getPlace(), 20)));
        }
        ConsoleUtils.pause();
    }

    private void createEvent() {
        ConsoleUtils.printSubHeader("CREAR EVENTO");
        Event event = readEvent(new Event());
        ViewUtils.executeWithId(() -> controller.createEvent(event),
                "Evento creado con");
        ConsoleUtils.pause();
    }

    private void updateEvent() {
        int id = ConsoleUtils.readInt("ID del evento: ");
        Event event = ViewUtils.query("find_event", id, () -> controller.findEvent(id));
        if (event == null) {
            ConsoleUtils.printError("El evento no existe.");
            ConsoleUtils.pause();
            return;
        }
        readEvent(event);
        ViewUtils.execute(() -> controller.updateEvent(event),
                "Evento actualizado correctamente.", event.getIdEvent());
        ConsoleUtils.pause();
    }

    private Event readEvent(Event e) {
        e.setName(ViewUtils.readWithDefault("Nombre del evento", e.getName()));
        e.setDescription(ViewUtils.readWithDefault("Descripción", e.getDescription()));
        e.setEventType(ConsoleUtils.readEnum("Tipo", "Académico", "Cultural",
                "Deportivo", "Social", "Científico"));
        e.setEventDate(ViewUtils.readDateWithDefault("Fecha del evento",
                e.getEventDate()));
        e.setPlace(ViewUtils.readWithDefault("Lugar", e.getPlace()));
        e.setHasCompetition(ConsoleUtils.readYesNo("¿Tiene competencia?"));
        e.setOrganizerId(ConsoleUtils.readInt("ID del profesor organizador: "));
        e.setActive(ConsoleUtils.readYesNo("¿Evento activo?"));
        return e;
    }

    private void deleteEvent() {
        int id = ConsoleUtils.readInt("ID del evento: ");
        if (ConsoleUtils.readYesNo("¿Confirma la eliminación?")) {
            ViewUtils.execute(() -> controller.removeEvent(id),
                    "Evento eliminado correctamente.", id);
        }
        ConsoleUtils.pause();
    }

    private void participantsMenu() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("PARTICIPANTES EN EVENTOS");
            System.out.println("  1. Listar participantes de un evento");
            System.out.println("  2. Listar participaciones de un estudiante");
            System.out.println("  3. Registrar participante");
            System.out.println("  4. Eliminar participante");
            System.out.println("  0. Volver");
            int option = ConsoleUtils.readInt("Seleccione una opción: ", 0, 4);
            switch (option) {
                case 1 -> listEventParticipants();
                case 2 -> listStudentParticipations();
                case 3 -> addParticipant();
                case 4 -> deleteParticipant();
                case 0 -> back = true;
                default -> { }
            }
        }
    }

    private void listEventParticipants() {
        int idEvent = ConsoleUtils.readInt("ID del evento: ");
        ViewUtils.execute(() -> printParticipants(
                controller.listEventParticipants(idEvent)), null);
        ConsoleUtils.pause();
    }

    private void listStudentParticipations() {
        int idStudent = ConsoleUtils.readInt("ID del estudiante: ");
        ViewUtils.execute(() -> printParticipants(
                controller.listStudentParticipations(idStudent)), null);
        ConsoleUtils.pause();
    }

    private void printParticipants(List<Participant> list) {
        ConsoleUtils.printSubHeader("PARTICIPANTES");
        if (list.isEmpty()) {
            ConsoleUtils.printInfo("No hay participaciones para mostrar.");
            return;
        }
        System.out.printf("%-4s %-28s %-28s %-14s %-14s%n",
                "ID", "ESTUDIANTE", "EVENTO", "ROL", "POSICIÓN");
        ViewUtils.printSeparator();
        list.forEach(p -> System.out.printf("%-4d %-28s %-28s %-14s %-14s%n",
                p.getIdParticipation(), truncate(p.getStudentName(), 28),
                truncate(p.getEventName(), 28), p.getRole(), p.getPosition()));
    }

    private void addParticipant() {
        Participant p = new Participant();
        p.setIdEvent(ConsoleUtils.readInt("ID del evento: "));
        p.setIdStudent(ConsoleUtils.readInt("ID del estudiante: "));
        p.setRole(ConsoleUtils.readEnum("Rol", "Participante", "Competidor",
                "Organizador", "Asistente"));
        p.setPosition(ConsoleUtils.readOptional("Posición obtenida: "));
        p.setObservations(ConsoleUtils.readOptional("Observaciones: "));
        ViewUtils.executeWithId(() -> controller.addParticipant(p),
                "Participante registrado con");
        ConsoleUtils.pause();
    }

    private void deleteParticipant() {
        int id = ConsoleUtils.readInt("ID de la participación: ");
        if (ConsoleUtils.readYesNo("¿Confirma la eliminación?")) {
            ViewUtils.execute(() -> controller.removeParticipant(id),
                    "Participante eliminado correctamente.", id);
        }
        ConsoleUtils.pause();
    }

    private String truncate(String value, int length) {
        if (value == null) {
            return "";
        }
        return value.length() <= length ? value : value.substring(0, length - 1) + ".";
    }
}
