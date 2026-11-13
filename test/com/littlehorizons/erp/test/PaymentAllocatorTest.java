package com.littlehorizons.erp.test;

import com.littlehorizons.erp.util.PaymentAllocator;
import com.littlehorizons.erp.util.PaymentAllocator.ChargeBalance;
import com.littlehorizons.erp.util.PaymentAllocator.PaymentCredit;
import com.littlehorizons.erp.util.PaymentAllocator.Result;

import java.math.BigDecimal;
import java.util.List;

/** Tests deterministic FIFO allocation of payment credits to charges. */
public final class PaymentAllocatorTest {

    private PaymentAllocatorTest() {
    }

    public static void register(TestSuite suite) {
        suite.add("Payment credits settle the oldest charge first", () -> {
            Result result = PaymentAllocator.allocate(
                    List.of(new PaymentCredit(1, money("75.00"))),
                    List.of(new ChargeBalance(10, money("50.00"), money("0.00")),
                            new ChargeBalance(11, money("50.00"), money("0.00"))));
            Assert.equals(2, result.applications().size(),
                    "the payment should be split across both charges");
            Assert.equals(money("0.00"), result.remainingCharges().get(10),
                    "the first charge should be settled");
            Assert.equals(money("25.00"), result.remainingCharges().get(11),
                    "the second charge should retain its unpaid balance");
            Assert.equals(money("0.00"), result.remainingCredits().get(1),
                    "no credit should remain unapplied");
        });

        suite.add("Partial payments combine to settle a charge", () -> {
            Result result = PaymentAllocator.allocate(
                    List.of(new PaymentCredit(1, money("30.00")),
                            new PaymentCredit(2, money("25.00"))),
                    List.of(new ChargeBalance(10, money("50.00"), money("0.00"))));
            Assert.equals(money("0.00"), result.remainingCharges().get(10),
                    "both payment credits should be applied");
            Assert.equals(money("5.00"), result.remainingCredits().get(2),
                    "excess from the second payment should remain as a credit");
        });

        suite.add("Existing applications are not allocated twice", () -> {
            Result result = PaymentAllocator.allocate(
                    List.of(new PaymentCredit(1, money("20.00"))),
                    List.of(new ChargeBalance(10, money("50.00"), money("40.00"))));
            Assert.equals(money("0.00"), result.remainingCharges().get(10),
                    "only the outstanding part should be filled");
            Assert.equals(money("10.00"), result.remainingCredits().get(1),
                    "unused payment value should stay as credit");
        });

        suite.add("Credits remain unapplied when there are no charges", () -> {
            Result result = PaymentAllocator.allocate(
                    List.of(new PaymentCredit(1, money("25.00"))), List.of());
            Assert.equals(money("25.00"), result.remainingCredits().get(1),
                    "the full payment should remain visible as credit");
            Assert.isTrue(result.applications().isEmpty(),
                    "no allocation row should be created without a charge");
        });
    }

    private static BigDecimal money(String amount) {
        return new BigDecimal(amount);
    }
}
