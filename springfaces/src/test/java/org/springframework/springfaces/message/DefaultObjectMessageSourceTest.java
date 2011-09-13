package org.springframework.springfaces.message;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.StaticMessageSource;

public class DefaultObjectMessageSourceTest {

	// FIXME
	private static final Locale LOCALE = Locale.getDefault();

	private static final String INNER = DefaultObjectMessageSourceTest.class.getName() + "$";

	private StaticMessageSource messageSource;

	private DefaultObjectMessageSource objectMessageSource;

	@Before
	public void setup() {
		this.messageSource = new StaticMessageSource();
		this.objectMessageSource = new DefaultObjectMessageSource(messageSource);
		setupMessage(INNER + "Mapped", "mapped");
		setupMessage(INNER + "MappedArguments", "a {name} b {numberEnum}");
		setupMessage(INNER + "NumberEnum.ONE", "1");
	}

	private void setupMessage(String code, String msg) {
		messageSource.addMessage(code, LOCALE, msg);
	}

	@Test
	public void shouldSupportMappedClasses() throws Exception {
		assertThat(objectMessageSource.containsMessage(Mapped.class), is(true));
		assertThat(objectMessageSource.containsMessage(NotMapped.class), is(false));
	}

	@Test
	public void shouldSupportMappedClassesWhenUsingCodeAsDefaultMessage() throws Exception {
		messageSource.setUseCodeAsDefaultMessage(true);
		assertThat(objectMessageSource.containsMessage(Mapped.class), is(true));
		assertThat(objectMessageSource.containsMessage(NotMapped.class), is(false));
	}

	@Test
	public void shouldGetMappedAsString() throws Exception {
		String actual = objectMessageSource.getMessage(new Mapped(), LOCALE);
		assertThat(actual, is("mapped"));
	}

	@Test
	public void shouldConvertNullToNull() throws Exception {
		String actual = objectMessageSource.getMessage(null, LOCALE);
		assertThat(actual, is(nullValue()));
	}

	@Test
	public void shouldGetWithExpandedArguments() throws Exception {
		String actual = objectMessageSource.getMessage(new MappedArguments("x", NumberEnum.ONE), LOCALE);
		assertThat(actual, is("a x b 1"));
	}

	@Test
	public void shouldGetEnum() throws Exception {
		String actual = objectMessageSource.getMessage(NumberEnum.ONE, LOCALE);
		assertThat(actual, is("1"));
	}

	static class NotMapped {
	}

	static class Mapped {
	}

	static class MappedArguments {
		private String name;
		private NumberEnum numberEnum;

		public MappedArguments(String name, NumberEnum numberEnum) {
			this.name = name;
			this.numberEnum = numberEnum;
		}

		public String getName() {
			return name;
		}

		public NumberEnum getNumberEnum() {
			return numberEnum;
		}
	}

	enum NumberEnum {
		ONE, TWO, THREE
	}

}