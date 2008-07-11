package org.pentaho.halogen.client.util;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.allen_sauer.gwt.dnd.client.drop.DropController;
import com.allen_sauer.gwt.dnd.client.util.DOMUtil;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.halogen.client.widgets.MouseListBox;


/**
 * DragController for {@link DualListExample}.
 */
public class ListBoxDragController extends PickupDragController {
  DropController dropController;

  public ListBoxDragController(RootPanel rootPanel) {
    super(rootPanel, false);
    setBehaviorDragProxy(true);
    setBehaviorMultipleSelection(true);
  }

  @Override
  public void dragEnd() {
    // process drop first
    super.dragEnd();

    if (context.vetoException == null) {
      // remove original items
      MouseListBox currentMouseListBox = (MouseListBox) context.draggable.getParent().getParent();
      while (!context.selectedWidgets.isEmpty()) {
        Widget widget = (Widget) context.selectedWidgets.get(0);
        toggleSelection(widget);
        currentMouseListBox.remove(widget);
      }
    }
  }

  @Override
  public void previewDragStart() throws VetoDragException {
    super.previewDragStart();
    if (context.selectedWidgets.isEmpty()) {
      throw new VetoDragException();
    }
  }
  
  public void registerDropController (DropController dropController){
    this.dropController = dropController;
    super.registerDropController(dropController);
    
  }
  
  public void addDropController (DropController dropController){
    super.registerDropController(dropController);
  }
  
  public void dragStart () 
  {
	  	super.dragStart();
  }

  @Override
  public void setBehaviorDragProxy(boolean dragProxyEnabled) {
    if (!dragProxyEnabled) {
      throw new IllegalArgumentException();
    }
    super.setBehaviorDragProxy(dragProxyEnabled);
  }

  @Override
  public void toggleSelection(Widget draggable) {
    super.toggleSelection(draggable);
    MouseListBox currentMouseListBox = (MouseListBox) draggable.getParent().getParent();
    List<Widget> otherWidgets = new ArrayList<Widget>();
    List<Widget> selectedWidgets = context.selectedWidgets;
    for (Widget widget : selectedWidgets) {
      if (widget.getParent().getParent() != currentMouseListBox) {
        otherWidgets.add(widget);
      }
    }
    for (Widget widget : otherWidgets) {
      super.toggleSelection(widget);
    }
  }

  @Override
  protected Widget newDragProxy(DragContext context) {
    MouseListBox currentMouseListBox = (MouseListBox) context.draggable.getParent().getParent();
    MouseListBox proxyMouseListBox = new MouseListBox(context.selectedWidgets.size());
    proxyMouseListBox.setWidth(DOMUtil.getClientWidth(currentMouseListBox.getElement()) + "px");
    List<Widget> selectedWidgets = context.selectedWidgets;
    for (Widget widget : selectedWidgets) {
      HTML htmlClone = new HTML(DOM.getInnerHTML(widget.getElement()));
      proxyMouseListBox.add(htmlClone);
    }
    return proxyMouseListBox;
  }

  public List<Widget> getSelectedWidgets(MouseListBox mouseListBox) {
    ArrayList<Widget> widgetList = new ArrayList<Widget>();
    List<Widget> selectedWidgets = context.selectedWidgets;
    for (Widget widget : selectedWidgets) {
      if (widget.getParent().getParent() == mouseListBox) {
        widgetList.add(widget);
      }
    }
    return widgetList;
  }
}
