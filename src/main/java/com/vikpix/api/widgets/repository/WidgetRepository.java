package com.vikpix.api.widgets.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vikpix.api.widgets.entities.Widget;
import com.vikpix.api.widgets.enums.WidgetType;

@Repository
public interface WidgetRepository extends JpaRepository<Widget, Long> {
    Optional<Widget> findByToken(UUID token);

    Optional<Widget> findByUser_IdAndType(Long userId, WidgetType type);
}
