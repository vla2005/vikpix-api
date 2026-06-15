package com.vikpix.api.widgets.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vikpix.api.widgets.entities.QrCodeConfig;
import com.vikpix.api.widgets.entities.Widget;

@Repository
public interface QrCodeConfigRepository extends JpaRepository<QrCodeConfig, Long> {
    Optional<QrCodeConfig> findByWidget(Widget widget);
}
