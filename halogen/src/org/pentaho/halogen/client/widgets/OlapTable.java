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
 * @created Jan 3, 2008 
 * @author wseyler
 */


package org.pentaho.halogen.client.widgets;

import org.pentaho.halogen.client.util.CellInfo;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;

/**
 * @author wseyler
 *
 */
public class OlapTable extends FlexTable {

  public OlapTable() {
    super();
    addStyleName("olap-table"); //$NON-NLS-1$
  }

  CellInfo[][] olapData = null;
  
  public void setData(CellInfo[][] olapData) {
    setData(olapData, true);
  }
  
  public void setData(CellInfo[][] olapData, boolean refresh) {
    this.olapData = olapData;
    if (refresh) {
      refresh();
    }
  }

  protected void refresh() {
    while (this.getRowCount() > 0) {
      this.removeRow(0);
    }
    for (int r=0; r<olapData.length; r++) {
      for (int c=0; c<olapData[r].length; c++) {
        if (olapData[r][c] != null) {
          CellInfo cellInfo = olapData[r][c];
          CellFormatter cellFormatter = getCellFormatter();
          Label label = new Label(cellInfo.getFormattedValue());
          if (cellInfo.isColumnHeader()) {
            label.addStyleName("olap-col-header-label"); //$NON-NLS-1$
            cellFormatter.addStyleName(r, c, "olap-col-header-cell"); //$NON-NLS-1$
           } else if (cellInfo.isRowHeader()) {
            label.addStyleName("olap-row-header-label"); //$NON-NLS-1$
            cellFormatter.addStyleName(r, c, "olap-row-header-cell"); //$NON-NLS-1$
          } else {
            label.addStyleName("olap-cell-label"); //$NON-NLS-1$
            String colorValueStr = cellInfo.getColorValue();
            if (colorValueStr != null) {
              DOM.setElementAttribute(label.getElement(), "style", "background-color: "+cellInfo.getColorValue()+";");   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
            } else {
              
            }
          }
          setWidget(r, c, label);
        }
      }
    }    
  }
  
}
