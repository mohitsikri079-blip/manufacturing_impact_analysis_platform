package com.miae.service;

import com.miae.analysis.ImpactEntityType;
import com.miae.analysis.ImpactStrategy;
import com.miae.api.dto.ImpactAnalysisRequest;
import com.miae.exception.UnsupportedImpactTypeException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class responsible for performing impact analysis based on the provided request.
 */
@Service
public class ImpactAnalysisService {

    private final Map<ImpactEntityType, ImpactStrategy> strategies;

    public ImpactAnalysisService(List<ImpactStrategy> strategies) {
        this.strategies = new EnumMap<>(ImpactEntityType.class);
        strategies.forEach(strategy -> this.strategies.put(strategy.supports(), strategy));
    }

    @Transactional(readOnly = true)
    public Object analyze(ImpactAnalysisRequest request) {
        ImpactStrategy strategy = strategies.get(request.entityType());
        if (strategy == null) {
            throw new UnsupportedImpactTypeException("Unsupported impact entity type: " + request.entityType());
        }
        return strategy.analyze(request.entityId());
    }
}
