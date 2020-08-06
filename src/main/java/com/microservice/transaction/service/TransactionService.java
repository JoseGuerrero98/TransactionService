package com.microservice.transaction.service;

import com.microservice.transaction.model.Transaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionService {
	
	public Flux<Transaction> findAll();
	public Mono<Transaction> findById(String id);
	public Mono<Transaction> createTransaction(Transaction transaction);
	public Mono<Transaction> updateTransaction(Transaction transaction, String id);
	public Mono<Void> deleteTransaction(String id);
	
}
