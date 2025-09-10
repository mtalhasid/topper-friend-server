package com.backend.topperfriendweb.repository;

import com.backend.topperfriendweb.model.CollectionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CollectionItemRepository extends JpaRepository<CollectionItem, Long> {
    List<CollectionItem> findByCollectionId(Long collectionId);
    boolean existsByCollectionIdAndItemTypeAndItemId(Long collectionId, String itemType, Long itemId);
    void deleteByCollectionIdAndItemTypeAndItemId(Long collectionId, String itemType, Long itemId);
}