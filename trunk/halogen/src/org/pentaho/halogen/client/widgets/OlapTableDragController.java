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
 * @created Jan 4, 2008 
 * @author wseyler
 */


package org.pentaho.halogen.client.widgets;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author wseyler
 *
 */
public class OlapTableDragController extends PickupDragController {

  OlapTable initialDraggableParent;
  int[] initialDraggableLocation;
  
  /**
   * @param boundaryPanel
   * @param allowDroppingOnBoundaryPanel
   */
  public OlapTableDragController(AbsolutePanel boundaryPanel, boolean allowDroppingOnBoundaryPanel) {
    super(boundaryPanel, allowDroppingOnBoundaryPanel);
  }

  protected void saveSelectedWidgetsLocationAndStyle() {
    Widget draggable = (Widget) context.selectedWidgets.get(0);
    initialDraggableParent = (OlapTable) draggable.getParent();
    
    initialDraggableLocation = getCellforWidget(initialDraggableParent, draggable);
  }
  
  protected void restoreSelectedWidgetsLocation() {
    Widget draggable = (Widget) context.selectedWidgets.get(0);
    initialDraggableParent = (OlapTable) draggable.getParent();
    initialDraggableParent.setWidget(initialDraggableLocation[0], initialDraggableLocation[1], draggable);
  }
  protected void restoreSelectedWidgetsStyle() {
    
  }
  
  protected int[] getCellforWidget(OlapTable olapTable, Widget widget) {
    int[] value = new int[2];
    boolean found = false;
    for (int row=0; row<olapTable.getRowCount() && !found; row++) {
      for (int col=0; col<olapTable.getCellCount(row); col++) {
        Widget testWidget = olapTable.getWidget(row, col);
        if (testWidget == widget) {
          value[0] = row;
          value[1] = col;
          found = true;
        }
      }
    }
    return value;
  }
}
