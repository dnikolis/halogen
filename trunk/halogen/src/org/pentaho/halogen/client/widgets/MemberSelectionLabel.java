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
 * @created Jan 21, 2008 
 * @author wseyler
 */


package org.pentaho.halogen.client.widgets;

import org.pentaho.halogen.client.images.SelectionModeImageBundle;
import org.pentaho.halogen.client.panels.SelectionModePopup;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.ClickListenerCollection;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SourcesClickEvents;

/**
 * @author wseyler
 *
 */
public class MemberSelectionLabel extends HorizontalPanel implements SourcesClickEvents {
  protected SelectionModeImageBundle selectionImageBundle;

  protected ClickListenerCollection clickListeners;
  
  private Label label = new Label();
  private Image image;
  
  public MemberSelectionLabel() {
    this.sinkEvents(Event.BUTTON_LEFT | Event.BUTTON_RIGHT);
    selectionImageBundle = (SelectionModeImageBundle)GWT.create(SelectionModeImageBundle.class);

    setStyleName("olap-MemberSelectionLabel"); //$NON-NLS-1$

    this.add(label);
  }
  
  public MemberSelectionLabel(String text) {
    this();
    label.setText(text);      
  }
  
  public void setImage(Image image) {
    if (this.image != null) {
      this.remove(this.image);
    }
    this.image = image;
    if (this.image != null) {
      this.add(this.image);
    }
  }
  
  public String getText() {
    return label.getText();
  }
  
  public void setText(String text) {
    label.setText(text);
  }

  /* (non-Javadoc)
   * @see com.google.gwt.user.client.ui.SourcesClickEvents#addClickListener(com.google.gwt.user.client.ui.ClickListener)
   */
  public void addClickListener(ClickListener listener) {
    if (clickListeners == null) {
      clickListeners = new ClickListenerCollection();
    }
    clickListeners.add(listener);
  }

  /* (non-Javadoc)
   * @see com.google.gwt.user.client.ui.SourcesClickEvents#removeClickListener(com.google.gwt.user.client.ui.ClickListener)
   */
  public void removeClickListener(ClickListener listener) {
    if (clickListeners != null) {
      clickListeners.remove(listener);
    }
  }

  public void onBrowserEvent(Event event) {
    super.onBrowserEvent(event);
    switch (DOM.eventGetType(event)) {
      case Event.ONCLICK:
        if (clickListeners != null) {
          clickListeners.fireClick(this);
        }
        break;
    }
  }      

  public void setSelectionMode(int mode) {
    Image selectionImage = null;
    switch (mode) {
      case SelectionModePopup.MEMBER:
        selectionImage = selectionImageBundle.member_select_icon().createImage();
        break;
      case SelectionModePopup.CHILDREN:
        selectionImage = selectionImageBundle.children_select_icon().createImage();
        break;
      case SelectionModePopup.INCLUDE_CHILDREN:
        selectionImage = selectionImageBundle.include_children_select_icon().createImage();
        break;
      case SelectionModePopup.SIBLINGS:
        selectionImage = selectionImageBundle.siblings_select_icon().createImage();
    }         
    setImage(selectionImage);
  }

  public Label getLabel() {
    return label;
  }

}
