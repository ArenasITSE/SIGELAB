package sigelab_api.dashboard.service;

import sigelab_api.dashboard.dto.DocenteDashboardResponse;

public interface DocenteDashboardService {

    DocenteDashboardResponse obtenerDashboard(
            Long docenteId
    );

}