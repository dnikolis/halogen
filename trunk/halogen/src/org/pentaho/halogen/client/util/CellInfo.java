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


package org.pentaho.halogen.client.util;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author wseyler
 *
 */
public class CellInfo implements IsSerializable {
  String formattedValue;
  String rawValue;
  boolean isColumnHeader = false;
  boolean isRowHeader = false;
  String colorValue = null; // Color held as hex String
  
  public String getFormattedValue() {
    return formattedValue;
  }
  public void setFormattedValue(String formattedValue) {
    this.formattedValue = formattedValue;
  }
  public boolean isHeader() {
  	return isRowHeader() || isColumnHeader();
  }
  public boolean isColumnHeader() {
    return isColumnHeader;
  }
  public void setColumnHeader(boolean isColumnHeader) {
    this.isColumnHeader = isColumnHeader;
  }
  public boolean isRowHeader() {
    return isRowHeader;
  }
  public void setRowHeader(boolean isRowHeader) {
    this.isRowHeader = isRowHeader;
  }
  public String getColorValue() {
    return colorValue;
  }
  public void setColorValue(String colorValue) {
    this.colorValue = colorValue;
  }
  public String getRawValue() {
    return rawValue;
  }
  public void setRawValue(String rawValue) {
    this.rawValue = rawValue;
  }
  
  public String toString() {
  	return formattedValue;
  }
}
