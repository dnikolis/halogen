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
 * @created May 30, 2008 
 * @author wseyler
 */


package org.pentaho.halogen.client.util;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.DockPanel.DockLayoutConstant;

/**
 * @author wseyler
 *
 */
public class LocationSelectionUtils {
  public static final String TOP_SELECTOR    = "0"; //$NON-NLS-1$
  public static final String BOTTOM_SELECTOR = "1"; //$NON-NLS-1$
  public static final String LEFT_SELECTOR   = "2"; //$NON-NLS-1$
  public static final String RIGHT_SELECTOR  = "3"; //$NON-NLS-1$

  public static String locationToSelector(DockLayoutConstant location) {
    if (location == DockPanel.NORTH) {
      return TOP_SELECTOR;
    }
    if (location == DockPanel.SOUTH) {
      return BOTTOM_SELECTOR;
    }
    if (location == DockPanel.WEST) {
      return LEFT_SELECTOR;
    }
    if (location == DockPanel.EAST) {
      return RIGHT_SELECTOR;
    }
    return BOTTOM_SELECTOR;
  }
  
  public static DockLayoutConstant selectorToLocation(String selector) {
    int selValue = Integer.parseInt(selector);
    
    switch (selValue) {
      case 0: return DockPanel.NORTH;
      case 1: return DockPanel.SOUTH;
      case 2: return DockPanel.WEST;
      case 3: return DockPanel.EAST;
      default: return DockPanel.SOUTH;
    }
  }

}
