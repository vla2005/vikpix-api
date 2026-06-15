package com.vikpix.api.widgets.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "qrcode_widget_configs")
@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QrCodeConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false, unique = true)
    @JsonIgnore
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "widget_id", nullable = false, unique = true)
    private Widget widget;

    @Column(nullable = false, name = "primary_color")
    private String primaryColor;

    @Column(nullable = false, name = "secondary_color")
    private String secondaryColor;

    @Column(nullable = false, name = "show_link")
    private boolean showLink;

    @Column(nullable = false, name = "show_message")
    private boolean showMessage;

    @Column(nullable = false)
    private String message;
}
