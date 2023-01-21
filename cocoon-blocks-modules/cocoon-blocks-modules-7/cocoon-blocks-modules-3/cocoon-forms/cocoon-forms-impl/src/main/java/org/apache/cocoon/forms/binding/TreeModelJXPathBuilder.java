package org.apache.cocoon.forms.binding;

import org.apache.cocoon.forms.binding.JXPathBindingManager.Assistant;
import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;

public class TreeModelJXPathBuilder extends JXPathBindingBuilderBase {

	public JXPathBindingBase buildBinding(Element bindingElm, Assistant assistant) throws BindingException {
        try {
            CommonAttributes commonAtts = JXPathBindingBuilderBase.getCommonAttributes(bindingElm);
            String xpath = DomHelper.getAttribute(bindingElm, "path", null);
            String widgetId = DomHelper.getAttribute(bindingElm, "id", null);

            return new TreeModelJXPath(commonAtts, widgetId, xpath);
        } catch (BindingException e) {
            throw e;
        } catch (Exception e) {
            throw new BindingException("Error building binding defined at " + DomHelper.getLocation(bindingElm), e);
        }
	}

}
