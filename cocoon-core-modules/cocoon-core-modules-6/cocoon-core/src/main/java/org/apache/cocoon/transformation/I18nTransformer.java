/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.transformation;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.treeprocessor.variables.VariableExpressionTokenizer;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.i18n.Bundle;
import org.apache.cocoon.i18n.BundleFactory;
import org.apache.cocoon.i18n.I18nUtils;
import org.apache.cocoon.sitemap.PatternException;
import org.apache.cocoon.xml.ParamSaxBuffer;
import org.apache.cocoon.xml.SaxBuffer;

import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Internationalization transformer is used to transform i18n markup into text
 * based on a particular locale.
 *
 * <h3>Overview</h3>
 * <p>The i18n transformer works by finding a translation for the user's locale
 * in the configured catalogues. Locale is passed as parameter to the transformer,
 * and it can be determined based on the request, session, or a cookie data by
 * the {@link org.apache.cocoon.acting.LocaleAction}.</p>
 *
 * <p>For the passed local it then attempts to find a message catalogue that
 * satisifies the locale, and uses it for for processing text replacement
 * directed by i18n markup.</p>
 *
 * <p>Message catalogues are maintained in separate files, with a naming
 * convention similar to that of {@link java.util.ResourceBundle}. I.e.
 * <code>basename_locale</code>, where <i>basename</i> can be any name,
 * and <i>locale</i> can be any locale specified using ISO 639/3166
 * characters (eg. <code>en_AU</code>, <code>de_AT</code>, <code>es</code>).</p>
 *
 * <p><strong>NOTE:</strong> ISO 639 is not a stable standard; some of the
 * language codes it defines (specifically, iw, ji, and in) have changed
 * (see {@link java.util.Locale} for details).
 *
 * <h3>Message Catalogues</h3>
 * <p>Catalogues are of the following format:
 * <pre>
 * &lt;?xml version="1.0"?&gt;
 * &lt;!-- message catalogue file for locale ... --&gt;
 * &lt;catalogue xml:lang=&quot;locale&quot;&gt;
 *   &lt;message key="key"&gt;text &lt;i&gt;or&lt;/i&gt; markup&lt;/message&gt;
 *   ....
 * &lt;/catalogue&gt;
 * </pre>
 * Where <code>key</code> specifies a particular message for that
 * language.
 *
 * <h3>Usage</h3>
 * <p>Files to be translated contain the following markup:
 * <pre>
 * &lt;?xml version="1.0"?&gt;
 * ... some text, translate &lt;i18n:text&gt;key&lt;/i18n:text&gt;
 * </pre>
 * At runtime, the i18n transformer will find a message catalogue for the
 * user's locale, and will appropriately replace the text between the
 * <code>&lt;i18n:text&gt;</code> markup, using the value between the tags as
 * the lookup key.</p>
 *
 * <p>If the i18n transformer cannot find an appropriate message catalogue for
 * the user's given locale, it will recursively try to locate a <i>parent</i>
 * message catalogue, until a valid catalogue can be found.
 * ie:
 * <ul>
 *  <li><strong>catalogue</strong>_<i>language</i>_<i>country</i>_<i>variant</i>.xml
 *  <li><strong>catalogue</strong>_<i>language</i>_<i>country</i>.xml
 *  <li><strong>catalogue</strong>_<i>language</i>.xml
 *  <li><strong>catalogue</strong>.xml
 * </ul>
 * eg: Assuming a basename of <i>messages</i> and a locale of <i>en_AU</i>
 * (no variant), the following search will occur:
 * <ul>
 *  <li><strong>messages</strong>_<i>en</i>_<i>AU</i>.xml
 *  <li><strong>messages</strong>_<i>en</i>.xml
 *  <li><strong>messages</strong>.xml
 * </ul>
 * This allows the developer to write a hierarchy of message catalogues,
 * at each defining messages with increasing depth of variation.</p>
 *
 * <p>In addition, catalogues can be split across multiple locations. For example,
 * there can be a default catalogue in one directory with a user or client specific
 * catalogue in another directory. The catalogues will be searched in the order of
 * the locations specified still following the locale ordering specified above.
 * eg: Assuming a basename of <i>messages</i> and a locale of <i>en_AU</i>
 * (no variant) and locations of <i>translations/client</i> and <i>translations</i>,
 * the following search will occur:
 * <ul>
 *   <li><i>translations/client/</i><strong>messages</strong>_<i>en</i>_<i>AU</i>.xml
 *   <li><i>translations/</i><strong>messages</strong>_<i>en</i>_<i>AU</i>.xml
 *   <li><i>translations/client/</i><strong>messages</strong>_<i>en</i>.xml
 *   <li><i>translations/</i><strong>messages</strong>_<i>en</i.xml
 *   <li><i>translations/client/</i><strong>messages</strong>.xml
 *   <li><i>translations/</i><strong>messages</strong>.xml
 * </ul>
 * </p>
 *
 * <p>The <code>i18n:text</code> element can optionally take an attribute
 * <code>i18n:catalogue</code> to indicate which specific catalogue to use.
 * The value of this attribute should be the id of the catalogue to use
 * (see sitemap configuration).
 *
 * <h3>Sitemap Configuration</h3>
 * <pre>
 * &lt;map:transformer name="i18n"
 *     src="org.apache.cocoon.transformation.I18nTransformer"&gt;
 *
 *     &lt;catalogues default="someId"&gt;
 *       &lt;catalogue id="someId" name="messages" [location="translations"]&gt;
 *         [&lt;location&gt;translations/client&lt;/location&gt;]
 *         [&lt;location&gt;translations&lt;/location&gt;]
 *       &lt;/catalogue&gt;
 *       ...
 *     &lt;/catalogues&gt;
 *     &lt;untranslated-text&gt;untranslated&lt;/untranslated-text&gt;
 *     &lt;preload&gt;en_US&lt;/preload&gt;
 *     &lt;preload catalogue="someId"&gt;fr_CA&lt;/preload&gt;
 * &lt;/map:transformer&gt;
 * </pre>
 * Where:
 * <ul>
 *  <li><strong>catalogues</strong>: container element in which the catalogues
 *      are defined. It must have an attribute 'default' whose value is one
 *      of the id's of the catalogue elements. (<i>mandatory</i>).
 *  <li><strong>catalogue</strong>: specifies a catalogue. It takes 2 required
 *      attributes: id (can be wathever you like) and name (base name of the catalogue).
 *      The location (location of the message catalogue) is also required, but can be
 *      specified either as an attribute or as one or more subelements, but not both.
 *      If more than one location is specified the catalogues will be searched in the
 *      order they appear in the configuration. The name and location can contain
 *      references to inputmodules (same syntax as in other places in the
 *      sitemap). They are resolved on each usage of the transformer, so they can
 *      refer to e.g. request parameters. (<i>at least 1 catalogue
 *      element required</i>).  After input module references are resolved the location
 *      string can be the root of a URI. For example, specifying a location of
 *      cocoon:/test with a name of messages and a locale of en_GB will cause the
 *      sitemap to try to process cocoon:/test/messages_en_GB.xml,
 *      cocoon:/test/messages_en.xml and cocoon:/test/messages.xml.
 *  <li><strong>untranslated-text</strong>: text used for
 *      untranslated keys (default is to output the key name).
 *  <li><strong>preload</strong>: locale of the catalogue to preload. Will attempt
 *      to resolve all configured catalogues for specified locale. If optional
 *      <code>catalogue</code> attribute is present, will preload only specified
 *      catalogue. Multiple <code>preload</code> elements can be specified.
 * </ul>
 *
 * <h3>Pipeline Usage</h3>
 * <p>To use the transformer in a pipeline, simply specify it in a particular
 * transform, and pass locale parameter:
 * <pre>
 * &lt;map:match pattern="file"&gt;
 *   &lt;map:generate src="file.xml"/&gt;
 *   &lt;map:transform type="i18n"&gt;
 *     &lt;map:parameter name="locale" value="..."/&gt;
 *   &lt;/map:transform&gt;
 *   &lt;map:serialize/&gt;
 * &lt;/map:match&gt;
 * </pre>
 * You can use {@link org.apache.cocoon.acting.LocaleAction} or any other
 * way to provide transformer with a locale.</p>
 *
 * <p>If in certain pipeline, you want to use a different catalogue as the
 * default catalogue, you can do so by specifying a parameter called
 * <strong>default-catalogue-id</strong>.
 *
 * <p>The <strong>untranslated-text</strong> can also be overridden at the
 * pipeline level by specifying it as a parameter.</p>
 *
 *
 * <h3>i18n markup</h3>
 *
 * <p>For date, time and number formatting use the following tags:
 * <ul>
 *  <li><strong>&lt;i18n:date/&gt;</strong> gives localized date.</li>
 *  <li><strong>&lt;i18n:date-time/&gt;</strong> gives localized date and time.</li>
 *  <li><strong>&lt;i18n:time/&gt;</strong> gives localized time.</li>
 *  <li><strong>&lt;i18n:number/&gt;</strong> gives localized number.</li>
 *  <li><strong>&lt;i18n:currency/&gt;</strong> gives localized currency.</li>
 *  <li><strong>&lt;i18n:percent/&gt;</strong> gives localized percent.</li>
 * </ul>
 * Elements <code>date</code>, <code>date-time</code> and <code>time</code>
 * accept <code>pattern</code> and <code>src-pattern</code> attribute, with
 * values of:
 * <ul>
 *  <li><code>short</code>
 *  <li><code>medium</code>
 *  <li><code>long</code>
 *  <li><code>full</code>
 * </ul>
 * See {@link java.text.DateFormat} for more info on these values.</p>
 *
 * <p>Elements <code>date</code>, <code>date-time</code>, <code>time</code> and
 * <code>number</code>, a different <code>locale</code> and
 * <code>source-locale</code> can be specified:
 * <pre>
 * &lt;i18n:date src-pattern="short" src-locale="en_US" locale="de_DE"&gt;
 *   12/24/01
 * &lt;/i18n:date&gt;
 * </pre>
 * Will result in 24.12.2001.</p>
 *
 * <p>A given real <code>pattern</code> and <code>src-pattern</code> (not
 * keywords <code>short, medium, long, full</code>) overrides any value
 * specified by <code>locale</code> and <code>src-locale</code> attributes.</p>
 *
 * <p>Future work coming:
 * <ul>
 *  <li>Introduce new &lt;get-locale/&gt; element
 *  <li>Move all formatting routines to I18nUtils
 * </ul>
 *
 * @cocoon.sitemap.component.documentation
 * Internationalization transformer is used to transform i18n markup into text
 * based on a particular locale.
 * @cocoon.sitemap.component.name   i18n
 * @cocoon.sitemap.component.documentation.caching Yes.
 * Currently, validity of used i18n catalogues is not verified
 *
 * @version $Id$
 */
public class I18nTransformer extends AbstractTransformer
                             implements CacheableProcessingComponent,
                                        Serviceable, Configurable, Disposable {

    /**
     * The namespace for i18n is "http://apache.org/cocoon/i18n/2.1".
     */
    public static final String I18N_NAMESPACE_URI = I18nUtils.NAMESPACE_URI;

    /**
     * The old namespace for i18n is "http://apache.org/cocoon/i18n/2.0".
     */
    public static final String I18N_OLD_NAMESPACE_URI = I18nUtils.OLD_NAMESPACE_URI;

    //
    // i18n elements
    //

    /**
     * <code>i18n:text</code> element is used to translate any text, with
     * or without markup. Example:
     * <pre>
     *   &lt;i18n:text&gt;
     *     This is &lt;strong&gt;translated&lt;/strong&gt; string.
     *   &lt;/i18n:text&gt;
     * </pre>
     */
    public static final String I18N_TEXT_ELEMENT            = "text";

    /**
     * <code>i18n:translate</code> element is used to translate text with
     * parameter substitution. Example:
     * <pre>
     * &lt;i18n:translate&gt;
     *   &lt;i18n:text&gt;This is translated string with {0} param&lt;/i18n:text&gt;
     *   &lt;i18n:param&gt;1&lt;/i18n:param&gt;
     * &lt;/i18n:translate&gt;
     * </pre>
     * The <code>i18n:text</code> fragment can include markup and parameters
     * at any place. Also do parameters, which can include <code>i18n:text</code>,
     * <code>i18n:date</code>, etc. elements (without keys only).
     *
     * @see #I18N_TEXT_ELEMENT
     * @see #I18N_PARAM_ELEMENT
     */
    public static final String I18N_TRANSLATE_ELEMENT       = "translate";

    /**
     * <code>i18n:choose</code> element is used to translate elements in-place.
     * The first <code>i18n:when</code> element matching the current locale
     * is selected and the others are discarded.
     *
     * <p>To specify what to do if no locale matched, simply add a node with
     * <code>locale="*"</code>. <em>Note that this element must be the last
     * child of &lt;i18n:choose&gt;.</em></p>
     * <pre>
     * &lt;i18n:choose&gt;
     *   &lt;i18n:when locale="en"&gt;
     *     Good Morning
     *   &lt;/en&gt;
     *   &lt;i18n:when locale="fr"&gt;
     *     Bonjour
     *   &lt;/jp&gt;
     *   &lt;i18n:when locale="jp"&gt;
     *     Aligato?
     *   &lt;/jp&gt;
     *   &lt;i18n:otherwise&gt;
     *     Sorry, i don't know how to say hello in your language
     *   &lt;/jp&gt;
     * &lt;i18n:translate&gt;
     * </pre>
     * <p>You can include any markup within <code>i18n:when</code> elements,
     * with the exception of other <code>i18n:*</code> elements.</p>
     *
     * @see #I18N_IF_ELEMENT
     * @see #I18N_LOCALE_ATTRIBUTE
     * @since 2.1
     */
    public static final String I18N_CHOOSE_ELEMENT          = "choose";

    /**
     * <code>i18n:when</code> is used to test a locale.
     * It can be used within <code>i18:choose</code> elements or alone.
     * <em>Note: Using <code>locale="*"</code> here has no sense.</em>
     * Example:
     * <pre>
     * &lt;greeting&gt;
     *   &lt;i18n:when locale="en"&gt;Hello&lt;/i18n:when&gt;
     *   &lt;i18n:when locale="fr"&gt;Bonjour&lt;/i18n:when&gt;
     * &lt;/greeting&gt;
     * </pre>
     *
     * @see #I18N_LOCALE_ATTRIBUTE
     * @see #I18N_CHOOSE_ELEMENT
     * @since 2.1
     */
    public static final String I18N_WHEN_ELEMENT            = "when";

    /**
     * <code>i18n:if</code> is used to test a locale. Example:
     * <pre>
     * &lt;greeting&gt;
     *   &lt;i18n:if locale="en"&gt;Hello&lt;/i18n:when&gt;
     *   &lt;i18n:if locale="fr"&gt;Bonjour&lt;/i18n:when&gt;
     * &lt;/greeting&gt;
     * </pre>
     *
     * @see #I18N_LOCALE_ATTRIBUTE
     * @see #I18N_CHOOSE_ELEMENT
     * @see #I18N_WHEN_ELEMENT
     * @since 2.1
     */
    public static final String I18N_IF_ELEMENT            = "if";

    /**
     * <code>i18n:otherwise</code> is used to match any locale when
     * no matching locale has been found inside an <code>i18n:choose</code>
     * block.
     *
     * @see #I18N_CHOOSE_ELEMENT
     * @see #I18N_WHEN_ELEMENT
     * @since 2.1
     */
    public static final String I18N_OTHERWISE_ELEMENT       = "otherwise";

    /**
     * <code>i18n:param</code> is used with i18n:translate to provide
     * substitution params. The param can have <code>i18n:text</code> as
     * its value to provide multilungual value. Parameters can have
     * additional attributes to be used for formatting:
     * <ul>
     *   <li><code>type</code>: can be <code>date, date-time, time,
     *   number, currency, currency-no-unit or percent</code>.
     *   Used to format params before substitution.</li>
     *   <li><code>value</code>: the value of the param. If no value is
     *   specified then the text inside of the param element will be used.</li>
     *   <li><code>locale</code>: used only with <code>number, date, time,
     *   date-time</code> types and used to override the current locale to
     *   format the given value.</li>
     *   <li><code>src-locale</code>: used with <code>number, date, time,
     *   date-time</code> types and specify the locale that should be used to
     *   parse the given value.</li>
     *   <li><code>pattern</code>: used with <code>number, date, time,
     *   date-time</code> types and specify the pattern that should be used
     *   to format the given value.</li>
     *   <li><code>src-pattern</code>: used with <code>number, date, time,
     *   date-time</code> types and specify the pattern that should be used
     *   to parse the given value.</li>
     * </ul>
     *
     * @see #I18N_TRANSLATE_ELEMENT
     * @see #I18N_DATE_ELEMENT
     * @see #I18N_TIME_ELEMENT
     * @see #I18N_DATE_TIME_ELEMENT
     * @see #I18N_NUMBER_ELEMENT
     */
    public static final String I18N_PARAM_ELEMENT           = "param";

    /**
     * This attribute affects a name to the param that could be used
     * for substitution.
     *
     * @since 2.1
     */
    public static final String I18N_PARAM_NAME_ATTRIBUTE    = "name";

    /**
     * <code>i18n:date</code> is used to provide a localized date string.
     * Allowed attributes are: <code>pattern, src-pattern, locale,
     * src-locale</code>. Usage examples:
     * <pre>
     *  &lt;i18n:date src-pattern="short" src-locale="en_US" locale="de_DE"&gt;
     *    12/24/01
     *  &lt;/i18n:date&gt;
     *
     *  &lt;i18n:date pattern="dd/MM/yyyy" /&gt;
     * </pre>
     *
     * If no value is specified then the current date will be used. E.g.:
     * <pre>
     *   &lt;i18n:date /&gt;
     * </pre>
     * Displays the current date formatted with default pattern for
     * the current locale.
     *
     * @see #I18N_PARAM_ELEMENT
     * @see #I18N_DATE_TIME_ELEMENT
     * @see #I18N_TIME_ELEMENT
     * @see #I18N_NUMBER_ELEMENT
     */
    public static final String I18N_DATE_ELEMENT            = "date";

    /**
     * <code>i18n:date-time</code> is used to provide a localized date and
     * time string. Allowed attributes are: <code>pattern, src-pattern,
     * locale, src-locale</code>. Usage examples:
     * <pre>
     *  &lt;i18n:date-time src-pattern="short" src-locale="en_US" locale="de_DE"&gt;
     *    12/24/01 1:00 AM
     *  &lt;/i18n:date&gt;
     *
     *  &lt;i18n:date-time pattern="dd/MM/yyyy hh:mm" /&gt;
     * </pre>
     *
     * If no value is specified then the current date and time will be used.
     * E.g.:
     * <pre>
     *  &lt;i18n:date-time /&gt;
     * </pre>
     * Displays the current date formatted with default pattern for
     * the current locale.
     *
     * @see #I18N_PARAM_ELEMENT
     * @see #I18N_DATE_ELEMENT
     * @see #I18N_TIME_ELEMENT
     * @see #I18N_NUMBER_ELEMENT
     */
    public static final String I18N_DATE_TIME_ELEMENT       = "date-time";

    /**
     * <code>i18n:time</code> is used to provide a localized time string.
     * Allowed attributes are: <code>pattern, src-pattern, locale,
     * src-locale</code>. Usage examples:
     * <pre>
     *  &lt;i18n:time src-pattern="short" src-locale="en_US" locale="de_DE"&gt;
     *    1:00 AM
     *  &lt;/i18n:time&gt;
     *
     * &lt;i18n:time pattern="hh:mm:ss" /&gt;
     * </pre>
     *
     * If no value is specified then the current time will be used. E.g.:
     * <pre>
     *  &lt;i18n:time /&gt;
     * </pre>
     * Displays the current time formatted with default pattern for
     * the current locale.
     *
     * @see #I18N_PARAM_ELEMENT
     * @see #I18N_DATE_TIME_ELEMENT
     * @see #I18N_DATE_ELEMENT
     * @see #I18N_NUMBER_ELEMENT
     */
    public static final String I18N_TIME_ELEMENT            = "time";

    /**
     * <code>i18n:number</code> is used to provide a localized number string.
     * Allowed attributes are: <code>pattern, src-pattern, locale, src-locale,
     * type</code>. Usage examples:
     * <pre>
     *  &lt;i18n:number src-pattern="short" src-locale="en_US" locale="de_DE"&gt;
     *    1000.0
     *  &lt;/i18n:number&gt;
     *
     * &lt;i18n:number type="currency" /&gt;
     * </pre>
     *
     * If no value is specifies then 0 will be used.
     *
     * @see #I18N_PARAM_ELEMENT
     * @see #I18N_DATE_TIME_ELEMENT
     * @see #I18N_TIME_ELEMENT
     * @see #I18N_DATE_ELEMENT
     */
    public static final String I18N_NUMBER_ELEMENT      = "number";

    /**
     * Currency element name
     */
    public static final String I18N_CURRENCY_ELEMENT    = "currency";

    /**
     * Percent element name
     */
    public static final String I18N_PERCENT_ELEMENT     = "percent";

    /**
     * Integer currency element name
     */
    public static final String I18N_INT_CURRENCY_ELEMENT = "int-currency";

    /**
     * Currency without unit element name
     */
    public static final String I18N_CURRENCY_NO_UNIT_ELEMENT = "currency-no-unit";

    /**
     * Integer currency without unit element name
     */
    public static final String I18N_INT_CURRENCY_NO_UNIT_ELEMENT = "int-currency-no-unit";

    //
    // i18n general attributes
    //

    /**
     * This attribute is used with i18n:text element to indicate the key of
     * the according message. The character data of the element will be used
     * if no message is found by this key. E.g.:
     * <pre>
     * &lt;i18n:text i18n:key="a_key"&gt;article_text1&lt;/i18n:text&gt;
     * </pre>
     */
    public static final String I18N_KEY_ATTRIBUTE           = "key";

    /**
     * This attribute is used with <strong>any</strong> element (even not i18n)
     * to translate attribute values. Should contain whitespace separated
     * attribute names that should be translated:
     * <pre>
     * &lt;para title="first" name="article" i18n:attr="title name"/&gt;
     * </pre>
     * Attribute value considered as key in message catalogue.
     */
    public static final String I18N_ATTR_ATTRIBUTE          = "attr";

    /**
     * This attribute is used with <strong>any</strong> element (even not i18n)
     * to evaluate attribute values. Should contain whitespace separated
     * attribute names that should be evaluated:
     * <pre>
     * &lt;para title="first" name="{one} {two}" i18n:attr="name"/&gt;
     * </pre>
     * Attribute value considered as expression containing text and catalogue
     * keys in curly braces.
     */
    public static final String I18N_EXPR_ATTRIBUTE          = "expr";

    //
    // i18n number and date formatting attributes
    //

    /**
     * This attribute is used with date and number formatting elements to
     * indicate the pattern that should be used to parse the element value.
     *
     * @see #I18N_PARAM_ELEMENT
     * @see #I18N_DATE_TIME_ELEMENT
     * @see #I18N_DATE_ELEMENT
     * @see #I18N_TIME_ELEMENT
     * @see #I18N_NUMBER_ELEMENT
     */
    public static final String I18N_SRC_PATTERN_ATTRIBUTE   = "src-pattern";

    /**
     * This attribute is used with date and number formatting elements to
     * indicate the pattern that should be used to format the element value.
     *
     * @see #I18N_PARAM_ELEMENT
     * @see #I18N_DATE_TIME_ELEMENT
     * @see #I18N_DATE_ELEMENT
     * @see #I18N_TIME_ELEMENT
     * @see #I18N_NUMBER_ELEMENT
     */
    public static final String I18N_PATTERN_ATTRIBUTE       = "pattern";

    /**
     * This attribute is used with date and number formatting elements to
     * indicate the locale that should be used to format the element value.
     * Also used for in-place translations.
     *
     * @see #I18N_PARAM_ELEMENT
     * @see #I18N_DATE_TIME_ELEMENT
     * @see #I18N_DATE_ELEMENT
     * @see #I18N_TIME_ELEMENT
     * @see #I18N_NUMBER_ELEMENT
     * @see #I18N_WHEN_ELEMENT
     */
    public static final String I18N_LOCALE_ATTRIBUTE        = "locale";

    /**
     * This attribute is used with date and number formatting elements to
     * indicate the locale that should be used to parse the element value.
     *
     * @see #I18N_PARAM_ELEMENT
     * @see #I18N_DATE_TIME_ELEMENT
     * @see #I18N_DATE_ELEMENT
     * @see #I18N_TIME_ELEMENT
     * @see #I18N_NUMBER_ELEMENT
     */
    public static final String I18N_SRC_LOCALE_ATTRIBUTE    = "src-locale";

    /**
     * This attribute is used with date and number formatting elements to
     * indicate the value that should be parsed and formatted. If value
     * attribute is not used then the character data of the element will be used.
     *
     * @see #I18N_PARAM_ELEMENT
     * @see #I18N_DATE_TIME_ELEMENT
     * @see #I18N_DATE_ELEMENT
     * @see #I18N_TIME_ELEMENT
     * @see #I18N_NUMBER_ELEMENT
     */
    public static final String I18N_VALUE_ATTRIBUTE         = "value";

    /**
     * This attribute is used with <code>i18:param</code> to
     * indicate the parameter type: <code>date, time, date-time</code> or
     * <code>number, currency, percent, int-currency, currency-no-unit,
     * int-currency-no-unit</code>.
     * Also used with <code>i18:translate</code> to indicate inplace
     * translations: <code>inplace</code>
     * @deprecated since 2.1. Use nested tags instead, e.g.:
     * &lt;i18n:param&gt;&lt;i18n:date/&gt;&lt;/i18n:param&gt;
     */
    public static final String I18N_TYPE_ATTRIBUTE          = "type";

    /**
     * This attribute is used to specify a different locale for the
     * currency. When specified, this locale will be combined with
     * the "normal" locale: e.g. the seperator symbols are taken from
     * the normal locale but the currency symbol and possition will
     * be taken from the currency locale.
     * This enables to see a currency formatted for Euro but with US
     * grouping and decimal char.
     */
    public static final String CURRENCY_LOCALE_ATTRIBUTE = "currency";

    /**
     * This attribute can be used on <code>i18n:text</code> to indicate the catalogue
     * from which the key should be retrieved. This attribute is optional,
     * if it is not mentioned the default catalogue is used.
     */
    public static final String I18N_CATALOGUE_ATTRIBUTE = "catalogue";

    //
    // Configuration parameters
    //

    /**
     * This configuration parameter specifies the default locale to be used.
     */
    public static final String I18N_LOCALE      = "locale";

    /**
     * This configuration parameter specifies the id of the catalogue to be used as
     * default catalogue, allowing to redefine the default catalogue on the pipeline
     * level.
     */
    public static final String I18N_DEFAULT_CATALOGUE_ID = "default-catalogue-id";

    /**
     * This configuration parameter specifies the message that should be
     * displayed in case of a not translated text (message not found).
     */
    public static final String I18N_UNTRANSLATED        = "untranslated-text";

    /**
     * This configuration parameter specifies locale for which catalogues should
     * be preloaded.
     */
    public static final String I18N_PRELOAD             = "preload";

    /**
     * <code>fraction-digits</code> attribute is used with
     * <code>i18:number</code> to
     * indicate the number of digits behind the fraction
     */
    public static final String I18N_FRACTION_DIGITS_ATTRIBUTE = "fraction-digits";

    //
    // States of the transformer
    //

    private static final int STATE_OUTSIDE                       = 0;
    private static final int STATE_INSIDE_TEXT                   = 10;
    private static final int STATE_INSIDE_PARAM                  = 20;
    private static final int STATE_INSIDE_TRANSLATE              = 30;
    private static final int STATE_INSIDE_CHOOSE                 = 50;
    private static final int STATE_INSIDE_WHEN                   = 51;
    private static final int STATE_INSIDE_OTHERWISE              = 52;
    private static final int STATE_INSIDE_DATE                   = 60;
    private static final int STATE_INSIDE_DATE_TIME              = 61;
    private static final int STATE_INSIDE_TIME                   = 62;
    private static final int STATE_INSIDE_NUMBER                 = 63;

    // All date-time related parameter types and element names
    private static final Set dateTypes;

    // All number related parameter types and element names
    private static final Set numberTypes;

    // Date pattern types map: short, medium, long, full
    private static final Map datePatterns;

    static {
        // initialize date types set
        HashSet set = new HashSet(5);
        set.add(I18N_DATE_ELEMENT);
        set.add(I18N_TIME_ELEMENT);
        set.add(I18N_DATE_TIME_ELEMENT);
        dateTypes = Collections.unmodifiableSet(set);

        // initialize number types set
        set = new HashSet(9);
        set.add(I18N_NUMBER_ELEMENT);
        set.add(I18N_PERCENT_ELEMENT);
        set.add(I18N_CURRENCY_ELEMENT);
        set.add(I18N_INT_CURRENCY_ELEMENT);
        set.add(I18N_CURRENCY_NO_UNIT_ELEMENT);
        set.add(I18N_INT_CURRENCY_NO_UNIT_ELEMENT);
        numberTypes = Collections.unmodifiableSet(set);

        // Initialize date patterns map
        Map map = new HashMap(7);
        map.put("SHORT", new Integer(DateFormat.SHORT));
        map.put("MEDIUM", new Integer(DateFormat.MEDIUM));
        map.put("LONG", new Integer(DateFormat.LONG));
        map.put("FULL", new Integer(DateFormat.FULL));
        datePatterns = Collections.unmodifiableMap(map);
    }


    //
    // Global configuration variables
    //

    /**
     * Component (service) manager
     */
    protected ServiceManager manager;

    /**
     * Message bundle loader factory component (service)
     */
    protected BundleFactory factory;

    /**
     * All catalogues (keyed by catalogue id). The values are instances
     * of {@link CatalogueInfo}.
     */
    private Map catalogues;

    /**
     * Default (global) catalogue
     */
    private CatalogueInfo defaultCatalogue;

    /**
     * Default (global) untranslated message value
     */
    private String defaultUntranslated;

    //
    // Local configuration variables
    //

    protected Map objectModel;

    /**
     * Locale
     */
    protected Locale locale;

    /**
     * Catalogue (local)
     */
    private CatalogueInfo catalogue;

    /**
     * Current (local) untranslated message value
     */
    private String untranslated;

    /**
     * {@link SaxBuffer} containing the contents of {@link #untranslated}.
     */
    private ParamSaxBuffer untranslatedRecorder;

    //
    // Current state of the transformer
    //

    /**
     * Current state of the transformer. Default value is STATE_OUTSIDE.
     */
    private int current_state;

    /**
     * Previous state of the transformer.
     * Used in text translation inside params and translate elements.
     */
    private int prev_state;

    /**
     * The i18n:key attribute is stored for the current element.
     * If no translation found for the key then the character data of element is
     * used as default value.
     */
    private String currentKey;

    /**
     * Contains the id of the current catalogue if it was explicitely mentioned
     * on an i18n:text element, otherwise it is null.
     */
    private String currentCatalogueId;

    /**
     * Character data buffer. used to concat chunked character data
     */
    private StringBuffer strBuffer;

    /**
     * A flag for copying the node when doing in-place translation
     */
    private boolean translate_copy;

    // A flag for copying the _GOOD_ node and not others
    // when doing in-place translation within i18n:choose
    private boolean translate_end;

    // Translated text. Inside i18n:translate, collects character events.
    private ParamSaxBuffer tr_text_recorder;

    // Current "i18n:text" events
    private ParamSaxBuffer text_recorder;

    // Current parameter events
    private SaxBuffer param_recorder;

    // Param count when not using i18n:param name="..."
    private int param_count;

    // Param name attribute for substitution.
    private String param_name;

    // i18n:param's hashmap for substitution
    private HashMap indexedParams;

    // Current parameter value (translated or not)
    private String param_value;

    // Date and number elements and params formatting attributes with values.
    private HashMap formattingParams;

    /**
     * Returns the current locale setting of this transformer instance.
     * @return current Locale object
     */
    public Locale getLocale() {
        return this.locale;
    }

    /**
     * Implemenation of CacheableProcessingComponents.
     * Generates unique key for the current locale.
     */
    public java.io.Serializable getKey() {
        // TODO: Key should be composed out of used catalogues locations, and locale.
        //       Right now it is hardcoded only to default catalogue location.
        StringBuffer key = new StringBuffer();
        if (catalogue != null) {
            key.append(catalogue.getLocation()[0]);
        }
        key.append("?");
        if (locale != null) {
            key.append(locale.getLanguage());
            key.append("_");
            key.append(locale.getCountry());
            key.append("_");
            key.append(locale.getVariant());
        }
        return key.toString();
    }

    /**
     * Implementation of CacheableProcessingComponent.
     * Generates validity object for this transformer or <code>null</code>
     * if this instance is not cacheable.
     */
    public SourceValidity getValidity() {
        // FIXME (KP): Cache validity should be generated by
        // Bundle implementations.
        return org.apache.excalibur.source.impl.validity.NOPValidity.SHARED_INSTANCE;
    }

    /**
     * Look up the {@link BundleFactory} to be used.
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        try {
            this.factory = (BundleFactory) manager.lookup(BundleFactory.ROLE);
        } catch (ServiceException e) {
            getLogger().debug("Failed to lookup <" + BundleFactory.ROLE + ">", e);
            throw e;
        }
    }

    /**
     * Implementation of Configurable interface.
     * Configure this transformer.
     */
    public void configure(Configuration conf) throws ConfigurationException {
        // Read in the config options from the transformer definition
        Configuration cataloguesConf = conf.getChild("catalogues", false);
        if (cataloguesConf == null) {
            throw new ConfigurationException("Required <catalogues> configuration is missing.",
                                             conf);
        }

        // new configuration style
        Configuration[] catalogueConfs = cataloguesConf.getChildren("catalogue");
        catalogues = new HashMap(catalogueConfs.length + 3);
        for (int i = 0; i < catalogueConfs.length; i++) {
            String id = catalogueConfs[i].getAttribute("id");
            String name = catalogueConfs[i].getAttribute("name");

            String[] locations;
            String location = catalogueConfs[i].getAttribute("location", null);
            Configuration[] locationConf = catalogueConfs[i].getChildren("location");
            if (location != null) {
                if (locationConf.length > 0) {
                    String msg = "Location attribute cannot be " +
                                 "specified with location elements";
                    getLogger().error(msg);
                    throw new ConfigurationException(msg, catalogueConfs[i]);
                }

                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("name=" + name + ", location=" +
                                      location);
                }
                locations = new String[1];
                locations[0] = location;
            } else {
                if (locationConf.length == 0) {
                    String msg = "A location attribute or location " +
                                 "elements must be specified";
                    getLogger().error(msg);
                    throw new ConfigurationException(msg, catalogueConfs[i]);
                }

                locations = new String[locationConf.length];
                for (int j=0; j < locationConf.length; ++j) {
                    locations[j] = locationConf[j].getValue();
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("name=" + name + ", location=" +
                                          locations[j]);
                    }
                }
            }

            CatalogueInfo catalogueInfo;
            try {
                catalogueInfo = new CatalogueInfo(name, locations);
            } catch (PatternException e) {
                throw new ConfigurationException("Error in name or location attribute on catalogue " +
                                                 "element with id " + id, catalogueConfs[i], e);
            }
            catalogues.put(id, catalogueInfo);
        }

        String defaultCatalogueId = cataloguesConf.getAttribute("default");
        defaultCatalogue = (CatalogueInfo) catalogues.get(defaultCatalogueId);
        if (defaultCatalogue == null) {
            throw new ConfigurationException("Default catalogue id '" + defaultCatalogueId +
                                             "' denotes a nonexisting catalogue", cataloguesConf);
        }

        // Obtain default text to use for untranslated messages
        defaultUntranslated = conf.getChild(I18N_UNTRANSLATED).getValue(null);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Default untranslated text is '" + defaultUntranslated + "'");
        }

        // Preload specified catalogues (if any)
        Configuration[] preloadConfs = conf.getChildren(I18N_PRELOAD);
        for (int i = 0; i < preloadConfs.length; i++) {
            String localeStr = preloadConfs[i].getValue();
            this.locale = I18nUtils.parseLocale(localeStr);

            String id = preloadConfs[i].getAttribute("catalogue", null);
            if (id != null) {
                CatalogueInfo catalogueInfo = (CatalogueInfo) catalogues.get(id);
                if (catalogueInfo == null) {
                    throw new ConfigurationException("Invalid catalogue id '" + id +
                                                     "' in preload element.", preloadConfs[i]);
                }

                try {
                    catalogueInfo.getCatalogue();
                } finally {
                    catalogueInfo.releaseCatalog();
                }
            } else {
                for (Iterator j = catalogues.values().iterator(); j.hasNext(); ) {
                    CatalogueInfo catalogueInfo = (CatalogueInfo) j.next();
                    try {
                        catalogueInfo.getCatalogue();
                    } finally {
                        catalogueInfo.releaseCatalog();
                    }
                }
            }
        }
        this.locale = null;
    }

    /**
     * Setup current instance of transformer.
     */
    public void setup(SourceResolver resolver, Map objectModel, String source,
                      Parameters parameters)
    throws ProcessingException, SAXException, IOException {

        this.objectModel = objectModel;

        untranslated = parameters.getParameter(I18N_UNTRANSLATED, defaultUntranslated);
        if (untranslated != null) {
            untranslatedRecorder = new ParamSaxBuffer();
            untranslatedRecorder.characters(untranslated.toCharArray(), 0, untranslated.length());
        }

        // Get current locale
        String lc = parameters.getParameter(I18N_LOCALE, null);
        Locale locale = I18nUtils.parseLocale(lc);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Using locale '" + locale + "'");
        }

        // Initialize instance state variables
        this.locale             = locale;
        this.current_state      = STATE_OUTSIDE;
        this.prev_state         = STATE_OUTSIDE;
        this.currentKey        = null;
        this.currentCatalogueId = null;
        this.translate_copy     = false;
        this.tr_text_recorder   = null;
        this.text_recorder      = new ParamSaxBuffer();
        this.param_count        = 0;
        this.param_name         = null;
        this.param_value        = null;
        this.param_recorder     = null;
        this.indexedParams      = new HashMap(3);
        this.formattingParams   = null;
        this.strBuffer          = null;

        // give the catalogue variable its value -- first look if it's locally overridden
        // and otherwise use the component-wide defaults.
        String catalogueId = parameters.getParameter(I18N_DEFAULT_CATALOGUE_ID, null);
        if (catalogueId != null) {
            CatalogueInfo catalogueInfo = (CatalogueInfo) catalogues.get(catalogueId);
            if (catalogueInfo == null) {
                throw new ProcessingException("I18nTransformer: '" +
                                              catalogueId +
                                              "' is not an existing catalogue id.");
            }
            catalogue = catalogueInfo;
        } else {
            catalogue = defaultCatalogue;
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Default catalogue is " + catalogue.getName());
        }
    }


    //
    // Standard SAX event handlers
    //

    public void startElement(String uri, String name, String raw,
                             Attributes attr)
    throws SAXException {

        // Handle previously buffered characters
        if (current_state != STATE_OUTSIDE && strBuffer != null) {
            i18nCharacters(strBuffer.toString());
            strBuffer = null;
        }

        // Process start element event
        if (I18nUtils.matchesI18nNamespace(uri)) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Starting i18n element: " + name);
            }
            startI18NElement(name, attr);
        } else {
            // We have a non i18n element event
            if (current_state == STATE_OUTSIDE) {
                super.startElement(uri, name, raw,
                                   translateAttributes(name, attr));
            } else if (current_state == STATE_INSIDE_PARAM) {
                param_recorder.startElement(uri, name, raw, attr);
            } else if (current_state == STATE_INSIDE_TEXT) {
                text_recorder.startElement(uri, name, raw, attr);
            } else if ((current_state == STATE_INSIDE_WHEN ||
                    current_state == STATE_INSIDE_OTHERWISE)
                    && translate_copy) {

                super.startElement(uri, name, raw, attr);
            }
        }
    }

    public void endElement(String uri, String name, String raw)
    throws SAXException {

        // Handle previously buffered characters
        if (current_state != STATE_OUTSIDE && strBuffer != null) {
            i18nCharacters(strBuffer.toString());
            strBuffer = null;
        }

        if (I18nUtils.matchesI18nNamespace(uri)) {
            endI18NElement(name);
        } else if (current_state == STATE_INSIDE_PARAM) {
            param_recorder.endElement(uri, name, raw);
        } else if (current_state == STATE_INSIDE_TEXT) {
            text_recorder.endElement(uri, name, raw);
        } else if (current_state == STATE_INSIDE_CHOOSE ||
                (current_state == STATE_INSIDE_WHEN ||
                current_state == STATE_INSIDE_OTHERWISE)
                && !translate_copy) {

            // Output nothing
        } else {
            super.endElement(uri, name, raw);
        }
    }

    public void characters(char[] ch, int start, int len)
    throws SAXException {

        if (current_state == STATE_OUTSIDE ||
                ((current_state == STATE_INSIDE_WHEN ||
                current_state == STATE_INSIDE_OTHERWISE) && translate_copy)) {

            super.characters(ch, start, len);
        } else {
            // Perform buffering to prevent chunked character data
            if (strBuffer == null) {
                strBuffer = new StringBuffer();
            }
            strBuffer.append(ch, start, len);
        }
    }

    //
    // i18n specific event handlers
    //

    private void startI18NElement(String name, Attributes attr)
    throws SAXException {

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Start i18n element: " + name);
        }

        if (I18N_TEXT_ELEMENT.equals(name)) {
            if (current_state != STATE_OUTSIDE
                    && current_state != STATE_INSIDE_PARAM
                    && current_state != STATE_INSIDE_TRANSLATE) {

                throw new SAXException(
                        getClass().getName()
                        + ": nested i18n:text elements are not allowed."
                        + " Current state: " + current_state);
            }

            prev_state = current_state;
            current_state = STATE_INSIDE_TEXT;

            currentKey = attr.getValue("", I18N_KEY_ATTRIBUTE);
            if (currentKey == null) {
                // Try the namespaced attribute
                currentKey = attr.getValue(I18N_NAMESPACE_URI, I18N_KEY_ATTRIBUTE);
                if (currentKey == null) {
                    // Try the old namespace
                    currentKey = attr.getValue(I18N_OLD_NAMESPACE_URI, I18N_KEY_ATTRIBUTE);
                }
            }

            currentCatalogueId = attr.getValue("", I18N_CATALOGUE_ATTRIBUTE);
            if (currentCatalogueId == null) {
                // Try the namespaced attribute
                currentCatalogueId = attr.getValue(I18N_NAMESPACE_URI, I18N_CATALOGUE_ATTRIBUTE);
            }

            if (prev_state != STATE_INSIDE_PARAM) {
                tr_text_recorder = null;
            }

            if (currentKey != null) {
                tr_text_recorder = getMessage(currentKey, (ParamSaxBuffer)null);
            }

        } else if (I18N_TRANSLATE_ELEMENT.equals(name)) {
            if (current_state != STATE_OUTSIDE) {
                throw new SAXException(
                        getClass().getName()
                        + ": i18n:translate element must be used "
                        + "outside of other i18n elements. Current state: "
                        + current_state);
            }

            prev_state = current_state;
            current_state = STATE_INSIDE_TRANSLATE;
        } else if (I18N_PARAM_ELEMENT.equals(name)) {
            if (current_state != STATE_INSIDE_TRANSLATE) {
                throw new SAXException(
                        getClass().getName()
                        + ": i18n:param element can be used only inside "
                        + "i18n:translate element. Current state: "
                        + current_state);
            }

            param_name = attr.getValue(I18N_PARAM_NAME_ATTRIBUTE);
            if (param_name == null) {
                param_name = String.valueOf(param_count++);
            }

            param_recorder = new SaxBuffer();
            setFormattingParams(attr);
            current_state = STATE_INSIDE_PARAM;
        } else if (I18N_CHOOSE_ELEMENT.equals(name)) {
            if (current_state != STATE_OUTSIDE) {
                throw new SAXException(
                        getClass().getName()
                        + ": i18n:choose elements cannot be used"
                        + "inside of other i18n elements.");
            }

            translate_copy = false;
            translate_end = false;
            prev_state = current_state;
            current_state = STATE_INSIDE_CHOOSE;
        } else if (I18N_WHEN_ELEMENT.equals(name) ||
                I18N_IF_ELEMENT.equals(name)) {

            if (I18N_WHEN_ELEMENT.equals(name) &&
                    current_state != STATE_INSIDE_CHOOSE) {
                throw new SAXException(
                        getClass().getName()
                        + ": i18n:when elements are can be used only"
                        + "inside of i18n:choose elements.");
            }

            if (I18N_IF_ELEMENT.equals(name) &&
                    current_state != STATE_OUTSIDE) {
                throw new SAXException(
                        getClass().getName()
                        + ": i18n:if elements cannot be nested.");
            }

            String locale = attr.getValue(I18N_LOCALE_ATTRIBUTE);
            if (locale == null)
                throw new SAXException(
                        getClass().getName()
                        + ": i18n:" + name
                        + " element cannot be used without 'locale' attribute.");

            if ((!translate_end && current_state == STATE_INSIDE_CHOOSE)
                    || current_state == STATE_OUTSIDE) {

                // Perform soft locale matching
                if (this.locale.toString().startsWith(locale)) {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Locale matching: " + locale);
                    }
                    translate_copy = true;
                }
            }

            prev_state = current_state;
            current_state = STATE_INSIDE_WHEN;

        } else if (I18N_OTHERWISE_ELEMENT.equals(name)) {
            if (current_state != STATE_INSIDE_CHOOSE) {
                throw new SAXException(
                        getClass().getName()
                        + ": i18n:otherwise elements are not allowed "
                        + "only inside i18n:choose.");
            }

            getLogger().debug("Matching any locale");
            if (!translate_end) {
                translate_copy = true;
            }

            prev_state = current_state;
            current_state = STATE_INSIDE_OTHERWISE;

        } else if (I18N_DATE_ELEMENT.equals(name)) {
            if (current_state != STATE_OUTSIDE
                    && current_state != STATE_INSIDE_TEXT
                    && current_state != STATE_INSIDE_PARAM) {
                throw new SAXException(
                        getClass().getName()
                        + ": i18n:date elements are not allowed "
                        + "inside of other i18n elements.");
            }

            setFormattingParams(attr);
            prev_state = current_state;
            current_state = STATE_INSIDE_DATE;
        } else if (I18N_DATE_TIME_ELEMENT.equals(name)) {
            if (current_state != STATE_OUTSIDE
                    && current_state != STATE_INSIDE_TEXT
                    && current_state != STATE_INSIDE_PARAM) {
                throw new SAXException(
                        getClass().getName()
                        + ": i18n:date-time elements are not allowed "
                        + "inside of other i18n elements.");
            }

            setFormattingParams(attr);
            prev_state = current_state;
            current_state = STATE_INSIDE_DATE_TIME;
        } else if (I18N_TIME_ELEMENT.equals(name)) {
            if (current_state != STATE_OUTSIDE
                    && current_state != STATE_INSIDE_TEXT
                    && current_state != STATE_INSIDE_PARAM) {
                throw new SAXException(
                        getClass().getName()
                        + ": i18n:date elements are not allowed "
                        + "inside of other i18n elements.");
            }

            setFormattingParams(attr);
            prev_state = current_state;
            current_state = STATE_INSIDE_TIME;
        } else if (I18N_NUMBER_ELEMENT.equals(name)) {
            if (current_state != STATE_OUTSIDE
                    && current_state != STATE_INSIDE_TEXT
                    && current_state != STATE_INSIDE_PARAM) {
                throw new SAXException(
                        getClass().getName()
                        + ": i18n:number elements are not allowed "
                        + "inside of other i18n elements.");
            }

            setFormattingParams(attr);
            prev_state = current_state;
            current_state = STATE_INSIDE_NUMBER;
        }
    }

    // Get all possible i18n formatting attribute values and store in a Map
    private void setFormattingParams(Attributes attr) {
        // average number of attributes is 3
        formattingParams = new HashMap(3);

        String attr_value = attr.getValue(I18N_SRC_PATTERN_ATTRIBUTE);
        if (attr_value != null) {
            formattingParams.put(I18N_SRC_PATTERN_ATTRIBUTE, attr_value);
        }

        attr_value = attr.getValue(I18N_PATTERN_ATTRIBUTE);
        if (attr_value != null) {
            formattingParams.put(I18N_PATTERN_ATTRIBUTE, attr_value);
        }

        attr_value = attr.getValue(I18N_VALUE_ATTRIBUTE);
        if (attr_value != null) {
            formattingParams.put(I18N_VALUE_ATTRIBUTE, attr_value);
        }

        attr_value = attr.getValue(I18N_LOCALE_ATTRIBUTE);
        if (attr_value != null) {
            formattingParams.put(I18N_LOCALE_ATTRIBUTE, attr_value);
        }

        attr_value = attr.getValue(CURRENCY_LOCALE_ATTRIBUTE);
        if (attr_value != null) {
            formattingParams.put(CURRENCY_LOCALE_ATTRIBUTE, attr_value);
        }

        attr_value = attr.getValue(I18N_SRC_LOCALE_ATTRIBUTE);
        if (attr_value != null) {
            formattingParams.put(I18N_SRC_LOCALE_ATTRIBUTE, attr_value);
        }

        attr_value = attr.getValue(I18N_TYPE_ATTRIBUTE);
        if (attr_value != null) {
            formattingParams.put(I18N_TYPE_ATTRIBUTE, attr_value);
        }

        attr_value = attr.getValue(I18N_FRACTION_DIGITS_ATTRIBUTE);
        if (attr_value != null) {
            formattingParams.put(I18N_FRACTION_DIGITS_ATTRIBUTE, attr_value);
        }
    }

    private void endI18NElement(String name) throws SAXException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("End i18n element: " + name);
        }

        switch (current_state) {
            case STATE_INSIDE_TEXT:
                endTextElement();
                break;

            case STATE_INSIDE_TRANSLATE:
                endTranslateElement();
                break;

            case STATE_INSIDE_CHOOSE:
                endChooseElement();
                break;

            case STATE_INSIDE_WHEN:
            case STATE_INSIDE_OTHERWISE:
                endWhenElement();
                break;

            case STATE_INSIDE_PARAM:
                endParamElement();
                break;

            case STATE_INSIDE_DATE:
            case STATE_INSIDE_DATE_TIME:
            case STATE_INSIDE_TIME:
                endDate_TimeElement();
                break;

            case STATE_INSIDE_NUMBER:
                endNumberElement();
                break;
        }
    }

    private void i18nCharacters(String textValue) throws SAXException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("i18n message text = '" + textValue + "'");
        }

        SaxBuffer buffer;
        switch (current_state) {
            case STATE_INSIDE_TEXT:
                buffer = text_recorder;
                break;

            case STATE_INSIDE_PARAM:
                buffer = param_recorder;
                break;

            case STATE_INSIDE_WHEN:
            case STATE_INSIDE_OTHERWISE:
                // Previously handeld to avoid the String() conversion.
                return;

            case STATE_INSIDE_TRANSLATE:
                if (tr_text_recorder == null) {
                    tr_text_recorder = new ParamSaxBuffer();
                }
                buffer = tr_text_recorder;
                break;

            case STATE_INSIDE_CHOOSE:
                // No characters allowed. Send an exception ?
                if (getLogger().isDebugEnabled()) {
                    textValue = textValue.trim();
                    if (textValue.length() > 0) {
                        getLogger().debug("No characters allowed inside <i18n:choose> tag. Received: " + textValue);
                    }
                }
                return;

            case STATE_INSIDE_DATE:
            case STATE_INSIDE_DATE_TIME:
            case STATE_INSIDE_TIME:
            case STATE_INSIDE_NUMBER:
                // Trim text values to avoid parsing errors.
                textValue = textValue.trim();
                if (textValue.length() > 0) {
                    if (formattingParams.get(I18N_VALUE_ATTRIBUTE) == null) {
                        formattingParams.put(I18N_VALUE_ATTRIBUTE, textValue);
                    } else {
                        // ignore the text inside of date element
                    }
                }
                return;

            default:
                throw new IllegalStateException(getClass().getName() +
                                                " developer's fault: characters not handled. " +
                                                "Current state: " + current_state);
        }

        char[] ch = textValue.toCharArray();
        buffer.characters(ch, 0, ch.length);
    }

    // Translate all attributes that are listed in i18n:attr attribute
    private Attributes translateAttributes(final String element, Attributes attr)
    throws SAXException {
        if (attr == null) {
            return null;
        }

        AttributesImpl tempAttr = null;

        // Translate all attributes from i18n:attr="name1 name2 ..."
        // using their values as keys.
        int attrIndex = attr.getIndex(I18N_NAMESPACE_URI, I18N_ATTR_ATTRIBUTE);
        if (attrIndex == -1) {
            // Try the old namespace
            attrIndex = attr.getIndex(I18N_OLD_NAMESPACE_URI, I18N_ATTR_ATTRIBUTE);
        }

        if (attrIndex != -1) {
            StringTokenizer st = new StringTokenizer(attr.getValue(attrIndex));

            // Make a copy which we are going to modify
            tempAttr = new AttributesImpl(attr);
            // Remove the i18n:attr attribute - we don't need it anymore
            tempAttr.removeAttribute(attrIndex);

            // Iterate through listed attributes and translate them
            while (st.hasMoreElements()) {
                final String name = st.nextToken();

                int index = tempAttr.getIndex(name);
                if (index == -1) {
                    getLogger().warn("Attribute " +
                                     name + " not found in element <" + element + ">");
                    continue;
                }

                String value = translateAttribute(element, name, tempAttr.getValue(index));
                if (value != null) {
                    // Set the translated value. If null, do nothing.
                    tempAttr.setValue(index, value);
                }
            }

            attr = tempAttr;
        }

        // Translate all attributes from i18n:expr="name1 name2 ..."
        // using their values as keys.
        attrIndex = attr.getIndex(I18N_NAMESPACE_URI, I18N_EXPR_ATTRIBUTE);
        if (attrIndex != -1) {
            StringTokenizer st = new StringTokenizer(attr.getValue(attrIndex));

            if (tempAttr == null) {
                tempAttr = new AttributesImpl(attr);
            }
            tempAttr.removeAttribute(attrIndex);

            // Iterate through listed attributes and evaluate them
            while (st.hasMoreElements()) {
                final String name = st.nextToken();

                int index = tempAttr.getIndex(name);
                if (index == -1) {
                    getLogger().warn("Attribute " +
                                     name + " not found in element <" + element + ">");
                    continue;
                }

                final StringBuffer translated = new StringBuffer();

                // Evaluate {..} expression
                VariableExpressionTokenizer.TokenReciever tr = new VariableExpressionTokenizer.TokenReciever () {
                    private String catalogueName;

                    public void addToken(int type, String value) {
                        if (type == MODULE) {
                            this.catalogueName = value;
                        } else if (type == VARIABLE) {
                            translated.append(translateAttribute(element, name, value));
                        } else if (type == TEXT) {
                            if (this.catalogueName != null) {
                                translated.append(translateAttribute(element,
                                                                     name,
                                                                     this.catalogueName + ":" + value));
                                this.catalogueName = null;
                            } else if (value != null) {
                                translated.append(value);
                            }
                        }
                    }
                };

                try {
                    VariableExpressionTokenizer.tokenize(tempAttr.getValue(index), tr);
                } catch (PatternException e) {
                    throw new SAXException(e);
                }

                // Set the translated value.
                tempAttr.setValue(index, translated.toString());
            }

            attr = tempAttr;
        }

        // nothing to translate, just return
        return attr;
    }

    /**
     * Translate attribute value.
     * Value can be prefixed with catalogue ID and semicolon.
     * @return Translated text, untranslated text, or null.
     */
    private String translateAttribute(String element, String name, String key) {
        // Check if the key contains a colon, if so the text before
        // the colon denotes a catalogue ID.
        int colonPos = key.indexOf(":");
        String catalogueID = null;
        if (colonPos != -1) {
            catalogueID = key.substring(0, colonPos);
            key = key.substring(colonPos + 1, key.length());
        }

        final SaxBuffer text = getMessage(catalogueID, key);
        if (text == null) {
            getLogger().warn("Translation not found for attribute " +
                             name + " in element <" + element + ">");
            return untranslated;
        }
        return text.toString();
    }

    private void endTextElement() throws SAXException {
        switch (prev_state) {
            case STATE_OUTSIDE:
                if (tr_text_recorder == null) {
                    if (currentKey == null) {
                        // Use the text as key. Not recommended for large strings,
                        // especially if they include markup.
                        tr_text_recorder = getMessage(text_recorder.toString(), text_recorder);
                    } else {
                        // We have the key, but couldn't find a translation
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("Translation not found for key '" + currentKey + "'");
                        }

                        // Use the untranslated-text only when the content of the i18n:text
                        // element was empty
                        if (text_recorder.isEmpty() && untranslatedRecorder != null) {
                            tr_text_recorder = untranslatedRecorder;
                        } else {
                            tr_text_recorder = text_recorder;
                        }
                    }
                }

                if (tr_text_recorder != null) {
                    tr_text_recorder.toSAX(this.contentHandler);
                }

                text_recorder.recycle();
                tr_text_recorder = null;
                currentKey = null;
                currentCatalogueId = null;
                break;

            case STATE_INSIDE_TRANSLATE:
                if (tr_text_recorder == null) {
                    if (!text_recorder.isEmpty()) {
                        tr_text_recorder = getMessage(text_recorder.toString(), text_recorder);
                        if (tr_text_recorder == text_recorder) {
                            // If the default value was returned, make a copy
                            tr_text_recorder = new ParamSaxBuffer(text_recorder);
                        }
                    }
                }

                text_recorder.recycle();
                break;

            case STATE_INSIDE_PARAM:
                // We send the translated text to the param recorder, after trying to translate it.
                // Remember you can't give a key when inside a param, that'll be nonsense!
                // No need to clone. We just send the events.
                if (!text_recorder.isEmpty()) {
                    getMessage(text_recorder.toString(), text_recorder).toSAX(param_recorder);
                    text_recorder.recycle();
                }
                break;
        }

        current_state = prev_state;
        prev_state = STATE_OUTSIDE;
    }

    // Process substitution parameter
    private void endParamElement() throws SAXException {
        String paramType = (String)formattingParams.get(I18N_TYPE_ATTRIBUTE);
        if (paramType != null) {
            // We have a typed parameter

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Param type: " + paramType);
            }
            if (formattingParams.get(I18N_VALUE_ATTRIBUTE) == null && param_value != null) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Put param value: " + param_value);
                }
                formattingParams.put(I18N_VALUE_ATTRIBUTE, param_value);
            }

            // Check if we have a date or a number parameter
            if (dateTypes.contains(paramType)) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Formatting date_time param: " + formattingParams);
                }
                param_value = formatDate_Time(formattingParams);
            } else if (numberTypes.contains(paramType)) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Formatting number param: " + formattingParams);
                }
                param_value = formatNumber(formattingParams);
            }
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Added substitution param: " + param_value);
            }
        }

        param_value = null;
        current_state = STATE_INSIDE_TRANSLATE;

        if(param_recorder == null) {
            return;
        }

        indexedParams.put(param_name, param_recorder);
        param_recorder = null;
    }

    private void endTranslateElement() throws SAXException {
        if (tr_text_recorder != null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("End of translate with params. " +
                                  "Fragment for substitution : " + tr_text_recorder);
            }
            tr_text_recorder.toSAX(super.contentHandler, indexedParams);
            tr_text_recorder = null;
            text_recorder.recycle();
        }

        indexedParams.clear();
        param_count = 0;
        current_state = STATE_OUTSIDE;
    }

    private void endChooseElement() {
        current_state = STATE_OUTSIDE;
    }

    private void endWhenElement() {
        current_state = prev_state;
        if (translate_copy) {
            translate_copy = false;
            translate_end = true;
        }
    }

    private void endDate_TimeElement() throws SAXException {
        String result = formatDate_Time(formattingParams);
        switch(prev_state) {
            case STATE_OUTSIDE:
                super.contentHandler.characters(result.toCharArray(), 0,
                                                result.length());
                break;
            case STATE_INSIDE_PARAM:
                param_recorder.characters(result.toCharArray(), 0, result.length());
                break;
            case STATE_INSIDE_TEXT:
                text_recorder.characters(result.toCharArray(), 0, result.length());
                break;
        }
        current_state = prev_state;
    }

    // Helper method: creates Locale object from a string value in a map
    private Locale getLocale(Map params, String attribute) {
        // the specific locale value
        String lc = (String)params.get(attribute);
        return I18nUtils.parseLocale(lc, this.locale);
    }

    private String formatDate_Time(Map params) throws SAXException {
        // Check that we have not null params
        if (params == null) {
            throw new IllegalArgumentException("Nothing to format");
        }

        // Formatters
        SimpleDateFormat to_fmt;
        SimpleDateFormat from_fmt;

        // Date formatting styles
        int srcStyle = DateFormat.DEFAULT;
        int style = DateFormat.DEFAULT;

        // Date formatting patterns
        boolean realPattern = false;
        boolean realSrcPattern = false;

        // From locale
        Locale srcLoc = getLocale(params, I18N_SRC_LOCALE_ATTRIBUTE);
        // To locale
        Locale loc = getLocale(params, I18N_LOCALE_ATTRIBUTE);

        // From pattern
        String srcPattern = (String)params.get(I18N_SRC_PATTERN_ATTRIBUTE);
        // To pattern
        String pattern = (String)params.get(I18N_PATTERN_ATTRIBUTE);
        // The date value
        String value = (String)params.get(I18N_VALUE_ATTRIBUTE);

        // A src-pattern attribute is present
        if (srcPattern != null) {
            // Check if we have a real pattern
            Integer patternValue = (Integer)datePatterns.get(srcPattern.toUpperCase());
            if (patternValue != null) {
                srcStyle = patternValue.intValue();
            } else {
                realSrcPattern = true;
            }
        }

        // A pattern attribute is present
        if (pattern != null) {
            Integer patternValue = (Integer)datePatterns.get(pattern.toUpperCase());
            if (patternValue != null) {
                style = patternValue.intValue();
            } else {
                realPattern = true;
            }
        }

        // If we are inside of a typed param
        String paramType = (String)formattingParams.get(I18N_TYPE_ATTRIBUTE);

        // Initializing date formatters
        if (current_state == STATE_INSIDE_DATE ||
                I18N_DATE_ELEMENT.equals(paramType)) {

            to_fmt = (SimpleDateFormat)DateFormat.getDateInstance(style, loc);
            from_fmt = (SimpleDateFormat)DateFormat.getDateInstance(
                    srcStyle,
                    srcLoc
            );
        } else if (current_state == STATE_INSIDE_DATE_TIME ||
                I18N_DATE_TIME_ELEMENT.equals(paramType)) {
            to_fmt = (SimpleDateFormat)DateFormat.getDateTimeInstance(
                    style,
                    style,
                    loc
            );
            from_fmt = (SimpleDateFormat)DateFormat.getDateTimeInstance(
                    srcStyle,
                    srcStyle,
                    srcLoc
            );
        } else {
            // STATE_INSIDE_TIME or param type='time'
            to_fmt = (SimpleDateFormat)DateFormat.getTimeInstance(style, loc);
            from_fmt = (SimpleDateFormat)DateFormat.getTimeInstance(
                    srcStyle,
                    srcLoc
            );
        }

        // parsed date object
        Date dateValue;

        // pattern overwrites locale format
        if (realSrcPattern) {
            from_fmt.applyPattern(srcPattern);
        }

        if (realPattern) {
            to_fmt.applyPattern(pattern);
        }

        // get current date and time by default
        if (value == null) {
            dateValue = new Date();
        } else {
            try {
                dateValue = from_fmt.parse(value);
            } catch (ParseException pe) {
                throw new SAXException(
                        this.getClass().getName()
                        + "i18n:date - parsing error.", pe
                );
            }
        }

        // we have all necessary data here: do formatting.
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("### Formatting date: " + dateValue + " with localized pattern " +
                              to_fmt.toLocalizedPattern() + " for locale: " + locale);
        }
        return to_fmt.format(dateValue);
    }

    private void endNumberElement() throws SAXException {
        String result = formatNumber(formattingParams);
        switch(prev_state) {
            case STATE_OUTSIDE:
                super.contentHandler.characters(result.toCharArray(), 0, result.length());
                break;
            case STATE_INSIDE_PARAM:
                param_recorder.characters(result.toCharArray(), 0, result.length());
                break;
            case STATE_INSIDE_TEXT:
                text_recorder.characters(result.toCharArray(), 0, result.length());
                break;
        }
        current_state = prev_state;
    }

    private String formatNumber(Map params) throws SAXException {
        if (params == null) {
            throw new SAXException(
                    this.getClass().getName()
                    + ": i18n:number - error in element attributes."
            );
        }

        // from pattern
        String srcPattern = (String)params.get(I18N_SRC_PATTERN_ATTRIBUTE);
        // to pattern
        String pattern = (String)params.get(I18N_PATTERN_ATTRIBUTE);
        // the number value
        String value = (String)params.get(I18N_VALUE_ATTRIBUTE);

        if (value == null) return "";
        // type
        String type = (String)params.get(I18N_TYPE_ATTRIBUTE);

        // fraction-digits
        int fractionDigits = -1;
        try {
            String fd = (String)params.get(I18N_FRACTION_DIGITS_ATTRIBUTE);
            if (fd != null)
                fractionDigits = Integer.parseInt(fd);
        } catch (NumberFormatException nfe) {
            getLogger().warn("Error in number format with fraction-digits", nfe);
        }

        // parsed number
        Number numberValue;

        // locale, may be switched locale
        Locale loc = getLocale(params, I18N_LOCALE_ATTRIBUTE);
        Locale srcLoc = getLocale(params, I18N_SRC_LOCALE_ATTRIBUTE);
        // currency locale
        Locale currencyLoc = getLocale(params, CURRENCY_LOCALE_ATTRIBUTE);
        // decimal and grouping locale
        Locale dgLoc = null;
        if (currencyLoc != null) {
            // the reasoning here is: if there is a currency locale, then start from that
            // one but take certain properties (like decimal and grouping seperation symbols)
            // from the default locale (this happens further on).
            dgLoc = loc;
            loc = currencyLoc;
        }

        // src format
        DecimalFormat from_fmt = (DecimalFormat)NumberFormat.getInstance(srcLoc);
        int int_currency = 0;

        // src-pattern overwrites locale format
        if (srcPattern != null) {
            from_fmt.applyPattern(srcPattern);
        }

        // to format
        DecimalFormat to_fmt;
        char dec = from_fmt.getDecimalFormatSymbols().getDecimalSeparator();
        int decAt = 0;
        boolean appendDec = false;

        if (type == null || type.equals( I18N_NUMBER_ELEMENT )) {
            to_fmt = (DecimalFormat)NumberFormat.getInstance(loc);
            to_fmt.setMaximumFractionDigits(309);
            for (int i = value.length() - 1;
                 i >= 0 && value.charAt(i) != dec; i--, decAt++) {
            }

            if (decAt < value.length())to_fmt.setMinimumFractionDigits(decAt);
            decAt = 0;
            for (int i = 0; i < value.length() && value.charAt(i) != dec; i++) {
                if (Character.isDigit(value.charAt(i))) {
                    decAt++;
                }
            }

            to_fmt.setMinimumIntegerDigits(decAt);
            if (value.charAt(value.length() - 1) == dec) {
                appendDec = true;
            }
        } else if (type.equals( I18N_CURRENCY_ELEMENT )) {
            to_fmt = (DecimalFormat)NumberFormat.getCurrencyInstance(loc);
        } else if (type.equals( I18N_INT_CURRENCY_ELEMENT )) {
            to_fmt = (DecimalFormat)NumberFormat.getCurrencyInstance(loc);
            int_currency = 1;
            for (int i = 0; i < to_fmt.getMaximumFractionDigits(); i++) {
                int_currency *= 10;
            }
        } else if ( type.equals( I18N_CURRENCY_NO_UNIT_ELEMENT ) ) {
            DecimalFormat tmp = (DecimalFormat) NumberFormat.getCurrencyInstance( loc );
            to_fmt = (DecimalFormat) NumberFormat.getInstance( loc );
            to_fmt.setMinimumFractionDigits(tmp.getMinimumFractionDigits());
            to_fmt.setMaximumFractionDigits(tmp.getMaximumFractionDigits());
        } else if ( type.equals( I18N_INT_CURRENCY_NO_UNIT_ELEMENT ) ) {
            DecimalFormat tmp = (DecimalFormat) NumberFormat.getCurrencyInstance( loc );
            int_currency = 1;
            for ( int i = 0; i < tmp.getMaximumFractionDigits(); i++ )
                int_currency *= 10;
            to_fmt = (DecimalFormat) NumberFormat.getInstance( loc );
            to_fmt.setMinimumFractionDigits(tmp.getMinimumFractionDigits());
            to_fmt.setMaximumFractionDigits(tmp.getMaximumFractionDigits());
        } else if (type.equals( I18N_PERCENT_ELEMENT )) {
            to_fmt = (DecimalFormat)NumberFormat.getPercentInstance(loc);
        } else {
            throw new SAXException("&lt;i18n:number>: unknown type: " + type);
        }

        if(fractionDigits > -1) {
            to_fmt.setMinimumFractionDigits(fractionDigits);
            to_fmt.setMaximumFractionDigits(fractionDigits);
        }

        if(dgLoc != null) {
            DecimalFormat df = (DecimalFormat)NumberFormat.getCurrencyInstance(dgLoc);
            DecimalFormatSymbols dfsNew = df.getDecimalFormatSymbols();
            DecimalFormatSymbols dfsOrig = to_fmt.getDecimalFormatSymbols();
            dfsOrig.setDecimalSeparator(dfsNew.getDecimalSeparator());
            dfsOrig.setMonetaryDecimalSeparator(dfsNew.getMonetaryDecimalSeparator());
            dfsOrig.setGroupingSeparator(dfsNew.getGroupingSeparator());
            to_fmt.setDecimalFormatSymbols(dfsOrig);
        }

        // pattern overwrites locale format
        if (pattern != null) {
            to_fmt.applyPattern(pattern);
        }

        try {
            numberValue = from_fmt.parse(value);
            if (int_currency > 0) {
                numberValue = new Double(numberValue.doubleValue() / int_currency);
            } else {
                // what?
            }
        } catch (ParseException pe) {
            throw new SAXException(this.getClass().getName() + "i18n:number - parsing error.", pe);
        }

        // we have all necessary data here: do formatting.
        String result = to_fmt.format(numberValue);
        if (appendDec) result = result + dec;
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("i18n:number result: " + result);
        }
        return result;
    }

    //-- Dictionary handling routines

    /**
     * Helper method to retrieve a message from the dictionary.
     *
     * @param catalogueID if not null, this catalogue will be used instead of the default one.
     * @return SaxBuffer containing message, or null if not found.
     */
    protected ParamSaxBuffer getMessage(String catalogueID, String key) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Getting key " + key + " from catalogue " + catalogueID);
        }

        CatalogueInfo catalogue = this.catalogue;
        if (catalogueID != null) {
            catalogue = (CatalogueInfo)catalogues.get(catalogueID);
            if (catalogue == null) {
                if (getLogger().isWarnEnabled()) {
                    getLogger().warn("Catalogue not found: " + catalogueID +
                                     ", will not translate key " + key);
                }
                return null;
            }
        }

        Bundle bundle = catalogue.getCatalogue();
        if (bundle == null) {
            // Can't translate
            getLogger().debug("Untranslated key: '" + key + "'");
            return null;
        }

        try {
            return (ParamSaxBuffer) bundle.getObject(key);
        } catch (MissingResourceException e)  {
            getLogger().debug("Untranslated key: '" + key + "'");
        }

        return null;
    }

    /**
     * Helper method to retrieve a message from the current dictionary.
     * A default value is returned if message is not found.
     *
     * @return SaxBuffer containing message, or defaultValue if not found.
     */
    private ParamSaxBuffer getMessage(String key, ParamSaxBuffer defaultValue) {
        SaxBuffer value = getMessage(currentCatalogueId, key);
        if (value == null) {
        	getLogger().debug("Untranslated key: '" + key + "'");
            return defaultValue;
        }

        return new ParamSaxBuffer(value);
    }

    public void recycle() {
        this.untranslatedRecorder = null;
        this.catalogue = null;
        this.objectModel = null;

        // Release catalogues which were selected for current locale
        Iterator i = catalogues.values().iterator();
        while (i.hasNext()) {
            CatalogueInfo catalogueInfo = (CatalogueInfo) i.next();
            catalogueInfo.releaseCatalog();
        }

        super.recycle();
    }

    public void dispose() {
        if (manager != null) {
            manager.release(factory);
        }
        factory = null;
        manager = null;
        catalogues = null;
    }


    /**
     * Holds information about one catalogue. The location and name of the catalogue
     * can contain references to input modules, and are resolved upon each transformer
     * usage. It is important that releaseCatalog is called when the transformer is recycled.
     */
    public final class CatalogueInfo {
        VariableResolver name;
        VariableResolver[] locations;
        String resolvedName;
        String[] resolvedLocations;
        Bundle catalogue;

        public CatalogueInfo(String name, String[] locations) throws PatternException {
            this.name = VariableResolverFactory.getResolver(name, manager);
            this.locations = new VariableResolver[locations.length];
            for (int i=0; i < locations.length; ++i) {
                this.locations[i] = VariableResolverFactory.getResolver(locations[i], manager);
            }
        }

        public String getName() {
            try {
                if (resolvedName == null) {
                    resolve();
                }
            } catch (Exception e) {
                // Ignore the error for now
            }
            return resolvedName;
        }

        public String[] getLocation() {
            try {
                if (resolvedName == null) {
                    resolve();
                }
            } catch (Exception e) {
                // Ignore the error for now
            }
            return resolvedLocations;
        }

        private void resolve() throws Exception {
            if (resolvedLocations == null) {
                resolvedLocations = new String[locations.length];
                for (int i=0; i < resolvedLocations.length; ++i) {
                    resolvedLocations[i] = locations[i].resolve(null, objectModel);
                }
            }
            if (resolvedName == null) {
                resolvedName = name.resolve(null, objectModel);
            }
        }

        public Bundle getCatalogue() {
            if (catalogue == null) {
                try {
                    resolve();
                    catalogue = factory.select(resolvedLocations, resolvedName, locale);
                } catch (Exception e) {
                    getLogger().error("Error obtaining catalogue '" + getName() +
                                      "' from  <" + getLocation() + "> for locale " +
                                      locale, e);
                }
            }

            return catalogue;
        }

        public void releaseCatalog() {
            if (catalogue != null) {
                factory.release(catalogue);
            }
            catalogue = null;
            resolvedName = null;
            resolvedLocations = null;
        }
    }

}
