/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.context.restart;

import org.junit.After;
import org.junit.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.LiveBeansView;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

public class RestartIntegrationTests {

	private ConfigurableApplicationContext context;

	public static void main(String[] args) {
		SpringApplication.run(TestConfiguration.class, args);
	}

	@After
	public void close() {
		if (this.context != null) {
			this.context.close();
		}
	}

	@Test
	public void testRestartTwice() throws Exception {

		this.context = SpringApplication.run(TestConfiguration.class,
				"--management.endpoint.restart.enabled=true", "--server.port=0",
				"--spring.liveBeansView.mbeanDomain=livebeans");

		RestartEndpoint endpoint = this.context.getBean(RestartEndpoint.class);
		assertNotNull(this.context.getParent());
		assertNull(this.context.getParent().getParent());
		this.context = endpoint.doRestart();

		assertNotNull(this.context);
		assertNotNull(this.context.getParent());
		assertNull(this.context.getParent().getParent());

		RestartEndpoint next = this.context.getBean(RestartEndpoint.class);
		assertNotSame(endpoint, next);
		this.context = next.doRestart();

		assertNotNull(this.context);
		assertNotNull(this.context.getParent());
		assertNull(this.context.getParent().getParent());

		LiveBeansView beans = new LiveBeansView();
		String json = beans.getSnapshotAsJson();
		assertThat(json).containsOnlyOnce("parent\": \"bootstrap");
		assertThat(json).containsOnlyOnce("parent\": null");
	}

	@Configuration
	@EnableAutoConfiguration
	protected static class TestConfiguration {

	}

}
