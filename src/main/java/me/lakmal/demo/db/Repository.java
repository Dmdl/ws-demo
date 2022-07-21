package me.lakmal.demo.db;

import org.springframework.data.repository.CrudRepository;

public interface Repository extends CrudRepository<Comment, Integer> {
}
