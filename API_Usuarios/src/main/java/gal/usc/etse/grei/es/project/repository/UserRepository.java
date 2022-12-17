package gal.usc.etse.grei.es.project.repository;

import gal.usc.etse.grei.es.project.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<Usuario, String> {
}
