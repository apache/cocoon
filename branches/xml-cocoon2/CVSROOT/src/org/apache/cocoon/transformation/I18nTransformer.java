/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.transformation;

import org.apache.cocoon.Roles;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.parser.Parser;

import javax.servlet.http.*;
import org.apache.avalon.*;
import org.w3c.dom.*;
import org.xml.sax.helpers.*;
import java.io.*;
import java.util.*;
import org.apache.cocoon.transformation.*;
import org.xml.sax.*;
import java.net.*;

import org.apache.cocoon.acting.LangSelect;

/**
* I18nTransformer. Cocoon2 port of Infozone groups I18nProcessor.
*
* Sitemap configuration:
*
* &lt;map:transformer   
*	name="translate" 
*	src="org.apache.cocoon.transformation.I18nTransformer"/&gt;
*
*
* &lt;map:match pattern="file"&gt;
*	&lt;map:generate src="file.xml"/&gt;
* 	&lt;map:transform type="translate"&gt;
*		&lt;parameter name="default_lang" value="fi"/&gt;
*		&lt;parameter name="available_lang_1" value="fi"/&gt;
*		&lt;parameter name="available_lang_2" value="en"/&gt;
*		&lt;parameter name="available_lang_3" value="sv"/&gt;
*		&lt;parameter name="src" 
*			value="translations/file_trans.xml"/&gt;    
*	&lt;/map:transform&gt;
* 	
*
* When user requests .../file?lang=fi
* transformer substitutes text surrounded &lt;i:tr&gt; or &lt;some-elem i:tr="y"&gt; with
* translations from file_trans.xml.  
*
* file.xml:
* &lt;root xmlns:i="http://apache.org/cocoon/i18n"&gt;
* 	&lt;elem i:tr="y"&gt;Translate me&lt;/elem&gt;
* 	&lt;elem&gt;&lt;i:tr&gt;Translate me&lt;/i:tr&gt;&lt;/elem&gt;
* &lt;/root&gt;
*
* file_trans.xml:
* &lt;translations&gt;
* 	&lt;entry&gt;&lt;key&gt;Translate me&lt;/key&gt;
* 		&lt;translation lang="sv"&gt;÷vers‰tta mej&lt;/translation&gt;
* 		&lt;translation lang="fi"&gt;K‰‰nn‰ minut&lt;/translation&gt;
*	&lt;/entry&gt;
* &lt;/translations&gt;
*
*It also provides path substitution to images that has to be translated:
*
*&lt;elem&gt;&lt;i:image&gt;image.jpg&lt;/i:image&gt;&lt;/elem&gt; 
*
* is substituted to be according to language
*
*&lt;elem&gt;en/image.jpg&lt;/elem&gt;,&lt;elem&gt;fi/image.jpg&lt;/elem&gt;,etc
*
*
*TODO 	-Caching dictionaries in memory.
* 		-Implementing Infozone group I18nProcessors param substitutions
* 		where you can enter params in the translated text.
*
* 
* @author <a href="mailto:lassi.immonen@valkeus.com">Lassi Immonen</a>
*/
public class I18nTransformer extends AbstractTransformer implements Composer {

	protected ComponentManager manager = null;

	public Map dictionary = null;

	public static final String I18N_NAMESPACE_URI = "http://apache.org/cocoon/i18n"; 
	public static final String I18N_ELEMENT = "i18n";

	public static final String I18N_ELEMENT_KEY_ATTRIBUTE = "key";
	public static final String I18N_ENTRY_ELEMENT = "entry";
	public static final String I18N_TRANSLATION_ELEMENT = "translation";
	public static final String I18N_LANG = "lang";
	public static final String I18N_KEY_ELEMENT = "key";
	public static final String I18N_TR_ATTRIBUTE = "tr";
	public static final String I18N_TR_ELEMENT = "tr";
	public static final String I18N_IMAGE_ELEMENT = "image";	
	
	protected boolean translate_image = false;
	protected boolean translate = false;
	protected String lang = null;

/**
*  Uses <code>org.apache.cocoon.acting.LangSelect.getLang()</code>
*  to get language user has selected. First it checks is lang set in 
*  objectModel. 
*/

	public void setup(
		EntityResolver resolver, 
		Map objectModel, 
		String source, 
		Parameters parameters)
		throws ProcessingException, SAXException, IOException {

		lang = (String)objectModel.get("lang");				
		if (lang == null) {
			lang = LangSelect.getLang(objectModel,parameters);
		}
		
		String translations_file = parameters.getParameter("src", null);

		URL tr = new URL(resolver.resolveEntity(null, translations_file).getSystemId()); 
		initialiseDictionary(tr);
	}

	public void compose(ComponentManager manager) {
		this.manager = manager;
	}

	public void startElement(String uri, String name, String raw, Attributes attr)
		throws SAXException {

		if (attr.getValue(I18N_NAMESPACE_URI, I18N_TR_ATTRIBUTE) != null) {
			translate = true;
		}
		if (uri.equals(I18N_NAMESPACE_URI) && name.equals(I18N_TR_ELEMENT)) {
			translate = true;
		}
		if(uri.equals(I18N_NAMESPACE_URI) && name.equals(I18N_IMAGE_ELEMENT)) {
			translate_image = true;
			return;
		}
		super.startElement(uri, name, raw, attr);
	}

	public void endElement(String uri, String name, String raw)
		throws SAXException {
		if (translate)
			translate = false;
		if (translate_image) {
			translate_image = false;
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

		public void startElement(
			String namespace, 
			String name, 
			String raw, 
			Attributes attr)
			throws SAXException {

			if (name.equals(I18N_ENTRY_ELEMENT)) {
				in_entry = true;
			} else if (in_entry) {
				if (name.equals(I18N_KEY_ELEMENT)) {
					in_key = true;
				} else if (
					name.equals(I18N_TRANSLATION_ELEMENT)
						&& attr.getValue(I18N_LANG).equals(lang)) {
					in_translation = true;
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
			} else if (name.equals(I18N_TRANSLATION_ELEMENT)) {
				in_translation = false;
			}

		}

		public void characters(char ary[], int start, int length) throws SAXException {
			if (in_key) {
				key = new String(ary, start, length);

			} else if (in_translation) {
				translation = new String(ary, start, length);
			}
		}

	}

public void characters(char ch[], int start, int len) throws SAXException {
	if (translate) {

		String text2translate = new String(ch, start, len);
		String result = (String) dictionary.get(text2translate);
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
	Parser parser=null;
	try {
		parser = (Parser) manager.lookup(Roles.PARSER);

	} catch (Exception e) {
		log.error("Could not find component", e);
		return;
	}
		InputSource input;
		if (object instanceof Reader) {
			input = new InputSource((Reader) object);
		} else if (object instanceof InputStream) {
			input = new InputSource((InputStream) object);
		} else {
			throw new SAXException("Unknown object type: " + object);
		}

		// How this could be cached?
	   dictionary = new Hashtable();
		I18nContentHandler i18n_handler = new I18nContentHandler();
		parser.setContentHandler(i18n_handler);
		parser.parse(input);

	}
}
