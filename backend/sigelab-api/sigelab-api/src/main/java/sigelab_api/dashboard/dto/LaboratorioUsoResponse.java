package sigelab_api.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LaboratorioUsoResponse {

    private String laboratorio;
    private Long total;

}