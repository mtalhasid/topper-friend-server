package com.backend.topperfriendweb.mapper;

import com.backend.topperfriendweb.dto.collection.CollectionDTO;
import com.backend.topperfriendweb.dto.collection.CollectionItemDTO;
import com.backend.topperfriendweb.dto.note.NoteDTO;
import com.backend.topperfriendweb.dto.studyplan.StudyPlanDTO;
import com.backend.topperfriendweb.model.Collection;
import com.backend.topperfriendweb.model.CollectionItem;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;

@Component
public class CollectionMapper {

    public CollectionDTO toCollectionDTO(Collection collection) {
        CollectionDTO dto = new CollectionDTO();
        dto.setId(collection.getId());
        dto.setTitle(collection.getTitle());
        dto.setDescription(collection.getDescription());
        dto.setUserId(collection.getUserId());
        dto.setCreatedAt(collection.getCreatedAt());
        dto.setUpdatedAt(collection.getUpdatedAt());
        return dto;
    }

    public CollectionItemDTO toItemDTOOptimized(CollectionItem item,
                                                Map<Long, NoteDTO> notesMap,
                                                Map<Long, StudyPlanDTO> studyPlansMap) {
        CollectionItemDTO dto = new CollectionItemDTO();
        dto.setId(item.getId());
        dto.setItemType(item.getItemType());
        dto.setAddedAt(item.getAddedAt());

        if ("NOTE".equalsIgnoreCase(item.getItemType())) {
            NoteDTO note = notesMap.get(item.getItemId());
            if (note != null) dto.setNote(note);
        } else if ("STUDY_PLAN".equalsIgnoreCase(item.getItemType())) {
            StudyPlanDTO sp = studyPlansMap.get(item.getItemId());
            if (sp != null) dto.setStudyPlan(sp);
        }
        return dto;
    }

    public NoteDTO mapNoteRowToDTOBasic(Object[] r) {
        try {
            NoteDTO dto = new NoteDTO();
            Long id = ((Number) r[0]).longValue();
            dto.set_id(id.toString());
            dto.setPostgresUserId(((Number) r[1]).longValue());
            dto.setTitle((String) r[2]);
            dto.setPdfLink((String) r[3]);
            dto.setLikes(r[4] == null ? 0 : ((Number) r[4]).intValue());
            dto.setCreatedAt(toLocalDateTime(r[5]));
            dto.setUpdatedAt(toLocalDateTime(r[6]));
            dto.setUsername((String) r[7]);
            dto.setTags(java.util.Collections.emptyList());
            dto.setLikedByUsers(java.util.Collections.emptyList());
            dto.setSavedByUsers(java.util.Collections.emptyList());
            return dto;
        } catch (Exception ex) {
            return null;
        }
    }

    public Map.Entry<Long, StudyPlanDTO> mapStudyPlanRowToDTO(Object[] r) {
        try {
            Long id = ((Number) r[0]).longValue();
            StudyPlanDTO dto = new StudyPlanDTO();
            dto.setId(id);
            dto.setPdfUrl((String) r[1]);
            dto.setPdfTitle((String) r[2]);
            dto.setMarkdown((String) r[3]);
            dto.setResources((String) r[4]);
            dto.setTasks((String) r[5]);
            String statusStr = r[6] == null ? "active" : r[6].toString();
            dto.setStatus(com.backend.topperfriendweb.model.StudyPlanStatus.valueOf(statusStr.toLowerCase()));
            dto.setCreatedAt(toLocalDateTime(r[7]));
            dto.setUpdatedAt(toLocalDateTime(r[8]));
            return new AbstractMap.SimpleEntry<>(id, dto);
        } catch (Exception ex) {
            return null;
        }
    }

    // Lite version: id, title, status, createdAt only (used in collections list)
    public Map.Entry<Long, StudyPlanDTO> mapStudyPlanLiteRowToDTO(Object[] r) {
        try {
            Long id = ((Number) r[0]).longValue();
            StudyPlanDTO dto = new StudyPlanDTO();
            dto.setId(id);
            dto.setPdfTitle((String) r[1]);
            String statusStr = r[2] == null ? "active" : r[2].toString();
            dto.setStatus(com.backend.topperfriendweb.model.StudyPlanStatus.valueOf(statusStr.toLowerCase()));
            dto.setCreatedAt(toLocalDateTime(r[3]));
            return new AbstractMap.SimpleEntry<>(id, dto);
        } catch (Exception ex) {
            return null;
        }
    }

    public LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        if (value instanceof Timestamp) return ((Timestamp) value).toLocalDateTime();
        if (value instanceof OffsetDateTime) return ((OffsetDateTime) value).toLocalDateTime();
        if (value instanceof ZonedDateTime) return ((ZonedDateTime) value).toLocalDateTime();
        try {
            return LocalDateTime.parse(Objects.toString(value));
        } catch (Exception ignored) {
            return null;
        }
    }
}
