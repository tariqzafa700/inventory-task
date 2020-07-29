package com.gildedroses.inventory.operations.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.gildedroses.inventory.operations.model.Item;
import com.gildedroses.inventory.operations.model.ItemRequestResponse;
import com.gildedroses.inventory.operations.service.InventoryService;

@RestController
public class InventoryController {
	
	@Autowired
	private InventoryService inventoryService;
	
	@GetMapping("/inventory")
	public ResponseEntity<List<Item>> getInventoryItems() {
		return ResponseEntity.ok(inventoryService.getInventoryList());
	}
	
	@PostMapping("/buy")
	public ResponseEntity<ItemRequestResponse> buyItems(@RequestBody ItemRequestResponse req) {
		return ResponseEntity.ok(inventoryService.buyItems(req));
	}
	
	@GetMapping("/inventory/{id}")
	public ResponseEntity<Item> buyItems(@PathVariable("id") String reqId) {
		return ResponseEntity.ok(inventoryService.getInventoryItemById(reqId));
	}
}
