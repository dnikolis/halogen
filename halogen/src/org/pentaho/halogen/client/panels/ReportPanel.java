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

import org.pentaho.halogen.client.listeners.ConnectionListener;
import org.pentaho.halogen.client.util.GuidFactory;
import org.pentaho.halogen.client.util.MessageFactory;
import org.pentaho.halogen.client.util.OlapData;
import org.pentaho.halogen.client.util.ServiceFactory;
import org.pentaho.halogen.client.widgets.OlapTable;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author wseyler
 *
 */
public class ReportPanel extends DockPanel implements ConnectionListener {

  TextArea mdxText;
  Button executeMDXBtn;
  Button executeQueryBtn;
  Button swapAxisBtn;
  CheckBox toggleParentMembers;
  OlapTable olapTable;
  Image chart;
  FlexTable content;
  boolean showGrid = true, showChart = true;
  
  public ReportPanel() {
    super();

    init();
  }

  /**
   * 
   */
  private void init() {
  	content = new FlexTable(); 	
    content.setText(0, 0, MessageFactory.getInstance().mdx_query());
    mdxText = new TextArea();
    mdxText.setWidth("300px"); //$NON-NLS-1$
    mdxText.setHeight("100px"); //$NON-NLS-1$
//    mdxText.setText("SELECT {[Measures].[Unit Sales], [Measures].[Store Sales]} ON COLUMNS, " +
//                    "CrossJoin( " +
//                      "{[Gender].Members}, " +
//                      "{[Product].[Food], [Product].[Drink]}) ON ROWS " +
//                    "FROM [Sales]");
    mdxText.setText("WITH MEMBER [Measures].[Profit] AS " + //$NON-NLS-1$
                    "'([Measures].[Store Sales] - [Measures].[Store Cost])', " + //$NON-NLS-1$
                    "FORMAT_STRING = Iif([Measures].[Profit] < 100000, '|#|style=green', '|#|style=red')" + //$NON-NLS-1$
                    "SELECT {[Measures].[Store Sales], [Measures].[Profit]} ON COLUMNS, " + //$NON-NLS-1$
                    "{[Product].CurrentMember.Children} ON ROWS " + //$NON-NLS-1$
                    "FROM [Sales]" ); //$NON-NLS-1$
    content.setWidget(0, 1, mdxText);
    olapTable = new OlapTable(MessageFactory.getInstance());
    content.getFlexCellFormatter().setColSpan(1, 0, 4);
    content.setWidget(1, 0, olapTable);
    chart = new Image();
    content.getFlexCellFormatter().setColSpan(2, 0, 4);
    content.setWidget(2, 0, chart);
    
    // Create the execute MDX button
    executeMDXBtn = new Button(MessageFactory.getInstance().execute_mdx(), new ClickListener(){
      public void onClick(Widget sender) {
      	doExecuteMDX();
	    }     
    });
    content.setWidget(3, 0, executeMDXBtn);
    
    // Create the execute query model button
    executeQueryBtn = new Button(MessageFactory.getInstance().execute_query(), new ClickListener() {
      public void onClick(Widget sender) {
      	doExecuteQueryModel();      
      }     
    });
    content.setWidget(3, 1, executeQueryBtn);
    
    // Create the swap axis button
    swapAxisBtn = new Button(MessageFactory.getInstance().swap_axis(), new ClickListener() {
      public void onClick(Widget sender) {
        doSwapAxis();
      }      
    });
    content.setWidget(3, 2, swapAxisBtn);
    
    // Create toggle for the parents
    toggleParentMembers = new CheckBox(MessageFactory.getInstance().toggle_parents());
    toggleParentMembers.setChecked(olapTable.isShowParentMembers());
    toggleParentMembers.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        boolean checked = ((CheckBox)sender).isChecked();
        olapTable.setShowParentMembers(checked);
      }      
    });
    content.setWidget(3, 3, toggleParentMembers);
    this.add(content, DockPanel.CENTER);
    this.add(new ReportMenuBar(), DockPanel.NORTH);
  }
  
  public void doExecuteMDX() {
    ServiceFactory.getInstance().executeMDXStr(mdxText.getText(), GuidFactory.getGuid(), new AsyncCallback() {

      public void onSuccess(Object result1) {
        olapTable.setData((OlapData)result1);
        ServiceFactory.getInstance().createChart((OlapData)result1, new AsyncCallback() {

          public void onFailure(Throwable caught) {
            Window.alert(MessageFactory.getInstance().no_server_data(caught.toString()));
          }

          public void onSuccess(Object result2) {
            chart.setUrl(result2.toString());
          }
          
        });
      }
      
      public void onFailure(Throwable caught) {
        Window.alert(MessageFactory.getInstance().no_server_data(caught.toString()));
      }

    });
  }
  
  public void doExecuteQueryModel() {
    ServiceFactory.getInstance().executeQuery(GuidFactory.getGuid(), new AsyncCallback() {

      public void onSuccess(Object result1) {
        olapTable.setData((OlapData)result1);
        ServiceFactory.getInstance().createChart((OlapData)result1, new AsyncCallback() {

          public void onFailure(Throwable caught) {
            Window.alert(MessageFactory.getInstance().no_server_data(caught.toString()));
          }

          public void onSuccess(Object result2) {
            chart.setUrl(result2.toString());
          }
          
        });
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
        ServiceFactory.getInstance().createChart((OlapData)result1, new AsyncCallback() {

          public void onFailure(Throwable caught) {
            Window.alert(MessageFactory.getInstance().no_server_data(caught.toString()));
          }

          public void onSuccess(Object result2) {
            chart.setUrl(result2.toString());
          }
          
        });
      }
      
    });
  	
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
			
			// Create a the top level menus
			MenuBar reportMenu = new MenuBar(true);
			reportMenu.addItem("Execute Query", new Command() {
				public void execute() {
					doExecuteQueryModel();
				}
			});
			reportMenu.addItem("Execute MDX", new Command() {
				public void execute() {
					doExecuteMDX();
				}				
			});
			MenuBar gridMenu = new MenuBar(true);
			MenuBar chartMenu = new MenuBar(true);
			
			this.addItem("Report", reportMenu);
			this.addItem("Grid", gridMenu);
			this.addItem("Chart", chartMenu);
		}
  	
  }
}