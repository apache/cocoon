/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.transformation;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.logger.Loggable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Roles;
import org.apache.cocoon.acting.LangSelect;
import org.apache.cocoon.components.parser.Parser;
import org.apache.cocoon.components.url.URLFactory;
import org.apache.avalon.excalibur.pool.Poolable;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

/**
 * I18nTransformer. Cocoon2 port of Infozone groups I18nProcessor.
 * <p>
 * Sitemap configuration:
 * </p>
 * <p>
 * &lt;map:transformer<br>
 *        name="translate"<br>
 *        src="org.apache.cocoon.transformation.I18nTransformer"/&gt;<br>
 * </p>
 * <p>
 * &lt;map:match pattern="file"&gt;<br>
 *        &lt;map:generate src="file.xml"/&gt;<br>
 *         &lt;map:transform type="translate"&gt;<br>
 *                &lt;parameter name="default_lang" value="fi"/&gt;<br>
 *                &lt;parameter name="available_lang_1" value="fi"/&gt;<br>
 *                &lt;parameter name="available_lang_2" value="en"/&gt;<br>
 *                &lt;parameter name="available_lang_3" value="sv"/&gt;<br>
 *                &lt;parameter name="src"<br>
 *                        value="translations/file_trans.xml"/&gt;<br>
 *        &lt;/map:transform&gt;<br>
 * </p>
 * <p>
 * When user requests .../file?lang=fi<br>
 * transformer substitutes text surrounded &lt;i:tr&gt; or &lt;some-elem i:tr="y"&gt; with
 * translations from file_trans.xml.
 * </p>
 * <p>
 * file.xml:<br>
 * &lt;root xmlns:i="http://apache.org/cocoon/i18n"&gt;<br>
 *         &lt;elem i:tr="y"&gt;Translate me&lt;/elem&gt;<br>
 *         &lt;elem&gt;&lt;i:tr&gt;Translate me&lt;/i:tr&gt;&lt;/elem&gt;<br>
 * &lt;/root&gt;
 * </p>
 * <p>
 * file_trans.xml:<br>
 * &lt;translations&gt;<br>
 *         &lt;entry&gt;&lt;key&gt;Translate me&lt;/key&gt;<br>
 *                 &lt;translation lang="sv"&gt;÷vers‰tta mej&lt;/translation&gt;<br>
 *                 &lt;translation lang="fi"&gt;K‰‰nn‰ minut&lt;/translation&gt;<br>
 *        &lt;/entry&gt;<br>
 * &lt;/translations&gt;<br>
 * </p>
 * <p>
 *It also provides path substitution to images that has to be translated:
 * </p>
 * <p>
 *&lt;elem&gt;&lt;i:image&gt;image.jpg&lt;/i:image&gt;&lt;/elem&gt;
 * </p>
 * <p>
 * is substituted to be according to language
 * </p>
 * <p>
 *&lt;elem&gt;en/image.jpg&lt;/elem&gt;,&lt;elem&gt;fi/image.jpg&lt;/elem&gt;,etc
 * </p>
 *
 *
 *TODO         -Caching dictionaries in memory.<br>
 *                 -Implementing Infozone group I18nProcessors param substitutions
 *                 where you can enter params in the translated text.
 *
 *
 * @author <a href="mailto:lassi.immonen@valkeus.com">Lassi Immonen</a>
 */
public class I18nTransformer extends AbstractTransformer implements Composable, Poolable {

    protected ComponentManager manager;

    public Map dictionary;

    //apache.org/cocoon/i18n";
    public final static String I18N_NAMESPACE_URI =
            "http://apache.org/cocoon/i18n";
    public final static String I18N_ELEMENT = "i18n";

    public final static String I18N_ELEMENT_KEY_ATTRIBUTE = "key";
    public final static String I18N_ENTRY_ELEMENT = "entry";
    public final static String I18N_TRANSLATION_ELEMENT = "translation";
    public final static String I18N_LANG = "lang";
    public final static String I18N_KEY_ELEMENT = "key";
    public final static String I18N_TR_ATTRIBUTE = "tr";
    public final static String I18N_TR_ELEMENT = "tr";
    public final static String I18N_IMAGE_ELEMENT = "image";

    protected boolean translate_image = false;
    protected boolean translate = false;
    protected boolean is_element = false;
    protected String lang;

    /**
     *  Uses <code>org.apache.cocoon.acting.LangSelect.getLang()</code>
     *  to get language user has selected. First it checks is lang set in
     *  objectModel.
     */

    public void setup(EntityResolver resolver, Map objectModel, String source,
            Parameters parameters)
            throws ProcessingException, SAXException, IOException {

        lang = (String)(objectModel.get("lang"));
        if (lang == null) {
            lang = LangSelect.getLang(objectModel, parameters);
        }

        String translations_file = parameters.getParameter("src", null);

        URL tr = null;
        URLFactory urlFactory = null;
        try {
            urlFactory = (URLFactory) this.manager.lookup(Roles.URL_FACTORY);
            tr = urlFactory.getURL(resolver.resolveEntity(null, translations_file).getSystemId());
        } catch (Exception e) {
            getLogger().error("cannot obtain the URLFactory", e);
            throw new SAXException("cannot obtain the URLFactory", e);
        } finally {
            if (urlFactory != null) this.manager.release((Component)urlFactory);
        }
        initialiseDictionary(tr);
    }


    public void compose(ComponentManager manager) {
        this.manager = manager;
    }


    public void startElement(String uri, String name, String raw,
            Attributes attr) throws SAXException {

        if (I18N_NAMESPACE_URI.equals(uri) && I18N_TR_ELEMENT.equals(name)) {
            translate = true;
            is_element = true;
            return;
        }
        if (I18N_NAMESPACE_URI.equals(uri) && I18N_IMAGE_ELEMENT.equals(name)) {
            translate_image = true;
            is_element = true;
            return;
        }
        if (attr != null) {
            AttributesImpl temp_attr = new AttributesImpl(attr);
            int attr_index =
                    temp_attr.getIndex(I18N_NAMESPACE_URI, I18N_TR_ATTRIBUTE);
            if (attr_index != -1) {
                translate = true;
                temp_attr.removeAttribute(attr_index);
                super.startElement(uri, name, raw, temp_attr);
                return;
            }
        }
        super.startElement(uri, name, raw, attr);
    }


    public void endElement(String uri, String name, String raw)
            throws SAXException {
        if (translate) {
            translate = false;
        }
        if (translate_image) {
            translate_image = false;
        }
        if (is_element) {
            is_element = false;
            return;
        }

        super.endElement(uri, name, raw);
    }



    /**
     *Gets translations from xml file to dictionary.
     */
    class I18nContentHandler extends DefaultHandler {
        boolean in_entry = false;
        boolean in_key = false;
        boolean in_translation = false;

        String key = null;
        String translation = null;


        public void startElement(String namespace, String name, String raw,
                Attributes attr) throws SAXException {

            if (name.equals(I18N_ENTRY_ELEMENT)) {
                in_entry = true;
            } else {
                if (in_entry) {
                    if (name.equals(I18N_KEY_ELEMENT)) {
                        in_key = true;
                    } else {
                        if (name.equals(I18N_TRANSLATION_ELEMENT)
                                && attr.getValue(I18N_LANG).equals(lang)) {
                            in_translation = true;
                        }
                    }
                }
            }
        }


        public void endElement(String namespace, String name, String raw)
                throws SAXException {

            if (name.equals(I18N_ENTRY_ELEMENT)) {
                if (key != null && translation != null) {
                    dictionary.put(key, translation);
                    key = null;
                    translation = null;
                }
                in_entry = false;
            } else if (name.equals(I18N_KEY_ELEMENT)) {
                in_key = false;
            } else {
                if (name.equals(I18N_TRANSLATION_ELEMENT)) {
                    in_translation = false;
                }
            }

        }


        public void characters(char[] ary, int start, int length)
                throws SAXException {
            if (in_key) {
                key = new String(ary, start, length);

            } else {
                if (in_translation) {
                    translation = new String(ary, start, length);
                }
            }
        }

    }


    public void characters(char[] ch, int start, int len) throws SAXException {
        if (translate) {

            String text2translate = new String(ch, start, len);
            String result = (String)(dictionary.get(text2translate));
            if (result != null) {
                super.contentHandler.characters(result.toCharArray(), 0, result.length());
                return;
            }
        }
        if (translate_image) {
            String image_name = new String(ch, start, len);
            String result = lang + "/" + image_name;
            super.contentHandler.characters(result.toCharArray(), 0, result.length());
            return;
        }
        super.characters(ch, start, len);

    }


    /**
     *Loads translations from given URL
     */
    private void initialiseDictionary(URL url)
            throws SAXException, MalformedURLException, IOException {

        Object object = url.getContent();
        Parser parser = null;

        try
        {
            parser = (Parser)(manager.lookup(Roles.PARSER));
            InputSource input;
            if (object instanceof Loggable) {
                ((Loggable)object).setLogger(getLogger());
            }
            if (object instanceof Reader) {
                input = new InputSource(new BufferedReader((Reader)(object)));
            } else if (object instanceof InputStream) {
                input = new InputSource(new BufferedInputStream((InputStream)(object)));
            } else {
                throw new SAXException("Unknown object type: " + object);
            }

            // How this could be cached?
            dictionary = new Hashtable();
            I18nContentHandler i18n_handler = new I18nContentHandler();
            parser.setContentHandler(i18n_handler);
            parser.parse(input);
        } catch(SAXException e) {
            getLogger().error("Error in initialiseDictionary", e);
            throw e;
        } catch(MalformedURLException e) {
            getLogger().error("Error in initialiseDictionary", e);
            throw e;
        } catch(IOException e) {
            getLogger().error("Error in initialiseDictionary", e);
            throw e;
        } catch(ComponentException e) {
            getLogger().error("Error in initialiseDictionary", e);
            throw new SAXException("ComponentException in initialiseDictionary");
        } finally {
            if(parser != null) this.manager.release((Component) parser);
        }
    }
}
