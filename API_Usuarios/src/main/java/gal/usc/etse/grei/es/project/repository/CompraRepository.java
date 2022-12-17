package gal.usc.etse.grei.es.project.repository;

import gal.usc.etse.grei.es.project.model.Compra;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CompraRepository extends MongoRepository<Compra, String> {
}
