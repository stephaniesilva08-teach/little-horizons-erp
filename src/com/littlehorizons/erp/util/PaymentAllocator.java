package com.littlehorizons.erp.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Allocates student payment credits to charges in their supplied FIFO order. */
public final class PaymentAllocator {

    private PaymentAllocator() {
    }

    public static Result allocate(List<PaymentCredit> credits,
                                  List<ChargeBalance> charges) {
        Map<Integer, BigDecimal> remainingCredits = new LinkedHashMap<>();
        for (PaymentCredit credit : credits) {
            if (credit.amount().signum() < 0) {
                throw new IllegalArgumentException("Payment credit cannot be negative.");
            }
            remainingCredits.put(credit.paymentId(), credit.amount());
        }

        Map<Integer, BigDecimal> remainingCharges = new LinkedHashMap<>();
        List<Application> applications = new ArrayList<>();
        for (ChargeBalance charge : charges) {
            BigDecimal remaining = charge.amount().subtract(charge.applied());
            if (remaining.signum() < 0) {
                throw new IllegalArgumentException("Applied amount exceeds its charge.");
            }
            for (PaymentCredit credit : credits) {
                if (remaining.signum() == 0) {
                    break;
                }
                BigDecimal available = remainingCredits.get(credit.paymentId());
                if (available.signum() == 0) {
                    continue;
                }
                BigDecimal applied = available.min(remaining);
                applications.add(new Application(credit.paymentId(), charge.chargeId(), applied));
                remainingCredits.put(credit.paymentId(), available.subtract(applied));
                remaining = remaining.subtract(applied);
            }
            remainingCharges.put(charge.chargeId(), remaining);
        }
        return new Result(List.copyOf(applications), Map.copyOf(remainingCredits),
                Map.copyOf(remainingCharges));
    }

    public record PaymentCredit(int paymentId, BigDecimal amount) { }

    public record ChargeBalance(int chargeId, BigDecimal amount, BigDecimal applied) { }

    public record Application(int paymentId, int chargeId, BigDecimal amount) { }

    public record Result(List<Application> applications,
                         Map<Integer, BigDecimal> remainingCredits,
                         Map<Integer, BigDecimal> remainingCharges) { }
}
