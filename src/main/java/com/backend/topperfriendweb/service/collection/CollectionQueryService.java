package com.backend.topperfriendweb.service.collection;

import com.backend.topperfriendweb.dto.collection.CollectionDTO;
import com.backend.topperfriendweb.dto.collection.CollectionItemDTO;
import com.backend.topperfriendweb.dto.note.NoteDTO;
import com.backend.topperfriendweb.dto.studyplan.StudyPlanDTO;
import com.backend.topperfriendweb.mapper.CollectionMapper;
import com.backend.topperfriendweb.model.Collection;
import com.backend.topperfriendweb.model.CollectionItem;
import com.backend.topperfriendweb.repository.CollectionItemRepository;
import com.backend.topperfriendweb.repository.CollectionRepository;
import com.backend.topperfriendweb.repository.NoteRepository;
import com.backend.topperfriendweb.repository.StudyPlanRepository;
import com.backend.topperfriendweb.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollectionQueryService {

    private final CollectionRepository collectionRepository;
    private final CollectionItemRepository collectionItemRepository;
    private final NoteRepository noteRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final CollectionMapper collectionMapper;

    @Transactional(readOnly = true)
    public List<CollectionDTO> getUserCollections(User user) {
        return collectionRepository.findByUser(user)
                .stream()
                .map(collectionMapper::toCollectionDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CollectionDTO getCollectionById(Long collectionId, User user) {
        long totalStart = System.currentTimeMillis();

        log.info("Step 1: Finding collection - START");
        Collection collection = collectionRepository.findByIdAndUser(collectionId, user)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found"));
        log.info("Step 1: Finding collection - DONE in {}ms", System.currentTimeMillis() - totalStart);

        long step2Start = System.currentTimeMillis();
        log.info("Step 2: Finding collection items - START");
        List<CollectionItem> items = collectionItemRepository.findByCollectionId(collectionId);
        log.info("Step 2: Finding collection items - DONE in {}ms. Found {} items",
                System.currentTimeMillis() - step2Start, items.size());

        long step3Start = System.currentTimeMillis();
        log.info("Step 3: Extracting note IDs - START");
        List<Long> noteIds = items.stream()
                .filter(item -> "NOTE".equalsIgnoreCase(item.getItemType()))
                .map(CollectionItem::getItemId)
                .collect(Collectors.toList());
        log.info("Step 3: Extracting note IDs - DONE in {}ms. Found {} note IDs",
                System.currentTimeMillis() - step3Start, noteIds.size());

        long step4Start = System.currentTimeMillis();
        log.info("Step 4: Extracting study plan IDs - START");
        List<Long> studyPlanIds = items.stream()
                .filter(item -> "STUDY_PLAN".equalsIgnoreCase(item.getItemType()))
                .map(CollectionItem::getItemId)
                .collect(Collectors.toList());
        log.info("Step 4: Extracting study plan IDs - DONE in {}ms. Found {} study plan IDs",
                System.currentTimeMillis() - step4Start, studyPlanIds.size());

        // Parallelize Step 5 and Step 6 to reduce total time
        long parallelStart = System.currentTimeMillis();
        CompletableFuture<Map<Long, NoteDTO>> notesFuture =
                CompletableFuture.supplyAsync(() -> {
                    long s = System.currentTimeMillis();
                    log.info("Step 5: Loading all notes - START");
                    if (noteIds.isEmpty()) {
                        log.info("Step 5: Loading all notes - DONE in {}ms. Loaded 0 notes", System.currentTimeMillis() - s);
                        return java.util.Collections.<Long, NoteDTO>emptyMap();
                    }
                    List<Object[]> rows = noteRepository.findNotesByIdsBasic(noteIds);
                    Map<Long, NoteDTO> map = rows.stream()
                            .map(collectionMapper::mapNoteRowToDTOBasic)
                            .filter(java.util.Objects::nonNull)
                            .collect(Collectors.toMap(n -> Long.parseLong(n.get_id()), n -> n));
                    log.info("Step 5: Loading all notes - DONE in {}ms. Loaded {} notes", System.currentTimeMillis() - s, map.size());
                    return map;
                });

        CompletableFuture<Map<Long, StudyPlanDTO>> plansFuture =
                CompletableFuture.supplyAsync(() -> {
                    long s = System.currentTimeMillis();
                    log.info("Step 6: Loading all study plans - START");
                    if (studyPlanIds.isEmpty()) {
                        log.info("Step 6: Loading all study plans - DONE in {}ms. Loaded 0 study plans", System.currentTimeMillis() - s);
                        return java.util.Collections.<Long, StudyPlanDTO>emptyMap();
                    }
                    // Ultra-light load: skip large TEXT columns to keep collections fast
                    List<Object[]> rows = studyPlanRepository.findStudyPlansLiteByIds(studyPlanIds);
                    log.info("Step 6: Study plan rows returned: {}", rows.size());
                    Map<Long, StudyPlanDTO> map = rows.stream()
                            .map(collectionMapper::mapStudyPlanLiteRowToDTO)
                            .filter(java.util.Objects::nonNull)
                            .collect(Collectors.toMap(java.util.Map.Entry::getKey, java.util.Map.Entry::getValue));
                    log.info("Step 6: Loading all study plans - DONE in {}ms. Loaded {} study plans", System.currentTimeMillis() - s, map.size());
                    return map;
                });

        Map<Long, NoteDTO> notesMap = notesFuture.join();
        Map<Long, StudyPlanDTO> studyPlansMap = plansFuture.join();
        log.info("Steps 5+6 parallel total: {}ms", System.currentTimeMillis() - parallelStart);

        long step7Start = System.currentTimeMillis();
        log.info("Step 7: Converting items to DTOs - START");
        List<CollectionItemDTO> enrichedItems = items.stream()
                .map(item -> collectionMapper.toItemDTOOptimized(item, notesMap, studyPlansMap))
                .collect(Collectors.toList());
        log.info("Step 7: Converting items to DTOs - DONE in {}ms",
                System.currentTimeMillis() - step7Start);

        long step8Start = System.currentTimeMillis();
        log.info("Step 8: Creating final DTO - START");
        CollectionDTO dto = collectionMapper.toCollectionDTO(collection);
        dto.setItems(enrichedItems);
        log.info("Step 8: Creating final DTO - DONE in {}ms",
                System.currentTimeMillis() - step8Start);

        log.info("TOTAL TIME: {}ms", System.currentTimeMillis() - totalStart);
        return dto;
    }
}
