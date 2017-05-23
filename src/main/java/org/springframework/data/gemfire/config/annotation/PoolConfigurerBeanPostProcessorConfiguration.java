/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.data.gemfire.config.annotation;

import static org.springframework.data.gemfire.util.CollectionUtils.nullSafeList;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.client.PoolFactoryBean;

/**
 * The {@link PoolConfigurerBeanPostProcessorConfiguration} class is a Spring {@link Configuration} annotated class
 * used to register a Spring {@link BeanPostProcessor} that will post process any {@link PoolFactoryBean} objects
 * in order to apply additional configuration specified in 1 or more {@link PoolConfigurer} beans registered
 * in the Spring application context.
 *
 * @author John Blum
 * @see org.apache.geode.cache.client.Pool
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.data.gemfire.client.PoolFactoryBean
 * @see org.springframework.data.gemfire.config.annotation.PoolConfigurer
 * @since 1.1.0
 */
@Configuration
public class PoolConfigurerBeanPostProcessorConfiguration {

	@Autowired(required = false)
	private List<PoolConfigurer> poolConfigurers = Collections.emptyList();

	private final PoolConfigurer compositePoolConfigurer = (beanName, bean) -> {
		nullSafeList(poolConfigurers).forEach(poolConfigurer -> poolConfigurer.configure(beanName, bean));
	};

	/**
	 * Bean definition registering a Spring {@link BeanPostProcessor} that will post process all
	 * {@link PoolFactoryBean PoolFactoryBeans} before initialization by delegating to
	 * any {@link PoolConfigurer PoolConfigurers} registered in the Spring application context
	 * used to apply additional configuration.
	 *
	 * @return the Spring {@link BeanPostProcessor}.
	 * @see org.springframework.beans.factory.config.BeanPostProcessor
	 */
	@Bean
	@SuppressWarnings("unused")
	public BeanPostProcessor poolConfigurerBeanPostProcessor() {

		return new BeanPostProcessor() {

			private final PoolConfigurer configurer =
				PoolConfigurerBeanPostProcessorConfiguration.this.compositePoolConfigurer;

			@Override
			public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
				if (bean instanceof PoolFactoryBean) {
					this.configurer.configure(beanName, (PoolFactoryBean) bean);
				}

				return bean;
			}
		};
	}
}