package com.groupeseb.kite;

import com.jayway.restassured.RestAssured;
import lombok.Data;

/**
 * This class is used to define destination services information.
 */
@Data
public class Service {
	private String baseURI = RestAssured.DEFAULT_URI;
	private String basePath = RestAssured.DEFAULT_PATH;
	private int port = RestAssured.DEFAULT_PORT;
	private boolean urlEncodingEnabled = false;
	private String charset = "UTF-8";

	/**
	 * Default constructor without parameters
	 */
	public Service() {
		// Empty constructor
	}

	/**
	 * Create new instance
	 *
	 * @param baseURI  First part of service's uri, e.g. "http://localhost"
	 * @param basePath Base path of service, e.g. "/common-api"
	 * @param port     Port number of service, e.g. 8080
	 */
	public Service(String baseURI, String basePath, int port) {
		this.baseURI = baseURI;
		this.basePath = basePath;
		this.port = port;
	}
}
