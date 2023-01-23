package uam.anomalydetector.controller;

import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uam.anomalydetector.service.request.RequestService;

@RestController
public class RequestController {

    private final RequestService requestService;

    @Autowired
    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping(value = "/api/v1/check/request")
    public ResponseEntity<String> checkRequest(@RequestBody String requestJson) {
        requestService.processRequest(requestJson);
        return ResponseEntity.status(HttpStatus.OK).body("Request receives successfully");
    }

    @GetMapping(value = "/api/v1/suspicious/requests")
    public ResponseEntity<String> getAllRequests(){
        return ResponseEntity.status(HttpStatus.OK).body(requestService.getAllRequests());
    }

}
