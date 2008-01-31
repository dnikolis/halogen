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

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author wseyler
 *
 */
public interface Olap4JServiceAsync {
  public void getServerInfo(AsyncCallback callback);
  public void connect(String connectStr, String guid, AsyncCallback callback);
  public void getCubes(String guid, AsyncCallback callback);
  public void setCube(String cubeName, String guid, AsyncCallback callback);
  public void getDimensions(String axis, String guid, AsyncCallback callback);
  public void getMembers(String dimName, String guid, AsyncCallback callback);
  public void moveDimension(String axisName, String DimName, String guid, AsyncCallback callback);
  public void validateQuery(String guid, AsyncCallback callback);
  public void executeQuery(String guid, AsyncCallback callback);
  public void executeMDXStr(String mdx, String guid, AsyncCallback callback);
  public void createSelection(String dimName, String memberName, Integer selectionType, String guid, AsyncCallback callback);
  public void swapAxis(String guid, AsyncCallback callback);
}
