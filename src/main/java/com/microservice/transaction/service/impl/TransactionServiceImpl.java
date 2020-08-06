package com.microservice.transaction.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.microservice.transaction.model.Transaction;
import com.microservice.transaction.model.TypeProduct;
import com.microservice.transaction.model.TypeTransaction;
import com.microservice.transaction.repo.TransactionRepo;
import com.microservice.transaction.service.TransactionService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TransactionServiceImpl implements TransactionService {

	@Autowired
	private TransactionRepo repo;
	
	@Override
	public Flux<Transaction> findAll() {
		return repo.findAll();
	}
	
	@Override
	public Mono<Transaction> findById(String id) {
		return repo.findById(id);
	}
	
	@Override
	public Mono<Transaction> createTransaction(Transaction transaction) {
		
		if(!(transaction.getIdproductfirst()==null || transaction.getIdproductfirst().equals(""))) {
			
			Mono<Boolean> existprodfirst = existProduct(transaction.getIdproductfirst());
			
			Mono<TypeProduct> typeprod = getTypeProduct(transaction.getIdproductfirst());
			
			return existprodfirst.flatMap(prodfirst -> {
				if(prodfirst) {
					System.out.println("El producto existe");
					return typeprod.flatMap(prod -> {
						
						Mono<Boolean> existTypeTransaction = getExistTypeTransaction(prod.getTypeProduct(), transaction.getTypetransaction());
						
						return existTypeTransaction.flatMap(typetrans -> {
							
							if(typetrans) {
								
								if(transaction.getTypetransaction().equals("TRANSFERENCIA") || transaction.getTypetransaction().equals("PAGO CREDITO")) {
									
									if(!(transaction.getIdproductsecond()==null || transaction.getIdproductsecond().equals(""))) {
										
										if(!transaction.getIdproductfirst().equals(transaction.getIdproductsecond())) {
											Mono<Boolean> existprodsec = existProduct(transaction.getIdproductsecond());
											
											return existprodsec.flatMap(prodsec -> {
												
												if(prodsec) {
													//validar transacciones con cuentas
													Mono<TypeProduct> typeprodsec = getTypeProduct(transaction.getIdproductsecond());
													
													return typeprodsec.flatMap(productsec -> {
														
														if(productsec.getTypeProduct().equals("CUENTA")) {
															if(!transaction.getTypetransaction().equals("PAGO CREDITO")) {
																
																Mono<Boolean> validateAmountAccount = validateAmountAccount(transaction.getIdproductfirst(), transaction.getAmount());
																
																return validateAmountAccount.flatMap(valamount-> {
																	
																	if(valamount) {
																		
																		Mono<Boolean> editprices = transactionAccountOptions(prod.getId(), prod.getAmount(), transaction.getAmount(), productsec.getId());
																		
																		return editprices.flatMap(editprice ->{
																			
																			if(editprice) {
																				System.out.println("transferencia completada");
																				return repo.save(transaction);
																			}else {
																				System.out.println("No se pudo hacer la transferencia");
																				return Mono.empty();
																			}
																			
																		});
																		
																		
																	}else {
																		System.out.println("No tiene sufiente saldo para esta transaccion");
																		return Mono.empty();
																	}
																	
																});
																
															}else {
																System.out.println("No se puede hacer un pago de credito a una cuenta");
																return Mono.empty();
															}
														}
														
														if(productsec.getTypeProduct().equals("CREDITO")) {
															if(!transaction.getTypetransaction().equals("TRANSFERENCIA")) {

																Mono<Boolean> validateAmountAccount = validateAmountAccount(transaction.getIdproductfirst(), transaction.getAmount());
																
																return validateAmountAccount.flatMap(valamount-> {
																	
																	if(valamount) {
																		
																		Mono<Boolean> editprices = transactionAccountOptions(prod.getId(), prod.getAmount(), transaction.getAmount(), productsec.getId());
																		
																		return editprices.flatMap(editprice ->{
																			
																			if(editprice) {
																				System.out.println("pago credito completada");
																				return repo.save(transaction);
																			}else {
																				System.out.println("No se pudo hacer el pago credito");
																				return Mono.empty();
																			}
																			
																		});
																		
																		
																	}else {
																		System.out.println("No tiene sufiente saldo para esta transaccion");
																		return Mono.empty();
																	}
																	
																});

															}else {
																System.out.println("No se puede hacer una transferencia a un credito");
																return Mono.empty();
															}
														}
														
														return Mono.empty();
														
													});
													
												}else {
													System.out.println("El segundo producto ingresado no existe");
													return Mono.empty();
												}
											});
										}else {
											System.out.println("No puede poner las mismas cuentas");
											return Mono.empty();
										}
										
									}else {
										System.out.println("El tipo de producto de la otra cuenta no puede ir vacio o nulo");
										return Mono.empty();
									}
									
								}else {
									//validar solo transacciones simples
									Mono<Boolean> respSimple = transactionSimple(prod.getId(), transaction.getTypetransaction(), transaction.getAmount());
									System.out.println("validar solo transacciones simples");
									return respSimple.flatMap(simple -> {
										if(simple) {
											return repo.save(transaction);
										}else {
											System.out.println("No se pudo hacer la transaccion");
											return Mono.empty();
										}
									});
								}
								
							}else {
								System.out.println("El tipo de transaccion no existe");
								return Mono.empty();
							}
							
							
						});
						
					});
					
				}else {
					//mensaje de: El producto no existe
					System.out.println("El producto ingresado no existe");
					return Mono.empty();
				}
			});
			
		}else {

			System.out.println("Campo de idproducto no puede ir vacio o nulo");
			return Mono.empty();
			
		}
		
		
		//Mono<Boolean> existprod = existProduct(transaction.getIdproductfirst());
		
		//Mono<TypeProduct> typeprod = getTypeProduct(transaction.getIdproductfirst());
		
		/*return existprod.flatMap(existproduct -> {
			if(existproduct) {
				return repo.save(transaction);
			}else {
				//mensaje de: El producto no existe
				System.out.println("El producto no existe");
				return Mono.empty();
			}
			
		});*/
		
		//return repo.save(transaction);
	}
	
	@Override
	public Mono<Transaction> updateTransaction(Transaction transaction, String id) {
		return repo.findById(id).flatMap(item -> {
			item.setAmount(transaction.getAmount());
			return repo.save(item);
		});
	}
	
	@Override
	public Mono<Void> deleteTransaction(String id) {
		try {
			
			return repo.findById(id).flatMap(item -> {
				return repo.delete(item);
			});
			
		} catch (Exception e) {
			return Mono.error(e);
		}
	}
	
	public Mono<Boolean> existProduct(String id) {
		
		String url = "http://localhost:8084/product/exist/" + id;
		
		return WebClient.create()
				.get()
				.uri(url)
				.retrieve()
				.bodyToMono(Boolean.class);
	}
	
	public Mono<Boolean> getTypeTransaction() {
		return null;
	}
	
	public Mono<TypeProduct> getTypeProduct(String idproduct) {
		
		String url = "http://localhost:8084/product/findbyid/" + idproduct;
		
		return WebClient.create()
				.get()
				.uri(url)
				.retrieve()
				.bodyToMono(TypeProduct.class);
	}
	
	public Mono<TypeProduct> updateAccountAmount(TypeProduct typeproduct, String idproduct) {
		String url = "http://localhost:8084/product/updateaccount/" + idproduct;
		
		return WebClient.create()
				.put()
				.uri(url)
				.body(Mono.just(typeproduct), TypeProduct.class)
				.retrieve()
				.bodyToMono(TypeProduct.class);
	}
	
	public Mono<Boolean> updatePriceAccount(TypeProduct typeproduct, String idproduct) {
		
		Mono<TypeProduct> updateAccount = updateAccountAmount(typeproduct, idproduct);
		
		return updateAccount.flatMap(update -> {
			return Mono.just(true);
		}).switchIfEmpty(Mono.just(false));
		
	}
	
	public Mono<TypeProduct> updateCreditAmount(TypeProduct typeproduct, String idproduct) {
		String url = "http://localhost:8084/product/updatecredit/" + idproduct;
		
		return WebClient.create()
				.put()
				.uri(url)
				.body(Mono.just(typeproduct), TypeProduct.class)
				.retrieve()
				.bodyToMono(TypeProduct.class);
	}
	
	public Mono<Boolean> updatePriceCredit(TypeProduct typeproduct, String idproduct) {
		
		Mono<TypeProduct> updateAccount = updateCreditAmount(typeproduct, idproduct);
		
		return updateAccount.flatMap(update -> {
			return Mono.just(true);
		}).switchIfEmpty(Mono.just(false));
		
	}
	
	public Mono<Boolean> updatePriceAccount2(String idproduct2, Double amount) {
		
		Mono<TypeProduct> product2 = getTypeProduct(idproduct2);
		
		return product2.flatMap(prod -> {
			
			TypeProduct typeproduct = new TypeProduct();
			
			Double newamount;
			
			newamount = prod.getAmount() + amount;
			
			typeproduct.setAmount(newamount);
			typeproduct.setCantTransaction(prod.getCantTransaction());
			
			if(prod.getTypeProduct().equals("CUENTA")) {
				System.out.println("CUENTA...");
				
				Mono<Boolean> response = updatePriceAccount(typeproduct, idproduct2);
				
				return response;
			}
			
			if(prod.getTypeProduct().equals("CREDITO")) {
				System.out.println("CREDITO...");
				
				Double creditAmount = prod.getCreditAmount() + amount;
				typeproduct.setCreditAmount(creditAmount);
				
				
				Mono<Boolean> response = updatePriceCredit(typeproduct, idproduct2);
				
				return response;
			}
			
			return Mono.just(false);
			
		});
		
	}
	
	public Mono<Boolean> transactionSimple(String idproduct, String type, Double amount) {
		
		Mono<TypeProduct> productbody = getTypeProduct(idproduct);
		
		return productbody.flatMap(body -> {
			
			TypeProduct typeproduct = new TypeProduct();
			Double newamount;
			Double canttrans;
			Double creditAmount;
			Mono<Boolean> response = Mono.just(false);
			
			switch (type) {
			case "DEPOSITO":
				newamount = body.getAmount() + amount;
				typeproduct.setAmount(newamount);
				if(body.getCantTransaction() > 0) {
					canttrans = body.getCantTransaction() -1 ;
				}else {
					canttrans = 0.0;
				}
				typeproduct.setCantTransaction(canttrans);
				response = updatePriceAccount(typeproduct, idproduct);
				break;
			case "CARGAR CONSUMO":
				newamount = body.getAmount() - amount;
				creditAmount = body.getCreditAmount() - amount;
				typeproduct.setAmount(newamount);
				typeproduct.setCreditAmount(creditAmount);
				response = updatePriceCredit(typeproduct, idproduct);
				break;
			case "RETIRO":
				newamount = body.getAmount() - amount;
				typeproduct.setAmount(newamount);
				if(body.getCantTransaction() > 0) {
					canttrans = body.getCantTransaction() -1 ;
				}else {
					canttrans = 0.0;
				}
				typeproduct.setCantTransaction(canttrans);
				response = updatePriceAccount(typeproduct, idproduct);
				break;
			case "PAGO DE CREDITO":
				newamount = body.getAmount() + amount;
				creditAmount = body.getCreditAmount() - amount;
				typeproduct.setAmount(newamount);
				typeproduct.setCreditAmount(creditAmount);
				response = updatePriceCredit(typeproduct, idproduct);
				break;
			}
			
			 return response;
			
		});
		
	}
	
	public Mono<Boolean> transactionAccountOptions(String idproduct, Double prodamount, Double amount, String idproduct2) {
		
		
		Mono<TypeProduct> productbody = getTypeProduct(idproduct);
		
		return productbody.flatMap(body -> {
			
			Double newamount;
			
			TypeProduct typeproduct = new TypeProduct();
			
			newamount = prodamount - amount;
			
			Double canttrans;
			
			if(body.getCantTransaction() > 0) {
				canttrans = body.getCantTransaction() -1 ;
			}else {
				canttrans = 0.0;
			}
			
			typeproduct.setAmount(newamount);
			typeproduct.setCantTransaction(canttrans);
			
			Mono<Boolean> response = updatePriceAccount(typeproduct, idproduct);
			
			Mono<Boolean> product2 = updatePriceAccount2(idproduct2, amount);
			
			return response.flatMap(resp -> {
				
				if(resp) {
					return product2;
				}else {
					return Mono.just(false);
				}
				
			});
			
			
		});
		
	}
	
	public Flux<Boolean> getTransactionAccount(String typetransaction) {
		
		TypeTransaction transaccion = new TypeTransaction();
		
		Flux<String> account = transaccion.getTypeAccount();
		
		return account.flatMap(acc -> {
			if(acc.equals(typetransaction)) {
				return Flux.just(true);
			}
			return Flux.empty();
		});
		
	}
	
	public Flux<Boolean> getTransactionCredit(String typetransaction) {
		
		TypeTransaction transaccion = new TypeTransaction();
		
		Flux<String> account = transaccion.getTypeCredit();
		
		return account.flatMap(acc -> {
			if(acc.equals(typetransaction)) {
				return Flux.just(true);
			}
			return Flux.empty();
		});
		
	}
	
	public Mono<Boolean> getExistTypeTransaction(String typeproduct, String typetransaction) {
		
		Mono<Boolean> response;
		
		if(typeproduct.equals("CUENTA")) {
			response = getTransactionAccount(typetransaction).next();
			return response.switchIfEmpty(Mono.just(false));
		}

		if(typeproduct.equals("CREDITO")) {
			response = getTransactionCredit(typetransaction).next();
			return response.switchIfEmpty(Mono.just(false));
		}
		
		return Mono.just(false);
		
	}
	
	public Mono<Boolean> validateAmountAccount(String idproduct, Double newamount) {
		
		Mono<TypeProduct> product = getTypeProduct(idproduct);
		
		return product.flatMap(prod -> {
			
			if(prod.getAmount() >= newamount) {
				return Mono.just(true);
			}else {
				return Mono.just(false);
			}
			
		});
		
	}
	
}
