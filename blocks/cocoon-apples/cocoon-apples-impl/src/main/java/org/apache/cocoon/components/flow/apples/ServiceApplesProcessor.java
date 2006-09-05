/*
 * $Id$
 *
 * Created on 2006-09-05
 *
 * Copyright (c) 2006, MobileBox sp. z o.o.
 * All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.apache.cocoon.components.flow.apples;

import org.apache.cocoon.ProcessingException;

/**
 * A special version of ApplesProcessor that interprets the parameter passed to
 * instantiateController as service/bean name instead of classname. The class is
 * probably most useful with spring container integration.
 * 
 * Declare your flow in sitemap as &lt;map:flow language=&quot;service-apples&quot/&gt>
 * Define your AppleController beans in block/config/spring/ and call them from
 * sitemap by &lt;map:call function=&quot;beanName&quot/&gt>
 * 
 * Please remember to declare your StatelessAppleControllers as singletons. If
 * you wish to use continuations beans have to be declared as non-singletons.
 * 
 * You are of course free to use any container features in your beans like
 * dependency injection.
 * 
 * @version $Id$
 */
public class ServiceApplesProcessor extends ApplesProcessor {
    protected AppleController instantiateController(String beanName) throws Exception {
        Object bean = this.manager.lookup(beanName);
        if (!(bean instanceof AppleController))
            throw new ProcessingException("The bean called is not a AppleController");
        return (AppleController) bean;
    }
}
