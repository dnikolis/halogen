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


package org.pentaho.halogen.client.panels;

import org.pentaho.halogen.client.HalogenTabPanel;
import org.pentaho.halogen.client.dialog.ChartDialog;
import org.pentaho.halogen.client.listeners.ChartPrefsListener;
import org.pentaho.halogen.client.listeners.ConnectionListener;
import org.pentaho.halogen.client.util.ChartPrefs;
import org.pentaho.halogen.client.util.GuidFactory;
import org.pentaho.halogen.client.util.LocationSelectionUtils;
import org.pentaho.halogen.client.util.MessageFactory;
import org.pentaho.halogen.client.util.OlapData;
import org.pentaho.halogen.client.util.ServiceFactory;
import org.pentaho.halogen.client.widgets.OlapTable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author wseyler
 *
 */
public class ReportPanel extends DockPanel implements ConnectionListener, ChartPrefsListener {

  OlapTable olapTable;
  Image chart;
  DockPanel content;
  ChartPrefs chartPrefs;
  boolean showGrid = true, showChart = true;
  
  MenuItem showParentsMenuItem;
  MenuItem groupHeadersMenuItem;
  
  public ReportPanel() {
    super();

    init();
  }

  /**
   * 
   */
  private void init() {
    chartPrefs = new ChartPrefs();
  	content = new DockPanel(); 	
    olapTable = new OlapTable(MessageFactory.getInstance());
    content.add(olapTable, DockPanel.CENTER);
    chart = new Image();
    content.add(chart, DockPanel.SOUTH);
    
    this.add(content, DockPanel.CENTER);
    this.add(new ReportMenuBar(), DockPanel.NORTH);   
  }
  
  public void doExecuteMDX() {
  	HalogenTabPanel tabPanel = (HalogenTabPanel)getParent().getParent().getParent();
  	String mdxText = tabPanel.getDimensionPanel().getMDXQueryText();
  	if (mdxText != null && mdxText.length() > 0) {
	    ServiceFactory.getInstance().executeMDXStr(mdxText, GuidFactory.getGuid(), new AsyncCallback() {
	
	      public void onSuccess(Object result1) {
	        olapTable.setData((OlapData)result1);
	        doCreateChart();
	      }
	      
	      public void onFailure(Throwable caught) {
	        Window.alert(MessageFactory.getInstance().no_server_data(caught.toString()));
	      }
	
	    });
  	}
  }
   
  public void doExecuteQueryModel() {
    ServiceFactory.getInstance().executeQuery(GuidFactory.getGuid(), new AsyncCallback() {

      public void onSuccess(Object result1) {
        olapTable.setData((OlapData)result1);
        doCreateChart();
      }
      
      public void onFailure(Throwable caught) {
        Window.alert(MessageFactory.getInstance().no_server_data(caught.toString()));      
      }

    });
  	
  }
  
  public void doSwapAxis() {
    ServiceFactory.getInstance().swapAxis(GuidFactory.getGuid(), new AsyncCallback() {

      public void onFailure(Throwable caught) {
        Window.alert(MessageFactory.getInstance().no_server_data(caught.toString()));   
      }

      public void onSuccess(Object result1) {
        olapTable.setData((OlapData) result1);
        doCreateChart();
      }
      
    });
  	
  }
  
  public void doCreateChart() {
    OlapData olapData = olapTable.getData();
    
    ServiceFactory.getInstance().createChart(olapData, chartPrefs, new AsyncCallback() {
      
      public void onFailure(Throwable caught) {
        Window.alert(MessageFactory.getInstance().no_server_data(caught.toString()));
      }

      public void onSuccess(Object result2) {
        String url = GWT.getModuleBaseURL() + "ChartServlet?guid=" + (String)result2;
        chart.setUrl(url);
      }
      
    });
  }


  public void doChartPrefs() {
    ChartDialog chartDialog = new ChartDialog(chartPrefs);
    chartDialog.addChartPrefsListener(this);
    chartDialog.show();
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.halogen.client.listeners.ConnectionListener#onConnectionBroken(com.google.gwt.user.client.ui.Widget)
   */
  public void onConnectionBroken(Widget sender) {
    olapTable.setData(null, true);
  }

  /* (non-Javadoc)
   * @see org.pentaho.halogen.client.listeners.ConnectionListener#onConnectionMade(com.google.gwt.user.client.ui.Widget)
   */
  public void onConnectionMade(Widget sender) {
    // no op    
  }
  
  public class ReportMenuBar extends MenuBar {

		public ReportMenuBar() {
			super();
			
			// Create the report menu
			MenuBar reportMenu = new MenuBar(true);
			reportMenu.addItem(MessageFactory.getInstance().execute_query(), new Command() {
				public void execute() {
					doExecuteQueryModel();
				}
			});
			reportMenu.addItem(MessageFactory.getInstance().execute_mdx(), new Command() {
				public void execute() {
					doExecuteMDX();
				}				
			});
			reportMenu.addItem("<hr>", true, new Command() {
				public void execute() {
					// noop for seperator
				}
			});
			reportMenu.addItem(MessageFactory.getInstance().swap_axis(), new Command() {
				public void execute() {
					doSwapAxis();
				}
			});
			
			// Create the grid menu
			MenuBar gridMenu = new MenuBar(true);
			showParentsMenuItem = new MenuItem(MessageFactory.getInstance().hide_parents(), new Command() {
				public void execute() {
					olapTable.setShowParentMembers(!olapTable.isShowParentMembers());
					if (olapTable.isShowParentMembers()) {
						showParentsMenuItem.setText(MessageFactory.getInstance().hide_parents());
					} else {
						showParentsMenuItem.setText(MessageFactory.getInstance().show_parents());
					}
				}
			});
			gridMenu.addItem(showParentsMenuItem);
			
			groupHeadersMenuItem = new MenuItem(MessageFactory.getInstance().ungroup_headers(), new Command() {
				public void execute() {
					olapTable.setGroupHeaders(!olapTable.isGroupHeaders());
					if (olapTable.isGroupHeaders()) {
						groupHeadersMenuItem.setText(MessageFactory.getInstance().ungroup_headers());
					} else {
						groupHeadersMenuItem.setText(MessageFactory.getInstance().group_headers());
					}
				}
			});
			gridMenu.addItem(groupHeadersMenuItem);

			// Create the chart menu
			MenuBar chartMenu = new MenuBar(true);
			chartMenu.addItem("Chart Preferences...", new Command() {
        public void execute() {
          doChartPrefs();
        }
			});
			
			this.addItem("Report", reportMenu);
			this.addItem("Grid", gridMenu);
			this.addItem("Chart", chartMenu);
		}
  	
  }

  /* (non-Javadoc)
   * @see org.pentaho.halogen.client.listeners.ChartPrefsListener#chartPrefsChanged(org.pentaho.halogen.client.util.ChartPrefs)
   */
  public void chartPrefsChanged(ChartPrefs newChartPrefs) {
    if (!chartPrefs.equals(newChartPrefs)) {
      chartPrefs = newChartPrefs;
      doCreateChart();
      if (chartPrefs.isVisible()) {
        content.add(chart, LocationSelectionUtils.selectorToLocation(chartPrefs.getLocation()));
      }
    }
  }
}