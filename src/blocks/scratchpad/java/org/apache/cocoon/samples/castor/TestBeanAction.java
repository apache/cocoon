package org.apache.cocoon.samples.castor;

import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.acting.AbstractAction;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;

import java.util.Map;

/**
 *
 * @version CVS $Id: TestBeanAction.java,v 1.1 2003/09/04 12:42:44 cziegeler Exp $
 */
public class TestBeanAction extends AbstractAction {

  public TestBeanAction() {
  }

  public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String src, Parameters param) throws java.lang.Exception {
      Request request = ObjectModelHelper.getRequest(objectModel);
      Context context = ObjectModelHelper.getContext(objectModel);
      if(context != null)
        request.setAttribute("Wale",new  TestBean("Wale in the big sea","context"));

      Session session =request.getSession(true);
      session.setAttribute("Mouse",new  TestBean("Liveing in the session","session"));
      objectModel.put("Lion", new TestBean("Lion:walking on the sitemap","sitemap") );
      request.setAttribute("Hamster",new TestBean("Hamster:Wer hat nach mir gefragt","request")  );
      session.setAttribute("Elefant",new  TestBean("Elefant:from Africa","session"));
      request.setAttribute("Elefant",new  TestBean("Elefant:from India","request"));


      return objectModel;

  }
}
