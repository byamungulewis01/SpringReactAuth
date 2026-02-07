package com.bmglewis.repository.specification;

import com.bmglewis.dto.common.SearchRequest;
import com.bmglewis.model.Permission;
import com.bmglewis.model.enums.Module;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * FILE TYPE: CLASS
 * Permission search/filter specifications
 */
public class PermissionSpecification {

    public static Specification<Permission> withFilters(SearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // General search across permissionName and description
            if (request.getSearch() != null && !request.getSearch().isEmpty()) {
                String searchPattern = "%" + request.getSearch().toLowerCase() + "%";

                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("permissionName")), searchPattern);
                Predicate descPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), searchPattern);

                predicates.add(criteriaBuilder.or(namePredicate, descPredicate));
            }

            // Filter by module
            if (request.getModule() != null && !request.getModule().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                        root.get("module"), Module.valueOf(request.getModule())));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}