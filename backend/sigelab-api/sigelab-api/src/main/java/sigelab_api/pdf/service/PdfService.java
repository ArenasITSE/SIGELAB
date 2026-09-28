package sigelab_api.pdf.service;

import java.time.LocalDate;

public interface PdfService {

    byte[] generarReporteSolicitudes();

    byte[] generarReporteSolicitudes(
            LocalDate fechaInicio,
            LocalDate fechaFin
    );
}