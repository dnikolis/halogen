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
 * @created Dec 12, 2007 
 * @author wseyler
 */


package org.pentaho.halogen.client;

import org.pentaho.halogen.client.panels.ConnectionPanel;
import org.pentaho.halogen.client.panels.DimensionPanel;
import org.pentaho.halogen.client.panels.ReportPanel;
import org.pentaho.halogen.client.services.Olap4JServiceAsync;

import com.google.gwt.user.client.ui.TabPanel;

/**
 * @author wseyler
 *
 */
public class HalogenTabPanel extends TabPanel {
  Messages messages;
  Olap4JServiceAsync olap4JService;
  String guid;
  
  public HalogenTabPanel(Olap4JServiceAsync olap4JService, String guid, Messages messages) {
    super();
    this.olap4JService = olap4JService;
    this.guid = guid;
    this.messages = messages;
    
    init();
  }

  /**
   * 
   */
  private void init() {
    DimensionPanel dimensionPanel = new DimensionPanel(olap4JService, guid, messages);
    ConnectionPanel connectionPanel = new ConnectionPanel(olap4JService, guid, messages);
    connectionPanel.addConnectionListener(dimensionPanel);
    ReportPanel reportPanel = new ReportPanel(olap4JService, guid, messages);
    
    this.add(connectionPanel, messages.connection());
    this.add(dimensionPanel, messages.selections());
    this.add(reportPanel, messages.report());
    
    selectTab(0);
    this.addTabListener(connectionPanel);
  }

}
