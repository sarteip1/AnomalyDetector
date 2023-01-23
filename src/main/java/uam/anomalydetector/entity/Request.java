package uam.anomalydetector.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "request")
public class Request {

    @MongoId
    private String id;

    private String method;
    private String rootPath;
    private String path;
    private Map<String, String> queryParams;
    private Map<String, String> headers;
    private String body;

    @Override
    public String toString() {
        return "RequestDTO{" +
                "method='" + method + '\'' +
                ", rootPath='" + rootPath + '\'' +
                ", path='" + path + '\'' +
                ", queryParams=" + queryParams +
                ", headers=" + headers +
                ", body='" + body + '\'' +
                '}';
    }
}
