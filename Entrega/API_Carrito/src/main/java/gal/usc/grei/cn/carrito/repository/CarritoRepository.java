package gal.usc.grei.cn.carrito.repository;

import gal.usc.grei.cn.carrito.model.Carrito;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CarritoRepository extends MongoRepository<Carrito, String>{

}