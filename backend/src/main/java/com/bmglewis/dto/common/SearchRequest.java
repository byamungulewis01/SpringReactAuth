package com.bmglewis.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {

    // Pagination
    private Integer page = 0;
    private Integer size = 10;

    // Sorting
    private String sortBy = "createdAt";
    private String sortDirection = "DESC"; // ASC or DESC

    // General search (searches across email, name, phone, etc.)
    private String search;

    // Specific Filtering (optional)
    private Boolean isActive;
    private Long departmentId;
    private Long managerId;
    private String roleName;
    private String module;
}