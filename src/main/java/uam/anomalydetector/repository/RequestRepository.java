package uam.anomalydetector.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import uam.anomalydetector.entity.Request;

public interface RequestRepository extends MongoRepository<Request, String> {

}
