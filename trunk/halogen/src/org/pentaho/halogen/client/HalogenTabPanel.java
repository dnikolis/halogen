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
import org.pentaho.halogen.client.util.MessageFactory;

import com.google.gwt.user.client.ui.TabPanel;

/**
 * @author wseyler
 *
 */
public class HalogenTabPanel extends TabPanel {
  
  public HalogenTabPanel() {
    super();
    
    init();
  }

  /**
   * 
   */
  private void init() {
    DimensionPanel dimensionPanel = new DimensionPanel();
    ConnectionPanel connectionPanel = new ConnectionPanel();
    ReportPanel reportPanel = new ReportPanel();

    connectionPanel.addConnectionListener(dimensionPanel);
    connectionPanel.addConnectionListener(reportPanel);
    
    this.add(connectionPanel, MessageFactory.getMessages().connection());
    this.add(dimensionPanel, MessageFactory.getMessages().selections());
    this.add(reportPanel, MessageFactory.getMessages().report());
    
    selectTab(0);
    this.addTabListener(connectionPanel);
  }

}
