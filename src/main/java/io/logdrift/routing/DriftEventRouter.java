package io.logdrift.routing;

import io.logdrift.drift.DriftEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Routes drift events to registered handlers based on service name and event type.
 */
public class DriftEventRouter {

    private final Map<String, List<Consumer<DriftEvent>>> serviceHandlers = new ConcurrentHashMap<>();
    private final List<Consumer<DriftEvent>> globalHandlers = new ArrayList<>();

    public void registerServiceHandler(String serviceName, Consumer<DriftEvent> handler) {
        if (serviceName == null || serviceName.isBlank()) {
            throw new IllegalArgumentException("Service name must not be blank");
        }
        if (handler == null) {
            throw new IllegalArgumentException("Handler must not be null");
        }
        serviceHandlers.computeIfAbsent(serviceName, k -> new ArrayList<>()).add(handler);
    }

    public void registerGlobalHandler(Consumer<DriftEvent> handler) {
        if (handler == null) {
            throw new IllegalArgumentException("Handler must not be null");
        }
        globalHandlers.add(handler);
    }

    public void route(DriftEvent event) {
        if (event == null) {
            return;
        }
        globalHandlers.forEach(h -> h.accept(event));
        String service = event.getServiceName();
        if (service != null && serviceHandlers.containsKey(service)) {
            serviceHandlers.get(service).forEach(h -> h.accept(event));
        }
    }

    public void routeAll(List<DriftEvent> events) {
        if (events == null) return;
        events.forEach(this::route);
    }

    public int handlerCount(String serviceName) {
        return serviceHandlers.getOrDefault(serviceName, List.of()).size();
    }

    public int globalHandlerCount() {
        return globalHandlers.size();
    }

    public void clearServiceHandlers(String serviceName) {
        serviceHandlers.remove(serviceName);
    }

    public void clearAll() {
        serviceHandlers.clear();
        globalHandlers.clear();
    }
}
