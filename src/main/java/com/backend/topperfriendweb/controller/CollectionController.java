package com.backend.topperfriendweb.controller;

import com.backend.topperfriendweb.dto.CollectionItemDTO;
import com.backend.topperfriendweb.dto.NoteDTO;
import com.backend.topperfriendweb.dto.StudyPlanDTO;
import com.backend.topperfriendweb.model.*;
import com.backend.topperfriendweb.repository.*;
import com.backend.topperfriendweb.utils.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/collections")
public class CollectionController {

    private final CollectionRepository collectionRepository;
    private final CollectionItemRepository collectionItemRepository;
    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final JwtUtil jwtUtil;

    public CollectionController(CollectionRepository collectionRepository,
                                CollectionItemRepository collectionItemRepository,
                                NoteRepository noteRepository,
                                StudyPlanRepository studyPlanRepository,
                                UserRepository userRepository, JwtUtil jwtUtil) {
        this.collectionRepository = collectionRepository;
        this.collectionItemRepository = collectionItemRepository;
        this.noteRepository = noteRepository;
        this.studyPlanRepository = studyPlanRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    // Create collection
    @PostMapping
    public ResponseEntity<?> createCollection(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.getEmailFromToken(token);
            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String title = request.get("title");
            String description = request.get("description");

            Collection collection = new Collection();
            collection.setTitle(title);
            collection.setDescription(description);
            collection.setUser(user);

            Collection savedCollection = collectionRepository.save(collection);
            return ResponseEntity.ok(Map.of("success", true, "collection", savedCollection));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // Get all collections for user
    @GetMapping
    public ResponseEntity<?> getUserCollections(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.getEmailFromToken(token);
            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Collection> collections = collectionRepository.findByUser(user);
            return ResponseEntity.ok(Map.of("success", true, "collections", collections));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // Get collection by ID with items
    @GetMapping("/{id}")
    public ResponseEntity<?> getCollectionById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.getEmailFromToken(token);
            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Collection collection = collectionRepository.findByIdAndUser(id, user)
                    .orElseThrow(() -> new RuntimeException("Collection not found"));

            List<CollectionItem> items = collectionItemRepository.findByCollectionId(id);

            List<CollectionItemDTO> enrichedItems = items.stream().map(item -> {
                CollectionItemDTO dto = new CollectionItemDTO();
                dto.setId(item.getId());
                dto.setItemType(item.getItemType());
                dto.setAddedAt(item.getAddedAt());

                if ("NOTE".equalsIgnoreCase(item.getItemType())) {
                    noteRepository.findById(item.getItemId()).ifPresent(note -> {
                        NoteDTO noteDTO = new NoteDTO();
                        noteDTO.set_id(note.getId().toString());
                        noteDTO.setPostgresUserId(note.getUserId());
                        noteDTO.setTitle(note.getTitle());
                        noteDTO.setPdfLink(note.getPdfLink());
                        noteDTO.setTags(note.getTags());
                        noteDTO.setLikes(note.getLikes());
                        noteDTO.setCreatedAt(note.getCreatedAt());
                        noteDTO.setUpdatedAt(note.getUpdatedAt());
                        noteDTO.setUsername(note.getUsername());
                        noteDTO.setLikedByUsers(note.getLikedByUsers());
                        noteDTO.setSavedByUsers(note.getSavedByUsers());
                        dto.setNote(noteDTO);
                    });
                } else if ("STUDY_PLAN".equalsIgnoreCase(item.getItemType())) {
                    studyPlanRepository.findById(item.getItemId()).ifPresent(sp -> {
                        dto.setStudyPlan(new StudyPlanDTO(sp));
                    });
                }
                return dto;
            }).toList();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "collection", collection,
                    "items", enrichedItems
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }


    // Update collection
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateCollection(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.getEmailFromToken(token);
            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Collection collection = collectionRepository.findByIdAndUser(id, user)
                    .orElseThrow(() -> new RuntimeException("Collection not found"));

            if (request.containsKey("title")) {
                collection.setTitle(request.get("title"));
            }
            if (request.containsKey("description")) {
                collection.setDescription(request.get("description"));
            }

            Collection updatedCollection = collectionRepository.save(collection);
            return ResponseEntity.ok(Map.of("success", true, "collection", updatedCollection));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // Delete collection
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCollection(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.getEmailFromToken(token);
            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Collection collection = collectionRepository.findByIdAndUser(id, user)
                    .orElseThrow(() -> new RuntimeException("Collection not found"));

            collectionRepository.delete(collection);
            return ResponseEntity.ok(Map.of("success", true, "message", "Collection deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // Add item to collection
    @PostMapping("/{collectionId}/items")
    public ResponseEntity<?> addItemToCollection(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long collectionId,
            @RequestBody Map<String, Object> request) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.getEmailFromToken(token);
            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Collection collection = collectionRepository.findByIdAndUser(collectionId, user)
                    .orElseThrow(() -> new RuntimeException("Collection not found"));

            String itemType = (String) request.get("itemType");
            Long itemId = Long.valueOf(request.get("itemId").toString());

            // Check if item already exists in collection
            if (collectionItemRepository.existsByCollectionIdAndItemTypeAndItemId(collectionId, itemType, itemId)) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Item already in collection"));
            }

            CollectionItem item = new CollectionItem();
            item.setCollection(collection);
            item.setItemType(itemType);
            item.setItemId(itemId);

            CollectionItem savedItem = collectionItemRepository.save(item);
            return ResponseEntity.ok(Map.of("success", true, "item", savedItem));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // Remove item from collection
    @DeleteMapping("/{collectionId}/items/{itemType}/{itemId}")
    @Transactional
    public ResponseEntity<?> removeItemFromCollection(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long collectionId,
            @PathVariable String itemType,
            @PathVariable Long itemId) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.getEmailFromToken(token);
            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Collection collection = collectionRepository.findByIdAndUser(collectionId, user)
                    .orElseThrow(() -> new RuntimeException("Collection not found"));

            collectionItemRepository.deleteByCollectionIdAndItemTypeAndItemId(collectionId, itemType, itemId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Item removed from collection"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}