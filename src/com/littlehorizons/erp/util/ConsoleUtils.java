package com.littlehorizons.erp.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Console input/output helpers. All user input is validated here so that
 * views never throw NumberFormatException or DateTimeParseException.
 */
public final class ConsoleUtils {

    private static final Scanner SCANNER = new Scanner(System.in);

    private ConsoleUtils() {
    }

    public static String readLine(String prompt) {
        System.out.print(prompt);
        if (!SCANNER.hasNextLine()) {
            System.out.println();
            System.out.println("\nFin de la entrada. Cerrando la aplicación.");
            System.exit(0);
        }
        return SCANNER.nextLine().trim();
    }

    public static String readNonEmpty(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (Validator.isNotEmpty(value)) {
                return value;
            }
            printError("Este campo es obligatorio. Intente nuevamente.");
        }
    }

    public static String readOptional(String prompt) {
        return readLine(prompt);
    }

    public static int readInt(String prompt, int min, int max) {
        while (true) {
            String value = readLine(prompt);
            try {
                int number = Integer.parseInt(value);
                if (number >= min && number <= max) {
                    if (prompt.trim().startsWith("Seleccione una opción")) {
                        AuditLogger.recordMenuSelection(number);
                    }
                    return number;
                }
                printError("El valor debe estar entre " + min + " y " + max + ".");
            } catch (NumberFormatException e) {
                printError("Ingrese un número válido.");
            }
        }
    }

    public static int readInt(String prompt) {
        return readInt(prompt, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static BigDecimal readDecimal(String prompt, double min, double max) {
        while (true) {
            String value = readLine(prompt);
            try {
                BigDecimal number = new BigDecimal(value.replace(",", "."));
                if (number.doubleValue() >= min && number.doubleValue() <= max) {
                    return number;
                }
                printError("El valor debe estar entre " + min + " y " + max + ".");
            } catch (NumberFormatException e) {
                printError("Ingrese un valor numérico válido.");
            }
        }
    }

    public static String readDate(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (Validator.isValidDate(value)) {
                return LocalDate.parse(value).toString();
            }
            printError("Fecha inválida. Use el formato YYYY-MM-DD.");
        }
    }

    public static LocalDate readLocalDate(String prompt) {
        while (true) {
            String value = readDate(prompt);
            try {
                return LocalDate.parse(value);
            } catch (DateTimeParseException e) {
                printError("Fecha inválida. Use el formato YYYY-MM-DD.");
            }
        }
    }

    public static String readEnum(String prompt, String... options) {
        List<String> allowed = Arrays.asList(options);
        while (true) {
            String value = readLine(prompt + " " + allowed + ": ");
            for (String option : allowed) {
                if (option.equalsIgnoreCase(value)) {
                    return option;
                }
            }
            printError("Opción inválida. Valores permitidos: " + allowed);
        }
    }

    public static boolean readYesNo(String prompt) {
        while (true) {
            String value = readLine(prompt + " (S/N): ");
            if (value.equalsIgnoreCase("S") || value.equalsIgnoreCase("SI")
                    || value.equalsIgnoreCase("SÍ")) {
                return true;
            }
            if (value.equalsIgnoreCase("N") || value.equalsIgnoreCase("NO")) {
                return false;
            }
            printError("Responda S o N.");
        }
    }

    public static void pause() {
        readLine("\nPresione Enter para continuar...");
    }

    public static void printHeader(String title) {
        AuditLogger.recordScreenOpened(title);
        System.out.println();
        System.out.println("==========================================================");
        System.out.println("  " + title);
        System.out.println("==========================================================");
    }

    public static void printSubHeader(String title) {
        AuditLogger.recordScreenOpened(title);
        System.out.println();
        System.out.println("----------------------------------------------------------");
        System.out.println("  " + title);
        System.out.println("----------------------------------------------------------");
    }

    public static void printSuccess(String message) {
        System.out.println("\n  [OK] " + message);
    }

    public static void printError(String message) {
        System.out.println("\n  [ERROR] " + message);
    }

    public static void printWarning(String message) {
        System.out.println("\n  [AVISO] " + message);
    }

    public static void printInfo(String message) {
        System.out.println("\n  [INFO] " + message);
    }
}
