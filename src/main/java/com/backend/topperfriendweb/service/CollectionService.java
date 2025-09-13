package com.backend.topperfriendweb.service;

import com.backend.topperfriendweb.dto.*;
import com.backend.topperfriendweb.model.Collection;
import com.backend.topperfriendweb.model.CollectionItem;
import com.backend.topperfriendweb.model.Note;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.CollectionItemRepository;
import com.backend.topperfriendweb.repository.CollectionRepository;
import com.backend.topperfriendweb.repository.NoteRepository;
import com.backend.topperfriendweb.repository.StudyPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final CollectionItemRepository collectionItemRepository;
    private final NoteRepository noteRepository;
    private final StudyPlanRepository studyPlanRepository;

    @Transactional
    public CollectionDTO createCollection(CreateCollectionRequest request, User user) {
        try {
            Collection collection = new Collection();
            collection.setTitle(request.getTitle());
            collection.setDescription(request.getDescription());
            collection.setUser(user);

            Collection savedCollection = collectionRepository.save(collection);
            return convertToDTO(savedCollection);
        } catch (Exception e) {
            log.error("Error creating collection", e);
            throw new RuntimeException("Failed to create collection: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<CollectionDTO> getUserCollections(User user) {
        return collectionRepository.findByUser(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CollectionDTO getCollectionById(Long collectionId, User user) {
        Collection collection = collectionRepository.findByIdAndUser(collectionId, user)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        List<CollectionItem> items = collectionItemRepository.findByCollectionId(collectionId);
        List<CollectionItemDTO> enrichedItems = items.stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList());

        CollectionDTO dto = convertToDTO(collection);
        dto.setItems(enrichedItems);
        return dto;
    }

    @Transactional
    public CollectionDTO updateCollection(Long collectionId, UpdateCollectionRequest request, User user) {
        try {
            Collection collection = collectionRepository.findByIdAndUser(collectionId, user)
                    .orElseThrow(() -> new RuntimeException("Collection not found"));

            if (request.getTitle() != null) {
                collection.setTitle(request.getTitle());
            }
            if (request.getDescription() != null) {
                collection.setDescription(request.getDescription());
            }

            Collection updatedCollection = collectionRepository.save(collection);
            return convertToDTO(updatedCollection);
        } catch (Exception e) {
            log.error("Error updating collection", e);
            throw new RuntimeException("Failed to update collection: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteCollection(Long collectionId, User user) {
        Collection collection = collectionRepository.findByIdAndUser(collectionId, user)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        collectionRepository.delete(collection);
    }

    @Transactional
    public CollectionItemDTO addItemToCollection(Long collectionId, AddItemToCollectionRequest request, User user) {
        try {
            Collection collection = collectionRepository.findByIdAndUser(collectionId, user)
                    .orElseThrow(() -> new RuntimeException("Collection not found"));

            // Check if item already exists in collection
            if (collectionItemRepository.existsByCollectionIdAndItemTypeAndItemId(
                    collectionId, request.getItemType(), request.getItemId())) {
                throw new RuntimeException("Item already in collection");
            }

            CollectionItem item = new CollectionItem();
            item.setCollection(collection);
            item.setItemType(request.getItemType());
            item.setItemId(request.getItemId());

            CollectionItem savedItem = collectionItemRepository.save(item);
            return convertItemToDTO(savedItem);
        } catch (Exception e) {
            log.error("Error adding item to collection", e);
            throw new RuntimeException("Failed to add item to collection: " + e.getMessage());
        }
    }

    @Transactional
    public void removeItemFromCollection(Long collectionId, String itemType, Long itemId, User user) {
        collectionRepository.findByIdAndUser(collectionId, user)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        collectionItemRepository.deleteByCollectionIdAndItemTypeAndItemId(collectionId, itemType, itemId);
    }

    private CollectionDTO convertToDTO(Collection collection) {
        CollectionDTO dto = new CollectionDTO();
        dto.setId(collection.getId());
        dto.setTitle(collection.getTitle());
        dto.setDescription(collection.getDescription());
        dto.setUserId(collection.getUserId());
        dto.setCreatedAt(collection.getCreatedAt());
        dto.setUpdatedAt(collection.getUpdatedAt());
        return dto;
    }

    private CollectionItemDTO convertItemToDTO(CollectionItem item) {
        CollectionItemDTO dto = new CollectionItemDTO();
        dto.setId(item.getId());
        dto.setItemType(item.getItemType());
        dto.setAddedAt(item.getAddedAt());

        // Enrich with actual item data
        if ("NOTE".equalsIgnoreCase(item.getItemType())) {
            noteRepository.findById(item.getItemId()).ifPresent(note -> {
                NoteDTO noteDTO = convertNoteToDTO(note);
                dto.setNote(noteDTO);
            });
        } else if ("STUDY_PLAN".equalsIgnoreCase(item.getItemType())) {
            studyPlanRepository.findById(item.getItemId()).ifPresent(studyPlan -> {
                StudyPlanDTO studyPlanDTO = new StudyPlanDTO(studyPlan);
                dto.setStudyPlan(studyPlanDTO);
            });
        }

        return dto;
    }

    private NoteDTO convertNoteToDTO(Note note) {
        NoteDTO dto = new NoteDTO();
        dto.set_id(note.getId().toString());
        dto.setPostgresUserId(note.getUserId());
        dto.setTitle(note.getTitle());
        dto.setPdfLink(note.getPdfLink());
        dto.setTags(note.getTags());
        dto.setLikes(note.getLikes());
        dto.setCreatedAt(note.getCreatedAt());
        dto.setUpdatedAt(note.getUpdatedAt());
        dto.setUsername(note.getUsername());
        dto.setLikedByUsers(note.getLikedByUsers());
        dto.setSavedByUsers(note.getSavedByUsers());
        return dto;
    }
}
