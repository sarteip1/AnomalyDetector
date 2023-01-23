package uam.anomalydetector.service.request;

import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uam.anomalydetector.common.helper.RegexParser;
import uam.anomalydetector.entity.Request;
import uam.anomalydetector.entity.dto.EndpointDTO;
import uam.anomalydetector.entity.dto.RequestDTO;
import uam.anomalydetector.repository.EndpointRepository;
import uam.anomalydetector.repository.RequestRepository;
import uam.anomalydetector.service.detector.DetectorService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Log4j2
public class RequestService {

    private final Gson gson;
    private final DetectorService detectorService;
    private final EndpointRepository endpointRepository;
    private final RequestRepository requestRepository;

    @Autowired
    public RequestService(Gson gson, DetectorService detectorService, EndpointRepository endpointRepository,
            RequestRepository requestRepository) {
        this.gson = gson;
        this.detectorService = detectorService;
        this.endpointRepository = endpointRepository;
        this.requestRepository = requestRepository;
    }

    public void processRequest(String jsonBody) {
        RequestDTO request = gson.fromJson(jsonBody, RequestDTO.class);
        EndpointDTO endpoint = searchForEndpoint(request.getPath(), request.getMethod());

        if (endpoint == null) {
            log.warn(String.format("Can't find an endpoint for this request path: %s, method: %s", request.getPath(),
                    request.getMethod()));
            return;
        }

        detectorService.checkRequest(endpoint, request);
    }

    public String getAllRequests() {
        return new Gson().toJson(requestRepository.findAll().stream()
                                                  .map(this::parseRequestToDTO)
                                                  .collect(Collectors.toList()));
    }

    private EndpointDTO searchForEndpoint(String path, String method) {
        for (EndpointDTO endpointDTO : getAllEndpoints()) {
            String pathRegex = RegexParser.getRegexForPath(endpointDTO.getPath());
            Pattern pattern = Pattern.compile(pathRegex);
            Matcher matcher = pattern.matcher(path);
            if (method.equals(endpointDTO.getHttpMethod()) && matcher.find()) {
                return endpointDTO;
            }
        }
        return null;
    }

    private List<EndpointDTO> getAllEndpoints() {
        return endpointRepository.findAll().stream()
                                 .map(endpoint -> new EndpointDTO(endpoint.getHttpMethod(),
                                         endpoint.getPath(), endpoint.getRequestBody(),
                                         endpoint.getPathParameters(), endpoint.getQueryParameters()))
                                 .collect(Collectors.toList());
    }

    private RequestDTO parseRequestToDTO(Request request) {
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setMethod(request.getMethod());
        requestDTO.setRootPath(request.getRootPath());
        requestDTO.setPath(request.getPath());
        requestDTO.setQueryParams(request.getQueryParams());
        requestDTO.setHeaders(request.getHeaders());
        requestDTO.setBody(request.getBody());
        return requestDTO;
    }

}
