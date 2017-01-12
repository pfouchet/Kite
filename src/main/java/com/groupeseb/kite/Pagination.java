package com.groupeseb.kite;

import lombok.Data;

/**
 * In case GET query need to be paginated, this structure should be used
 */
@Data
public class Pagination {
	private final String totalPagesField;
	private final String pageParameterName;
	private final String sizeParameterName;
	private final Integer size;
	private final Integer startPage;

	public Pagination(Json specification) {
		totalPagesField = specification.getString("totalPagesField");

		pageParameterName = specification.getStringOrDefault("pageParameterName", "page");
		sizeParameterName = specification.getStringOrDefault("sizeParameterName", "size");

		size = specification.getIntegerOrDefault("size", 20);
		startPage = specification.getIntegerOrDefault("startPage", 1);
	}
}
