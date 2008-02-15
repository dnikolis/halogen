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

import org.pentaho.halogen.client.Messages;
import org.pentaho.halogen.client.services.Olap4JServiceAsync;
import org.pentaho.halogen.client.util.OlapData;
import org.pentaho.halogen.client.widgets.OlapTable;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author wseyler
 *
 */
public class ReportPanel extends FlexTable {

  Olap4JServiceAsync olap4JService;
  String guid;
  Messages messages;

  TextArea mdxText;
  Button executeMDXBtn;
  Button executeQueryBtn;
  Button swapAxisBtn;
  CheckBox toggleParentMembers;
  OlapTable olapTable;
  
  public ReportPanel(Olap4JServiceAsync olap4JService, String guid, Messages messages) {
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
    this.setText(0, 0, messages.mdx_query());
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
    this.setWidget(0, 1, mdxText);
    olapTable = new OlapTable(messages);
    getFlexCellFormatter().setColSpan(1, 0, 4);
    this.setWidget(1, 0, olapTable);
    executeMDXBtn = new Button(messages.execute_mdx(), new ClickListener(){

      public void onClick(Widget sender) {
        olap4JService.executeMDXStr(mdxText.getText(), guid, new AsyncCallback() {

          public void onSuccess(Object result) {
            olapTable.setData((OlapData)result);
          }
          
          public void onFailure(Throwable caught) {
            Window.alert(messages.no_server_data(caught.toString()));
          }

        });
      }
      
    });
    this.setWidget(2, 0, executeMDXBtn);
    executeQueryBtn = new Button(messages.execute_query(), new ClickListener() {

      public void onClick(Widget sender) {
        olap4JService.executeQuery(guid, new AsyncCallback() {

          public void onSuccess(Object result) {
            olapTable.setData((OlapData)result);
          }
          
          public void onFailure(Throwable caught) {
            Window.alert(messages.no_server_data(caught.toString()));      
          }

        });
        
      }
      
    });
    this.setWidget(2, 1, executeQueryBtn);
    
    swapAxisBtn = new Button(messages.swap_axis(), new ClickListener() {

      public void onClick(Widget sender) {
        olap4JService.swapAxis(guid, new AsyncCallback() {

          public void onFailure(Throwable caught) {
            Window.alert(messages.no_server_data(caught.toString()));   
          }

          public void onSuccess(Object result) {
            olapTable.setData((OlapData) result);
          }
          
        });
      }
      
    });
    this.setWidget(2, 2, swapAxisBtn);
    
    toggleParentMembers = new CheckBox(messages.toggle_parents());
    toggleParentMembers.setChecked(olapTable.isShowParentMembers());
    toggleParentMembers.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        boolean checked = ((CheckBox)sender).isChecked();
        olapTable.setShowParentMembers(checked);
      }
      
    });
    this.setWidget(2, 3, toggleParentMembers);
  }
}