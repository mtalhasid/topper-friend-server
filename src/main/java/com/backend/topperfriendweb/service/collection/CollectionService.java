package com.backend.topperfriendweb.service.collection;

import com.backend.topperfriendweb.dto.collection.*;
import com.backend.topperfriendweb.dto.note.NoteDTO;
import com.backend.topperfriendweb.dto.studyplan.StudyPlanDTO;
import com.backend.topperfriendweb.model.Collection;
import com.backend.topperfriendweb.model.CollectionItem;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.CollectionItemRepository;
import com.backend.topperfriendweb.repository.CollectionRepository;
import com.backend.topperfriendweb.repository.NoteRepository;
import com.backend.topperfriendweb.repository.StudyPlanRepository;
import com.backend.topperfriendweb.mapper.NoteMapper;
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
    private final NoteMapper noteMapper;

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
            throw new IllegalArgumentException("Failed to create collection: " + e.getMessage());
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
                .orElseThrow(() -> new IllegalArgumentException("Collection not found"));

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
                    .orElseThrow(() -> new IllegalArgumentException("Collection not found"));

            if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
                collection.setTitle(request.getTitle());
            }
            if (request.getDescription() != null) {
                collection.setDescription(request.getDescription());
            }

            Collection updatedCollection = collectionRepository.save(collection);
            return convertToDTO(updatedCollection);
        } catch (IllegalArgumentException e) {
            throw e; // Re-throw IllegalArgumentException as-is
        } catch (Exception e) {
            log.error("Error updating collection", e);
            throw new IllegalArgumentException("Failed to update collection: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteCollection(Long collectionId, User user) {
        Collection collection = collectionRepository.findByIdAndUser(collectionId, user)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found"));

        collectionRepository.delete(collection);
    }

    @Transactional
    public CollectionItemDTO addItemToCollection(Long collectionId, AddItemToCollectionRequest request, User user) {
        try {
            Collection collection = collectionRepository.findByIdAndUser(collectionId, user)
                    .orElseThrow(() -> new IllegalArgumentException("Collection not found"));

            // Check if item already exists in collection
            if (collectionItemRepository.existsByCollectionIdAndItemTypeAndItemId(
                    collectionId, request.getItemType(), request.getItemId())) {
                throw new IllegalArgumentException("Item already exists in collection");
            }

            // Validate that the item actually exists
            if ("NOTE".equalsIgnoreCase(request.getItemType())) {
                if (!noteRepository.existsById(request.getItemId())) {
                    throw new IllegalArgumentException("Note not found");
                }
            } else if ("STUDY_PLAN".equalsIgnoreCase(request.getItemType())) {
                if (!studyPlanRepository.existsById(request.getItemId())) {
                    throw new IllegalArgumentException("Study plan not found");
                }
            } else {
                throw new IllegalArgumentException("Invalid item type. Must be NOTE or STUDY_PLAN");
            }

            CollectionItem item = new CollectionItem();
            item.setCollection(collection);
            item.setItemType(request.getItemType().toUpperCase());
            item.setItemId(request.getItemId());

            CollectionItem savedItem = collectionItemRepository.save(item);
            return convertItemToDTO(savedItem);
        } catch (IllegalArgumentException e) {
            throw e; // Re-throw IllegalArgumentException as-is
        } catch (Exception e) {
            log.error("Error adding item to collection", e);
            throw new IllegalArgumentException("Failed to add item to collection: " + e.getMessage());
        }
    }

    @Transactional
    public void removeItemFromCollection(Long collectionId, String itemType, Long itemId, User user) {
        collectionRepository.findByIdAndUser(collectionId, user)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found"));

        boolean existed = collectionItemRepository.existsByCollectionIdAndItemTypeAndItemId(
                collectionId, itemType.toUpperCase(), itemId);

        if (!existed) {
            throw new IllegalArgumentException("Item not found in collection");
        }

        collectionItemRepository.deleteByCollectionIdAndItemTypeAndItemId(
                collectionId, itemType.toUpperCase(), itemId);
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
                NoteDTO noteDTO = noteMapper.toDTO(note);
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
}