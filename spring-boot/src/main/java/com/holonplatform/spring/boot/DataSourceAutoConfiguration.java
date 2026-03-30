/*
 * Copyright 2016-2017 Axioma srl.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.holonplatform.spring.boot;

import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * Stub auto-configuration class required by the holon-spring-boot:5.5.1 library, which registers this class in its
 * {@code org.springframework.boot.autoconfigure.AutoConfiguration.imports} file. Spring Boot 4.0+ fails hard when a
 * referenced auto-configuration class cannot be found on the classpath. The actual DataSource auto-configuration is
 * performed by {@link com.holonplatform.jdbc.spring.boot.DataSourcesAutoConfiguration}.
 *
 * @since 5.5.3
 */
@AutoConfiguration
public class DataSourceAutoConfiguration {
	// No bean definitions – actual DataSource registration is handled by
	// com.holonplatform.jdbc.spring.boot.DataSourcesAutoConfiguration
}

