package com.devsuperior.dscatalog.resources;

import com.devsuperior.dscatalog.DTO.ProductDTO;
import com.devsuperior.dscatalog.services.ProductService;
import com.devsuperior.dscatalog.services.exceptions.DataBaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.tests.Factory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductResource.class)
public class ProductResourceTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService service;

    @Autowired
    private ObjectMapper objectMapper;

    private PageImpl<ProductDTO> page;
    private ProductDTO productDTO;
    private long existingId;
    private long nonExistingId;
    private long dependentId;

    @BeforeEach
    void setUp() throws Exception {

        productDTO = Factory.createProductDto();
        page = new PageImpl<>(List.of(productDTO));
        existingId = 1L;
        nonExistingId = 1000L;
        dependentId = 500L;

        when(service.findAllPaged(any())).thenReturn(page);

        when(service.findById(existingId)).thenReturn(productDTO);
        when(service.findById(nonExistingId)).thenThrow(ResourceNotFoundException.class);

        when(service.update(eq(existingId), any())).thenReturn(productDTO);
        when(service.update(eq(nonExistingId), any())).thenThrow(ResourceNotFoundException.class);

        doNothing().when(service).delete(existingId);
        doThrow(ResourceNotFoundException.class).when(service).delete(nonExistingId);
        doThrow(DataBaseException.class).when(service).delete(dependentId);

        when(service.insert(any())).thenReturn(productDTO);

    }

    //Status ok: 200
    @Test
    public void findAllShouldReturnPage() throws Exception {

        ResultActions resultActions = mockMvc.perform(get("/products").accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk());
    }

    @Test
    public void findByIdShouldReturnProductWhenIdExisting() throws Exception {

        ResultActions resultActions = mockMvc.perform(get("/products/{id}", existingId).accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.id").exists());
        resultActions.andExpect(jsonPath("$.name").exists());
        resultActions.andExpect(jsonPath("$.description").exists());
    }

    @Test
    public void findByIdShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {

        ResultActions resultActions = mockMvc.perform(get("/products/{id}", nonExistingId).accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isNotFound());

    }

    @Test
    public void updateShouldReturnProductDTOWhenIdExisting() throws Exception {

        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions resultActions = mockMvc.perform(put("/products/{id}", existingId)
                .content(jsonBody)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.id").exists());
        resultActions.andExpect(jsonPath("$.name").exists());
        resultActions.andExpect(jsonPath("$.description").exists());

    }

    @Test
    public void updateShouldReturnNotFoundWhenIdDoesNotExisting() throws Exception {

        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions resultActions = mockMvc.perform(put("/products/{id}", nonExistingId)
                .content(jsonBody)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isNotFound());

    }

    @Test
    public void insertShouldReturnProductDTOCreated() throws Exception{

        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions resultActions = mockMvc.perform(post("/products")
                .content(jsonBody)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isCreated());
        resultActions.andExpect(jsonPath("$.id").exists());
        resultActions.andExpect(jsonPath("$.name").exists());
        resultActions.andExpect(jsonPath("$.description").exists());
    }

    @Test
    public void deleteShouldReturnNoContentWhenExistingID() throws Exception {

        ResultActions resultActions = mockMvc.perform(delete("/products/{id}", existingId)
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isNoContent());

    }

    @Test
    public void deleteShouldReturnNotFoundWhenIdDoesNotExisting() throws Exception {

        ResultActions resultActions = mockMvc.perform(delete("/products/{id}", nonExistingId)
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isNotFound());

    }



}
