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
 * @created Jan 8, 2008 
 * @author wseyler
 */


package org.pentaho.halogen.client.panels;

import org.pentaho.halogen.client.Messages;
import org.pentaho.halogen.client.services.Olap4JServiceAsync;
import org.pentaho.halogen.client.widgets.MemberSelectionLabel;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author wseyler
 *
 */
public class SelectionModePopup extends PopupPanel {

  public static final int MEMBER = 0;
  public static final int CHILDREN = 1;
  public static final int INCLUDE_CHILDREN = 2;
  public static final int SIBLINGS = 3;

  Olap4JServiceAsync olap4JService;
  String guid;
  Messages messages;
  
  MenuBar menuBar;
  
  Integer selectionValue = new Integer(0); // Member
  Widget source;

  public SelectionModePopup(Olap4JServiceAsync olap4JService, String guid, Messages messages) {
    super(false, true);
    
    this.olap4JService = olap4JService;
    this.guid = guid;
    this.messages = messages;
        
    init();
  }

  /**
   * 
   */
  protected void init() {
    menuBar = new MenuBar(true);
    menuBar.setAutoOpen(true);
    menuBar.addItem(new MenuItem(messages.member(), new SelectionModeCommand(MEMBER)));
    menuBar.addItem(new MenuItem(messages.children(), new SelectionModeCommand(CHILDREN)));
    menuBar.addItem(new MenuItem(messages.include_children(), new SelectionModeCommand(INCLUDE_CHILDREN)));
    menuBar.addItem(new MenuItem(messages.siblings(), new SelectionModeCommand(SIBLINGS)));
    
    this.setWidget(menuBar);
  }

  public Integer getSelectionValue() {
    return selectionValue;
  }

  public void setSelectionValue(Integer selectionValue) {
    this.selectionValue = selectionValue;
  }

  public Widget getSource() {
    return source;
  }

  public void setSource(Widget source) {
    this.source = source;
  }



  public Olap4JServiceAsync getOlap4JService() {
    return olap4JService;
  }

  public void setOlap4JService(Olap4JServiceAsync olap4JService) {
    this.olap4JService = olap4JService;
  } 

  /**
   * @param targetLabel
   * @return
   */
  protected String getDimensionName(MemberSelectionLabel targetLabel) {
    Tree tree = (Tree) targetLabel.getParent();
    TreeItem rootItem = tree.getItem(0);
    Label rootLabel = (Label) rootItem.getWidget();
    return rootLabel.getText();
  }


  /**
   * @author wseyler
   *
   */
  public class SelectionModeCommand implements Command {
    protected int selectionMode = -1;
    /**
     * @param member
     */
    public SelectionModeCommand(int selectionMode) {
      this.selectionMode = selectionMode;
    }
    /* (non-Javadoc)
     * @see com.google.gwt.user.client.Command#execute()
     */
    public void execute() {
      final MemberSelectionLabel targetLabel = (MemberSelectionLabel)getSource();
      String dimName = getDimensionName(targetLabel);
      getOlap4JService().createSelection(dimName, targetLabel.getText(), new Integer(selectionMode), guid, new AsyncCallback() {
        public void onFailure(Throwable caught) {
          Window.alert(messages.no_selection_set(caught.getLocalizedMessage()));
        }
        public void onSuccess(Object result) {
          targetLabel.setSelectionMode(selectionMode);
        }         
      });
      SelectionModePopup.this.hide();
    }
  }
}
