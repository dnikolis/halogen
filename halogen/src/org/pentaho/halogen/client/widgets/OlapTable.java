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

import org.pentaho.halogen.client.Messages;
import org.pentaho.halogen.client.util.CellInfo;
import org.pentaho.halogen.client.util.OlapData;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;

/**
 * @author wseyler
 *
 */
public class OlapTable extends FlexTable {
  OlapData olapData = null;
  boolean showParentMembers = true;
  Messages messages = null;
  
  /**
   * @param messages
   */
  public OlapTable(Messages messages) {
    super();
    this.messages = messages;
    
    addStyleName("olap-table"); //$NON-NLS-1$
  }

  
  public OlapTable(Messages messages, boolean showParentMembers) {
    this(messages);
    this.showParentMembers = showParentMembers;
  }

  public void setData(OlapData olapData) {
    setData(olapData, true);
  }
  
  public void setData(OlapData olapData, boolean refresh) {
    this.olapData = olapData;
    if (refresh) {
      refresh();
    }
  }
  
  public void refresh() {
    while (this.getRowCount() > 0) {
      this.removeRow(0);
    }
    
    if (olapData == null) {
      Window.alert(messages.no_data());
      return;
    }
    
    createColumnHeaders();
    createRowHeaders();
    populateData();
  }

  protected void createColumnHeaders() {
    CellFormatter cellFormatter = getCellFormatter();
    
    CellInfo[][] headerData;
    if (showParentMembers) {
    	headerData = olapData.getColumnHeaders().getColumnHeaderMembers();
    } else {
    	headerData = new CellInfo[1][olapData.getColumnHeaders().getAcrossCount()];
    	headerData[0] = olapData.getColumnHeaders().getColumnHeaderMembers()[olapData.getColumnHeaders().getDownCount()-1];
    }
    for (int row=0; row<headerData.length; row++) {
    	for (int column=0; column<headerData[row].length; column++) {
    		CellInfo cellInfo = headerData[row][column];
    		if (cellInfo != null) {
    			Label label = new Label(cellInfo.getFormattedValue());
    			label.addStyleName("olap-col-header-label");
    			cellFormatter.addStyleName(row, showParentMembers ? column + olapData.getRowHeaders().getAcrossCount() : column + 1, "olap-col-header-cell");
    			setWidget(row, showParentMembers ? column + olapData.getRowHeaders().getAcrossCount() : column + 1, label);
    		}
    	}
    }  	
  }
  
  protected void createRowHeaders() {
  	CellFormatter cellFormatter = getCellFormatter();
  	
  	CellInfo[][] headerData;
    if (showParentMembers) {
    	headerData = olapData.getRowHeaders().getRowHeaderMembers();
    } else {
    	headerData = new CellInfo[olapData.getRowHeaders().getDownCount()][1];
    	for (int row = 0; row<olapData.getRowHeaders().getDownCount(); row++) {
    		headerData[row][0] = olapData.getRowHeaders().getCell(row, olapData.getRowHeaders().getAcrossCount() -1);
    	}
    }
    for (int row=0; row<headerData.length; row++) {
    	for (int column=0; column<headerData[row].length; column++) {
    		CellInfo cellInfo = headerData[row][column];
    		if (cellInfo != null) {
    			Label label = new Label(cellInfo.getFormattedValue());
	        label.addStyleName("olap-row-header-label");
	        cellFormatter.addStyleName(showParentMembers ? row + olapData.getColumnHeaders().getDownCount() : row + 1, column, "olap-row-header-cell");
	        setWidget(showParentMembers ? row + olapData.getColumnHeaders().getDownCount() : row + 1, column, label);
    		}
    	}
    }  	
  }
  
  protected void populateData() {
    for (int row=0; row<olapData.getCellData().getDownCount(); row++) {
    	for (int column=0; column<olapData.getCellData().getAcrossCount(); column++) {
    		CellInfo cellInfo = olapData.getCellData().getCell(row, column);
    		if (cellInfo != null) {
    			Label label = new Label(cellInfo.getFormattedValue());
	        label.addStyleName("olap-cell-label"); //$NON-NLS-1$
	        String colorValueStr = cellInfo.getColorValue();
	        if (colorValueStr != null) {
	          DOM.setElementAttribute(label.getElement(), "style", "background-color: "+cellInfo.getColorValue()+";");   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
	        }
	        setWidget(showParentMembers ? row + olapData.getColumnHeaders().getDownCount() : row + 1, showParentMembers ? column + olapData.getRowHeaders().getAcrossCount() : column + 1, label);
    		}
    	}
    }  	
  }
  
  public boolean isShowParentMembers() {
    return showParentMembers;
  }

  public void setShowParentMembers(boolean showParentMembers, boolean refresh) {
    this.showParentMembers = showParentMembers;
    if (refresh) {
      refresh();
    }
  }
  
  public void setShowParentMembers(boolean showParentMembers) {
    setShowParentMembers(showParentMembers, true);
  }
}
