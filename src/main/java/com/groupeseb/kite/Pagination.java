package com.groupeseb.kite;

import lombok.Data;

@Data
public class Pagination {
    private final String totalPagesField;

    private final String pageParameterName;
    private final String sizeParameterName;

    private final int size;
    private final int startPage;

    public Pagination(Json specification) {
        totalPagesField = specification.getString("totalPagesField");

        pageParameterName = specification.getStringOrDefault("pageParameterName", "page");
        sizeParameterName = specification.getStringOrDefault("sizeParameterName", "size");

        size = specification.getIntegerOrDefault("size", 20);
        startPage = specification.getIntegerOrDefault("startPage", 1);
    }
}
