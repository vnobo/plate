package com.plate.boot.relational.dictionaries;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST controller for managing data dictionaries.
 * Provides RESTful API endpoints for CRUD operations on dictionary entries.
 * <p>
 * Endpoints:
 * - GET /rel/dictionaries - Search and paginate dictionaries
 * - GET /rel/dictionaries/type/{type} - Get dictionaries by type
 * - GET /rel/dictionaries/children/{pcode} - Get child dictionaries
 * - POST /rel/dictionaries - Create a new dictionary
 * - PUT /rel/dictionaries - Update an existing dictionary
 * - DELETE /rel/dictionaries - Delete a dictionary
 * <p>
 * All endpoints support multi-tenancy and caching for improved performance.
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Log4j2
@RestController
@RequestMapping("/rel/dictionaries")
@RequiredArgsConstructor
@Tag(name = "Dictionaries", description = "Dictionary management APIs for managing system data dictionaries and configurations")
public class DictionariesController {

    private final DictionariesService dictionariesService;

    /**
     * Searches and paginates dictionaries based on query parameters.
     * Supports filtering by type, key, enabled status, and full-text search.
     *
     * @param request  Query parameters for filtering
     * @param pageable Pagination parameters (page, size, sort)
     * @return A Mono emitting a Page of dictionaries
     * <p>
     * Example:
     * GET /rel/dictionaries?dictType=USER_STATUS&enabled=true&page=0&size=20
     */
    @Operation(summary = "Search dictionaries", description = "Search and paginate dictionaries with filtering support")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved dictionaries",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    @GetMapping
    public Mono<Page<Dictionary>> search(
            @Parameter(description = "Query parameters for filtering dictionaries") DictionaryReq request,
            @Parameter(description = "Pagination parameters (page, size, sort)") Pageable pageable) {
        log.debug("Search dictionaries request: {}, pageable: {}", request, pageable);

        if (pageable.isUnpaged()) {
            pageable = PageRequest.of(0, 20);
        }

        return this.dictionariesService.page(request, pageable);
    }

    /**
     * Retrieves all dictionaries of a specific type.
     * Optionally filters by enabled status.
     *
     * @param tenantCode Tenant UUID (required)
     * @param dictType   Dictionary type (required)
     * @param enabled    Optional - if true, only returns enabled dictionaries
     * @return A Flux of dictionaries ordered by sortNo
     * <p>
     * Example:
     * GET /rel/dictionaries/type/USER_STATUS?tenantCode=xxx&enabled=true
     */
    @Operation(summary = "Get dictionaries by type", description = "Retrieve all dictionaries of a specific type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved dictionaries"),
            @ApiResponse(responseCode = "404", description = "Dictionary type not found")
    })
    @GetMapping("/type/{dictType}")
    public Flux<Dictionary> findByType(
            @Parameter(description = "Tenant UUID", required = true) @RequestParam UUID tenantCode,
            @Parameter(description = "Dictionary type", required = true, example = "USER_STATUS") @PathVariable String dictType,
            @Parameter(description = "Filter by enabled status") @RequestParam(required = false) Boolean enabled) {

        log.debug("Find dictionaries by type: {}, tenant: {}, enabled: {}",
                dictType, tenantCode, enabled);

        if (enabled != null && enabled) {
            return this.dictionariesService.findEnabledByType(tenantCode, dictType);
        }

        return this.dictionariesService.findByType(tenantCode, dictType);
    }

    /**
     * Retrieves child dictionaries under a parent.
     * Supports hierarchical dictionary structures.
     *
     * @param pcode Parent dictionary code UUID
     * @return A Flux of child dictionaries ordered by sortNo
     * <p>
     * Example:
     * GET /rel/dictionaries/children/550e8400-e29b-41d4-a716-446655440000
     */
    @Operation(summary = "Get child dictionaries", description = "Retrieve child dictionaries under a parent")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved child dictionaries")
    @GetMapping("/children/{pcode}")
    public Flux<Dictionary> findChildren(
            @Parameter(description = "Parent dictionary code UUID", required = true) @PathVariable UUID pcode) {
        log.debug("Find children dictionaries for parent: {}", pcode);
        return this.dictionariesService.findChildren(pcode);
    }

    /**
     * Creates a new dictionary entry.
     * Validates uniqueness of tenant + type + key combination.
     *
     * @param request Dictionary creation request with validation
     * @return A Mono emitting the created dictionary
     * <p>
     * Example POST body:
     * {
     * "tenantCode": "00000000-0000-0000-0000-000000000000",
     * "dictType": "USER_STATUS",
     * "dictKey": "ACTIVE",
     * "dictValue": "1",
     * "dictLabel": "Active",
     * "sortNo": 1,
     * "enabled": true
     * }
     */
    @Operation(summary = "Create dictionary", description = "Create a new dictionary entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dictionary created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "409", description = "Dictionary already exists")
    })
    @PostMapping
    public Mono<Dictionary> add(
            @Parameter(description = "Dictionary creation request", required = true)
            @Valid @RequestBody DictionaryReq request) {
        log.info("Add dictionary request: {}", request);
        return this.dictionariesService.add(request);
    }

    /**
     * Updates an existing dictionary entry.
     * Requires the dictionary code to identify the entry to update.
     *
     * @param request Dictionary update request with validation
     * @return A Mono emitting the updated dictionary
     * <p>
     * Example PUT body:
     * {
     * "code": "550e8400-e29b-41d4-a716-446655440000",
     * "dictValue": "2",
     * "dictLabel": "Updated Label",
     * "enabled": false
     * }
     */
    @Operation(summary = "Update dictionary", description = "Update an existing dictionary entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dictionary updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "404", description = "Dictionary not found")
    })
    @PutMapping
    public Mono<Dictionary> modify(
            @Parameter(description = "Dictionary update request", required = true)
            @Valid @RequestBody DictionaryReq request) {
        log.info("Modify dictionary request: {}", request);

        if (request.getCode() == null) {
            return Mono.error(new IllegalArgumentException("Dictionary code is required for modification"));
        }

        return this.dictionariesService.modify(request);
    }

    /**
     * Deletes a dictionary entry.
     * Requires the dictionary code to identify the entry to delete.
     *
     * @param request Dictionary delete request containing the code
     * @return A Mono indicating completion
     * <p>
     * Example DELETE body:
     * {
     * "code": "550e8400-e29b-41d4-a716-446655440000"
     * }
     */
    @Operation(summary = "Delete dictionary", description = "Delete a dictionary entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dictionary deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "404", description = "Dictionary not found")
    })
    @DeleteMapping
    public Mono<Void> delete(
            @Parameter(description = "Dictionary delete request", required = true)
            @RequestBody DictionaryReq request) {
        log.warn("Delete dictionary request: {}", request);

        if (request.getCode() == null) {
            return Mono.error(new IllegalArgumentException("Dictionary code is required for deletion"));
        }

        return this.dictionariesService.delete(request);
    }

    /**
     * Batch imports dictionary entries.
     * Useful for initial data loading or bulk updates.
     *
     * @param dictionaries Array of dictionary entries to import
     * @return A Flux emitting all imported dictionaries
     * <p>
     * Example POST /rel/dictionaries/batch body:
     * [
     * {"dictType": "STATUS", "dictKey": "ACTIVE", "dictValue": "1", "dictLabel": "Active"},
     * {"dictType": "STATUS", "dictKey": "INACTIVE", "dictValue": "0", "dictLabel": "Inactive"}
     * ]
     */
    @Operation(summary = "Batch import dictionaries", description = "Import multiple dictionary entries at once")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dictionaries imported successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PostMapping("/batch")
    public Flux<Dictionary> batchAdd(
            @Parameter(description = "Array of dictionary entries to import", required = true)
            @Valid @RequestBody DictionaryReq[] dictionaries) {
        log.info("Batch add {} dictionaries", dictionaries.length);

        return Flux.fromArray(dictionaries)
                .flatMap(this.dictionariesService::add);
    }
}
