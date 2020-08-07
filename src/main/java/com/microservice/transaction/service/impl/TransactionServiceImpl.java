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
			System.out.println("**************************************************");
			System.out.println("**************************************************");
			System.out.println("**************************************************");
			return existprodfirst.flatMap(prodfirst -> {
				if(prodfirst) {
					
					return typeprod.flatMap(prod -> {
						
						Mono<Boolean> validate = getValidateTransaction(transaction, prod);
						
						return validate.flatMap(val -> {
							
							if(val) {
								System.out.println("Se completo la transferencia");;
								return repo.save(transaction);
							}else {
								System.out.println("No se pudo completar la transferencia");;
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
				
				Mono<Boolean> response = updatePriceAccount(typeproduct, idproduct2);
				
				return response;
			}
			
			if(prod.getTypeProduct().equals("CREDITO")) {
				
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
	//valida que sea el tipo de transaccion que esta permitido para cuenta
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
	//valida que sea el tipo de transaccion que esta permitido para credito
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
	
	public Double getSumAmount(Double amount1, Double amount2) {
		
		Double amount = amount1 + amount2;
		return amount;
		
	}
	
	public Double getResAmount(Double amount1, Double amount2) {
		
		Double amount = amount1 - amount2;
		return amount;
		
	}
	
	public Double getAmount(String typetransaction, Double amount1, Double amount2) {
		
		Double amount = amount1 - amount2;
		
		switch (typetransaction) {
		case "DEPOSITO":
		case "PAGO DE CREDITO":
			amount = getSumAmount(amount1,amount2);
			break;
		case "RETIRO":
		case "TRANSFERENCIA":
		case "PAGO CREDITO":
		case "CARGAR CONSUMO":
			amount = getResAmount(amount1,amount2);
			break;
		
		}
		
		return amount;
		
	}
	
	public Mono<Boolean> validateTransactionProducts(TypeProduct typeproduct, TypeProduct newtypeproduct, Transaction transaction) {
		
		if(transaction.getIdproductsecond()== null || transaction.getIdproductsecond().equals(""))
		{
			Mono<TypeProduct> product2 = getTypeProduct(transaction.getIdproductsecond());
			
			return product2.flatMap(item -> {
				
				Mono<Boolean> complete;
				
				complete = updatePriceAccount(newtypeproduct, typeproduct.getId());

				return complete.flatMap(resp -> {
					
					TypeProduct newproduct2 = new TypeProduct();
					Double amount;
					Mono<Boolean> complete2;
					
					amount = item.getAmount() + transaction.getAmount();
					
					newproduct2.setAmount(amount);
					
					switch (item.getTypeProduct()) {
					case "CUENTA":
						newproduct2.setCantTransaction(item.getCantTransaction());
						break;
					case "CREDITO":
						Double credit = item.getCreditAmount() + transaction.getAmount();
						newproduct2.setCreditAmount(credit);
						break;
					}
					
					complete2 = updatePriceAccount(newproduct2, item.getId());
					
					return complete2;
					
				});
				
			});
			
		}else {
			System.out.println("El segundo id del producto no debe ser nulo o vacio");
			return Mono.just(false);
		}
		
	}
	
	public Mono<Boolean> getValidateTransaction(Transaction transaction,TypeProduct typeproduct1) {
		
		Mono<Boolean> valTypeTransaction = null;
		
		if(typeproduct1.getTypeProduct().equals("CUENTA")) {
			
			System.out.println("CUENTA");
			
			valTypeTransaction = getTransactionAccount(transaction.getTypetransaction()).next().switchIfEmpty(Mono.just(false));
			
			//validar si tiene fondos en la cuenta
			switch (transaction.getTypetransaction()) {
			case "RETIRO":
			case "TRANSFERENCIA":
			case "PAGO CREDITO":
				if(typeproduct1.getAmount() < transaction.getAmount()) {
					System.out.println("No tiene fondos en esta cuenta para esta transaccion");
					return Mono.just(false);
				}
				break;
			}
			
		}
		
		if(typeproduct1.getTypeProduct().equals("CREDITO")) {
			
			System.out.println("CREDITO");
			
			valTypeTransaction = getTransactionCredit(transaction.getTypetransaction()).next().switchIfEmpty(Mono.just(false));
			
			//validar si tiene fondos en el credito
			if(transaction.getTypetransaction().equals("CARGAR CONSUMO")) {
				if(typeproduct1.getCreditAmount() < transaction.getAmount()) {
					System.out.println("Sobrepaso el limite de su credito, no puede realizar esta transaccion");
					return Mono.just(false);
				}
			}
			
		}
		
		return valTypeTransaction.flatMap(valtype -> {
			System.out.println(valtype);
			Mono<Boolean> complete = null;
			
			if(valtype) {
				Double amount1;
				Double cant = typeproduct1.getCantTransaction();
				System.out.println("Cantidad de transacciones actuales: " + cant);
				Double newcant = 0.0;
				
				amount1 = getAmount(transaction.getTypetransaction(), typeproduct1.getAmount(), transaction.getAmount());
				
				TypeProduct newtypeproduct = new TypeProduct();
				newtypeproduct.setAmount(amount1);
				
				System.out.println("MONTO DE DINERO" + amount1);
				switch (transaction.getTypetransaction()) {
				case "DEPOSITO":
				case "RETIRO":
					
					if(cant > 0) {
						newcant = typeproduct1.getCantTransaction() - 1;
					}else {
						System.out.println("validar las comisiones");
					}
					
					System.out.println("CANTIDAD DE TRANSACCIONES DESPUES DE LA TRANSACION: " + newcant);
					newtypeproduct.setCantTransaction(newcant);
					
					complete = updatePriceAccount(newtypeproduct, typeproduct1.getId());
					
					break;
				case "PAGO DE CREDITO":
				case "CARGAR CONSUMO":
					
					Double credit = getAmount(transaction.getTypetransaction(), typeproduct1.getCreditAmount(), transaction.getAmount());
					newtypeproduct.setCreditAmount(credit);
					System.out.println("MONTO DE CREDITO" + credit);
					complete = updatePriceCredit(newtypeproduct, typeproduct1.getId());
					break;
				
				case "TRANSFERENCIA":
				case "PAGO CREDITO":
					
					if(cant > 0) {
						newcant = typeproduct1.getCantTransaction() - 1;
					}else {
						System.out.println("validar las comisiones");
					}
					newtypeproduct.setCantTransaction(newcant);
					
					System.out.println("transferencia con dos cuentas");
					complete = validateTransactionProducts(typeproduct1, newtypeproduct, transaction);
					break;
				
				}
				
				return complete;
				
			}else {
				
				System.out.println("Tipo de transaccion no valida para el producto seleccionado");
				return Mono.just(false);
				
			}
			
		});
		
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
