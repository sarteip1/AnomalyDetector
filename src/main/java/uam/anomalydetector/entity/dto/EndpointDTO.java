package uam.anomalydetector.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EndpointDTO {

    private String httpMethod;
    private String path;
    private String requestBody;
    private String pathParameters;
    private String queryParameters;

    @Override
    public String toString() {
        return "Endpoints{" +
                "httpMethod=" + httpMethod +
                ", path='" + path + '\'' +
                ", requestBody='" + requestBody + '\'' +
                ", pathParameters='" + pathParameters + '\'' +
                ", queryParameters='" + queryParameters + '\'' +
                '}';
    }

}

