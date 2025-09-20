package com.backend.topperfriendweb.controller;

import com.backend.topperfriendweb.dto.CommonResponse;
import com.backend.topperfriendweb.dto.collection.*;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.UserRepository;
import com.backend.topperfriendweb.service.collection.CollectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collections")
@PreAuthorize("isAuthenticated()")
@Validated
@RequiredArgsConstructor
@Slf4j
public class CollectionController {

    private final CollectionService collectionService;
    private final UserRepository userRepository;

    // Helper to get logged-in user (consistent with other controllers)
    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = (String) auth.getPrincipal();
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    // Create collection
    @PostMapping
    public ResponseEntity<CommonResponse<CollectionDTO>> createCollection(
            @Valid @RequestBody CreateCollectionRequest request) {
        User user = getLoggedInUser();
        CollectionDTO collection = collectionService.createCollection(request, user);
        return ResponseEntity.ok(CommonResponse.success("Collection created successfully", collection));
    }

    // Get all collections for user
    @GetMapping
    public ResponseEntity<CommonResponse<List<CollectionDTO>>> getUserCollections() {
        User user = getLoggedInUser();
        List<CollectionDTO> collections = collectionService.getUserCollections(user);
        return ResponseEntity.ok(CommonResponse.success("Collections retrieved successfully", collections));
    }

    // Get collection by ID with items
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<CollectionDTO>> getCollectionById(@PathVariable Long id) {
        User user = getLoggedInUser();
        CollectionDTO collection = collectionService.getCollectionById(id, user);
        return ResponseEntity.ok(CommonResponse.success("Collection retrieved successfully", collection));
    }

    // Update collection
    @PatchMapping("/{id}")
    public ResponseEntity<CommonResponse<CollectionDTO>> updateCollection(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCollectionRequest request) {
        User user = getLoggedInUser();
        CollectionDTO collection = collectionService.updateCollection(id, request, user);
        return ResponseEntity.ok(CommonResponse.success("Collection updated successfully", collection));
    }

    // Delete collection
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<String>> deleteCollection(@PathVariable Long id) {
        User user = getLoggedInUser();
        collectionService.deleteCollection(id, user);
        return ResponseEntity.ok(CommonResponse.success("Collection deleted successfully"));
    }

    // Add item to collection
    @PostMapping("/{collectionId}/items")
    public ResponseEntity<CommonResponse<CollectionItemDTO>> addItemToCollection(
            @PathVariable Long collectionId,
            @Valid @RequestBody AddItemToCollectionRequest request) {
        User user = getLoggedInUser();
        CollectionItemDTO item = collectionService.addItemToCollection(collectionId, request, user);
        return ResponseEntity.ok(CommonResponse.success("Item added to collection successfully", item));
    }

    // Remove item from collection
    @DeleteMapping("/{collectionId}/items/{itemType}/{itemId}")
    public ResponseEntity<CommonResponse<String>> removeItemFromCollection(
            @PathVariable Long collectionId,
            @PathVariable String itemType,
            @PathVariable Long itemId) {
        User user = getLoggedInUser();
        collectionService.removeItemFromCollection(collectionId, itemType, itemId, user);
        return ResponseEntity.ok(CommonResponse.success("Item removed from collection successfully"));
    }
}