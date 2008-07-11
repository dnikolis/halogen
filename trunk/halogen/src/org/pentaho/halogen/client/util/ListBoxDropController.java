package org.pentaho.halogen.client.util;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.halogen.client.panels.DimensionPanel;
import org.pentaho.halogen.client.widgets.MouseListBox;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.allen_sauer.gwt.dnd.client.drop.AbstractDropController;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * DropController for {@link DualListExample}.
 */
public class ListBoxDropController extends AbstractDropController {

  private MouseListBox mouseListBox;
  DimensionPanel dimensionPanel;
  private String axis;

  public ListBoxDropController(MouseListBox mouseListBox, DimensionPanel panel, String axis) {
    super(mouseListBox);
    this.mouseListBox = mouseListBox;
    if (panel != null)
      this.dimensionPanel = panel;
    this.axis = axis;
  }

  public void moveDimension(String dim, String axis){
    final String finalAxis = axis;
    if (dimensionPanel == null)
      return;
    ServiceFactory.getInstance().moveDimension(axis, dim, GuidFactory.getGuid(), new AsyncCallback() {
      public void onSuccess(Object result) {
        boolean success = ((Boolean)result).booleanValue();
      if (success) {
        List axisList = new ArrayList();
        axisList.add(DimensionPanel.AXIS_NONE);
        axisList.add(finalAxis);
        dimensionPanel.populateDimensions(axisList);
      }
      }

      public void onFailure(Throwable arg0) {
        // TODO Auto-generated method stub
        
      }
    });  
  }
  
  
  @Override
  public void onDrop(DragContext context) {
    MouseListBox from = (MouseListBox) context.draggable.getParent().getParent();
    Label label = (Label) context.draggable;
    moveDimension(label.getText(), axis); // TODO: e se o cara nao mover?!
    List<Widget> selectedWidgets = context.selectedWidgets;
    for (Widget widget : selectedWidgets) {
      if (widget.getParent().getParent() == from) {
        HTML htmlClone = new HTML(DOM.getInnerHTML(widget.getElement()));
        mouseListBox.add(htmlClone);
      }
      //widget.removeStyleName("olap-background-blue");
    }
    super.onDrop(context);
    //context.draggable.setStyleName("olap-background-blue");
  }

  @Override
  public void onPreviewDrop(DragContext context) throws VetoDragException {
    MouseListBox from = (MouseListBox) context.draggable.getParent().getParent();
    if (from == mouseListBox) {
      throw new VetoDragException();
    }
    super.onPreviewDrop(context);
  }
}
