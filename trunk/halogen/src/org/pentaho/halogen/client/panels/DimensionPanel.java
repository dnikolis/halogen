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

import org.pentaho.halogen.client.listeners.ConnectionListener;
import org.pentaho.halogen.client.util.DraggableTree;
import org.pentaho.halogen.client.util.GuidFactory;
import org.pentaho.halogen.client.util.ListBoxDragController;
import org.pentaho.halogen.client.util.ListBoxDropController;
import org.pentaho.halogen.client.util.MessageFactory;
import org.pentaho.halogen.client.util.ServiceFactory;
import org.pentaho.halogen.client.util.StringTree;
import org.pentaho.halogen.client.widgets.MemberSelectionLabel;
import org.pentaho.halogen.client.widgets.MouseListBox;

import com.allen_sauer.gwt.dnd.client.drop.DropController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author wseyler
 *
 */
public class DimensionPanel extends FlexTable implements ConnectionListener {
  public static final String AXIS_NONE = "none"; //$NON-NLS-1$
  public static final String AXIS_UNUSED = "UNUSED"; //$NON-NLS-1$
  public static final String AXIS_FILTER = "FILTER"; //$NON-NLS-1$
  public static final String AXIS_COLUMNS = "COLUMNS"; //$NON-NLS-1$
  public static final String AXIS_ROWS = "ROWS"; //$NON-NLS-1$
  public static final String AXIS_PAGES = "PAGES"; //$NON-NLS-1$
  public static final String AXIS_CHAPTERS = "CHAPTERS"; //$NON-NLS-1$
  public static final String AXIS_SECTIONS = "SECTIONS"; //$NON-NLS-1$
  
  Button moveToRowButton;
  Button moveToColButton;
  Button moveToFilterButton;
  ListBox cubeListBox;
  TextArea mdxText;
  
  SelectionModePopup selectionModePopup;
  
  ClickListener memberClickListener;
  
  //PickupDragController dragController;
  
  ListBoxDragController dragController;
  DropController dropController;
  ListBoxDropController rightDropController;
  
  MouseListBox dimensionsList, rowDimensions, colDimensions, filterDimensions ;

  public DimensionPanel() {
    super();

    init();
  }

  /**
   * 
   */
  private void init() {
    selectionModePopup = new SelectionModePopup(GuidFactory.getGuid());
    cubeListBox = new ListBox();
    
    cubeListBox.addChangeListener(new ChangeListener() {
      public void onChange(Widget sender) {
        ServiceFactory.getInstance().setCube(cubeListBox.getItemText(cubeListBox.getSelectedIndex()), GuidFactory.getGuid(), new AsyncCallback() {
          public void onSuccess(Object result) {
            populateDimensions();
          }         
          public void onFailure(Throwable caught) {}
        });
      } 
    });
    cubeListBox.setVisibleItemCount(1); // Make this a drop down list
    this.setText(0, 1, MessageFactory.getInstance().select_cube());
    this.setWidget(0, 2, cubeListBox);

    this.setText(1, 0, MessageFactory.getInstance().dimensions());
    this.setText(1, 1, MessageFactory.getInstance().row_dimensions());
    this.setText(1, 2, MessageFactory.getInstance().column_dimensions());
    this.setText(1, 3, MessageFactory.getInstance().filter_dimensions());
    
//    dimensionsList = new ListBox();
//    dimensionsList.addClickListener(new ClickListener() {
//      public void onClick(Widget sender) {
//        updateMoveButtons();
//      }     
//    });
//    dimensionsList.setWidth("150px"); //$NON-NLS-1$
//    dimensionsList.setVisibleItemCount(14);
    initDragNDropComponents(2, 0);
    //this.setWidget(2, 0, dimensionsList);
    
    // Set up the Move To Row Button
    moveToRowButton = new Button(MessageFactory.getInstance().move_to_row());
    moveToRowButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {        
        for (Widget widget : dragController.getSelectedWidgets(dimensionsList)){
          String title = ((Label)widget).getText();
          moveDimensionThroughDropController(title, AXIS_ROWS);
        }
        moveItems(dimensionsList, rowDimensions);        
      }     
    });
    this.setWidget(3, 1, moveToRowButton);
    moveToRowButton.setEnabled(true);
//    
//    // Set up the Move To Column Button
    moveToColButton = new Button(MessageFactory.getInstance().move_to_column());
    moveToColButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {        
        for (Widget widget : dragController.getSelectedWidgets(dimensionsList)){
          String title = ((Label)widget).getText();
          moveDimensionThroughDropController(title, AXIS_COLUMNS);
        }
        moveItems(dimensionsList, colDimensions);         
      }     
    });
    this.setWidget(3, 2, moveToColButton);
    moveToColButton.setEnabled(true);
    
//  // Set up the Move To Filter Button
    moveToFilterButton = new Button(MessageFactory.getInstance().move_to_filter());
    moveToFilterButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {        
        for (Widget widget : dragController.getSelectedWidgets(dimensionsList)){
          String title = ((Label)widget).getText();
          moveDimensionThroughDropController(title, AXIS_FILTER);
        }
        moveItems(dimensionsList, filterDimensions);         
      }     
    });
    this.setWidget(3, 3, moveToFilterButton);
    moveToFilterButton.setEnabled(true);


    this.setText(4, 0, MessageFactory.getInstance().mdx_query());
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
    this.setWidget(5, 0, mdxText);
    this.getFlexCellFormatter().setColSpan(5, 0, 2);
  }

  /**
   * 
   */
  private void initDragNDropComponents(int row, int col)
  {
	  dragController = new ListBoxDragController( RootPanel.get() );
	  
	  dragController.setBehaviorDragStartSensitivity(1);
	  dimensionsList = new MouseListBox(dragController, 10);
	  dimensionsList.setStylePrimaryName("panelBox");
	  rowDimensions = new MouseListBox(5);
	  rowDimensions.setStylePrimaryName("panelBox");
	  colDimensions = new MouseListBox(5);
	  colDimensions.setStylePrimaryName("panelBox");
	  filterDimensions = new MouseListBox(5);
	  filterDimensions.setStylePrimaryName("panelBox");
	  
	  dimensionsList.setHeight("50px");
	  rowDimensions.setHeight("50px");
	  colDimensions.setHeight("50px");
	  filterDimensions.setHeight("50px");
	  
	  dimensionsList.setWidth("140px");
	  rowDimensions.setWidth("100px");
	  colDimensions.setWidth("100px");
	  filterDimensions.setWidth("100px");
	  	    
	  //ListBoxDropController leftDropController = new ListBoxDropController(list1);
	  rightDropController = new ListBoxDropController(rowDimensions, this, AXIS_ROWS);
	  //dragController.registerDropController(leftDropController);
	  dragController.registerDropController(rightDropController);
	  
	  dropController = new ListBoxDropController(colDimensions, this, AXIS_COLUMNS);
	  dragController.addDropController(dropController);
	  
	  dropController = new ListBoxDropController(filterDimensions, this, AXIS_FILTER);
	  dragController.addDropController(dropController);
	  
	  ScrollPanel scroller = new ScrollPanel(dimensionsList);
    scroller.addStyleName("olap-scroller"); //$NON-NLS-1$
    scroller.setWidth("150px"); //$NON-NLS-1$
    scroller.setAlwaysShowScrollBars(false);
    this.setWidget(row, col++, scroller);
	  
    scroller = new ScrollPanel(rowDimensions);
    scroller.addStyleName("olap-scroller"); //$NON-NLS-1$
    scroller.setWidth("200px"); //$NON-NLS-1$
    scroller.setAlwaysShowScrollBars(false);
    this.setWidget(row, col++, scroller);
    
    scroller = new ScrollPanel(colDimensions);
    scroller.addStyleName("olap-scroller"); //$NON-NLS-1$
    scroller.setWidth("200px"); //$NON-NLS-1$
    scroller.setAlwaysShowScrollBars(false);
    this.setWidget(row, col++, scroller);

    scroller = new ScrollPanel(filterDimensions);
    scroller.addStyleName("olap-scroller"); //$NON-NLS-1$
    scroller.setWidth("200px"); //$NON-NLS-1$
    scroller.setAlwaysShowScrollBars(false);
    this.setWidget(row, col++, scroller);

  }
  
  public void addItemToMouseListBox (MouseListBox mouseList, Widget item) {
    if (mouseList.getCapacitySize() <= mouseList.getWidgetCount()){
      mouseList.increaseGridSize();
    }
    mouseList.add(item);
  }
  
  protected void moveItems(MouseListBox from, MouseListBox to) {
    List<Widget> widgetList = dragController.getSelectedWidgets(from);
    for (Widget widget : widgetList) {
      // TODO let widget.removeFromParent() take care of from.remove()
      from.remove(widget);
      to.add(widget);      
    }
  }
  
  public void moveDimensionThroughDropController (String dim, String axis){
    if (rightDropController != null)
      rightDropController.moveDimension(dim, axis);      
  }
  
  public void populateDimensions() {
	List axis = new ArrayList();
	axis.add(AXIS_NONE);
	axis.add(AXIS_ROWS);
	axis.add(AXIS_COLUMNS);
	axis.add(AXIS_FILTER);
	populateDimensions(axis);
}
	  

  public void populateDimensions( List axis) {
    if (axis.contains(AXIS_NONE)) {
      ServiceFactory.getInstance().getDimensions(AXIS_NONE, GuidFactory.getGuid(), new AsyncCallback() {
        public void onSuccess(Object result) {
          String[] dimStrs = (String[]) result;
//          dimensionsList.clear();
          
          ArrayList<Widget> widgetsList = dimensionsList.widgetList();
          for (Widget widget: widgetsList) 
            dimensionsList.remove(widget);
          
          for (int i=0; i<dimStrs.length; i++) {
            //dimensionsList.addItem(dimStrs[i]);
            Label label = new Label(dimStrs[i]);
            label.addClickListener(new ClickListener() {

              public void onClick(Widget arg0) {
                updateMoveButtons();
              }
              
            });
            addItemToMouseListBox (dimensionsList, label);
          }
          updateMoveButtons();
        }
        
        public void onFailure(Throwable caught) {
          // TODO Auto-generated method stub
          
        }
  
      });
    }
    
    if (axis.contains(AXIS_ROWS)) {
      ServiceFactory.getInstance().getDimensions(AXIS_ROWS, GuidFactory.getGuid(), new AsyncCallback() {
  
        public void onSuccess(Object result) {
          String[] dimStrs = (String[]) result;          
          
          ArrayList<Widget> widgetsList = rowDimensions.widgetList();
          for (Widget widget: widgetsList) 
            if (widget instanceof Label)
              rowDimensions.remove(widget);
          
          boolean breakOut;
          for (int i=0; i<dimStrs.length; i++) {
            breakOut = false;
            for (Widget widget: widgetsList) // if the tree is already showed
              if (widget instanceof Tree)
                if (dimStrs[i].equals( ((Tree)widget).getItem(0).getText()))
                  breakOut = true;
              
            if (breakOut == true) continue;
            DraggableTree tree = getDimTree(dimStrs[i]);
            addItemToMouseListBox (rowDimensions, tree);
          }
        }
        
        public void onFailure(Throwable caught) {
          // TODO Auto-generated method stub
        }
      });
    }
    
    if (axis.contains(AXIS_COLUMNS)) {
      ServiceFactory.getInstance().getDimensions(AXIS_COLUMNS, GuidFactory.getGuid(), new AsyncCallback() {
  
        public void onSuccess(Object result) {
          String[] dimStrs = (String[]) result;
          
          ArrayList<Widget> widgetsList = colDimensions.widgetList();
          for (Widget widget: widgetsList) 
            if (widget instanceof Label)
              colDimensions.remove(widget);

          boolean breakOut;
          for (int i=0; i<dimStrs.length; i++) {
            breakOut = false;
            for (Widget widget: widgetsList) // if the tree is already showed
              if (widget instanceof Tree)
                if (dimStrs[i].equals( ((Tree)widget).getItem(0).getText()))
                  breakOut = true;
              
            if (breakOut == true) continue;
            DraggableTree tree = getDimTree(dimStrs[i]);
            addItemToMouseListBox (colDimensions, tree);
          }
        }
        public void onFailure(Throwable caught) {
          // TODO Auto-generated method stub
        }
      });
    }
    
    if (axis.contains(AXIS_FILTER)) {
      ServiceFactory.getInstance().getDimensions(AXIS_FILTER, GuidFactory.getGuid(), new AsyncCallback() {

        public void onSuccess(Object result) {
          String[] dimStrs = (String[]) result;
          
          ArrayList<Widget> widgetsList = filterDimensions.widgetList();
          for (Widget widget: widgetsList) 
            if (widget instanceof Label)
              filterDimensions.remove(widget);

          boolean breakOut;
          for (int i=0; i<dimStrs.length; i++) {
            breakOut = false;
            for (Widget widget: widgetsList) // if the tree is already showed
              if (widget instanceof Tree)
                if (dimStrs[i].equals( ((Tree)widget).getItem(0).getText()))
                  breakOut = true;
              
            if (breakOut == true) continue;
            DraggableTree tree = getDimTree(dimStrs[i]);
            addItemToMouseListBox (filterDimensions, tree);
          }
        }
        
        public void onFailure(Throwable caught) {
          // TODO Auto-generated method stub
          
        }     
      });
    }
    updateMoveButtons();
  }
  
  protected DraggableTree getDimTree(String dimName) {
    final DraggableTree dimTree = new DraggableTree();
    ServiceFactory.getInstance().getMembers(dimName, GuidFactory.getGuid(), new AsyncCallback() {
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
    memberLabel.setTreeItem(childItem);
    parent.addItem(childItem);
    for (int i=0; i<node.getChildren().size(); i++) {
      createPathForMember(childItem, (StringTree)node.getChildren().get(i));
    }
    return parent;
  }
      
  public void getCubes() {
    ServiceFactory.getInstance().getCubes(GuidFactory.getGuid(), new AsyncCallback() {
      public void onSuccess(Object result1) {
        if (result1 != null) {
          cubeListBox.clear();
          String[] cubeNames = (String[]) result1;
          for (int i=0; i<cubeNames.length; i++) {
            cubeListBox.addItem(cubeNames[i]);
          }
        }
        ServiceFactory.getInstance().setCube(cubeListBox.getItemText(cubeListBox.getSelectedIndex()), GuidFactory.getGuid(), new AsyncCallback() {
          public void onSuccess(Object result2) {
            populateDimensions();
          }         
          public void onFailure(Throwable caught) {}
        });
      }      
      public void onFailure(Throwable caught) {}
    });
  }

  protected void updateMoveButtons() {
    boolean enabled = dragController.getSelectedWidgets(dimensionsList).size() > 0 ? true : false;      
    moveToColButton.setEnabled(enabled);
    moveToRowButton.setEnabled(enabled);
    moveToFilterButton.setEnabled(enabled);
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
  
  public String getMDXQueryText() {
  	return mdxText.getText();
  }
}