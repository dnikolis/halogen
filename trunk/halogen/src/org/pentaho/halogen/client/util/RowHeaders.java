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
 * @created Feb 1, 2008 
 * @author wseyler
 */


package org.pentaho.halogen.client.util;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author wseyler
 *
 */
public class RowHeaders implements IOlapDataStructure, IsSerializable {
  CellInfo[][] rowHeaderMembers;
  
  public RowHeaders() {
    super();
  }
  
  public RowHeaders(CellInfo[][] rowHeaderMembers) {
    this();
    this.rowHeaderMembers = rowHeaderMembers;
  }

  public CellInfo[][] getColumnHeaderMembers() {
    return rowHeaderMembers;
  }

  public void setColumnHeaderMembers(CellInfo[][] rowHeaderMembers) {
    this.rowHeaderMembers = rowHeaderMembers;
  }
  
  public int getAcrossCount() {
    if (rowHeaderMembers == null) {
      return 0;
    }
    return rowHeaderMembers[0].length;
  }
  
  public int getDownCount() {
    if (rowHeaderMembers == null) {
      return 0;
    }
    return rowHeaderMembers.length;
  }

  /* (non-Javadoc)
   * @see org.pentaho.halogen.client.util.IOlapDataStructure#getCell(int, int)
   */
  public CellInfo getCell(int row, int column) {
    return rowHeaderMembers == null ? null : rowHeaderMembers[row][column];
  }
  
}
