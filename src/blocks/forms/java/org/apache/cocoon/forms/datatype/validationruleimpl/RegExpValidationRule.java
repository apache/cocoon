/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
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
package org.apache.cocoon.forms.datatype.validationruleimpl;

import org.apache.cocoon.forms.Constants;
import org.apache.cocoon.forms.datatype.ValidationError;
import org.apache.cocoon.forms.util.I18nMessage;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Matcher;
import org.outerj.expression.ExpressionContext;


/**
 * Checks that a String matches a regular expression.
 *
 * <p>The <a href="http://jakarta.apache.org/oro/">Jakarta ORO</a> library
 * is used as regexp engine.
 * 
 * @version $Id: RegExpValidationRule.java,v 1.1 2004/03/09 10:34:10 reinhard Exp $
 */
public class RegExpValidationRule extends AbstractValidationRule {
	/** Compiled regular expression. */
	private Pattern pattern;
    /** Original string representation of the regexp, used for informational purposes only. */
    private String regexp;

    public ValidationError validate(Object value, ExpressionContext expressionContext) {
    	String string = (String)value;
    	
    	if(matchesRegExp(string))
	     	return null;   
	    else
	    	return hasFailMessage() ? getFailMessage() : new ValidationError(new I18nMessage("validation.string.regexp", new String[] {regexp}, Constants.I18N_CATALOGUE));
    }
    
    private boolean matchesRegExp(String string) {
        PatternMatcher matcher = new Perl5Matcher();
        return matcher.matches(string, pattern);
    }
    
    void setPattern(String regexp, Pattern pattern) {
        this.regexp = regexp;
    	this.pattern = pattern;
    }

    
    public boolean supportsType(Class clazz, boolean arrayType) {
        return clazz.isAssignableFrom(String.class) && !arrayType;
    }
}
