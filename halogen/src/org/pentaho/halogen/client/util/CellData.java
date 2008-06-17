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

import java.io.Serializable;

/**
 * @author wseyler
 *
 */
public class CellData implements IOlapDataStructure, Serializable {
  CellInfo[][] olapDataCells;
  
  public CellData() {
    super();
  }
  
  public CellData(CellInfo[][] olapDataCells) {
    this();
    this.olapDataCells = olapDataCells;
  }

  public CellInfo[][] getOlapDataCells() {
    return olapDataCells;
  }

  public void setOlapCells(CellInfo[][] olapDataCells) {
    this.olapDataCells = olapDataCells;
  }
  
  public int getAcrossCount() {
    if (olapDataCells == null) {
      return 0;
    }
    return olapDataCells[0].length;
  }
  
  public int getDownCount() {
    if (olapDataCells == null) {
      return 0;
    }
    return olapDataCells.length;
  }

  /* (non-Javadoc)
   * @see org.pentaho.halogen.client.util.IOlapDataStructure#getCell(int, int)
   */
  public CellInfo getCell(int row, int column) {
    return olapDataCells == null ? null : olapDataCells[row][column];
  }
  
}
