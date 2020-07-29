package com.gildedroses.inventory.operations.service.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gildedroses.inventory.operations.model.Item;
import com.gildedroses.inventory.operations.model.ItemRequestResponse;
import com.gildedroses.inventory.operations.service.InventoryServiceImpl;
import com.gildedroses.inventory.operations.utils.test.TestUtils;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class InventoryServiceTest {

	private Clock mockClock;
	
	@InjectMocks
	InventoryServiceImpl inventoryServiceImpl;
	
	public InventoryServiceTest() {
		mockClock = Mockito.mock(Clock.class);
		inventoryServiceImpl = new InventoryServiceImpl(60000L, 3, mockClock);
	}
	
	@Test
	public void testGetInventoryItemById() throws JsonProcessingException {
		Instant first = Instant.now();                  
        Instant second = first.plusSeconds(5);          
        Instant third = second.plusSeconds(6);
        Instant fourth = third.plusSeconds(9);
                
        Mockito.when(mockClock.instant()).thenReturn(first, second, third, fourth);

        Float originalPrice = Float.parseFloat(inventoryServiceImpl.getInventoryItemById("1").getPrice());
        inventoryServiceImpl.getInventoryItemById("1");
        inventoryServiceImpl.getInventoryItemById("1");
        Item expectedSurgePrice = inventoryServiceImpl.getInventoryItemById("1");

        Float expected = originalPrice + 0.10f * originalPrice;
        //Price expected to increase as we had more than 3 calls in the last minute.
        assertThat(String.format("%.2f", expected)).isEqualTo(expectedSurgePrice.getPrice());
        
        Instant fifth = fourth.plusSeconds(5);          
        Instant sixth = fifth.plusSeconds(8);
        Mockito.when(mockClock.instant()).thenReturn(fifth, sixth);
        inventoryServiceImpl.getInventoryItemById("1");
        inventoryServiceImpl.getInventoryItemById("1");
        
        //Price should not increase because its not yet 1 minute since last update.
        assertThat(String.format("%.2f", expected)).isEqualTo(expectedSurgePrice.getPrice());
        
        Instant seventh = sixth.plusSeconds(30);          
        Instant eighth = seventh.plusSeconds(20);
        Mockito.when(mockClock.instant()).thenReturn(seventh, eighth);

        inventoryServiceImpl.getInventoryItemById("1");
        inventoryServiceImpl.getInventoryItemById("1");
        
        //Price should update now. Last update happened a minute ago
        // and we have made three calls in the last minute.
        expected = expected + 0.10f * expected;

        assertThat(String.format("%.2f", expected)).isEqualTo(expectedSurgePrice.getPrice());
        
        Instant ninth = eighth.plusSeconds(30);          
        Instant tenth = ninth.plusSeconds(35);
        Mockito.when(mockClock.instant()).thenReturn(ninth, tenth);
        
        inventoryServiceImpl.getInventoryItemById("1");
        inventoryServiceImpl.getInventoryItemById("1");
        // Price should not increase now but go back to original level.
        // Last update happened more than a minute ago
        // We have not made three calls in the last minute. So surge is over.
        assertThat(String.format("%.2f", originalPrice)).isEqualTo(expectedSurgePrice.getPrice());
	}
	
	@Test
	public void testBuyItem() {
		ItemRequestResponse itemReq = TestUtils.getFileAsJson("ItemRequest.json", ItemRequestResponse.class);
		
		ItemRequestResponse itemResp = inventoryServiceImpl.buyItems(itemReq);
		assertThat(itemResp.getRequestedItems().size()).isEqualTo(2);
		// 40 - (3.40*4) - (5.00*3) = 11.40
		assertThat(itemResp.getMoney()).isEqualTo("11.40");
	}
	
	@Test
	public void testBuyItemWithSurge() {
        ItemRequestResponse itemReq = TestUtils.getFileAsJson("ItemRequest.json", ItemRequestResponse.class);
        itemReq.setMoney("80.00");
		
		ItemRequestResponse itemResp = inventoryServiceImpl.buyItems(itemReq);
		assertThat(itemResp.getRequestedItems().size()).isEqualTo(2);
		// 80 - (3.40*4) - (5.00*3) = 51.40
		assertThat(itemResp.getMoney()).isEqualTo("51.40");
        itemReq.setMoney("51.40");

		Instant first = Instant.now();                  
        Instant second = first.plusSeconds(5);          
        Instant third = second.plusSeconds(6);
        Instant fourth = third.plusSeconds(9);
                
        Mockito.when(mockClock.instant()).thenReturn(first, second, third, fourth);
        
        inventoryServiceImpl.getInventoryItemById("1");
        inventoryServiceImpl.getInventoryItemById("1");
        inventoryServiceImpl.getInventoryItemById("1");
        inventoryServiceImpl.getInventoryItemById("1");
        
        itemResp = inventoryServiceImpl.buyItems(itemReq);
		assertThat(itemResp.getRequestedItems().size()).isEqualTo(2);
		// 51.40 - (3.74*4) - (5.00*3) = 21.44
		assertThat(itemResp.getMoney()).isEqualTo("21.44");
        itemReq.setMoney("21.44");
	}
	
	@Test
	public void buyItemsInsufficientMoney() {
		ItemRequestResponse itemReq = TestUtils.getFileAsJson("ItemRequest.json", ItemRequestResponse.class);
        itemReq.setMoney("25.00");
        
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> inventoryServiceImpl.buyItems(itemReq)
         );

         assertThat(thrown.getMessage()).contains("Money provided is not sufficient.");
	}

	@Test
	public void buyItemsInsuficientItemsInInventory() {
		ItemRequestResponse itemReq = TestUtils.getFileAsJson("ItemRequest.json", ItemRequestResponse.class);
        itemReq.setMoney("96.00");
        
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> {inventoryServiceImpl.buyItems(itemReq);
                    inventoryServiceImpl.buyItems(itemReq);
                    inventoryServiceImpl.buyItems(itemReq);
                }
         );

         assertThat(thrown.getMessage()).contains("Not enough quantity available. Available quantity: ");
	}
 }
