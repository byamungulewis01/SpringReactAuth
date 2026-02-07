package com.bmglewis.repository.specification;

import com.bmglewis.dto.common.SearchRequest;
import com.bmglewis.model.Department;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * FILE TYPE: CLASS
 * Department search/filter specifications
 */
public class DepartmentSpecification {

    public static Specification<Department> withFilters(SearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // General search across departmentCode and departmentName
            if (request.getSearch() != null && !request.getSearch().isEmpty()) {
                String searchPattern = "%" + request.getSearch().toLowerCase() + "%";

                Predicate codePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("departmentCode")), searchPattern);
                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("departmentName")), searchPattern);

                predicates.add(criteriaBuilder.or(codePredicate, namePredicate));
            }

            // Filter by active status
            if (request.getIsActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), request.getIsActive()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}