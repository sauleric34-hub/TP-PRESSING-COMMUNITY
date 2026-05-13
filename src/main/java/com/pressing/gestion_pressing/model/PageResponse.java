package com.pressing.gestion_pressing.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int currentPage;
    private int itemsPerPage;
    private long totalItems;
    private int totalPages;
}
