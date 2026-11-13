package com.littlehorizons.erp;

import com.littlehorizons.erp.util.AuditLogger;
import com.littlehorizons.erp.util.ConnectionFactory;
import com.littlehorizons.erp.util.ConsoleUtils;
import com.littlehorizons.erp.util.Database;
import com.littlehorizons.erp.util.LoggerUtil;
import com.littlehorizons.erp.view.MainMenu;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Entry point of the Little Horizons ERP console application.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("   COLEGIO LITTLE HORIZONS - ERP DE GESTIÓN EDUCATIVA");
        System.out.println("==========================================================");
        String configError = ConnectionFactory.missingConfiguration();
        if (configError != null) {
            System.err.println(configError);
            System.err.println("Consulte la sección 6.2 del README para configurar");
            System.err.println("ERP_DB_USER y ERP_DB_PASSWORD.");
            System.exit(1);
        }
        ConnectionFactory factory = ConnectionFactory.getInstance();
        try (Connection ignored = factory.getConnection()) {
            System.out.println("  Conexión a la base de datos establecida.");
            System.out.println("  Base de datos: erp_little_horizons");
            LoggerUtil.info("Application started");
            String operator = ConsoleUtils.readNonEmpty(
                    "Nombre del operador para la bitácora: ");
            AuditLogger.startSession(operator);
            new MainMenu().show();
        } catch (SQLException e) {
            LoggerUtil.error("Could not connect to the database; SQLState="
                    + e.getSQLState() + ", vendorCode=" + e.getErrorCode(), null);
            System.err.println("\nNo se pudo conectar a la base de datos.");
            System.err.println("Verifique que MySQL esté activo y que los datos de");
            System.err.println("conexión (ERP_DB_URL, ERP_DB_USER, ERP_DB_PASSWORD)");
            System.err.println("sean correctos y que el certificado TLS sea confiable.");
            System.exit(1);
        } finally {
            AuditLogger.endSession();
            Database.shutdown();
        }
    }
}
