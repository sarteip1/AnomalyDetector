package uam.anomalydetector.service.endpoint;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uam.anomalydetector.entity.Endpoint;
import uam.anomalydetector.entity.dto.EndpointDTO;
import uam.anomalydetector.repository.EndpointRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
public class SwaggerFileService {

    private final EndpointRepository endpointRepository;

    @Autowired
    public SwaggerFileService(EndpointRepository endpointRepository) {
        this.endpointRepository = endpointRepository;
    }

    public void serviceSwaggerFile(JsonObject swagger) {
        List<EndpointDTO> endpoints = EndpointsParser.parseEndpoints(swagger.toString());
        for (EndpointDTO endpoint : endpoints) {
            try {
                endpointRepository.save(parseDTOToEndpoint(endpoint));
            } catch (Exception e) {
                new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
                return;
            }
        }
    }

    public String getAllEndpoints() {
        return new Gson().toJson(endpointRepository.findAll().stream()
                                                   .map(endpoint -> new EndpointDTO(endpoint.getHttpMethod(),
                                                           endpoint.getPath(), endpoint.getRequestBody(),
                                                           endpoint.getPathParameters(), endpoint.getQueryParameters()))
                                                   .collect(Collectors.toList()));
    }

    private Endpoint parseDTOToEndpoint(EndpointDTO endpointDTO) {
        Endpoint endpoint = new Endpoint();
        BeanUtils.copyProperties(endpointDTO, endpoint);
        return endpoint;
    }

}
