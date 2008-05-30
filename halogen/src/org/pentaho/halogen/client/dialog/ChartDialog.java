/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created May 29, 2008 
 * @author wseyler
 */

package org.pentaho.halogen.client.dialog;

import org.pentaho.halogen.client.events.SourcesChartPrefsEvents;
import org.pentaho.halogen.client.listeners.ChartPrefsListener;
import org.pentaho.halogen.client.listeners.ChartPrefsListenerCollection;
import org.pentaho.halogen.client.util.ChartPrefs;
import org.pentaho.halogen.client.util.LocationSelectionUtils;
import org.pentaho.halogen.client.util.MessageFactory;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.DockPanel.DockLayoutConstant;

public class ChartDialog extends DialogBox implements SourcesChartPrefsEvents {
  
  protected ChartPrefs chartPrefs = null;
  protected Grid content;
  ChartPrefsListenerCollection chartPrefsListeners;
  
  public ChartDialog(ChartPrefs chartPrefs) {
    this.chartPrefs = new ChartPrefs(chartPrefs);
    init();
  }
    
  public void init() {
    content = new Grid(4, 3);
    
    CheckBox visibleCB = new CheckBox(MessageFactory.getInstance().visible());
    visibleCB.setChecked(chartPrefs.isVisible());
    visibleCB.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        CheckBox cb = (CheckBox)sender;
        ChartDialog.this.chartPrefs.setVisible(cb.isChecked());
      }
    });
    content.setWidget(0, 0, visibleCB);
    content.setText(1, 0, MessageFactory.getInstance().location());
    
    ListBox locationLB = new ListBox();
    locationLB.addItem(MessageFactory.getInstance().top(), LocationSelectionUtils.TOP_SELECTOR);
    locationLB.addItem(MessageFactory.getInstance().bottom(), LocationSelectionUtils.BOTTOM_SELECTOR);
    locationLB.addItem(MessageFactory.getInstance().left(), LocationSelectionUtils.LEFT_SELECTOR);
    locationLB.addItem(MessageFactory.getInstance().right(), LocationSelectionUtils.RIGHT_SELECTOR);
    int i;
    for (i=0; i<locationLB.getItemCount(); i++) {
      String value = locationLB.getValue(i);
      if (value.equals(chartPrefs.getLocation())) {
        break;
      }
    }
    locationLB.setSelectedIndex(i);
    locationLB.addChangeListener(new ChangeListener() {
      public void onChange(Widget sender) {
        ListBox listBox = (ListBox)sender;
        chartPrefs.setLocation(listBox.getValue(listBox.getSelectedIndex()));
      }
    });
    content.setWidget(1, 1, locationLB);
    
    content.setText(2, 0, "Chart Title:");
    TextBox titleTB = new TextBox();
    titleTB.setText(chartPrefs.getChartTitle());
    titleTB.addFocusListener(new FocusListener() {
      public void onFocus(Widget sender) {}

      public void onLostFocus(Widget sender) {
        chartPrefs.setChartTitle(((TextBox)sender).getText());
      }     
    });
    content.setWidget(2, 1, titleTB);
   
    Button okBtn = new Button(MessageFactory.getInstance().ok());
    okBtn.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        ChartDialog.this.hide();
        chartPrefsListeners.fireChartPrefsChanged(chartPrefs);
      }
    });
    content.setWidget(3, 2, okBtn);
    
    Button cancelBtn = new Button(MessageFactory.getInstance().cancel());
    cancelBtn.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        ChartDialog.this.hide();
      }
    });
    content.setWidget(3, 1, cancelBtn);
    
    setWidget(content);
  }

  /* (non-Javadoc)
   * @see org.pentaho.halogen.client.events.SourcesChartPrefsEvents#addChartPrefsListener(org.pentaho.halogen.client.listeners.ChartPrefsListener)
   */
  public void addChartPrefsListener(ChartPrefsListener listener) {
    if (chartPrefsListeners == null) {
      chartPrefsListeners = new ChartPrefsListenerCollection();
    }
    chartPrefsListeners.add(listener);
  }

  /* (non-Javadoc)
   * @see org.pentaho.halogen.client.events.SourcesChartPrefsEvents#removePrefsListener(org.pentaho.halogen.client.listeners.ChartPrefsListener)
   */
  public void removePrefsListener(ChartPrefsListener listener) {
    if (chartPrefsListeners != null) {
      chartPrefsListeners.remove(listener);
    }
  }
  
}
