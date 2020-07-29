package com.gildedroses.inventory.operations.controller.test;

import java.util.List;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.gildedroses.inventory.operations.controller.InventoryController;
import com.gildedroses.inventory.operations.model.Item;
import com.gildedroses.inventory.operations.model.ItemRequestResponse;
import com.gildedroses.inventory.operations.service.InventoryService;
import com.gildedroses.inventory.operations.utils.test.TestUtils;

@ExtendWith(SpringExtension.class)
@AutoConfigureJsonTesters
@WebMvcTest(InventoryController.class)
public class InventoryControllerTest {
    @Autowired
    private MockMvc mockmvc;
    
    @Autowired
    protected WebApplicationContext wac;
    
    @Autowired
    private JacksonTester<Item> jsonItem;
    
    @MockBean
    private InventoryService inventoryService;
    
    @Before
    public void applySecurity() {
        this.mockmvc = MockMvcBuilders.webAppContextSetup(wac)
            .apply(springSecurity())
            .build();
    }
    
    @Test
    public void testGetAllInventory() throws Exception {
    	List<Item> inventoryItems = TestUtils.getFileAsJsonArray("InventoryTest.json", Item.class);
    	String inventoryItemsAsString = TestUtils.getObjectAsString(inventoryItems);
    	Mockito.when(inventoryService.getInventoryList()).thenReturn(inventoryItems);
    	
    	MockHttpServletResponse response = mockmvc.perform(
                 get("/inventory"))
    			 .andDo(MockMvcResultHandlers.print())
                 .andReturn().getResponse();
                 
    	assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    	assertThat(response.getContentAsString()).isEqualTo(inventoryItemsAsString);
    }
    
    @Test
    public void testGetInventoryById() throws Exception {
    	List<Item> inventoryItems = TestUtils.getFileAsJsonArray("InventoryTest.json", Item.class);

    	Mockito.when(inventoryService.getInventoryItemById(Mockito.anyString())).thenReturn(inventoryItems.get(1));
    	
    	MockHttpServletResponse response = mockmvc.perform(
                 get("/inventory/2"))
    			 .andDo(MockMvcResultHandlers.print())
                 .andReturn().getResponse();
                 
    	assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    	assertThat(response.getContentAsString()).isEqualTo(jsonItem.write(new Item("2", "Rice", "5.00", "6", null)).getJson());
    }
    
    @Test
    public void testBuyItem() throws Exception {
    	ItemRequestResponse itemResp = TestUtils.getFileAsJson("ItemResponse.json", ItemRequestResponse.class);
    	ItemRequestResponse itemReq = TestUtils.getFileAsJson("ItemRequest.json", ItemRequestResponse.class);
    	String reqContent = TestUtils.getObjectAsString(itemReq);
    	String respContent = TestUtils.getObjectAsString(itemResp);

    	Mockito.when(inventoryService.buyItems(Mockito.any(ItemRequestResponse.class))).thenReturn(itemResp);
    	
    	MockHttpServletResponse response = mockmvc.perform(
                post("/buy")
                .with(httpBasic("tariq", "gilded-roses"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqContent))
   			 .andDo(MockMvcResultHandlers.print())
   			 .andReturn().getResponse();
    	
    	assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    	assertThat(response.getContentAsString()).isEqualTo(respContent);
    }
    
    @Test
    public void testBuyItemUnauthorized() throws Exception {
    	ItemRequestResponse itemReq = TestUtils.getFileAsJson("ItemRequest.json", ItemRequestResponse.class);
    	String reqContent = TestUtils.getObjectAsString(itemReq);
    	
    	MockHttpServletResponse response = mockmvc.perform(
                post("/buy")
                .with(httpBasic("user", "password"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqContent))
   			 .andDo(MockMvcResultHandlers.print())
   			 .andReturn().getResponse();

    	assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }
}
