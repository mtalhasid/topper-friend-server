package com.backend.topperfriendweb.repository;

import com.backend.topperfriendweb.model.Collection;
import com.backend.topperfriendweb.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
    List<Collection> findByUser(User user);
    List<Collection> findByUser_Id(Long userId); // ✅ FIXED
    Optional<Collection> findByIdAndUser(Long id, User user);
}
