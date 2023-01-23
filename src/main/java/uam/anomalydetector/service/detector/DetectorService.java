package uam.anomalydetector.service.detector;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uam.anomalydetector.entity.Request;
import uam.anomalydetector.entity.dto.EndpointDTO;
import uam.anomalydetector.entity.dto.RequestDTO;
import uam.anomalydetector.repository.RequestRepository;

import java.util.HashMap;
import java.util.Map;

@Service
@Log4j2
public class DetectorService {

    private static final String WARNING = "Potential attack attempt detected with suspicious parameters %s in %s for %s";

    private final RequestRepository requestRepository;

    @Autowired
    public DetectorService(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    public void checkRequest(EndpointDTO endpoint, RequestDTO requestDTO) {
        boolean status;
        val queryParams = requestDTO.getQueryParams();
        val path = requestDTO.getPath();
        val headers = requestDTO.getHeaders();
        val body = requestDTO.getBody();
        status = checkRequestObject(endpoint, requestDTO);

        isInjection(path, "path");

        if (!queryParams.isEmpty()) {
            for (val entry : queryParams.entrySet()) {
                val paramValue = entry.getValue();
                status = status && isInjection(paramValue, entry.getKey());
            }
        }

        if (!headers.isEmpty()) {
            for (val entry : headers.entrySet()) {
                val headerName = entry.getKey();
                val headerValue = entry.getValue();
                status = status && isInjection(headerName, headerName) && isInjection(headerValue, headerName);
            }
        }

        if (body != null && !body.isEmpty()) {
            status = status && isInjection(body, "body");
        }

        if (!status) {
            saveRequest(requestDTO);
        }
    }

    public boolean checkRequestObject(EndpointDTO endpoint, RequestDTO request) {
        if (endpoint.getRequestBody() != null && !endpoint.getRequestBody().isEmpty()) {
            return checkQueryParameters(endpoint.getQueryParameters(), request.getQueryParams()) && checkBody(
                    endpoint.getRequestBody(), request.getBody(), endpoint.getHttpMethod());
        }
        return checkQueryParameters(endpoint.getQueryParameters(), request.getQueryParams());
    }

    private boolean checkQueryParameters(String endpointQueryParameters, Map<String, String> requestQueryParams) {
        if (endpointQueryParameters == null || requestQueryParams.isEmpty()) {
            return true;
        }

        for (val entry : requestQueryParams.entrySet()) {
            val key = entry.getKey();
            val value = entry.getValue();
            val endpointParam = new JsonParser().parse(endpointQueryParameters).getAsJsonObject();
            if (parseValue(endpointParam.get(key).getAsString(), value) == null) {
                log.warn("Parameters are different");
                return false;
            }
        }
        return true;
    }

    private boolean checkBody(String endpointBody, String requestBody, String method) {
        val endpointBodyJson = new JsonParser().parse(endpointBody).getAsJsonObject();
        val requestBodyJson = new JsonParser().parse(requestBody).getAsJsonObject();
        return validateJson(endpointBodyJson, requestBodyJson, method);
    }

    private boolean validateJson(JsonObject schema, JsonObject data, String method) {
        JsonObject properties = schema.getAsJsonObject("content")
                                      .getAsJsonObject("application/json")
                                      .getAsJsonObject("schema")
                                      .getAsJsonObject("properties");
        return matchTypes(properties, data, method) && matchKeys(properties, data);
    }

    private boolean matchTypes(JsonObject schema, JsonObject data, String method) {
        for (String key : schema.keySet()) {
            if (!method.equals("PUT") && !data.has(key)) {
                return false;
            }

            String schemaType = schema.getAsJsonObject(key).get("type").getAsString();

            if (method.equals("PUT") && !data.has(key)) {
                break;
            }

            if (parseValue(schemaType, data.get(key).getAsString()) == null) {
                return false;
            }
        }
        return true;
    }

    private boolean matchKeys(JsonObject schema, JsonObject data) {
        for (String key : data.keySet()) {
            if (!schema.has(key)) {
                return false;
            }
        }
        return true;
    }

    private Object parseValue(String key, String value) {
        Map<String, Class<?>> classMap = new HashMap<>();
        classMap.put("string", String.class);
        classMap.put("number", Long.class);
        classMap.put("integer", Long.class);
        classMap.put("boolean", Boolean.class);
        classMap.put("array", Object[].class);

        Class<?> targetClass = classMap.get(key);
        if (targetClass == null) {
            return null;
        }

        try {
            if (targetClass.equals(String.class)) {
                return value;
            } else if (targetClass.equals(Long.class)) {
                return Long.parseLong(value);
            } else if (targetClass.equals(Boolean.class)) {
                return Boolean.parseBoolean(value);
            } else if (targetClass.equals(Object[].class)) {
                return value.split(",");
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    private static boolean isInjection(String input, String key) {
        boolean status = true;
        // Lista potencjalnych znaków specjalnych
        String[] specialChars = { "'", "\"", ";", "--", "/*", "==" };

        // Lista potencjalnych słów kluczowych
        String[] keywords = { "SELECT", "FROM", "WHERE", "UNION", "LIKE", "TABLE", "DELETE", "DROP", "UPDATE",
                "SLEEP" };

        String[] xssTags = { "<script>", "onload", "eval" };

        // Sprawdzanie ciągu wejściowego pod kątem znaków specjalnych
        for (String chars : specialChars) {
            if (input.contains(chars)) {
                log.warn(String.format(WARNING, chars, input, key));
                status = false;
            }
        }

        // Sprawdzanie ciągu wejściowego pod kątem słów kluczowych
        for (String keyword : keywords) {
            if (input.toUpperCase().contains(keyword)) {
                log.warn(String.format(WARNING, keyword, input, key));
                status = false;
            }
        }

        // Sprawdzanie ciągu wejściowego pod kątem znaczników skryptów
        for (String tag : xssTags) {
            if (input.contains(tag)) {
                log.warn(String.format(WARNING, tag, input, key));
                status = false;
            }
        }

        return status;
    }

    private void saveRequest(RequestDTO requestDTO) {
        try {
            requestRepository.save(parseDTOToRequest(requestDTO));
        } catch (Exception e) {
            log.warn("Problem during saving request:", e);
        }
    }

    private Request parseDTOToRequest(RequestDTO requestDTO) {
        Request request = new Request();
        BeanUtils.copyProperties(requestDTO, request);
        return request;
    }

}
