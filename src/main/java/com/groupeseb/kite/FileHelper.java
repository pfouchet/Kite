package com.groupeseb.kite;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileHelper {

	/**
	 * Create a new InputStream for input fileName.
	 * The resource must be exist in the Classpath
	 *
	 * @param filename the absolute path within the class path
	 */
	public static InputStream getFileInputStream(String filename) {
		Resource resource = new ClassPathResource(filename);

		if (!resource.exists()) {

			// try external file
			resource = new FileSystemResource(filename);
			if (!resource.exists()) {
				throw new IllegalArgumentException("Incorrect file location :" + filename);
			}
		}
		try {
			return resource.getInputStream();
		} catch (IOException e) {
			throw new IllegalStateException("getFileResource error", e);
		}
	}
}
