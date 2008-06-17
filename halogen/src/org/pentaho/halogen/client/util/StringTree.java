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
 * @created Dec 20, 2007 
 * @author wseyler
 */


package org.pentaho.halogen.client.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wseyler
 *
 */
public class StringTree implements Serializable {
  String value;

  List<StringTree> children;
  StringTree parent;
  
  public StringTree() {
    children = new ArrayList<StringTree>();
  }
  public StringTree(String value, StringTree parent) {
    this();
    this.value = value;
    this.parent = parent;
    if (this.parent != null) {
      this.parent.addChild(this);
    }
  }
  public String getValue() {
    return value;
  }
  public void setValue(String value) {
    this.value = value;
  }
  public void addChild(StringTree stringTree) {
    children.add(stringTree);
  }
  public List<StringTree> getChildren() {
    return children;
  } 
  public StringTree getParent() {
    return parent;
  }
  public void setParent(StringTree parent) {
    this.parent = parent;
  }
  public boolean hasChildren() {
    return children.size() > 0; 
  }
}
