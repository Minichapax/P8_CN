package gal.usc.etse.grei.es.project.repository;

import gal.usc.etse.grei.es.project.model.Disco;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DiskRepository extends MongoRepository<Disco, String> {
}