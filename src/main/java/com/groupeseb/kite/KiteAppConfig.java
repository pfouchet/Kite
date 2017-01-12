package com.groupeseb.kite;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Enable scan of the package. This configuration bean should be loaded by calling api.
 */
@Configuration
@ComponentScan(basePackages = "com.groupeseb.kite")
public class KiteAppConfig {
}
