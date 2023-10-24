/*
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
package org.apache.cocoon.spring.configurator.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.util.StatusPrinter;
import org.apache.cocoon.configuration.Settings;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

public class LogbackConfigurator extends JoranConfigurator
        implements InitializingBean {

    /** The settings object that is used to substitute variable values. */
    protected Settings settings;

    /** The configuration resources. */
    protected Resource resource;

    /**
     * Inject the settings object.
     * @param settings The settings bean.
     */
    public void setSettings(final Settings settings) {
        this.settings = settings;
    }

    /**
     * Set the configuration resource.
     * @param resource The resource.
     */
    public void setResource(final Resource resource) {
        this.resource = resource;
    }

    /**
     * This method is called after the instance is setup and before it is used.
     * @throws Exception If anything during configuration goes wrong an exception
     * is thrown.
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet()
            throws Exception {

        final LoggerContext loggerCtx =
                (LoggerContext) LoggerFactory.getILoggerFactory();
        setContext(loggerCtx);
        loggerCtx.reset();

        getContext().putProperty(Settings.KEY_CACHE_DIRECTORY,
                this.settings.getCacheDirectory());
        getContext().putProperty(Settings.KEY_CONTAINER_ENCODING,
                this.settings.getContainerEncoding());
        getContext().putProperty(Settings.KEY_FORM_ENCODING,
                this.settings.getFormEncoding());
        getContext().putProperty(Settings.KEY_WORK_DIRECTORY,
                this.settings.getWorkDirectory());
        for (Object propName : this.settings.getPropertyNames()) {
            getContext().putProperty((String) propName,
                    this.settings.getProperty((String) propName));
        }

        doConfigure(resource.getURL());
        StatusPrinter.print(loggerCtx);
    }
}
