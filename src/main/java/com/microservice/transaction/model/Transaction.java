package com.microservice.transaction.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document( collection = "Transaction")
public class Transaction {

	@Id
	private String id;
	
	private String idproductfirst;
	
	private String typetransaction;
	
	private double amount;
	
	private String idproductsecond;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIdproductfirst() {
		return idproductfirst;
	}

	public void setIdproductfirst(String idproductfirst) {
		this.idproductfirst = idproductfirst;
	}

	public String getTypetransaction() {
		return typetransaction;
	}

	public void setTypetransaction(String typetransaction) {
		this.typetransaction = typetransaction;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getIdproductsecond() {
		return idproductsecond;
	}

	public void setIdproductsecond(String idproductsecond) {
		this.idproductsecond = idproductsecond;
	}

	
	
}
