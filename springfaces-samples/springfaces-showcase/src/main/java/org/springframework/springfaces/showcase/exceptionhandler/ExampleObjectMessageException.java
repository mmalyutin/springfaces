/*
 * Copyright 2010-2012 the original author or authors.
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
package org.springframework.springfaces.showcase.exceptionhandler;

import org.springframework.springfaces.message.ObjectMessageSource;

/**
 * Example exception that is mapped via {@link ObjectMessageSource}.
 * 
 * @author Phillip Webb
 */
public class ExampleObjectMessageException extends Exception {

	private String name;

	public ExampleObjectMessageException(String message, String name) {
		super(message);
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
}
