package com.gildedroses.inventory.operations.service;

import java.util.List;

import com.gildedroses.inventory.operations.model.Item;
import com.gildedroses.inventory.operations.model.ItemRequestResponse;

public interface InventoryService {

	List<Item> getInventoryList();
	
	Item getInventoryItemById(String id);
	
    ItemRequestResponse buyItems(ItemRequestResponse req);
}
