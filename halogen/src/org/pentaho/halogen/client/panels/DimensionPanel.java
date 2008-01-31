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


import java.util.ArrayList;
import java.util.List;

import org.pentaho.halogen.client.Messages;
import org.pentaho.halogen.client.listeners.ConnectionListener;
import org.pentaho.halogen.client.services.Olap4JServiceAsync;
import org.pentaho.halogen.client.util.StringTree;
import org.pentaho.halogen.client.widgets.MemberSelectionLabel;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author wseyler
 *
 */
public class DimensionPanel extends FlexTable implements ConnectionListener {
  private static final String AXIS_NONE = "none"; //$NON-NLS-1$
  private static final String AXIS_UNUSED = "UNUSED"; //$NON-NLS-1$
  private static final String AXIS_FILTER = "FILTER"; //$NON-NLS-1$
  private static final String AXIS_COLUMNS = "COLUMNS"; //$NON-NLS-1$
  private static final String AXIS_ROWS = "ROWS"; //$NON-NLS-1$
  private static final String AXIS_PAGES = "PAGES"; //$NON-NLS-1$
  private static final String AXIS_CHAPTERS = "CHAPTERS"; //$NON-NLS-1$
  private static final String AXIS_SECTIONS = "SECTIONS"; //$NON-NLS-1$

  Olap4JServiceAsync olap4JService;
  String guid;
  Messages messages;
  Button moveToRowButton;
  Button moveToColButton;
  Button moveToFilterButton;
  ListBox dimensionsList;
  ListBox cubeListBox;
  FlexTable rowDimensions;
  FlexTable colDimensions;
  FlexTable filterDimensions;
  SelectionModePopup selectionModePopup;
  
  ClickListener memberClickListener;
  
  public DimensionPanel(Olap4JServiceAsync olap4JService, String guid, Messages messages) {
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
    selectionModePopup = new SelectionModePopup(olap4JService, guid, messages);
    cubeListBox = new ListBox();
    
    cubeListBox.addChangeListener(new ChangeListener() {
      public void onChange(Widget sender) {
        olap4JService.setCube(cubeListBox.getItemText(cubeListBox.getSelectedIndex()), guid, new AsyncCallback() {
          public void onSuccess(Object result) {
            populateDimensions();
          }         
          public void onFailure(Throwable caught) {}
        });
      } 
    });
    cubeListBox.setVisibleItemCount(1); // Make this a drop down list
    this.setText(0, 1, messages.select_cube());
    this.setWidget(0, 2, cubeListBox);

    // Set up the Dimensions List
    this.setText(1, 0, messages.dimensions());
    this.setText(1, 1, messages.row_dimensions());
    this.setText(1, 2, messages.column_dimensions());
    this.setText(1, 3, messages.filter_dimensions());
    
    dimensionsList = new ListBox();
    dimensionsList.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        updateMoveButtons();
      }     
    });
    dimensionsList.setWidth("150px"); //$NON-NLS-1$
    dimensionsList.setVisibleItemCount(14);
    this.setWidget(2, 0, dimensionsList);
    
    // Set up the Row Dimensions List
    rowDimensions = new FlexTable();
    ScrollPanel scroller = new ScrollPanel(rowDimensions);
    scroller.addStyleName("olap-scroller"); //$NON-NLS-1$
    scroller.setWidth("200px"); //$NON-NLS-1$
    this.setWidget(2, 1, scroller);

    // Set up the Column Dimensions List   
    colDimensions = new FlexTable();
    scroller = new ScrollPanel(colDimensions);
    scroller.addStyleName("olap-scroller"); //$NON-NLS-1$
    scroller.setWidth("200px"); //$NON-NLS-1$
    this.setWidget(2, 2, scroller);

    // Set up the Filter Dimensions List
    filterDimensions = new FlexTable();
    scroller = new ScrollPanel(filterDimensions);
    scroller.addStyleName("olap-scroller"); //$NON-NLS-1$
    scroller.setWidth("200px"); //$NON-NLS-1$
    this.setWidget(2, 3, scroller);

    // Set up the Move To Row Button
    moveToRowButton = new Button(messages.move_to_row());
    moveToRowButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        String dimName = dimensionsList.getValue(dimensionsList.getSelectedIndex());
        olap4JService.moveDimension(AXIS_ROWS, dimName, guid, new AsyncCallback() {
          public void onSuccess(Object result) {
            boolean success = ((Boolean)result).booleanValue();
            if (success) {
              List axis = new ArrayList();
              axis.add(AXIS_NONE);
              axis.add(AXIS_ROWS);
              populateDimensions(axis);
            }
          }          
         public void onFailure(Throwable caught) {
            // TODO Auto-generated method stub           
          }

        });
      }     
    });
    this.setWidget(3, 1, moveToRowButton);
    moveToRowButton.setEnabled(false);
    
    // Set up the Move To Column Button
    moveToColButton = new Button(messages.move_to_column());
    moveToColButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        String dimName = dimensionsList.getValue(dimensionsList.getSelectedIndex());
        olap4JService.moveDimension(AXIS_COLUMNS, dimName, guid, new AsyncCallback() {
          public void onSuccess(Object result) {
            boolean success = ((Boolean)result).booleanValue();
            if (success) {
              List axis = new ArrayList();
              axis.add(AXIS_NONE);
              axis.add(AXIS_COLUMNS);
              populateDimensions(axis);
            }
          }          
         public void onFailure(Throwable caught) {
            // TODO Auto-generated method stub           
          }

        });
      }     
    });
    this.setWidget(3, 2, moveToColButton);
    moveToColButton.setEnabled(false);
    
    moveToFilterButton = new Button(messages.move_to_filter());
    moveToFilterButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        String dimName = dimensionsList.getValue(dimensionsList.getSelectedIndex());
        olap4JService.moveDimension(AXIS_FILTER, dimName, guid, new AsyncCallback() {
          public void onSuccess(Object result) {
            boolean success = ((Boolean)result).booleanValue();
            if (success) {
              List axis = new ArrayList();
              axis.add(AXIS_NONE);
              axis.add(AXIS_FILTER);
              populateDimensions(axis);
            }
          }         
          public void onFailure(Throwable caught) {
            // TODO Auto-generated method stub           
          }
        });
      }      
    });
    this.setWidget(3, 3, moveToFilterButton);
    moveToFilterButton.setEnabled(false);
  }

  public void populateDimensions() {
    List axis = new ArrayList();
    axis.add(AXIS_NONE);
    axis.add(AXIS_ROWS);
    axis.add(AXIS_COLUMNS);
    axis.add(AXIS_FILTER);
    populateDimensions(axis);
  }
  
  /**
   * 
   */
  public void populateDimensions( List axis) {
    if (axis.contains(AXIS_NONE)) {
      olap4JService.getDimensions(AXIS_NONE, guid, new AsyncCallback() {
        public void onSuccess(Object result) {
          String[] dimStrs = (String[]) result;
          dimensionsList.clear();
          for (int i=0; i<dimStrs.length; i++) {
            dimensionsList.addItem(dimStrs[i]);
          }
          updateMoveButtons();
        }
        
        public void onFailure(Throwable caught) {
          // TODO Auto-generated method stub
          
        }
  
      });
    }
    
    if (axis.contains(AXIS_ROWS)) {
      olap4JService.getDimensions(AXIS_ROWS, guid, new AsyncCallback() {
  
        public void onSuccess(Object result) {
          String[] dimStrs = (String[]) result;
          rowDimensions.clear();
          for (int i=0; i<dimStrs.length; i++) {
            rowDimensions.setWidget(i, 0, getDimTree(dimStrs[i]));
          }
        }
        
        public void onFailure(Throwable caught) {
          // TODO Auto-generated method stub
          
        }
  
      });
    }
    
    if (axis.contains(AXIS_COLUMNS)) {
      olap4JService.getDimensions(AXIS_COLUMNS, guid, new AsyncCallback() {
  
        public void onSuccess(Object result) {
          String[] dimStrs = (String[]) result;
          colDimensions.clear();
          for (int i=0; i<dimStrs.length; i++) {
            colDimensions.setWidget(i, 0, getDimTree(dimStrs[i]));
          }
        }
        
        public void onFailure(Throwable caught) {
          // TODO Auto-generated method stub
          
        }
  
      });
    }
    
    if (axis.contains(AXIS_FILTER)) {
      olap4JService.getDimensions(AXIS_FILTER, guid, new AsyncCallback() {

        public void onSuccess(Object result) {
          String[] dimStrs = (String[]) result;
          filterDimensions.clear();
          for (int i=0; i<dimStrs.length; i++) {
            filterDimensions.setWidget(i, 0, getDimTree(dimStrs[i]));
          }
        }
        
        public void onFailure(Throwable caught) {
          // TODO Auto-generated method stub
          
        }     
      });
    }
  }
  
  protected Tree getDimTree(String dimName) {
    final Tree dimTree = new Tree();
    olap4JService.getMembers(dimName, guid, new AsyncCallback() {
      public void onSuccess(Object result) {
        StringTree memberTree = (StringTree) result;
        Label rootLabel = new Label(memberTree.getValue());
        TreeItem root = new TreeItem(rootLabel);
        for (int i=0; i<memberTree.getChildren().size(); i++) {
          root = createPathForMember(root, (StringTree)memberTree.getChildren().get(i));
        }
        dimTree.addItem(root);
      }
      
     public void onFailure(Throwable caught) {
        // TODO Auto-generated method stub
        
      }
    });

    return dimTree;
  }
  
  protected TreeItem createPathForMember(TreeItem parent, StringTree node ) {
    MemberSelectionLabel memberLabel = new MemberSelectionLabel(node.getValue());
    memberLabel.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        selectionModePopup.setPopupPosition(sender.getAbsoluteLeft(), sender.getAbsoluteTop());
        selectionModePopup.setSource(sender);
        selectionModePopup.show();
      }     
    });
    TreeItem childItem = new TreeItem(memberLabel);
    parent.addItem(childItem);
    for (int i=0; i<node.getChildren().size(); i++) {
      createPathForMember(childItem, (StringTree)node.getChildren().get(i));
    }
    return parent;
  }
      
  public void getCubes() {
    olap4JService.getCubes(guid, new AsyncCallback() {
      public void onSuccess(Object result1) {
        if (result1 != null) {
          cubeListBox.clear();
          String[] cubeNames = (String[]) result1;
          for (int i=0; i<cubeNames.length; i++) {
            cubeListBox.addItem(cubeNames[i]);
          }
        }
        olap4JService.setCube(cubeListBox.getItemText(cubeListBox.getSelectedIndex()), guid, new AsyncCallback() {
          public void onSuccess(Object result2) {
            populateDimensions();
          }         
          public void onFailure(Throwable caught) {}
        });
      }      
      public void onFailure(Throwable caught) {}
    });
  }

  public Olap4JServiceAsync getOlap4JService() {
    return olap4JService;
  }
 
  protected void updateMoveButtons() {
    moveToColButton.setEnabled(dimensionsList.getSelectedIndex() != -1);
    moveToRowButton.setEnabled(dimensionsList.getSelectedIndex() != -1);
    moveToFilterButton.setEnabled(dimensionsList.getSelectedIndex() != -1);
  }

  /* (non-Javadoc)
   * @see org.pentaho.halogen.client.listeners.ConnectionListener#onConnectionBroken(com.google.gwt.user.client.ui.Widget)
   */
  public void onConnectionBroken(Widget sender) {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.pentaho.halogen.client.listeners.ConnectionListener#onConnectionMade(com.google.gwt.user.client.ui.Widget)
   */
  public void onConnectionMade(Widget sender) {
    getCubes();
  }
  
}