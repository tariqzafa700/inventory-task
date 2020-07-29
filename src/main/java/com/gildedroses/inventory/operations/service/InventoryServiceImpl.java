package com.gildedroses.inventory.operations.service;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gildedroses.inventory.operations.exception.ServiceException;
import com.gildedroses.inventory.operations.model.Item;
import com.gildedroses.inventory.operations.model.ItemRequestResponse;

@Service
public class InventoryServiceImpl implements InventoryService {

	private static ObjectMapper jsonMapper = new ObjectMapper();
		
	private Map<String, Item> inventoryItems = new HashMap<>();
	
	private int surgePriceCallsThreshold;
	
	private long surgePriceDuration;
	
	private final Clock clock;
	
	private static class CallTimestampInfo {
		private String originalPrice;
		private Instant lastUpdatedInstance;
		private List<Instant> prevCallsInstances = new LinkedList<>();;
		
		public CallTimestampInfo(String price, Instant updatedTime) {
            this.originalPrice = price;
            this.lastUpdatedInstance = updatedTime;
		}
	}
	
	private Map<String, CallTimestampInfo> countMapById = new HashMap<>();
	
	@Autowired
	public InventoryServiceImpl(@Value("#{T(java.lang.Integer).parseInt(${app.surge.pricing.duration.ms})}") Long surgePriceDuration, 
            @Value("#{T(java.lang.Integer).parseInt(${app.calls.surge.call.limit})}") Integer surgePriceCallsThreshold,
            final Clock clock) {
		InputStream inventoryStream = getClass().getResourceAsStream("/Inventory.json");
		try {
			List<Item> readItems = jsonMapper.readValue(inventoryStream, new TypeReference<List<Item>>() { });
			readItems.stream().forEach(item -> inventoryItems.put(item.getId(), item));
		} catch (IOException e) {
            throw new ServiceException("Inventory read exception.");
		}
		this.surgePriceDuration = surgePriceDuration;
		this.surgePriceCallsThreshold = surgePriceCallsThreshold;
		this.clock = clock;
	}
	
	@Override
	public List<Item> getInventoryList() {
        return new ArrayList<>(inventoryItems.values());
	}

	@Override
	public ItemRequestResponse buyItems(ItemRequestResponse req) {
        float sum = 0;
        float providedMoney = Float.parseFloat(req.getMoney());
        ItemRequestResponse resp = new ItemRequestResponse();
        List<Item> respItems = new ArrayList<>();
        for (Item reqItem : req.getRequestedItems()) {
        	if (inventoryItems.containsKey(reqItem.getId())) {
        		Item inventoryItem = inventoryItems.get(reqItem.getId());
        		if (Float.parseFloat(reqItem.getQuantity()) > Float.parseFloat(inventoryItem.getQuantity())) {
        			throw new IllegalArgumentException("Not enough quantity available. Available quantity: " + inventoryItem.getQuantity() +
        					" for " + inventoryItem.getName());
        		}
        		Item respItem = new Item(inventoryItem);
        		respItem.setQuantity(reqItem.getQuantity());
                respItems.add(respItem);
        		sum = sum + Float.parseFloat(inventoryItem.getPrice()) * Integer.parseInt(reqItem.getQuantity());
        		if (sum > providedMoney) {
        			throw new IllegalArgumentException("Money provided is not sufficient.");
        		}
        	}
        }
        resp.setMoney(String.format("%.2f", providedMoney - sum));
        resp.setRequestedItems(respItems);
        
        // Update the reduced quantities in inventory.
        for (Item reqItem : req.getRequestedItems()) {
        	if (inventoryItems.containsKey(reqItem.getId())) {
        		Item inventoryItem = inventoryItems.get(reqItem.getId());
        		int quantity = Integer.parseInt(inventoryItem.getQuantity());
        		int reducedQuantity = quantity - Integer.parseInt(reqItem.getQuantity());
        		inventoryItem.setQuantity(Integer.toString(reducedQuantity));
        	}
        }
        return resp;
	}

	@Override
	public Item getInventoryItemById(String id) {
		if (inventoryItems.containsKey(id) && countMapById.containsKey(id)) {
			CallTimestampInfo callTimeInfo = countMapById.get(id);
			Instant lastUpdatedTime = callTimeInfo.lastUpdatedInstance;
			ListIterator<Instant> iter = callTimeInfo.prevCallsInstances.listIterator();
			Instant now = clock.instant();
			while ( iter.hasNext() ) {
				Instant callTime = iter.next();
				if (Duration.between(callTime, now).toMillis() > 60000L) {
					iter.remove();
				}
			}
			callTimeInfo.prevCallsInstances.add(now);
			// If the number of requests for this item is more than threshold
			if (callTimeInfo.prevCallsInstances.size() > surgePriceCallsThreshold && 
					(lastUpdatedTime == null || Duration.between(lastUpdatedTime, now).toMillis() > surgePriceDuration)) {
				Float price = Float.parseFloat(inventoryItems.get(id).getPrice());
				price = price + price * (0.10f);
				inventoryItems.get(id).setPrice(String.format("%.2f", price));
				callTimeInfo.lastUpdatedInstance = now;
			} else if (callTimeInfo.prevCallsInstances.size() < surgePriceCallsThreshold && 
					lastUpdatedTime != null && Duration.between(lastUpdatedTime, now).toMillis() > surgePriceDuration) {
				inventoryItems.get(id).setPrice(callTimeInfo.originalPrice);
			}
			countMapById.put(id, callTimeInfo);
		} else if (inventoryItems.containsKey(id) && !countMapById.containsKey(id)){
			CallTimestampInfo callTimestampInfo = new CallTimestampInfo(inventoryItems.get(id).getPrice(), null);
			Instant instant = clock.instant();
			callTimestampInfo.prevCallsInstances.add(instant);
			countMapById.put(id, callTimestampInfo);
		}
        return inventoryItems.get(id);
	}

}
