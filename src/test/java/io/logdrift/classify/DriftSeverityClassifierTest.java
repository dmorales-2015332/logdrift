package io.logdrift.classify;

import io.logdrift.drift.DriftEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DriftSeverityClassifierTest {

    private DriftSeverityClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new DriftSeverityClassifier(
            Map.of(
                DriftSeverity.CRITICAL, Set.of("userId", "traceId"),
                DriftSeverity.HIGH, Set.of("timestamp", "level")
            ),
            50
        );
    }

    private DriftEvent mockEvent(String fieldName, String driftType, int occurrences) {
        DriftEvent event = mock(DriftEvent.class);
        when(event.getFieldName()).thenReturn(fieldName);
        when(event.getDriftType()).thenReturn(driftType);
        when(event.getOccurrenceCount()).thenReturn(occurrences);
        return event;
    }

    @Test
    void classifiesCriticalFieldAsCritical() {
        DriftEvent event = mockEvent("userId", "TYPE_CHANGE", 1);
        DriftClassification result = classifier.classify(event);
        assertEquals(DriftSeverity.CRITICAL, result.getSeverity());
    }

    @Test
    void classifiesHighPriorityFieldAsHigh() {
        DriftEvent event = mockEvent("timestamp", "FIELD_ADDED", 1);
        DriftClassification result = classifier.classify(event);
        assertEquals(DriftSeverity.HIGH, result.getSeverity());
    }

    @Test
    void classifiesHighVolumeUnknownFieldAsHigh() {
        DriftEvent event = mockEvent("metadata", "FIELD_ADDED", 75);
        DriftClassification result = classifier.classify(event);
        assertEquals(DriftSeverity.HIGH, result.getSeverity());
    }

    @Test
    void classifiesRemovedUnknownFieldAsMedium() {
        DriftEvent event = mockEvent("debugInfo", "FIELD_REMOVED", 3);
        DriftClassification result = classifier.classify(event);
        assertEquals(DriftSeverity.MEDIUM, result.getSeverity());
    }

    @Test
    void classifiesLowImpactEventAsLow() {
        DriftEvent event = mockEvent("optionalTag", "FIELD_ADDED", 2);
        DriftClassification result = classifier.classify(event);
        assertEquals(DriftSeverity.LOW, result.getSeverity());
    }

    @Test
    void classificationContainsRationale() {
        DriftEvent event = mockEvent("userId", "TYPE_CHANGE", 1);
        DriftClassification result = classifier.classify(event);
        assertNotNull(result.getRationale());
        assertTrue(result.getRationale().contains("userId"));
    }

    @Test
    void isAtLeastReturnsTrueForEqualSeverity() {
        DriftEvent event = mockEvent("userId", "TYPE_CHANGE", 1);
        DriftClassification result = classifier.classify(event);
        assertTrue(result.isAtLeast(DriftSeverity.CRITICAL));
        assertTrue(result.isAtLeast(DriftSeverity.HIGH));
    }

    @Test
    void throwsOnNullEvent() {
        assertThrows(IllegalArgumentException.class, () -> classifier.classify(null));
    }

    @Test
    void defaultConstructorProducesWorkingClassifier() {
        DriftSeverityClassifier defaultClassifier = new DriftSeverityClassifier();
        DriftEvent event = mockEvent("traceId", "TYPE_CHANGE", 1);
        DriftClassification result = defaultClassifier.classify(event);
        assertEquals(DriftSeverity.CRITICAL, result.getSeverity());
    }
}
