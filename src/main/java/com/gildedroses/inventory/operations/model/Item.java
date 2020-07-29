package com.gildedroses.inventory.operations.model;

public class Item {
	
	public Item() {
		
	}
	
	public Item(final String id, final String name, final String price, final String quantity, final String desc) {
		this.Id = id;
		this.name = name;
		this.price = price;
		this.quantity = quantity;
		this.description = desc;
	}
	
	public Item(Item other) {
		this.Id = other.Id;
		this.name = other.name;
		this.price = other.price;
		this.quantity = other.quantity;
		this.description = other.description;
	}

	private String Id;

	private String name;
	
	private String description;
	
	private String price;
	
	private String quantity;
	
	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}
	
	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String availableQuatity) {
		this.quantity = availableQuatity;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}
	
	public boolean equals(final Object obj) {
		if (obj instanceof Item) {
		    Item otherItem = (Item) obj;
		    return otherItem.Id == this.Id;
		}
		return false;
	}
	
	public String toString() {
		return "Item: id " + this.Id + " price " + this.price + " qty " + this.quantity;
	}

}
