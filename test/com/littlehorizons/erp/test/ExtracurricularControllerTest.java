package com.littlehorizons.erp.test;

import com.littlehorizons.erp.controller.ExtracurricularController;
import com.littlehorizons.erp.dao.ClubDao;
import com.littlehorizons.erp.dao.EventDao;
import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.model.Membership;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/** Tests membership reactivation rules independently of JDBC. */
public final class ExtracurricularControllerTest {

    private ExtracurricularControllerTest() {
    }

    public static void register(TestSuite suite) {
        suite.add("Withdrawn membership can be reactivated below club capacity", () -> {
            AtomicBoolean reactivated = new AtomicBoolean();
            AtomicInteger activeCount = new AtomicInteger(1);
            ExtracurricularController controller = controller(activeCount, reactivated);
            Membership request = new Membership(9);
            request.setEntryDate("2026-11-13");
            controller.reactivateMembership(request);
            Assert.isTrue(reactivated.get(), "reactivation must reach the DAO");
            Assert.equals(4, request.getIdClub(), "the club ID comes from stored membership");
            Assert.equals(12, request.getIdStudent(), "the student ID comes from stored membership");
            Assert.equals("Activa", request.getState(), "reactivated membership must be active");
        });

        suite.add("Reactivation rejects a full club", () -> {
            AtomicBoolean reactivated = new AtomicBoolean();
            ExtracurricularController controller = controller(new AtomicInteger(2), reactivated);
            IllegalArgumentException error = Assert.assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.reactivateMembership(request()),
                    "the maximum capacity must be enforced");
            Assert.isTrue(error.getMessage().contains("capacidad máxima"),
                    "the user should receive a capacity message");
            Assert.isFalse(reactivated.get(), "the DAO must not reactivate a full club");
        });

        suite.add("Re-enrolling after withdrawal directs user to reactivation", () -> {
            ClubDao clubs = proxy(ClubDao.class, (proxy, method, args) -> switch (method.getName()) {
                case "findById" -> activeClub();
                case "findMembershipByStudentAndClub" -> withdrawnMembership();
                default -> throw new UnsupportedOperationException(method.getName());
            });
            ExtracurricularController controller = new ExtracurricularController(
                    clubs, unusedEvents());
            Membership enrollment = new Membership();
            enrollment.setIdStudent(12);
            enrollment.setIdClub(4);
            enrollment.setEntryDate("2026-11-13");
            IllegalArgumentException error = Assert.assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.enrollInClub(enrollment),
                    "a duplicate history row must not be inserted");
            Assert.isTrue(error.getMessage().contains("reactivación"),
                    "the user should be directed to the reactivation option");
        });
    }

    private static Membership request() {
        Membership membership = new Membership(9);
        membership.setEntryDate("2026-11-13");
        return membership;
    }

    private static ExtracurricularController controller(AtomicInteger activeCount,
                                                         AtomicBoolean reactivated) {
        ClubDao clubs = proxy(ClubDao.class, (proxy, method, args) -> switch (method.getName()) {
            case "findMembershipById" -> withdrawnMembership();
            case "findById" -> activeClub();
            case "countActiveMembers" -> activeCount.get();
            case "reactivateMembership" -> {
                reactivated.set(true);
                yield null;
            }
            default -> throw new UnsupportedOperationException(method.getName());
        });
        return new ExtracurricularController(clubs, unusedEvents());
    }

    private static Membership withdrawnMembership() {
        return new Membership(9, 12, 4, "2026-01-10", "Retirada", "Retiro", null);
    }

    private static Club activeClub() {
        return new Club(4, "Robótica", "", 1, 2, "Viernes", true, null, null);
    }

    private static EventDao unusedEvents() {
        return proxy(EventDao.class, (proxy, method, args) -> {
            throw new UnsupportedOperationException(method.getName());
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, java.lang.reflect.InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }
}
