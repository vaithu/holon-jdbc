/*
 * Copyright 2000-2016 Holon TDCN.
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
package com.holonplatform.jdbc.spring.boot.test;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.holonplatform.jdbc.spring.boot.DataSourcesAutoConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ActiveProfiles("test1")
public class TestExcludeAutoConfig {

	@Configuration
	@EnableAutoConfiguration(exclude = DataSourcesAutoConfiguration.class)
	protected static class Config {

	}

	@Autowired
	private DataSource dataSource;

	@Test
	public void testDataSource() throws SQLException {
		assertNotNull(dataSource);

		// Spring boot default is tomcat datasource
		assertTrue(dataSource instanceof org.apache.tomcat.jdbc.pool.DataSource);

		try (Connection c = dataSource.getConnection()) {
			assertNotNull(c);

			c.createStatement().executeQuery("select count(*) from test");
		}
	}

}
