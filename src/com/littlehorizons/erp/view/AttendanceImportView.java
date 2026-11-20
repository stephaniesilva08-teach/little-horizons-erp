package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.AttendanceImportController;
import com.littlehorizons.erp.model.AttendanceImportResult;
import com.littlehorizons.erp.util.ConsoleUtils;
import com.littlehorizons.erp.util.DataAccessException;

/**
 * Console view for the bulk attendance CSV import.
 */
public class AttendanceImportView {

    private final AttendanceImportController controller = new AttendanceImportController();

    public void show() {
        ConsoleUtils.printSubHeader("IMPORTAR ASISTENCIAS DESDE CSV");
        AttendanceImportResult result;
        try {
            result = ViewUtils.query("import_attendance_csv",
                    controller::importAttendances);
        } catch (DataAccessException e) {
            ConsoleUtils.printError(e.getMessage());
            ConsoleUtils.pause();
            return;
        } catch (RuntimeException e) {
            ConsoleUtils.printError("No se pudo completar la importación de asistencias.");
            ConsoleUtils.pause();
            return;
        }

        switch (result.status()) {
            case FILE_NOT_FOUND -> {
                System.out.println();
                System.out.println("Error: El archivo '"
                        + AttendanceImportController.FILE_NAME
                        + "' no se encuentra en la carpeta raíz del proyecto.");
                ConsoleUtils.pause();
            }
            case INVALID_HEADER -> {
                System.out.println();
                System.out.println("Error: El archivo no tiene el formato correcto. "
                        + "La cabecera debe ser: " + AttendanceImportController.HEADER);
                ConsoleUtils.pause();
            }
            case COMPLETED -> printSummary(result);
        }
    }

    private void printSummary(AttendanceImportResult result) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║           REPORTE DE IMPORTACIÓN DE ASISTENCIAS                  ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Total de líneas en el archivo: " + result.totalLines());
        System.out.println("Líneas procesadas exitosamente: " + result.successful());
        System.out.println("Líneas con errores (no procesadas): " + result.failed());
        System.out.println();
        System.out.println("═══════════════════════════════════════════════════════════════════");
        ConsoleUtils.readLine("Presione Enter para volver al menú principal...");
        System.out.println("═══════════════════════════════════════════════════════════════════");
    }
}
