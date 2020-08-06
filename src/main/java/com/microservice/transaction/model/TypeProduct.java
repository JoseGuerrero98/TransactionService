package com.microservice.transaction.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TypeProduct {
	
	private String id;
	
	private String typeProduct;
	
	private Double amount;
	
	private Double cantTransaction;
	
	private Double creditAmount;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public String getTypeProduct() {
		return typeProduct;
	}

	public void setTypeProduct(String typeProduct) {
		this.typeProduct = typeProduct;
	}

	public Double getCantTransaction() {
		return cantTransaction;
	}

	public void setCantTransaction(Double cantTransaction) {
		this.cantTransaction = cantTransaction;
	}

	public Double getCreditAmount() {
		return creditAmount;
	}

	public void setCreditAmount(Double creditAmount) {
		this.creditAmount = creditAmount;
	}
	
}
