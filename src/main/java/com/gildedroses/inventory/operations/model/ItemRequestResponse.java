package com.gildedroses.inventory.operations.model;

import java.util.List;

public class ItemRequestResponse {
    private List<Item> requestedItems;

	private String money;
	
	public String getMoney() {
		return money;
	}
	
	public void setMoney(String money) {
		this.money = money;
	}

	public List<Item> getRequestedItems() {
		return requestedItems;
	}

	public void setRequestedItems(List<Item> requestedItems) {
		this.requestedItems = requestedItems;
	}
}
