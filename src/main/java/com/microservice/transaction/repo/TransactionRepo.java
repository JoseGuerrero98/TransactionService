package com.microservice.transaction.repo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.microservice.transaction.model.Transaction;
@Repository
public interface TransactionRepo extends ReactiveMongoRepository<Transaction, String> {

}
