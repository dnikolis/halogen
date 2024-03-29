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
import org.pentaho.halogen.client.panels.CellFormatPopup;
import org.pentaho.halogen.client.util.CellInfo;
import org.pentaho.halogen.client.util.CellSpanInfo;
import org.pentaho.halogen.client.util.GuidFactory;
import org.pentaho.halogen.client.util.OlapData;
import org.pentaho.halogen.client.util.OlapUtils;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ClickListener;
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
  private static final char USED = 'u';
  private static final char SPANNED = 's';
  private static final char FREE = 'f';
  
  OlapData olapData = null;
  boolean showParentMembers = true;
  boolean groupHeaders = true;
  Messages messages = null;
  
  private ClickListener clickListener;
  private CellFormatPopup cellFormatPopup;
  
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
  
  public OlapData getData() {
    return olapData;
  }
  
  public void refresh() {
    removeAllRows();

    if (olapData != null) {
    	createColumnHeaders();
      createRowHeaders();
      populateData();
    }   
  }

  protected void removeAllRows() {
    while (this.getRowCount() > 0) {
      this.removeRow(0);
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
    	int addition = showParentMembers ? 0 : -1;
    	for (int row=0; row<headerData.length; row++) {
      	for (int column=0; column<headerData[row].length; column++) {
      		CellInfo cellInfo = headerData[row][column];
      		if (cellInfo != null) {
      			Label label = new Label(cellInfo.getFormattedValue());
      			label.addStyleName(OLAP_COL_HEADER_LABEL);
      			cellFormatter.addStyleName(row, showParentMembers ? column + olapData.getRowHeaders().getAcrossCount() : column + 1, OLAP_COL_HEADER_CELL);
      			// aki tinha +1 antes do label (em cima tbm, no correspondente)
      			setWidget(row, showParentMembers ? column + olapData.getRowHeaders().getAcrossCount() : column + 1, label);
      		}
      	}
      }
    }
  }
  
  protected void createRowHeaders() {
  	FlexCellFormatter cellFormatter = getFlexCellFormatter();
  	int columnHeadersHeight = showParentMembers ? olapData.getColumnHeaders().getDownCount() : 2;
  	
  	int rowHeadersHeight = olapData.getRowHeaders().getDownCount();
  	int rowHeadersWidth = olapData.getRowHeaders().getAcrossCount();
  	int offset = 0;
  	
  	CellInfo[][] headerData;
    if (showParentMembers) {
    	headerData = olapData.getRowHeaders().getRowHeaderMembers();
    } else {
    	headerData = new CellInfo[rowHeadersHeight][1];
    	for (int row = 0; row<rowHeadersHeight; row++) {
    		headerData[row][0] = olapData.getRowHeaders().getCell(row, rowHeadersWidth - 1);
    	}
    }
    char matrix[][] = createMatrix(headerData.length + columnHeadersHeight, headerData[0].length);
    
    if (groupHeaders) {
    	for (int column=0; column < headerData[0].length; column++) { // columns

    		CellInfo actualColumn[] = OlapUtils.extractColumn(headerData, column);
    		if (actualColumn == null || actualColumn.length == 0)
				continue;			    		
    		Iterator iter = OlapUtils.getCellSpans(actualColumn).iterator();
    		int actualRow = 0; // the current row (considering just the headerData, excluding column headers)
    		while (iter.hasNext())
    		{
    			if (showParentMembers == false)
    				actualRow--;
    			CellSpanInfo spanInfo = (CellSpanInfo) iter.next();
    			//Prepares the label
    			Label label = new Label(spanInfo.getInfo().getFormattedValue());
          		label.addStyleName(OLAP_ROW_HEADER_LABEL);

          		int newColumn = offset;
          		matrix[columnHeadersHeight + actualRow][column] = USED;
          		spanMatrixRow(matrix, columnHeadersHeight + actualRow, newColumn, spanInfo.getSpan());
          		
          		cellFormatter.setRowSpan(columnHeadersHeight + actualRow , 
          				newColumn - getSpanInRow(matrix, columnHeadersHeight +actualRow), spanInfo.getSpan());
          		cellFormatter.addStyleName(columnHeadersHeight + actualRow , 
          				newColumn - getSpanInRow(matrix, columnHeadersHeight +actualRow), OLAP_ROW_HEADER_CELL);
          		setWidget (columnHeadersHeight + actualRow , 
          				newColumn - getSpanInRow(matrix, columnHeadersHeight + actualRow), label);
          		
          		actualRow+=spanInfo.getSpan();
          		
          		if (showParentMembers == false)
    				actualRow++;
          		
    		}
    		offset++;
    	}
    }
    else 
    {
    	int rowAddition = showParentMembers ? columnHeadersHeight : 1;
    	int columnAddition = showParentMembers ? 0 : -1 ;
    	for (int row=0; row<headerData.length; row++) {
           	for (int column=0; column<headerData[row].length; column++) {
           		CellInfo cellInfo = headerData[row][column];
           		if (cellInfo != null) {
           			Label label = new Label(cellInfo.getFormattedValue());
           			label.addStyleName(OLAP_ROW_HEADER_CELL);
           			cellFormatter.addStyleName(row + rowAddition, 
           					showParentMembers ? column : column, OLAP_ROW_HEADER_CELL);
           					//showParentMembers ? column + olapData.getRowHeaders().getAcrossCount() : column + 1, OLAP_COL_HEADER_CELL);
           			setWidget(row + rowAddition, 
           					showParentMembers ? column : column, label);
           		}
           	}
         }//for
    }//else
  }
  
  /*
  private void printTable()
  {
	  int j = 0;
	  System.out.println("==============");
	  for (int i = 0; i < getRowCount(); i++)
	  {
		for (j = 0; j < getCellCount(i); j++)
		{
			if (isCellPresent (i,j))
				System.out.print("P ");
			else
				System.out.print("n ");
		}
		System.out.println((j));
	  }
	  System.out.println("==============");
  }
  */
  
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
	        label.addClickListener(new ClickCellCommand());
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
    setGroupHeaders(groupHeaders, true);
  }
  
  /**
   * @param groupHeaders2
   * @param b
   */
  public void setGroupHeaders(boolean groupHeaders, boolean refresh) {
    this.groupHeaders = groupHeaders;
    if (refresh) {
      refresh();
    }
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
  
  protected char[][] createMatrix (int row, int column)
  {
	  char m[][] = new char[row][column];
	  for (int i = 0; i < m.length; i++)
		  for (int j = 0; j < m[0].length; j++)
			  m[i][j] = FREE;
	  
	  return m;
  }
  
  /*
   * There's no clone() method in GWT, so it's done by hand.
   */
  protected void copyMatrix( char source[][], char destination[][])
  {
	  if (source.length > destination.length |
		  source[0].length > destination[0].length)
	  {
		  throw new IndexOutOfBoundsException 
		  ("The destination[" + destination.length + "][" + destination[0].length +"]" +
				 "is smaller than source[" + source.length + "][" + source[0].length +"]");
	  }

	  for (int i = 0; i < source.length; i++)
		  for (int j = 0; j < source[0].length; j++)
			  destination[i][j]=source[i][j];
		  
  
  }
  
  protected void spanMatrixRow( char matrix[][], int row, int column, int span)
  {
	  for (int i = 1; i < span; i++)
		  matrix[row  + i][column] = SPANNED;
  }

  protected void printMatrix (char m[][])
  {
	  for (int i = 0; i < m.length; i++)
	  {
		  for (int j = 0; j < m[0].length; j++)
			  System.out.print( " [" + i + "][" + j +"]=" + m[i][j]);
		  
		  System.out.println();
	  }
	  
  }
  
  protected int getSpanInRow(char[][] matrix, int row)
  {
	  int result = 0;
	  for (int i = 0; i < matrix[row].length; i++)
		  if (matrix[row][i] == SPANNED)
			  result++;
	  
	  return result;
  }
  
  protected int getSpanInColumn(char[][] matrix, int column)
  {
	  int result = 0;
	  for (int i = 0; i < matrix.length; i++)
		  if (matrix[i][column] == SPANNED)
			  result++;
	  
	  return result;
  }
  
  public class ClickCellCommand implements ClickListener{
	  public void onClick(Widget sender){
		  cellFormatPopup =  new CellFormatPopup(GuidFactory.getGuid(), 
				  sender.getAbsoluteTop(), sender.getAbsoluteLeft(), sender );
		  cellFormatPopup.show();
		  //sender.addStyleName(cellFormatPopup.getReturnStyle());
		  
	  };
  }
}
