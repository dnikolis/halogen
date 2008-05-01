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

import java.util.Iterator;

import org.pentaho.halogen.client.Messages;
import org.pentaho.halogen.client.util.CellInfo;
import org.pentaho.halogen.client.util.CellSpanInfo;
import org.pentaho.halogen.client.util.OlapData;
import org.pentaho.halogen.client.util.OlapUtils;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author wseyler
 *
 */
public class OlapTable extends FlexTable {
  
  private static final String OLAP_ROW_HEADER_LABEL = "olap-row-header-label"; //$NON-NLS-1$
  private static final String OLAP_ROW_HEADER_CELL = "olap-row-header-cell"; //$NON-NLS-1$
  private static final String OLAP_COL_HEADER_CELL = "olap-col-header-cell"; //$NON-NLS-1$
  private static final String OLAP_COL_HEADER_LABEL = "olap-col-header-label"; //$NON-NLS-1$
  
  OlapData olapData = null;
  boolean showParentMembers = true;
  boolean groupHeaders = false;
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
    
    if (olapData != null) {
      createColumnHeaders();
      createRowHeaders();
      populateData();
    }
    
  }

  protected void createColumnHeaders() {
    FlexCellFormatter cellFormatter = getFlexCellFormatter();
    
    CellInfo[][] headerData;
    if (showParentMembers) {
    	headerData = olapData.getColumnHeaders().getColumnHeaderMembers();
    } else {
    	headerData = new CellInfo[1][olapData.getColumnHeaders().getAcrossCount()];
    	headerData[0] = olapData.getColumnHeaders().getColumnHeaderMembers()[olapData.getColumnHeaders().getDownCount()-1];
    }
    if (groupHeaders && showParentMembers) {  
      for (int row=0; row<headerData.length; row++) {
      	int currentColumn = 0;
      	Iterator iter = OlapUtils.getCellSpans(OlapUtils.extractRow(headerData, row)).iterator();
      	while (iter.hasNext()) {
      		CellSpanInfo spanInfo = (CellSpanInfo) iter.next();
      		Label label = new Label(spanInfo.getInfo().getFormattedValue());
      		label.addStyleName(OLAP_COL_HEADER_LABEL);
      		setWidget(row, currentColumn + olapData.getRowHeaders().getAcrossCount(), label);
      		cellFormatter.addStyleName(row, currentColumn + olapData.getRowHeaders().getAcrossCount(), OLAP_COL_HEADER_CELL);
      		cellFormatter.setColSpan(row, currentColumn + olapData.getRowHeaders().getAcrossCount(), spanInfo.getSpan());
      		currentColumn ++;
      	}
      }
    } else {
      for (int row=0; row<headerData.length; row++) {
      	for (int column=0; column<headerData[row].length; column++) {
      		CellInfo cellInfo = headerData[row][column];
      		if (cellInfo != null) {
      			Label label = new Label(cellInfo.getFormattedValue());
      			label.addStyleName(OLAP_COL_HEADER_LABEL);
      			cellFormatter.addStyleName(row, showParentMembers ? column + olapData.getRowHeaders().getAcrossCount() : column + 1, OLAP_COL_HEADER_CELL);
      			setWidget(row, showParentMembers ? column + olapData.getRowHeaders().getAcrossCount() : column + 1, label);
      		}
      	}
      }
    }
  }
  
  protected void createRowHeaders() {
  	FlexCellFormatter cellFormatter = getFlexCellFormatter();
  	int columnHeadersHeight = olapData.getColumnHeaders().getDownCount();
  	
  	int rowHeadersHeight = olapData.getRowHeaders().getDownCount();
  	int rowHeadersWidth = olapData.getRowHeaders().getAcrossCount();
  	
  	CellInfo[][] headerData;
    if (showParentMembers) {
    	headerData = olapData.getRowHeaders().getRowHeaderMembers();
    } else {
    	headerData = new CellInfo[rowHeadersHeight][1];
    	for (int row = 0; row<rowHeadersHeight; row++) {
    		headerData[row][0] = olapData.getRowHeaders().getCell(row, rowHeadersWidth - 1);
    	}
    }
    if (groupHeaders && showParentMembers) {
     	for (int column = 0; column < headerData[0].length; column++) {
      	int currentRow = 0;
      	Iterator iter = OlapUtils.getCellSpans(OlapUtils.extractColumn(headerData, column)).iterator();
      	while (iter.hasNext()) {
      		CellSpanInfo spanInfo = (CellSpanInfo) iter.next();
      		Label label = new Label(spanInfo.getInfo().getFormattedValue());
      		label.addStyleName(OLAP_ROW_HEADER_LABEL);
      		int offsetColumn = getFirstUnusedColumnForRow(columnHeadersHeight + currentRow);
      		setWidget(columnHeadersHeight + currentRow, offsetColumn, label);
      		cellFormatter.addStyleName(columnHeadersHeight + currentRow, offsetColumn, OLAP_ROW_HEADER_CELL);
      		cellFormatter.setRowSpan(columnHeadersHeight + currentRow, offsetColumn, spanInfo.getSpan());
      		currentRow++;
      	}
    	}
    } else {
      for (int row=0; row<headerData.length; row++) {
      	for (int column=0; column<headerData[row].length; column++) {
      		CellInfo cellInfo = headerData[row][column];
      		if (cellInfo != null) {
      			Label label = new Label(cellInfo.getFormattedValue());
  	        label.addStyleName(OLAP_ROW_HEADER_LABEL);
  	        cellFormatter.addStyleName(showParentMembers ? columnHeadersHeight + row: row + 1, column, OLAP_ROW_HEADER_CELL);
  	        setWidget(showParentMembers ? columnHeadersHeight + row : row + 1, column, label);
      		}
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
	        setWidget(showParentMembers ? row + olapData.getColumnHeaders().getDownCount() : row + 1, showParentMembers ? getFirstUnusedColumnForRow(row + olapData.getColumnHeaders().getDownCount())/*column + olapData.getRowHeaders().getAcrossCount() */: column + 1, label);
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


  public boolean isGroupHeaders() {
    return groupHeaders;
  }


  public void setGroupHeaders(boolean groupHeaders) {
    this.groupHeaders = groupHeaders;
  }
  
  public int getFirstUnusedColumnForRow(int row) {
  	int column = 0;
  	
  	try {
  		while (true) {
	  		Widget widget = getWidget(row, column);
	  		String text = getText(row, column);
	  		if ((text == null || text.length() < 1) && widget == null) {
	  			return column;
	  		}
	  		column ++;
  		}
  	} catch (IndexOutOfBoundsException e) {
  		return column;
  	}
  }
}