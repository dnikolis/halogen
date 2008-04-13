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
 * @created Apr 3, 2008 
 * @author wseyler
 */


package org.pentaho.halogen.client.util;

import org.pentaho.halogen.client.services.Olap4JService;
import org.pentaho.halogen.client.services.Olap4JServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * @author wseyler
 *
 */
public class ServiceFactory {
  static Olap4JServiceAsync service = null;
  
  public static Olap4JServiceAsync getInstance() {
    if (service == null) {
      service = (Olap4JServiceAsync) GWT.create(Olap4JService.class);
      ServiceDefTarget endpoint = (ServiceDefTarget) service;
      String moduleRelativeURL = GWT.getModuleBaseURL() + "olap4j"; //$NON-NLS-1$
      endpoint.setServiceEntryPoint(moduleRelativeURL);
    }
    return service;
  }
}
