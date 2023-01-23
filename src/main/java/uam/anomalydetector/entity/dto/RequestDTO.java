package uam.anomalydetector.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestDTO {

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
