package sigelab_api.dashboard.service;

import sigelab_api.dashboard.dto.EstadisticaGeneralResponse;
import sigelab_api.dashboard.dto.LaboratorioUsoResponse;
import sigelab_api.dashboard.dto.SolicitudesMesResponse;

import java.util.List;

public interface DashboardGraficaService {

    EstadisticaGeneralResponse obtenerEstados();

    List<LaboratorioUsoResponse> laboratoriosMasUsados();

    List<SolicitudesMesResponse> solicitudesPorMes();

}