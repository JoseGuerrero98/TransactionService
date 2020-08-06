package com.microservice.transaction.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Flux;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeTransaction {

	private Flux<String> typeAccount = Flux.just("DEPOSITO", "RETIRO", "TRANSFERENCIA", "PAGO CREDITO");
	
	private Flux<String> typeCredit = Flux.just("PAGO DE CREDITO", "CARGAR CONSUMO");

	public Flux<String> getTypeAccount() {
		return typeAccount;
	}

	public void setTypeAccount(Flux<String> typeAccount) {
		this.typeAccount = typeAccount;
	}

	public Flux<String> getTypeCredit() {
		return typeCredit;
	}

	public void setTypeCredit(Flux<String> typeCredit) {
		this.typeCredit = typeCredit;
	}
	
}
