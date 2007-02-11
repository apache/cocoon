/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.portal.tools.copletManagement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.forms.formmodel.Repeater;
import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.cocoon.forms.formmodel.Repeater.RepeaterRow;
import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.coplet.CopletFactory;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.layout.CompositeLayout;
import org.apache.cocoon.portal.layout.Item;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.LayoutFactory;
import org.apache.cocoon.portal.layout.NamedItem;
import org.apache.cocoon.portal.layout.impl.CopletLayout;
import org.apache.cocoon.portal.profile.ProfileManager;

/**
 * 
 * @version CVS $Id$
 */
public class LayoutActions {
    
    private final Layout layout;
    private final LayoutFactory lf;
    private final ProfileManager pm;
    private final CopletFactory cf;
    
    public LayoutActions(Layout layout, LayoutFactory lf, CopletFactory cf, ProfileManager pm) {
        this.layout = layout;
        this.lf = lf;
        this.pm = pm;
        this.cf = cf;
    }
    
    // FIXME - where is this used?
    public static int line = 1;
    
    /**
     * Delets the Object with the id in the layout
     * @param id 
     * @return true if the object could be deleted.
     */
	public boolean del(String id) {
		
		// get layout element:
		Object layoutObj = getLayoutElement (layout, id, "", 1);
		if (layoutObj == null) return false;
		
		// do the job:
		Layout lay;
		if (layoutObj instanceof NamedItem)
			lay =  ((NamedItem)layoutObj).getLayout();
		else
			lay = (Layout) layoutObj;
		
		try {
			// an empty item can not be handled by the LayoutFactory, do the job manual:
			if (lay == null) {
				List items = ((NamedItem)layoutObj).getParent().getItems(); 
				for (ListIterator iter = items.listIterator(); iter.hasNext(); ) {
					
					Item itemElem = (Item) iter.next();
					
					if( itemElem.equals(layoutObj)) {
						items.remove (iter.nextIndex()-1);
						return true;
					}
				}
			} else if(lay.getParent() instanceof NamedItem) {
				// FIXME: Causes that only the contents inside a tab are deleted instead of the tab
			    NamedItem par = (NamedItem) lay.getParent();
			    par.setLayout(null);
			} else { 
				lf.remove(lay);
			}
			
		} catch (ProcessingException e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	
	/**
	 * Moves the object one position up or down
	 * @param id id of the element
	 * @param moveUp set 'true', to move the element up ('false' to move it down)
	 * @return true if the object could be moved.
	 */
	public boolean move(String id, boolean moveUp) {
		
		// get layout element:
		Object layoutObj = getLayoutElement (layout, id, "", 1);
		if (layoutObj == null) return false;
		
		// do the job:
		Layout lay;
		Item item;
		if (layoutObj instanceof NamedItem) {
			lay =  ((NamedItem)layoutObj).getLayout();
			if (lay == null)
				item = (NamedItem) layoutObj;
			else
				item = lay.getParent();
		}
		else {
			lay = (Layout) layoutObj;
			item = lay.getParent();
		}
		
		// find element in the list and move it:
		List items = item.getParent().getItems();
		for (ListIterator iter = items.listIterator(); iter.hasNext(); ) {
			
			Item itemElem = (Item) iter.next();
			
			if(itemElem.equals(item)) {
				
				int pos = iter.nextIndex()-1;
				int newpos = pos;
				if (moveUp)
					newpos --;
				else
					newpos ++;
				
				if (newpos >= items.size()) newpos = 0;
				if (newpos < 0) newpos = items.size()-1;
				
				Object obj = items.remove (pos);
				items.add(newpos,obj);
				
				return true;
			}
		}
		return false;
	}
    
    /**
     * Adds the object to the layout
     * @param parent Object to which the new Object should be added
     * @param type Type of the Object (row, col ...)
     */
    public void add(String parent, String type) {
		
		Object layoutObj = getLayoutElement (layout, parent, "", 1);
		if (layoutObj == null) return;
		
		Layout lay;
		if (layoutObj instanceof NamedItem)
			lay =  ((NamedItem)layoutObj).getLayout();
		
		else
			lay = (Layout) layoutObj;
		
		try {
			Layout nObj = lf.newInstance(type);
			pm.register(nObj);
			
			Item e = new Item();
			nObj.setParent(e);
			e.setLayout(nObj);
			
			if (lay != null)
				((CompositeLayout) lay).addItem(e);
			else
			{
				NamedItem ni = (NamedItem)layoutObj;
				nObj.setParent(ni);
				ni.setLayout(nObj);
			}
			
		} catch (ProcessingException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Adds a new Tab
     * @param parent Parent Object
     * @param name Name of the Tab
     */
    public void addTab(String parent, String name) {
    	
    	// get layout element:
		Object layoutObj = getLayoutElement (layout, parent, "", 1);
		if (layoutObj == null) return;
		
		Layout lay;
		
		if (layoutObj instanceof NamedItem)
			lay =  ((NamedItem)layoutObj).getLayout();
		else
			lay = (Layout) layoutObj;
		
		// add tab:
		if(lay != null && lay.getName().equals("tab")) {
			
			NamedItem tab = new NamedItem();
			tab.setName(name);
			((CompositeLayout) lay).addItem(tab);
			
		} else {
			
			try {
				
				Layout tab = lf.newInstance("tab");
				pm.register(tab);
				
				NamedItem e = new NamedItem();
				e.setName(name);
				
				((CompositeLayout) tab).addItem(e);
				
				if (lay == null) {
					
					((NamedItem)layoutObj).setLayout(tab);
				}
				else {
					Item m = new Item();
					m.setParent((CompositeLayout) lay);
					((CompositeLayout) lay).addItem(m);
					m.setLayout(tab);
				}
				
			} catch (ProcessingException e) {
				e.printStackTrace();
			}
		}
    }
    
    public Collection getSelectedCoplets(Repeater r, Collection lets, String parent) {
    	
		// get layout element:
		Object obj = getLayoutElement (layout, parent, "", 1);
		if (obj == null) return null;
		
		ArrayList coplets = new ArrayList();
		ArrayList copletDatas = new ArrayList();
        
		int size = r.getSize();
		for(int i = 0; i < size; i++) {
			RepeaterRow row = r.getRow(i);
			Widget widget = row.getChild("selected");
			Boolean val = (Boolean) widget.getValue();
			if(val.booleanValue()) {
				coplets.add(row.getChild("coplet").getValue());
			}
		}
		for(Iterator it = lets.iterator(); it.hasNext();) {
			CopletData cd = (CopletData) it.next();
			String cdid = cd.getId();
			for(Iterator it2 = coplets.iterator(); it2.hasNext();) {
				String cdidTmp = (String) it2.next();
				if(cdidTmp.equals(cdid))
					copletDatas.add(cd);
			}
		}
		
		for(Iterator it = copletDatas.iterator(); it.hasNext();) {
			CopletData cd = (CopletData) it.next();
			
			try {
				CopletInstanceData cinst = cf.newInstance(cd);
				CopletLayout lay = (CopletLayout) lf.newInstance("coplet");
				lay.setCopletInstanceData(cinst);
				
			   if(obj instanceof Item) {
				   Item item = (Item) obj;
				   item.setLayout(lay);
				   lay.setParent(item);
			   } else if(obj instanceof CompositeLayout) {
				   CompositeLayout cl = (CompositeLayout) obj;
				   Item item = new Item();
				   item.setLayout(lay);
				   lay.setParent(item);
				   cl.addItem(item);
			   }
			   
			} catch (ProcessingException e) {
                // ignore it
            }
		}
		return copletDatas;
    }
    
    public CopletInstanceData getCopletInstanceData(String id) {
        Object obj = getLayoutElement(layout, id, "", 1);
        if(obj instanceof CopletLayout) {
            return ((CopletLayout) obj).getCopletInstanceData();
        }
        return null;    
    }
    
    /**
     * interal method; search for a Layout or an Item Object
     */
	private Object getLayoutElement (Layout layout, String id, String prefix, int pos) {
		
		if (layout != null) {
			
			if (id.equals((prefix+pos)))
				return layout;
			
			if (layout instanceof CompositeLayout) {
				Iterator i = ((CompositeLayout) layout).getItems().iterator();
				
				int currentpos = pos;
				pos = 1;
				while (i.hasNext()) {
					
					Item current = (Item) i.next();
					
					if (id.equals((prefix+currentpos+"."+pos)))
						return current;
					
					Object lay = getLayoutElement(current.getLayout(), id, prefix+currentpos+"."+pos+".",1);
					if(lay != null)
						return lay;
					
					pos ++;
				}
			}
		}
		return null;
	}
}
