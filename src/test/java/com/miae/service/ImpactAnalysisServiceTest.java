package com.miae.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.miae.analysis.ImpactEntityType;
import com.miae.analysis.ImpactStrategy;
import com.miae.api.dto.ImpactAnalysisRequest;
import com.miae.exception.UnsupportedImpactTypeException;
import java.util.List;
import org.junit.jupiter.api.Test;

class ImpactAnalysisServiceTest {

    @Test
    void dispatchesToMatchingStrategy() {
        ImpactStrategy strategy = mock(ImpactStrategy.class);
        Object expected = new Object();
        when(strategy.supports()).thenReturn(ImpactEntityType.COMPONENT);
        when(strategy.analyze("PCB-A")).thenReturn(expected);

        ImpactAnalysisService service = new ImpactAnalysisService(List.of(strategy));

        Object actual = service.analyze(new ImpactAnalysisRequest(ImpactEntityType.COMPONENT, "PCB-A"));

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void rejectsMissingStrategy() {
        ImpactAnalysisService service = new ImpactAnalysisService(List.of());

        assertThatThrownBy(() -> service.analyze(new ImpactAnalysisRequest(ImpactEntityType.SUPPLIER, "SUP-ABC")))
                .isInstanceOf(UnsupportedImpactTypeException.class);
    }
}
