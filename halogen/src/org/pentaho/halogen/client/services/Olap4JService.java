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
 * @created Dec 11, 2007 
 * @author wseyler
 */


package org.pentaho.halogen.client.services;


import org.pentaho.halogen.client.util.OlapData;
import org.pentaho.halogen.client.util.StringTree;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * @author wseyler
 *
 */
public interface Olap4JService extends RemoteService {
  public String getServerInfo();
  public Boolean connect(String connectStr, String guid);
  public String[] getCubes(String guid);
  public Boolean setCube(String cubeName, String guid);
  public String[] getDimensions(String axis, String guid);
  public Boolean moveDimension(String axisName, String DimName, String guid);
  public StringTree getMembers(String dimName, String guid);
  public Boolean validateQuery(String guid);
  public OlapData executeQuery(String guid);
  public OlapData executeMDXStr(String mdx, String guid);
  public Boolean createSelection(String dimName, String[] memberName, Integer selectionType, String guid);
  public OlapData swapAxis(String guid);
}
