package com.backend.topperfriendweb.controller;

import com.backend.topperfriendweb.dto.collection.*;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.UserRepository;
import com.backend.topperfriendweb.service.CollectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/collections")
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
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Create collection
    @PostMapping
    public ResponseEntity<?> createCollection(@Valid @RequestBody CreateCollectionRequest request) {
        try {
            User user = getLoggedInUser();
            CollectionDTO collection = collectionService.createCollection(request, user);
            return ResponseEntity.ok(Map.of("success", true, "collection", collection));
        } catch (Exception e) {
            log.error("Error creating collection", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // Get all collections for user
    @GetMapping
    public ResponseEntity<?> getUserCollections() {
        try {
            User user = getLoggedInUser();
            List<CollectionDTO> collections = collectionService.getUserCollections(user);
            return ResponseEntity.ok(Map.of("success", true, "collections", collections));
        } catch (Exception e) {
            log.error("Error getting user collections", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // Get collection by ID with items
    @GetMapping("/{id}")
    public ResponseEntity<?> getCollectionById(@PathVariable Long id) {
        try {
            User user = getLoggedInUser();
            CollectionDTO collection = collectionService.getCollectionById(id, user);
            return ResponseEntity.ok(Map.of("success", true, "collection", collection));
        } catch (RuntimeException e) {
            log.error("Error getting collection by ID", e);
            return ResponseEntity.status(404).body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting collection by ID", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // Update collection
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateCollection(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCollectionRequest request) {
        try {
            User user = getLoggedInUser();
            CollectionDTO collection = collectionService.updateCollection(id, request, user);
            return ResponseEntity.ok(Map.of("success", true, "collection", collection));
        } catch (RuntimeException e) {
            log.error("Error updating collection", e);
            return ResponseEntity.status(404).body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating collection", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // Delete collection
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCollection(@PathVariable Long id) {
        try {
            User user = getLoggedInUser();
            collectionService.deleteCollection(id, user);
            return ResponseEntity.ok(Map.of("success", true, "message", "Collection deleted successfully"));
        } catch (RuntimeException e) {
            log.error("Error deleting collection", e);
            return ResponseEntity.status(404).body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting collection", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // Add item to collection
    @PostMapping("/{collectionId}/items")
    public ResponseEntity<?> addItemToCollection(
            @PathVariable Long collectionId,
            @Valid @RequestBody AddItemToCollectionRequest request) {
        try {
            User user = getLoggedInUser();
            CollectionItemDTO item = collectionService.addItemToCollection(collectionId, request, user);
            return ResponseEntity.ok(Map.of("success", true, "item", item));
        } catch (RuntimeException e) {
            log.error("Error adding item to collection", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error adding item to collection", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // Remove item from collection
    @DeleteMapping("/{collectionId}/items/{itemType}/{itemId}")
    public ResponseEntity<?> removeItemFromCollection(
            @PathVariable Long collectionId,
            @PathVariable String itemType,
            @PathVariable Long itemId) {
        try {
            User user = getLoggedInUser();
            collectionService.removeItemFromCollection(collectionId, itemType, itemId, user);
            return ResponseEntity.ok(Map.of("success", true, "message", "Item removed from collection"));
        } catch (RuntimeException e) {
            log.error("Error removing item from collection", e);
            return ResponseEntity.status(404).body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error removing item from collection", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}