package sigelab_api.excel.service;

import java.time.LocalDate;

public interface ExcelService {

    byte[] generarExcelSolicitudes();

    byte[] generarExcelSolicitudes(
            LocalDate fechaInicio,
            LocalDate fechaFin
    );
}