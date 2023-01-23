package uam.anomalydetector.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import uam.anomalydetector.entity.Endpoint;

public interface EndpointRepository extends MongoRepository<Endpoint, String> {

}
