package com.littlehorizons.erp;

import com.littlehorizons.erp.util.ConnectionFactory;
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
        ConnectionFactory factory = ConnectionFactory.getInstance();
        try (Connection ignored = factory.getConnection()) {
            System.out.println("  Conexión a la base de datos establecida.");
            System.out.println("  Base de datos: erp_little_horizons");
            if (factory.isUsingDefaultCredentials()) {
                System.out.println();
                System.out.println("  ADVERTENCIA: se usan las credenciales de desarrollo");
                System.out.println("  por defecto. Defina ERP_DB_USER y ERP_DB_PASSWORD");
                System.out.println("  antes de ejecutar el sistema en producción.");
                LoggerUtil.warn("Default development database credentials are in use");
            }
            LoggerUtil.info("Application started");
            new MainMenu().show();
        } catch (SQLException e) {
            LoggerUtil.error("Could not connect to the database", e);
            System.err.println("\nNo se pudo conectar a la base de datos.");
            System.err.println("URL: " + ConnectionFactory.getInstance().getUrl());
            System.err.println("Verifique que MySQL esté activo y que los datos de");
            System.err.println("conexión (ERP_DB_URL, ERP_DB_USER, ERP_DB_PASSWORD)");
            System.err.println("sean correctos. Detalle: " + e.getMessage());
            System.exit(1);
        }
    }
}
