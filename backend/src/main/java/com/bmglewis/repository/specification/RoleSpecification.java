package com.bmglewis.repository.specification;

import com.bmglewis.dto.common.SearchRequest;
import com.bmglewis.model.Role;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * FILE TYPE: CLASS
 * Role search/filter specifications
 */
public class RoleSpecification {

    public static Specification<Role> withFilters(SearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // General search across roleName and description
            if (request.getSearch() != null && !request.getSearch().isEmpty()) {
                String searchPattern = "%" + request.getSearch().toLowerCase() + "%";

                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("roleName")), searchPattern);
                Predicate descPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), searchPattern);

                predicates.add(criteriaBuilder.or(namePredicate, descPredicate));
            }

            // Filter by active status
            if (request.getIsActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), request.getIsActive()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}