package org.uvhnael.ktal.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uvhnael.ktal.constants.AppConstants;
import org.uvhnael.ktal.dto.response.ApiResponse;
import org.uvhnael.ktal.model.Service;
import org.uvhnael.ktal.service.ServiceService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ServiceController {

    private final ServiceService serviceService;

    /**
     * Retrieves all services with optional filtering
     *
     * @param search     Search in title, description, and features
     * @param priceRange Filter by price range
     * @return List of services matching the criteria
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Service>>> getAllServices(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String priceRange) {

        log.info("GET /api/v1/services - Request params: search={}, priceRange={}", search, priceRange);

        try {
            List<Service> services = serviceService.findAll();
            int originalSize = services.size();

            // Apply search filter if provided
            if (search != null && !search.isEmpty()) {
                services = services.stream()
                        .filter(service -> service.getTitle().toLowerCase().contains(search.toLowerCase()) ||
                                service.getDescription().toLowerCase().contains(search.toLowerCase()) ||
                                service.getFeatures().toLowerCase().contains(search.toLowerCase()))
                        .toList();
                log.debug("{} search: {} -> {} services", AppConstants.LogMessages.DATA_FILTERED,
                        originalSize, services.size());
            }

            // Apply price range filter if provided
            if (priceRange != null && !priceRange.isEmpty()) {
                services = services.stream()
                        .filter(service -> service.getPrice() != null &&
                                service.getPrice().toLowerCase().contains(priceRange.toLowerCase()))
                        .toList();
                log.debug("{} priceRange '{}': {} services remaining", AppConstants.LogMessages.DATA_FILTERED,
                        priceRange, services.size());
            }

            log.info("GET /api/v1/services - Success: Retrieved {} services (filtered from {} total)",
                    services.size(), originalSize);
            return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.SERVICES_RETRIEVED, services));

        } catch (Exception e) {
            log.error("GET /api/v1/services - Error retrieving services: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve services: " + e.getMessage()));
        }
    }

    /**
     * Retrieves a specific service by ID
     *
     * @param id Service ID
     * @return Service details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Service>> getServiceById(@PathVariable Long id) {
        log.info("GET /api/v1/services/{} - Request to get service by ID", id);

        try {
            Service service = serviceService.findById(id).orElse(null);
            if (service != null) {
                log.info("GET /api/v1/services/{} - {}: Found service with title '{}'",
                        id, AppConstants.LogMessages.ENTITY_FOUND, service.getTitle());
                return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.SERVICE_RETRIEVED, service));
            } else {
                log.warn("GET /api/v1/services/{} - {}", id, AppConstants.LogMessages.ENTITY_NOT_FOUND);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("GET /api/v1/services/{} - Error retrieving service: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve service: " + e.getMessage()));
        }
    }

    /**
     * Retrieves featured services with configurable limit
     *
     * @param limit Maximum number of services to return
     * @return List of featured services
     */
    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<Service>>> getFeaturedServices(
            @RequestParam(defaultValue = "6") int limit) {

        // Use constants for default and max limits
        if (limit > AppConstants.Defaults.MAX_PAGE_SIZE) {
            limit = AppConstants.Defaults.DEFAULT_FEATURED_LIMIT;
        }

        log.info("GET /api/v1/services/featured - Request to get {} featured services", limit);

        try {
            List<Service> services = serviceService.findAll();
            List<Service> featuredServices = services.stream()
                    .limit(limit)
                    .toList();

            log.info("GET /api/v1/services/featured - Success: Retrieved {} featured services", featuredServices.size());
            return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.FEATURED_SERVICES_RETRIEVED, featuredServices));

        } catch (Exception e) {
            log.error("GET /api/v1/services/featured - Error retrieving featured services: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve featured services: " + e.getMessage()));
        }
    }

    /**
     * Retrieves all unique price ranges
     *
     * @return List of distinct price ranges
     */
    @GetMapping("/price-ranges")
    public ResponseEntity<ApiResponse<List<String>>> getPriceRanges() {
        log.info("GET /api/v1/services/price-ranges - Request to get price ranges");

        try {
            List<Service> services = serviceService.findAll();
            List<String> priceRanges = services.stream()
                    .map(Service::getPrice)
                    .distinct()
                    .filter(price -> price != null && !price.isEmpty())
                    .toList();

            log.info("GET /api/v1/services/price-ranges - Success: Retrieved {} unique price ranges", priceRanges.size());
            return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.PRICE_RANGES_RETRIEVED, priceRanges));

        } catch (Exception e) {
            log.error("GET /api/v1/services/price-ranges - Error retrieving price ranges: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve price ranges: " + e.getMessage()));
        }
    }

    /**
     * Retrieves service statistics
     *
     * @return Service statistics data
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getServiceStatistics() {
        log.info("GET /api/v1/services/statistics - Request to get service statistics");

        try {
            List<Service> services = serviceService.findAll();

            long totalServices = services.size();
            long servicesWithFeatures = services.stream()
                    .filter(service -> service.getFeatures() != null && !service.getFeatures().isEmpty())
                    .count();
            long servicesWithPrice = services.stream()
                    .filter(service -> service.getPrice() != null && !service.getPrice().isEmpty())
                    .count();

            Map<String, Object> statistics = Map.of(
                    "totalServices", totalServices,
                    "servicesWithFeatures", servicesWithFeatures,
                    "servicesWithPrice", servicesWithPrice,
                    "uniquePriceRanges", services.stream().map(Service::getPrice).distinct().count()
            );

            log.info("GET /api/v1/services/statistics - Success: Stats calculated - Total: {}, WithFeatures: {}, WithPrice: {}",
                    totalServices, servicesWithFeatures, servicesWithPrice);
            return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.STATISTICS_RETRIEVED, statistics));

        } catch (Exception e) {
            log.error("GET /api/v1/services/statistics - Error retrieving statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve service statistics: " + e.getMessage()));
        }
    }

    /**
     * Creates a new service
     *
     * @param service Service data to create
     * @return Success message
     */
    @PostMapping
    public ResponseEntity<ApiResponse<String>> createService(@RequestBody Service service) {
        log.info("POST /api/v1/services - Request to create service with title '{}'", service.getTitle());

        try {
            int result = serviceService.create(service);
            if (result > 0) {
                log.info("POST /api/v1/services - {}: Created service with title '{}'",
                        AppConstants.LogMessages.ENTITY_CREATED, service.getTitle());
                return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.SERVICE_CREATED,
                        "Created " + result + " record(s)"));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(AppConstants.Messages.ERROR_DATABASE_OPERATION));
            }
        } catch (Exception e) {
            log.error("POST /api/v1/services - Error creating service: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create service: " + e.getMessage()));
        }
    }

    /**
     * Updates an existing service
     *
     * @param id      Service ID
     * @param service Updated service data
     * @return Update result
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> updateService(@PathVariable Long id, @RequestBody Service service) {
        log.info("PUT /api/v1/services/{} - Request to update service", id);

        try {
            service.setId(id);
            int result = serviceService.update(service);
            if (result > 0) {
                log.info("PUT /api/v1/services/{} - {}: Updated {} record(s)",
                        id, AppConstants.LogMessages.ENTITY_UPDATED, result);
                return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.SERVICE_UPDATED,
                        "Updated " + result + " record(s)"));
            } else {
                log.warn("PUT /api/v1/services/{} - {}", id, AppConstants.LogMessages.ENTITY_NOT_FOUND);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("PUT /api/v1/services/{} - Error updating service: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update service: " + e.getMessage()));
        }
    }

    /**
     * Deletes a service
     *
     * @param id Service ID
     * @return Deletion result
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteService(@PathVariable Long id) {
        log.info("DELETE /api/v1/services/{} - Request to delete service", id);

        try {
            int result = serviceService.delete(id);
            if (result > 0) {
                log.info("DELETE /api/v1/services/{} - {}: Deleted {} record(s)",
                        id, AppConstants.LogMessages.ENTITY_DELETED, result);
                return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.SERVICE_DELETED,
                        "Deleted " + result + " record(s)"));
            } else {
                log.warn("DELETE /api/v1/services/{} - {}", id, AppConstants.LogMessages.ENTITY_NOT_FOUND);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("DELETE /api/v1/services/{} - Error deleting service: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete service: " + e.getMessage()));
        }
    }
}
