package org.springframework.springfaces.message.ui;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.application.ProjectStage;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.springfaces.SpringFacesIntegration;
import org.springframework.springfaces.util.FacesUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Exposes a messages from a Spring {@link MessageSource} for use with JSF pages. The specified {@link #getSource()
 * source} is exposed as a {@link MessageSourceMap} under the specified {@link #getVar() var}. If a source is not
 * explicitly defined the current {@link SpringFacesIntegration#getApplicationContext() application context} will be
 * used.
 * <p>
 * By default the key of message to load is prefixed with a string build from {@link UIViewRoot#getViewId() root view
 * ID} as follows:
 * <ul>
 * <li>Any 'WEB-INF' prefix is removed</li>
 * <li>Any file extension is removed</li>
 * <li>All '/' characters are converted to '.'</li>
 * </ul>
 * For example the prefix the view "<b><tt>/WEB-INF/pages/spring/example.xhtml</tt></b>" will use the prefix "<b>
 * <tt>pages.spring.example.</tt></b> ".
 * <p>
 * Use the {@link #setPrefix(String) prefix} attribute if a different prefix is required.
 * 
 * @author Phillip Webb
 */
public class UIMessageSource extends UIComponentBase {

	// FIXME how to deal with missing messages? exception? warning? FacesMessage?

	private static final String SLASH = "/";

	private final Log logger = LogFactory.getLog(getClass());

	public static final String COMPONENT_FAMILY = "spring.faces.MessageSource";

	private static final String WEB_INF = "WEB-INF";

	@Override
	public String getFamily() {
		return COMPONENT_FAMILY;
	}

	@Override
	public void encodeEnd(FacesContext context) throws IOException {
		MessageSourceMap messageSourceMap = createMessageSourceMap(context);
		String var = getVar();
		Assert.state(StringUtils.hasLength(var), "No 'var' attibute specified for UIMessageSource component");
		Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
		Object previous = requestMap.put(var, messageSourceMap);
		if (previous != null && logger.isWarnEnabled()) {
			logger.warn("The request scoped JSF variable '" + var + "' of type " + previous.getClass().getName()
					+ " has been replaced by UIMessageSource");
		}
		super.encodeEnd(context);
	}

	/**
	 * Create a new {@link MessageSourceMap}.
	 * @param context the faces context
	 * @return a {@link MessageSourceMap} instance
	 */
	private MessageSourceMap createMessageSourceMap(final FacesContext context) {
		MessageSource messageSource = getMessageSource(context);
		String[] prefixCodes = getPrefixCodes(context);
		return new UIMessageSourceMap(context, messageSource, prefixCodes);
	}

	/**
	 * Obtain a {@link MessageSourceMap} by using the specified {@link #getSource()} or falling back to the current
	 * {@link SpringFacesIntegration#getApplicationContext() ApplicationContext}.
	 * @param context the faces context
	 * @return a {@link MessageSource} instance
	 */
	private MessageSource getMessageSource(FacesContext context) {
		Assert.notNull(context, "Context must not be null");
		MessageSource source = getSource();
		if (source == null) {
			ExternalContext externalContext = context.getExternalContext();
			if (SpringFacesIntegration.isInstalled(externalContext)) {
				source = SpringFacesIntegration.getCurrentInstance(externalContext).getApplicationContext();
			}
		}
		Assert.state(source != null, "Unable to find MessageSource, ensure that SpringFaces intergation "
				+ "is enabled or set the 'source' attribute");
		return source;
	}

	/**
	 * Returns the prefix codes either has {@link #getPrefix() defined} by the user or built from the root view ID.
	 * @param context the faces context
	 * @return the prefix codes
	 */
	private String[] getPrefixCodes(FacesContext context) {
		String definedPrefix = getPrefix();
		if (definedPrefix != null) {
			return StringUtils.commaDelimitedListToStringArray(definedPrefix);
		}
		return new String[] { buildPrefixCodeFromViewRoot(context) };
	}

	/**
	 * Build a prefix code from the current view root.
	 * @param context the faces context
	 * @return a prefix code
	 */
	private String buildPrefixCodeFromViewRoot(FacesContext context) {
		String code = context.getViewRoot().getViewId();
		code = removePrefix(code, SLASH);
		code = removePrefix(code, WEB_INF);
		code = removePrefix(code, SLASH);
		code = removeExtension(code);
		code = code.replaceAll("\\/", ".");
		if (!code.endsWith(".")) {
			code = code + ".";
		}
		return code;
	}

	private String removePrefix(String s, String prefix) {
		if (s.toUpperCase().startsWith(prefix)) {
			return s.substring(prefix.length());
		}
		return s;
	}

	private String removeExtension(String s) {
		int lastDot = s.lastIndexOf(".");
		if (lastDot > -1) {
			return s.substring(0, lastDot);
		}
		return s;
	}

	public String getVar() {
		return (String) getStateHelper().get(PropertyKeys.var);
	}

	public void setVar(String var) {
		getStateHelper().put(PropertyKeys.var, var);
	}

	public MessageSource getSource() {
		return (MessageSource) getStateHelper().get(PropertyKeys.source);
	}

	public void setSource(MessageSource source) {
		getStateHelper().put(PropertyKeys.source, source);
	}

	public String getPrefix() {
		return (String) getStateHelper().put(PropertyKeys.prefix, null);
	}

	public void setPrefix(String prefix) {
		getStateHelper().put(PropertyKeys.prefix, prefix);
	}

	private enum PropertyKeys {
		source, var, prefix
	}

	private class UIMessageSourceMap extends MessageSourceMap {

		private FacesContext context;

		public UIMessageSourceMap(FacesContext context, MessageSource messageSource, String[] prefixCodes) {
			super(messageSource, prefixCodes);
			this.context = context;
		}

		@Override
		protected Locale getLocale() {
			return FacesUtils.getLocale(context);
		}

		@Override
		protected Object resolveMessageArgument(Object argument) {
			if (argument != null) {
				try {
					Converter converter = context.getApplication().createConverter(argument.getClass());
					if (converter != null) {
						return converter.getAsString(context, UIMessageSource.this, argument);
					}
				} catch (FacesException e) {
				}
			}
			return argument;
		}

		@Override
		protected void handleNoSuchMessageException(NoSuchMessageException exception) {
			if (context.isProjectStage(ProjectStage.Production)) {
				throw exception;
			}
			if (logger.isWarnEnabled()) {
				logger.warn(exception.getMessage(), exception);
			}
			FacesMessage message = new FacesMessage(exception.getMessage());
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			context.addMessage(UIMessageSource.this.getClientId(context), message);
		}
	}
}
