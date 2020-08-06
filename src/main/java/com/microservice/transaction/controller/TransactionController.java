package com.microservice.transaction.controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microservice.transaction.model.Transaction;
import com.microservice.transaction.service.TransactionService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

	@Autowired
	private TransactionService service;
	
	@GetMapping("/")
	public Flux<Transaction> findAll() {
		return service.findAll();
	}
	
	@GetMapping("/findbyid/{id}")
	public Mono<Transaction> findById(@PathVariable("id") String id) {
		return service.findById(id);
	}
	
	@PostMapping("/create")
	public Mono<ResponseEntity<Transaction>> createTransaction(@RequestBody Transaction transaction) {
		return service.createTransaction(transaction)
				.map(item ->
				ResponseEntity.created(URI.create("/transaction".concat(item.getId())))
				.contentType(MediaType.APPLICATION_JSON)
				.body(item)
						);
	}
	
	@PutMapping("/update/{id}")
	public Mono<ResponseEntity<Transaction>> updateTransaction(@RequestBody Transaction transaction, @PathVariable("id") String id) {
		return service.updateTransaction(transaction, id)
				.map(item -> 
				ResponseEntity.created(URI.create("/transaction".concat(item.getId())))
				.contentType(MediaType.APPLICATION_JSON)
				.body(item)
						);
	}
	
	@DeleteMapping("/delete/{id}")
	public Mono<ResponseEntity<Void>> deleteTransaction(@PathVariable("id") String id) {
		return service.deleteTransaction(id).then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT))
				.defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND))
				);
	}
	
}
