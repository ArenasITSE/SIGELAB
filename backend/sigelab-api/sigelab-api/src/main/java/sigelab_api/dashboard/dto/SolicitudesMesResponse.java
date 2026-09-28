package sigelab_api.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SolicitudesMesResponse {

    private String mes;
    private Long total;

}