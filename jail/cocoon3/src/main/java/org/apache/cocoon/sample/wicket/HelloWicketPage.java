/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.sample.wicket;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;

public class HelloWicketPage extends WebPage {

    private AjaxFallbackLink<Object> ajaxFallbackLink;

    private Label text;

    private int counter = 1;

    public HelloWicketPage() {
        super();

        this.add(new Label("message", "hello, wicket!"));
        this.text = new Label("text", "text-1");
        this.text.setOutputMarkupId(true);
        this.add(this.text);

        this.ajaxFallbackLink = new AjaxFallbackLink<Object>("link") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(final AjaxRequestTarget target) {
                HelloWicketPage.this.text.setDefaultModelObject(
                        "text-" + ++HelloWicketPage.this.counter);
                target.add(HelloWicketPage.this.text);
            }
        };
        this.add(this.ajaxFallbackLink);

        this.add(new Link<Object>("link-other-page") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                this.setResponsePage(OtherPage.class);
            }
        });
    }
}
