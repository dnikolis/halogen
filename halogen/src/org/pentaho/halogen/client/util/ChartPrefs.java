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


package org.pentaho.halogen.client.util;

import org.pentaho.halogen.client.dialog.ChartDialog;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.DockPanel.DockLayoutConstant;

/**
 * @author wseyler
 *
 */
public class ChartPrefs implements IsSerializable {
  protected boolean visible = true;
  protected String location = LocationSelectionUtils.BOTTOM_SELECTOR;
  protected String chartTitle = "Olap Chart";
  
  public ChartPrefs() {
    super();
  }
  
  /**
   * @param chartPrefs
   */
  public ChartPrefs(ChartPrefs chartPrefs) {
    this();
    this.visible = chartPrefs.visible;
    this.location = chartPrefs.location;
    this.chartTitle = chartPrefs.chartTitle;
  }
  
  public boolean isVisible() {
    return visible;
  }
  public void setVisible(boolean visible) {
    this.visible = visible;
  }
  public String getLocation() {
    return location;
  }
  public void setLocation(String location) {
    this.location = location;
  }
  
  public String getChartTitle() {
    return chartTitle;
  }

  public void setChartTitle(String chartTitle) {
    this.chartTitle = chartTitle;
  }
  
  public boolean equals(Object obj) {
    if (!(obj instanceof ChartPrefs)) {
      return false;
    }
    ChartPrefs chartPrefs = (ChartPrefs)obj;
    return (this.visible == chartPrefs.isVisible() && 
            this.location == chartPrefs.getLocation() &&
            this.chartTitle.equals(chartPrefs.getChartTitle()));
  }

}
