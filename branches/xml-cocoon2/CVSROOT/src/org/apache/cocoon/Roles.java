/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon;

/**
 * Created this interface to specify the Avalon role names.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.14 $ $Date: 2001-04-17 16:04:12 $
 */

public interface Roles {

    String BROWSER              = "org.apache.cocoon.components.browser.Browser";
    String PARSER               = "org.apache.cocoon.components.parser.Parser";
    String PROCESSOR            = "org.apache.cocoon.Processor";
    String STORE                = "org.apache.cocoon.components.store.Store";
    String REPOSITORY           = "org.apache.cocoon.components.store.Repository";
    String SERVERPAGES          = "org.apache.cocoon.components.language.generator.ServerPagesSelector";

    String MARKUP_LANGUAGE      = "org.apache.cocoon.components.language.markup.MarkupLanguageSelector";
    String PROGRAMMING_LANGUAGE = "org.apache.cocoon.components.language.programming.ProgrammingLanguageSelector";
    String PROGRAM_GENERATOR    = "org.apache.cocoon.components.language.generator.ProgramGenerator";
    String CLASS_LOADER         = "org.apache.cocoon.components.classloader.ClassLoaderManager";
    String POOL_CONTROLLER      = "org.apache.excalibur.pool.PoolController";
    String SAX_CONNECTOR        = "org.apache.cocoon.components.saxconnector.SAXConnector";
    String IMAGE_ENCODER        = "org.apache.cocoon.components.image.ImageEncoderSelector";
    String DB_CONNECTION        = "org.apache.excalibur.datasource.DataSourceComponentSelector";
    String URL_FACTORY          = "org.apache.cocoon.components.url.URLFactory";

    String ACTIONS              = "org.apache.cocoon.acting.ActionSelector";
    String SELECTORS            = "org.apache.cocoon.selection.SelectorSelector";
    String MATCHERS             = "org.apache.cocoon.matching.MatcherSelector";
    String GENERATORS           = "org.apache.cocoon.generation.GeneratorSelector";
    String TRANSFORMERS         = "org.apache.cocoon.transformation.TransformerSelector";
    String SERIALIZERS          = "org.apache.cocoon.serialization.SerializerSelector";
    String READERS              = "org.apache.cocoon.reading.ReaderSelector";

    String EVENT_PIPELINE       = "org.apache.cocoon.components.pipeline.EventPipeline";
    String STREAM_PIPELINE      = "org.apache.cocoon.components.pipeline.StreamPipeline";

    String XML_SERIALIZER = "org.apache.cocoon.components.sax.XMLSerializer";
    String XML_DESERIALIZER = "org.apache.cocoon.components.sax.XMLDeserializer";
    String EVENT_CACHE = "org.apache.cocoon.caching.EventCache";
    String STREAM_CACHE = "org.apache.cocoon.caching.StreamCache";

}
