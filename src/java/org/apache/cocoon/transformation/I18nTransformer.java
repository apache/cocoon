/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.transformation;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
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

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.treeprocessor.variables.PreparedVariableResolver;
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

/**
 * Internationalization transformer is used to transform i18n markup into text
 * based on a particular locale.
 *
 * <h3>i18n transformer</h3>
 * <p>The <strong>i18n transformer</strong> works by obtaining the users locale
 * based on request, session attributes or a cookie data. See
 * {@link org.apache.cocoon.acting.LocaleAction#getLocaleAttribute(Map, String) } for details.
 * It then attempts to find a <strong>message catalogue</strong> that satisifies
 * the particular locale, and use it for for text replacement within i18n markup.
 * 
 * <p>Catalogues are maintained in separate files, with a naming convention
 * similar to that of ResourceBundle (See java.util.ResourceBundle).
 * ie.
 * <strong>basename</strong>_<strong>locale</strong>, where <i>basename</i>
 * can be any name, and <i>locale</i> can be any locale specified using
 * ISO 639/3166 characters (eg. en_AU, de_AT, es).<br/>
 * <strong>NOTE: </strong>ISO 639 is not a stable standard; some of the language
 * codes it defines (specifically iw, ji, and in) have changed
 * (see java.util.Locale for details).
 *
 * <h3>Catalogues</h3>
 * <p>Catalogues are of the following format:
 * <pre>
 * &lt;?xml version="1.0"?&gt;
 * &lt;!-- message catalogue file for locale ... --&gt;
 * &lt;catalogue xml:lang=&quot;locale&quot;&gt;
 *        &lt;message key="key"&gt;text&lt;/message&gt;
 *        ....
 * &lt;/catalogue&gt;
 * </pre> Where <strong>key</strong> specifies a particular message for that
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
 * the lookup key.
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
 * at each defining messages with increasing depth of variation.
 *
 * <p>The i18n:text element can optionally take an attribute <strong>i18n:catalogue</strong>
 * to specify a specific catalogue to use. The value of this attribute should be
 * the id of the catalogue to use (see sitemap configuration).
 *
 * <h3>Sitemap configuration</h3>
 * <pre>
 * &lt;map:transformer name="i18n"
 *     src="org.apache.cocoon.transformation.I18nTransformer"&gt;
 *
 *     &lt;catalogues default="someId"&gt;
 *       &lt;catalogue id="someId" name="messages" location="translations"&gt;
 *     &lt;/catalogues&gt;
 *     &lt;untranslated-text&gt;untranslated&lt;/untranslated-text&gt;
 *     &lt;cache-at-startup&gt;true&lt;/cache-at-startup&gt;
 * &lt;/map:transformer&gt;
 * </pre> where:
 * <ul>
 *  <li><strong>catalogues</strong>: container element in which the catalogues
 *      are defined. It must have an attribute 'default' whose value is one
 *      of the id's of the catalogue elements. (<i>mandatory</i>).
 *  <li><strong>catalogue</strong>: specifies a catalogue. It takes 3 required
 *      attributes: id (can be wathever you like), name (base name of the catalogue)
 *      and location (location of the message catalogue). The name and location attributes
 *      can contain references to inputmodules (same syntax as in other places in the
 *      sitemap). They are resolved on each usage of the transformer, so they can
 *      refer to e.g. request parameters. (<i>at least 1 catalogue
 *      element required</i>).
 *  <li><strong>untranslated-text</strong>: text used for
 *      untranslated keys (default is to output the key name).
 *  <li><strong>cache-at-startup</strong>: flag whether to cache
 *      messages at startup (false by default).
 * </ul>
 * 
 * <p><strong>NOTE:</strong> before using multiple catalogues was supported,
 * the catalogue name and location was specified using elements named
 * <code>catalogue-name</code> and <code>catalogue-location</code>. This syntax is
 * <strong>NOT</strong> supported anymore.
 *
 * <p>To use the transformer in a pipeline, simply specify it in a particular
 * transform. eg:
 * <pre>
 * &lt;map:match pattern="file"&gt;
 *     &lt;map:generate src="file.xml"/&gt;
 *     &lt;map:transform type="i18n"/&gt;
 *     &lt;map:serialize/&gt;
 * &lt;/map:match&gt;
 * </pre>
 *
 * <p>If in certain pipeline, you want to use a different catalogue as the
 * default catalogue, you can do so by specifying a parameter called
 * <strong>default-catalogue-id</strong>.
 *
 * <p>The <strong>untranslated-text</strong> can also be overridden at the
 * pipeline level by specifying it as a parameter.
 *
 * <p>Note: before multiple catalogues were supported, the catalogue to use
 * could be overridden at the pipeline level by specifying parameters called
 * <strong>catalogue-name</strong>, <strong>catalogue-location</strong>. This
 * is still supported, but can't be used together with the new parameter default-catalogue-id.
 *
 * <p>For date, time and number formatting use the following tags:
 * <ul>
 *  <li><strong>&lt;i18n:date/&gt;</strong> gives localized date.</li>
 *  <li><strong>&lt;i18n:date-time/&gt;</strong> gives localized date and time.</li>
 *  <li><strong>&lt;i18n:time/&gt;</strong> gives localized time.</li>
 * </ul>
 * For <code>date</code>, <code>date-time</code> and <code>time</code> the
 * <code>pattern</code> and <code>src-pattern</code> attribute may have also
 * values of: <code>short</code>, <code>medium</code>, <code>long</code> or
 * <code>full</code>. (See java.text.DateFormat for more info on this).
 * <p>For <code>date</code>, <code>date-time</code>, <code>time</code> and
 * <code>number</code> a different <code>locale</code> and
 * <code>source-locale</code> can be specified:
 * <pre>
 * &lt;i18n:date src-pattern="short" src-locale="en_US" locale="de_DE"&gt;
 *      12/24/01
 * &lt;/i18n:date&gt;
 * </pre> will result in 24.12.2001.
 *
 * <p>A given real <code>pattern</code> and <code>src-pattern</code> (not
 * <code>short, medium, long, full</code>) overwrites the
 * <code>locale</code> and <code>src-locale</code>
 *
 * <p>Future work coming:
 * <ul>
 *  <li>Introduce new &lt;get-locale /&gt; element
 *  <li>Move all formatting routines to I18nUtils
 * </ul>
 *
 * @author <a href="mailto:kpiroumian@apache.org">Konstantin Piroumian</a>
 * @author <a href="mailto:mattam@netcourrier.com">Matthieu Sozeau</a>
 * @author <a href="mailto:crafterm@apache.org">Marcus Crafter</a>
 * @author <a href="mailto:Michael.Enke@wincor-nixdorf.com">Michael Enke</a>
 * @version CVS $Id: I18nTransformer.java,v 1.19 2003/12/12 05:39:38 antonio Exp $
 */
public class I18nTransformer extends AbstractTransformer
        implements CacheableProcessingComponent,
                   Composable, Configurable, Disposable {

    /**
     * The namespace for i18n is "http://apache.org/cocoon/i18n/2.1".
     */
    public static final String I18N_NAMESPACE_URI =
            "http://apache.org/cocoon/i18n/2.1";

    /**
     * The old namespace for i18n is "http://apache.org/cocoon/i18n/2.0".
     */
    public static final String I18N_OLD_NAMESPACE_URI =
            "http://apache.org/cocoon/i18n/2.0";

    /**
     * Did we already encountered an old namespace? This is static to ensure that
     * the associated message will be logged only once.
     */
    private static boolean deprecationFound = false;

    //
    // i18n elements
    //

    /**
     * i18n:text element is used to translate any text, with or without markup,
     * e.g.:<br/>
     * <pre>
     *  &lt;i18n:text&gt;This is a &lt;strong&gt;multilanguage&lt;/strong&gt; string&lt;/i18n:text&gt;
     * </pre>
     */
    public static final String I18N_TEXT_ELEMENT            = "text";

    /**
     * i18n:translate element is used to translate text with parameter
     * substitution, e.g.:<br/>
     * <pre>
     * &lt;i18n:translate&gt;
     *     &lt;i:text&gt;This is a multilanguage string with {0} param&lt;/i:text&gt;
     *     &lt;i18n:param&gt;1&lt;/i18n:param&gt;
     * &lt;/i18n:translate&gt;
     * </pre>
     * The &lt;text&gt; fragment can include markup and parameters at any place.
     * Also do parameters, which can also include i18n:text, i18n:date, etc.
     * elements (whithout keys only).
     * <p>
     *
     * @see #I18N_TEXT_ELEMENT
     * @see #I18N_PARAM_ELEMENT
     */
    public static final String I18N_TRANSLATE_ELEMENT       = "translate";

    /**
     * <strong>i18n:choose<strong> element is used to translate elements in-place.
     * The first <strong>i18n:when<strong> element matching the current locale
     * is selected and the others are discarded.
     *
     * <p>To specify what to do if no locale matched, simply add a node with
     * <code>locale="*"</code>.
     * <em>Note that this element must be the last child of &lt;i18n:choose&gt;.</em>
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
     * <p>
     * You can include any markup in i18n:when nodes, minus i18n:*.
     * </p>
     *
     * @see #I18N_IF_ELEMENT
     * @see #I18N_LOCALE_ATTRIBUTE
     * @since 2.1
     */
    public static final String I18N_CHOOSE_ELEMENT          = "choose";

    /**
     * i18n:when is used to test a locale.
     * It can be used within &lt;i18:choose&gt; elements or alone.
     * <em>Note: Using <code>locale="*"</code> here has no sense.</em>
     * e.g.:
     * <pre>
     * &lt;greeting&gt;
     *   &lt;i18n:when locale="en"&gt;Hello&lt;/i18n:when&gt;
     *   &lt;i18n:when locale="fr"&gt;Bonjour&lt;/i18n:when&gt;
     * &lt;/greeting&gt;
     * </pre>
     * @see #I18N_LOCALE_ATTRIBUTE
     * @see #I18N_CHOOSE_ELEMENT
     * @since 2.1
     */
    public static final String I18N_WHEN_ELEMENT            = "when";

    /**
     * i18n:if is used to test a locale.
     * e.g.:
     * <pre>
     * &lt;greeting&gt;
     *   &lt;i18n:if locale="en"&gt;Hello&lt;/i18n:when&gt;
     *   &lt;i18n:if locale="fr"&gt;Bonjour&lt;/i18n:when&gt;
     * &lt;/greeting&gt;
     * </pre>
     * @see #I18N_LOCALE_ATTRIBUTE
     * @see #I18N_CHOOSE_ELEMENT
     * @see #I18N_WHEN_ELEMENT
     * @since 2.1
     */
    public static final String I18N_IF_ELEMENT            = "if";

    /**
     * i18n:otherwise is used to match any locale when no matching
     * locale has been found inside an i18n:choose block.
     *
     * @see #I18N_CHOOSE_ELEMENT
     * @see #I18N_WHEN_ELEMENT
     * @since 2.1
     */
    public static final String I18N_OTHERWISE_ELEMENT       = "otherwise";

    /**
     * i18n:param is used with i18n:translate to provide substitution params.
     * The param can have i18n:text as its value to provide multilungual value.
     * Parameters can have additional attributes to be used for formatting:
     * <ul>
     *      <li><code>type</code> - can be <code>date, date-time, time,
     *      number, currency, currency-no-unit or percent</code>.
     *      Used to format params before substitution.
     *      </li>
     *      <li><code>value</code> - the value of the param. If no value is
     *      specified then the text inside of the param element will be used.
     *      </li>
     *      <li><code>locale</code> - used only with <code>number, date, time,
     *      date-time</code> types and used to override the current locale to
     *      format the given value.
     *      </li>
     *      <li><code>src-locale</code> - used with <code>number, date, time,
     *      date-time</code> types and specify the locale that should be used to
     *      parse the given value.
     *      </li>
     *      <li><code>pattern</code> - used with <code>number, date, time,
     *      date-time</code> types and specify the pattern that should be used
     *      to format the given value.
     *      </li>
     *      <li><code>src-pattern</code> - used with <code>number, date, time,
     *      date-time</code> types and specify the pattern that should be used
     *      to parse the given value.
     *      </li>
     * </ul>
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
     * @since 2.1
     */
    public static final String I18N_PARAM_NAME_ATTRIBUTE    = "name";

    /**
     * i18n:date is used to provide a localized date string. Allowed attributes
     * are: <code>pattern, src-pattern, locale, src-locale</code>
     * Usage examples:
     * <pre>
     *  &lt;i18n:date src-pattern="short" src-locale="en_US" locale="de_DE"&gt;
     *      12/24/01
     *  &lt;/i18n:date&gt;
     *
     * &lt;i18n:date pattern="dd/MM/yyyy" /&gt;
     * </pre>
     *
     * If no value is specified then the current date will be used. E.g.:
     * <pre>
     * &lt;i18n:date /&gt;
     * </pre> displays the current date formatted with default pattern for
     * the current locale.
     *
     * @see #I18N_PARAM_ELEMENT
     * @see #I18N_DATE_TIME_ELEMENT
     * @see #I18N_TIME_ELEMENT
     * @see #I18N_NUMBER_ELEMENT
     */
    public static final String I18N_DATE_ELEMENT            = "date";

    /**
     * i18n:date-time is used to provide a localized date and time string.
     * Allowed attributes are: <code>pattern, src-pattern, locale,
     * src-locale</code>
     * Usage examples:
     * <pre>
     *  &lt;i18n:date-time src-pattern="short" src-locale="en_US" locale="de_DE"
     *  &gt;
     *      12/24/01 1:00 AM
     *  &lt;/i18n:date&gt;
     *
     *  &lt;i18n:date-time pattern="dd/MM/yyyy hh:mm" /&gt;
     * </pre>
     *
     * If no value is specified then the current date and time will be used.
     * E.g.:
     * <pre>
     * &lt;i18n:date-time /&gt;
     * </pre> displays the current date formatted with default pattern for
     * the current locale.
     *
     * @see #I18N_PARAM_ELEMENT
     * @see #I18N_DATE_ELEMENT
     * @see #I18N_TIME_ELEMENT
     * @see #I18N_NUMBER_ELEMENT
     */
    public static final String I18N_DATE_TIME_ELEMENT       = "date-time";

    /**
     * i18n:time is used to provide a localized time string. Allowed attributes
     * are: <code>pattern, src-pattern, locale, src-locale</code>
     * Usage examples:
     * <pre>
     *  &lt;i18n:time src-pattern="short" src-locale="en_US" locale="de_DE"&gt;
     *      1:00 AM
     *  &lt;/i18n:time&gt;
     *
     * &lt;i18n:time pattern="hh:mm:ss" /&gt;
     * </pre>
     *
     * If no value is specified then the current time will be used. E.g.:
     * <pre>
     * &lt;i18n:time /&gt;
     * </pre> displays the current time formatted with default pattern for
     * the current locale.
     *
     * @see #I18N_PARAM_ELEMENT
     * @see #I18N_DATE_TIME_ELEMENT
     * @see #I18N_DATE_ELEMENT
     * @see #I18N_NUMBER_ELEMENT
     */
    public static final String I18N_TIME_ELEMENT            = "time";

    /**
     * i18n:number is used to provide a localized number string. Allowed
     * attributes are: <code>pattern, src-pattern, locale, src-locale, type
     * </code>
     * Usage examples:
     * <pre>
     *  &lt;i18n:number src-pattern="short" src-locale="en_US" locale="de_DE"&gt;
     *      1000.0
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
     * attribute names that should be translated. E.g.
     * <pre>
     * &lt;para title="first" name="article" i18n:attr="title name" /&gt;
     * </pre>
     */
    public static final String I18N_ATTR_ATTRIBUTE          = "attr";

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
     * This configuration parameter specifies the message catalog name.
     */
    public static final String I18N_CATALOGUE_NAME      = "catalogue-name";

    /**
     * This configuration parameter specifies the message catalog location
     * relative to the current sitemap.
     */
    public static final String I18N_CATALOGUE_LOCATION  = "catalogue-location";

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
     * This configuration parameter specifies if the message catalog should be
     * cached at startup.
     */
    public static final String I18N_CACHE_STARTUP       = "cache-at-startup";

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


    /**
     * Component Manager
     */
    protected ComponentManager manager;

    /**
     * Source Resolver
     */
    private SourceResolver sourceResolver;

    //
    // i18n configuration variables
    //

    /**
     * Default catalogue id
     */
    private String defaultCatalogueId;

    /**
     * Default (global) untranslated message value
     */
    private String globalUntranslated;

    /**
     * Current (local) untranslated message value
     */
    private String untranslated;

    /**
     * SaxBuffer containing the contents of {@link #untranslated}.
     */
    private ParamSaxBuffer untranslatedRecorder;

    /**
     * Cache at startup configuration parameter value
     */
    private boolean cacheAtStartup;

    // Default catalogue
    private Bundle defaultCatalogue;

    // All catalogues (hashed on catalgue id). The values are instances of CatalogueInfo.
    private Map catalogues = new HashMap();

    // Dictionary loader factory
    protected BundleFactory factory;

    //
    // Current state of the transformer
    //
    
    protected Map objectModel;

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
     * Character data buffer. used to concat chunked character data
     */
    private StringBuffer strBuffer;

    /**
     * The i18n:key attribute is stored for the current element.
     * If no translation found for the key then the character data of element is
     * used as default value.
     */
    private String current_key;

    /**
     * Contains the id of the current catalogue if it was explicitely mentioned
     * on an i18n:text element, otherwise it is null.
     */
    private String currentCatalogueId;

    // A flag for copying the node when doing in-place translation
    private boolean translate_copy;

    // A flag for copying the _GOOD_ node and not others
    // when doing in-place translation whitin i18n:choose
    private boolean translate_end;

    // Translated text. Inside i:translate, collects character events.
    private ParamSaxBuffer tr_text_recorder;

    // Current "i18n:text" events
    private ParamSaxBuffer text_recorder;

    // Current parameter events
    private SaxBuffer param_recorder;

    // Param count when not using i:param name="..."
    private int param_count;

    // Param name attribute for substitution.
    private String param_name;

    // i18n:param's hashmap for substitution
    private HashMap indexedParams;

    // Current parameter value (translated or not)
    private String param_value;

    // Message formatter for param substitution.
    private MessageFormat formatter;

    // Current locale
    protected Locale locale;

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
        CatalogueInfo catalogueInfo = (CatalogueInfo)catalogues.get(defaultCatalogueId);
        StringBuffer key = new StringBuffer();
        if (catalogueInfo != null) {
            key.append(catalogueInfo.resolvedLocation);
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
     * Implementation of composable interface.
     * Looksup the Bundle Factory to be used.
     */
    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
        try {
            this.factory = (BundleFactory)manager.lookup(BundleFactory.ROLE);
        } catch (ComponentException e) {
            getLogger().debug("Failed to load BundleFactory", e);
            throw e;
        }
    }

    /**
     * Implemenation of configurable interface.
     * Configure this transformer.
     */
    public void configure(Configuration conf) throws ConfigurationException {
        // read in the config options from the transformer definition
        Configuration cataloguesConf = conf.getChild("catalogues", false);

        // new configuration style
        Configuration[] catalogueConfs = cataloguesConf.getChildren("catalogue");
        for (int i = 0; i < catalogueConfs.length; i++) {
            String id = catalogueConfs[i].getAttribute("id");
            String name = catalogueConfs[i].getAttribute("name");
            String location = catalogueConfs[i].getAttribute("location");
            CatalogueInfo newCatalogueInfo;
            try {
                newCatalogueInfo = new CatalogueInfo(name, location);
            } catch (PatternException e) {
                throw new ConfigurationException("I18nTransformer: error in name or location " +
                                                 "attribute on catalogue element with id " + id, e);
            }
            catalogues.put(id, newCatalogueInfo);
        }

        this.defaultCatalogueId = cataloguesConf.getAttribute("default");
        if (!catalogues.containsKey(this.defaultCatalogueId)) {
            throw new ConfigurationException("I18nTransformer: default catalogue id '" +
                                             this.defaultCatalogueId + "' denotes a nonexisting catalogue");
        }

        // obtain default text to use for untranslated messages
        globalUntranslated = conf.getChild(I18N_UNTRANSLATED).getValue(null);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Default untranslated text is '" + globalUntranslated + "'");
        }

        // obtain config option, whether to cache messages at startup time
        cacheAtStartup = conf.getChild(I18N_CACHE_STARTUP).getValueAsBoolean(false);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug((cacheAtStartup ? "will" : "won't") +
              " cache messages during startup, by default"
            );
        }
    }

    /**
     * Setup current instance of transformer.
     */
    public void setup(SourceResolver resolver, Map objectModel, String source,
                      Parameters parameters)
    throws ProcessingException, SAXException, IOException {

        this.sourceResolver = resolver;
        this.objectModel = objectModel;

        try {
            untranslated = parameters.getParameter(I18N_UNTRANSLATED, globalUntranslated);
            if (untranslated != null) {
                untranslatedRecorder = new ParamSaxBuffer();
                untranslatedRecorder.characters(untranslated.toCharArray(), 0, untranslated.length());
            }

            String lc = parameters.getParameter(I18N_LOCALE, null);
            String localDefaultCatalogueId = parameters.getParameter(I18N_DEFAULT_CATALOGUE_ID, null);

            // Get current locale
            Locale locale = I18nUtils.parseLocale(lc);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Using locale '" + locale.toString() + "'");
            }

            // Initialize instance state variables
            this.locale             = locale;
            this.current_state      = STATE_OUTSIDE;
            this.prev_state         = STATE_OUTSIDE;
            this.current_key        = null;
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

            // give the defaultCatalogue variable its value -- first look if it's locally overridden
            // and otherwise use the component-wide defaults.
            if (localDefaultCatalogueId != null) {
                CatalogueInfo catalogueInfo = (CatalogueInfo)catalogues.get(localDefaultCatalogueId);
                if (catalogueInfo == null) {
                    throw new ProcessingException("I18nTransformer: '" +
                                                  localDefaultCatalogueId +
                                                  "' is not an existing catalogue id.");
                }
                defaultCatalogue = catalogueInfo.getCatalogue();
            } else {
                defaultCatalogue = ((CatalogueInfo)catalogues.get(defaultCatalogueId)).getCatalogue();
            }
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Using default catalogue " + defaultCatalogue);
            }

            // Create and initialize a formatter
            this.formatter = new MessageFormat("");
            this.formatter.setLocale(locale);
        } catch (Exception e) {
            getLogger().debug("exception generated, leaving unconfigured");
            throw new ProcessingException(e.getMessage(), e);
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
        if (I18N_NAMESPACE_URI.equals(uri)) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Starting i18n element: " + name);
            }
            startI18NElement(name, attr);

        } else if (I18N_OLD_NAMESPACE_URI.equals(uri)) {
            if (!deprecationFound) {
                deprecationFound = true;
                getLogger().warn("The namespace '" +
                                 I18N_OLD_NAMESPACE_URI +
                                 "' for i18n is deprecated, use: '" +
                                 I18N_NAMESPACE_URI + "'");
            }
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Starting deprecated i18n element: " + name);
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

        if (I18N_NAMESPACE_URI.equals(uri) || I18N_OLD_NAMESPACE_URI.equals(uri)) {
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
                        this.getClass().getName()
                        + ": nested i18n:text elements are not allowed."
                        + " Current state: " + current_state
                );
            }

            prev_state = current_state;
            current_state = STATE_INSIDE_TEXT;

            current_key = attr.getValue("", I18N_KEY_ATTRIBUTE);
            if (current_key == null) {
                // Try the namespaced attribute
                current_key = attr.getValue(I18N_NAMESPACE_URI, I18N_KEY_ATTRIBUTE);
                if (current_key == null) {
                    // Try the old namespace
                    current_key = attr.getValue(I18N_OLD_NAMESPACE_URI, I18N_KEY_ATTRIBUTE);
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

            if (current_key != null) {
                tr_text_recorder = getMessage(current_key, (ParamSaxBuffer)null);
            }

        } else if (I18N_TRANSLATE_ELEMENT.equals(name)) {
            if (current_state != STATE_OUTSIDE) {
                throw new SAXException(
                        this.getClass().getName()
                        + ": i18n:translate element must be used "
                        + "outside of other i18n elements. Current state: "
                        + current_state
                );
            }

            prev_state = current_state;
            current_state = STATE_INSIDE_TRANSLATE;
        } else if (I18N_PARAM_ELEMENT.equals(name)) {
            if (current_state != STATE_INSIDE_TRANSLATE) {
                throw new SAXException(
                        this.getClass().getName()
                        + ": i18n:param element can be used only inside "
                        + "i18n:translate element. Current state: "
                        + current_state
                );
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
                        this.getClass().getName()
                        + ": i18n:choose elements cannot be used"
                        + "inside of other i18n elements."
                );
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
                        this.getClass().getName()
                        + ": i18n:when elements are can be used only"
                        + "inside of i18n:choose elements."
                );
            }

            if (I18N_IF_ELEMENT.equals(name) &&
                    current_state != STATE_OUTSIDE) {
                throw new SAXException(
                        this.getClass().getName()
                        + ": i18n:if elements cannot be nested."
                );
            }

            String locale = attr.getValue(I18N_LOCALE_ATTRIBUTE);
            if (locale == null)
                throw new SAXException(
                        this.getClass().getName()
                        + ": i18n:" + name
                        + " element cannot be used without 'locale' attribute."
                );

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
                        this.getClass().getName()
                        + ": i18n:otherwise elements are not allowed "
                        + "only inside i18n:choose."
                );
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
                        this.getClass().getName()
                        + ": i18n:date elements are not allowed "
                        + "inside of other i18n elements."
                );
            }

            setFormattingParams(attr);
            prev_state = current_state;
            current_state = STATE_INSIDE_DATE;
        } else if (I18N_DATE_TIME_ELEMENT.equals(name)) {
            if (current_state != STATE_OUTSIDE
                    && current_state != STATE_INSIDE_TEXT
                    && current_state != STATE_INSIDE_PARAM) {
                throw new SAXException(
                        this.getClass().getName()
                        + ": i18n:date-time elements are not allowed "
                        + "inside of other i18n elements."
                );
            }

            setFormattingParams(attr);
            prev_state = current_state;
            current_state = STATE_INSIDE_DATE_TIME;
        } else if (I18N_TIME_ELEMENT.equals(name)) {
            if (current_state != STATE_OUTSIDE
                    && current_state != STATE_INSIDE_TEXT
                    && current_state != STATE_INSIDE_PARAM) {
                throw new SAXException(
                        this.getClass().getName()
                        + ": i18n:date elements are not allowed "
                        + "inside of other i18n elements."
                );
            }

            setFormattingParams(attr);
            prev_state = current_state;
            current_state = STATE_INSIDE_TIME;
        } else if (I18N_NUMBER_ELEMENT.equals(name)) {
            if (current_state != STATE_OUTSIDE
                    && current_state != STATE_INSIDE_TEXT
                    && current_state != STATE_INSIDE_PARAM) {
                throw new SAXException(
                        this.getClass().getName()
                        + ": i18n:number elements are not allowed "
                        + "inside of other i18n elements."
                );
            }

            setFormattingParams(attr);
            prev_state = current_state;
            current_state = STATE_INSIDE_NUMBER;
        }
    }

    // Get all possible i18n formatting attribute values and store in a Map
    private void setFormattingParams(Attributes attr) {
        formattingParams = new HashMap(3);  // average number of attributes is 3

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
        // Trim text values to avoid parsing errors.
        textValue = textValue.trim();
        if (textValue.length() == 0) {
            return;
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug( "i18n message text = '" + textValue + "'" );
        }

        char[] ch = textValue.toCharArray();
        switch (current_state) {
            case STATE_INSIDE_TEXT:
                text_recorder.characters(ch, 0, ch.length);
                break;

            case STATE_INSIDE_PARAM:
                param_recorder.characters(ch, 0, ch.length);
                break;

            case STATE_INSIDE_WHEN:
            case STATE_INSIDE_OTHERWISE:
                // Previously handeld to avoid the String() conversion.
                break;

            case STATE_INSIDE_TRANSLATE:
                if(tr_text_recorder == null) {
                    tr_text_recorder = new ParamSaxBuffer();
                }
                tr_text_recorder.characters(ch, 0, ch.length);
                break;

            case STATE_INSIDE_CHOOSE:
                // No characters allowed. Send an exception ?
                getLogger().debug("No characters allowed inside <i18n:choose> tags");
                break;

            case STATE_INSIDE_DATE:
            case STATE_INSIDE_DATE_TIME:
            case STATE_INSIDE_TIME:
            case STATE_INSIDE_NUMBER:
                if (formattingParams.get(I18N_VALUE_ATTRIBUTE) == null) {
                    formattingParams.put(I18N_VALUE_ATTRIBUTE, textValue);
                } else {
                    // ignore the text inside of date element
                }
                break;

            default:
                throw new IllegalStateException(getClass().getName() +
                                                " developer's fault: characters not handled. " +
                                                "Current state: " + current_state);
        }
    }

    // Translate all attributes that are listed in i18n:attr attribute
    private Attributes translateAttributes(String name, Attributes attr) {
        if (attr == null) {
            return attr;
        }

        AttributesImpl temp_attr = new AttributesImpl(attr);

        // Translate all attributes from i18n:attr="name1 name2 ..."
        // using their values as keys
        int i18n_attr_index = temp_attr.getIndex(I18N_NAMESPACE_URI,I18N_ATTR_ATTRIBUTE);
        if (i18n_attr_index == -1) {
            // Try the old namespace
            i18n_attr_index = temp_attr.getIndex(I18N_OLD_NAMESPACE_URI,I18N_ATTR_ATTRIBUTE);
        }

        if (i18n_attr_index != -1) {
            StringTokenizer st =
                    new StringTokenizer(temp_attr.getValue(i18n_attr_index));
            // remove the i18n:attr attribute - we don't need it anymore
            temp_attr.removeAttribute(i18n_attr_index);

            // iterate through listed attributes and translate them
            while (st.hasMoreElements()) {
                String attr_name = st.nextToken();

                int attr_index = temp_attr.getIndex(attr_name);
                if (attr_index != -1) {
                    String text2translate = temp_attr.getValue(attr_index);
                    // check if the text2translate contains a colon, if so the text before
                    // the colon denotes a catalogue id
                    int colonPos = text2translate.indexOf(":");
                    String catalogueID = null;
                    if (colonPos != -1) {
                        catalogueID = text2translate.substring(0, colonPos);
                        text2translate = text2translate.substring(colonPos + 1, text2translate.length());
                    }
                    String result = getString(catalogueID, text2translate,
                                              untranslated == null? text2translate : untranslated);

                    // set the translated value
                    if (result != null) {
                        temp_attr.setValue(attr_index, result);
                    } else {
                        getLogger().warn("translation not found for attribute "
                                         + attr_name + " in element: " + name);
                    }
                } else {
                    getLogger().warn("i18n attribute '" + attr_name
                                     + "' not found in element: " + name);
                }
            }

            return temp_attr;
        }

        // nothing to translate, just return
        return attr;
    }

    private void endTextElement() throws SAXException {
        switch (prev_state) {
            case STATE_OUTSIDE:
                if (tr_text_recorder == null) {
                    if (current_key == null) {
                        // Use the text as key. Not recommended for large strings,
                        // especially if they include markup.
                        tr_text_recorder = getMessage(text_recorder.toString(), text_recorder);
                    } else {
                        // We have the key, but couldn't find a translation
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("Translation not found for key '" + current_key + "'");
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
                current_key = null;
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
        SimpleDateFormat to_fmt = null;
        SimpleDateFormat from_fmt = null;

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
        Date dateValue = null;

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
        Number numberValue = null;

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
        DecimalFormat to_fmt = null;
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

        if (value == null) {
            numberValue = new Long(0);
        } else {
            try {
                numberValue = from_fmt.parse(value);
                if (int_currency > 0) {
                    numberValue = new Double(numberValue.doubleValue() /
                                             int_currency);
                } else {
                    // what?
                }
            } catch (ParseException pe) {
                throw new SAXException(
                        this.getClass().getName()
                        + "i18n:number - parsing error.", pe
                );
            }
        }

        // we have all necessary data here: do formatting.
        String result = to_fmt.format(numberValue);
        if (appendDec) result = result + dec;
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("i18n:number result: " + result);
        }
        return result;
    }

    //-- Dictionary handling routins

    /**
     * Helper method to retrieve a message from the dictionary.
     *
     * @param catalogueID if not null, this catalogue will be used instead of the default one.
     * @return SaxBuffer containing message, or null if not found.
     */
    private ParamSaxBuffer getMessage(String catalogueID, String key) {
        try {
            Bundle catalogue = defaultCatalogue;
            if (catalogueID != null) {
                CatalogueInfo catalogueInfo = (CatalogueInfo)catalogues.get(catalogueID);
                if (catalogueInfo == null) {
                    if (getLogger().isWarnEnabled()) {
                        getLogger().warn("Catalogue not found: " + catalogueID +
                                         ", will not translate key " + key);
                    }
                    return null;
                }
                try {
                    catalogue = catalogueInfo.getCatalogue();
                } catch (Exception e) {
                    getLogger().error("Error getting catalogue " + catalogueInfo.getName() +
                                      " from location " + catalogueInfo.getLocation() +
                                      " for locale " + locale +
                                      ", will not translate key " + key);
                    return null;
                }
            }
            return (ParamSaxBuffer)catalogue.getObject(key);
        } catch (MissingResourceException e)  {
            getLogger().debug("Untranslated key: '" + key + "'");
            return null;
        }
    }

    /**
     * Helper method to retrieve a message from the dictionary.
     * mattam: now only used for i:attr.
     * A default value is returned if message is not found
     *
     * @param catalogueId if not null, this catalogue will be used instead of the default one.
     */
    private String getString(String catalogueID, String key, String defaultValue) {
        final SaxBuffer res = getMessage(catalogueID, key);
        if (res == null) {
            return defaultValue;
        }
        return res.toString();
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
            return defaultValue;
        }

        return new ParamSaxBuffer(value);
    }

    public void recycle() {
        untranslatedRecorder = null;

        // clean up default catalogue
        factory.release(defaultCatalogue);
        defaultCatalogue = null;

        // clean up the other catalogues
        Iterator i = catalogues.values().iterator();
        while (i.hasNext()) {
            CatalogueInfo catalogueInfo = (CatalogueInfo)i.next();
            catalogueInfo.releaseCatalog();
        }

        sourceResolver = null;
        objectModel = null;
        super.recycle();
    }

    public void dispose() {
        if (manager != null) {
            manager.release(this.factory);
        }
        factory = null;
        manager = null;
    }

    /**
     * Holds information about one catalogue. The location and name of the catalogue
     * can contain references to input modules, and are resolved upon each transformer
     * usage. It is important that releaseCatalog is called when the transformer is recycled.
     */
    private final class CatalogueInfo {
        PreparedVariableResolver name;
        PreparedVariableResolver location;
        String resolvedName;
        String resolvedLocation;
        Bundle catalogue;

        public CatalogueInfo(String name, String location) throws PatternException {
            this.name = new PreparedVariableResolver(name, manager);
            this.location = new PreparedVariableResolver(location, manager);
        }

        public String getName() {
            return resolvedName;
        }

        public String getLocation() {
            return resolvedLocation;
        }

        private void resolve() throws Exception {
            if (resolvedLocation == null) {
                resolvedLocation = location.resolve(null, objectModel);
            }
            if (resolvedName == null) {
                resolvedName = name.resolve(null, objectModel);
            }
        }

        public Bundle getCatalogue() throws Exception {
            if (catalogue == null) {
                resolve();
                catalogue = factory.select(resolvedLocation, resolvedName, locale);
            }
            return catalogue;
        }

        public void releaseCatalog() {
            if (catalogue != null) {
                factory.release(catalogue);
            }
            catalogue = null;
            resolvedName = null;
            resolvedLocation = null;
        }
    }
}
