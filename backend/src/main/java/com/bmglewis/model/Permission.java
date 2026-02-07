package com.bmglewis.model;

import com.bmglewis.model.enums.Module;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * FILE TYPE: CLASS (Entity)
 * Permission entity - represents system permissions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "permissions")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long permissionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "module", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private Module module;

    @Column(nullable = false, unique = true, length = 50)
    private String permissionName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permission)) return false;
        Permission that = (Permission) o;
        return permissionId != null && permissionId.equals(that.permissionId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}