package uam.anomalydetector.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "endpoint")
public class Endpoint {

    @MongoId
    private String id;

    private String httpMethod;
    private String path;
    private String requestBody;
    private String pathParameters;
    private String queryParameters;
}
