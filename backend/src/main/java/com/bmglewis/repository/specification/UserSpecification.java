package com.bmglewis.repository.specification;

import com.bmglewis.dto.common.SearchRequest;
import com.bmglewis.model.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * FILE TYPE: CLASS
 * User search/filter specifications
 */
public class UserSpecification {

    public static Specification<User> withFilters(SearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // General search across email, firstName, lastName, phone
            if (request.getSearch() != null && !request.getSearch().isEmpty()) {
                String searchPattern = "%" + request.getSearch().toLowerCase() + "%";

                Predicate emailPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")), searchPattern);
                Predicate firstNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("firstName")), searchPattern);
                Predicate lastNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("lastName")), searchPattern);
                Predicate phonePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("phone")), searchPattern);

                predicates.add(criteriaBuilder.or(
                        emailPredicate, firstNamePredicate, lastNamePredicate, phonePredicate
                ));
            }

            // Filter by active status
            if (request.getIsActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), request.getIsActive()));
            }

            // Filter by department
            if (request.getDepartmentId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("department").get("departmentId"), request.getDepartmentId()));
            }

            // Filter by manager
            if (request.getManagerId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("manager").get("userId"), request.getManagerId()));
            }

            // Filter by role name
            if (request.getRoleName() != null && !request.getRoleName().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                        root.join("roles").get("roleName"), request.getRoleName()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}