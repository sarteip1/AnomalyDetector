package uam.anomalydetector.service.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.http.HttpMethod;
import uam.anomalydetector.entity.dto.EndpointDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EndpointsParser {

    public static List<EndpointDTO> parseEndpoints(String body) {
        List<EndpointDTO> endpointList = new ArrayList<>();

        JsonObject jsonBody = JsonParser.parseString(body).getAsJsonObject();
        JsonObject paths = jsonBody.getAsJsonObject("paths");
        Set<Map.Entry<String, JsonElement>> pathEntries = paths.entrySet();

        for (Map.Entry<String, JsonElement> pathEntry : pathEntries) {
            String path = pathEntry.getKey();
            JsonObject pathObject = pathEntry.getValue().getAsJsonObject();

            Set<Map.Entry<String, JsonElement>> methodEntries = pathObject.entrySet();
            for (Map.Entry<String, JsonElement> methodEntry : methodEntries) {
                HttpMethod httpMethod = HttpMethod.valueOf(methodEntry.getKey().toUpperCase());
                JsonObject methodObject = methodEntry.getValue().getAsJsonObject();

                JsonObject requestBody = methodObject.getAsJsonObject("requestBody");

                EndpointDTO endpoint = new EndpointDTO();
                endpoint.setHttpMethod(httpMethod.name());
                endpoint.setPath(path);
                endpoint.setRequestBody(requestBody != null ? requestBody.toString() : null);

                if (methodObject.has("parameters")) {
                    JsonArray parametersArray = methodObject.getAsJsonArray("parameters");
                    JsonObject pathParameters = new JsonObject();
                    for (JsonElement parameterElement : parametersArray) {
                        JsonObject parameterObject = parameterElement.getAsJsonObject();
                        String in = parameterObject.get("in").getAsString();
                        if (in.equals("path")) {
                            String name = parameterObject.get("name").getAsString();
                            JsonObject schema = parameterObject.getAsJsonObject("schema");
                            String type = schema.get("type").getAsString();
                            pathParameters.addProperty(name, type);
                        }
                    }
                    endpoint.setPathParameters(pathParameters.toString());
                }

                if (methodObject.has("parameters")) {
                    JsonArray parametersArray = methodObject.getAsJsonArray("parameters");
                    JsonObject queryParameters = new JsonObject();
                    for (JsonElement parameterElement : parametersArray) {
                        JsonObject parameterObject = parameterElement.getAsJsonObject();
                        String in = parameterObject.get("in").getAsString();
                        if (in.equals("query")) {
                            String name = parameterObject.get("name").getAsString();
                            JsonObject schema = parameterObject.getAsJsonObject("schema");
                            String type = schema.get("type").getAsString();
                            queryParameters.addProperty(name, type);
                        }
                    }
                    endpoint.setQueryParameters(queryParameters.toString());
                }
                endpointList.add(endpoint);
            }
        }
        return endpointList;
    }

}