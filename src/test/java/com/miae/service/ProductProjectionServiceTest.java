package com.miae.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.miae.api.dto.ProductRequest;
import com.miae.graph.repository.ManufacturingGraphRepository;
import org.junit.jupiter.api.Test;

class ProductProjectionServiceTest {

    @Test
    void delegatesProductUpsertToRepository() {
        ManufacturingGraphRepository repository = mock(ManufacturingGraphRepository.class);
        ProductProjectionService service = new ProductProjectionService(repository);
        ProductRequest request = new ProductRequest("P100", "SENSOR-100", "Industrial Sensor");

        String id = service.upsert(request);

        assertThat(id).isEqualTo("P100");
        verify(repository).upsertProduct(request);
    }
}
