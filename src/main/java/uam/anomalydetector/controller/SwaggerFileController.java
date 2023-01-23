package uam.anomalydetector.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uam.anomalydetector.service.endpoint.SwaggerFileService;

import java.util.Objects;

@Log4j2
@RestController
public class SwaggerFileController {

    private SwaggerFileService service;

    @Autowired
    public SwaggerFileController(SwaggerFileService service) {
        this.service = service;
    }

    @PostMapping(value = "/api/v1/upload/swagger/file")
    public ResponseEntity<String> postFileUpload(@RequestPart("x-file") MultipartFile file) {

        if (!Objects.equals(file.getContentType(), MediaType.APPLICATION_JSON_VALUE)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File should be application/json type");
        }

        try {
            JsonObject jsonObject = new Gson().fromJson(new String(file.getBytes()), JsonObject.class);
            service.serviceSwaggerFile(jsonObject);
            return ResponseEntity.status(HttpStatus.OK).body(jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("An error occurred while processing the attached JSON file");
        }
    }

    @GetMapping(value = "/api/v1/upload/swagger/file")
    public ResponseEntity<String> getUploadedEndpoints() {
        return ResponseEntity.status(HttpStatus.OK).body(service.getAllEndpoints());
    }

}