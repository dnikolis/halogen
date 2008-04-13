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
import org.pentaho.halogen.client.listeners.ConnectionListenerCollection;
import org.pentaho.halogen.client.listeners.SourcesConnectionEvents;
import org.pentaho.halogen.client.util.GuidFactory;
import org.pentaho.halogen.client.util.MessageFactory;
import org.pentaho.halogen.client.util.ServiceFactory;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.SourcesTabEvents;
import com.google.gwt.user.client.ui.TabListener;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author wseyler
 *
 */
public class ConnectionPanel extends FlexTable implements TabListener, SourcesConnectionEvents {

  TextArea connectionText;
  Button connectBtn;
  
  static String queryTypeGroup = "QUERY_TYPE"; //$NON-NLS-1$
  boolean connectionEstablished = false;
  ConnectionListenerCollection connectionListeners;
  
  public ConnectionPanel() {
    super();

    init();
  }

  /**
   * 
   */
  private void init() {
   
    this.setText(0, 0, MessageFactory.getInstance().connection_string());
    connectionText = new TextArea();
    connectionText.addChangeListener(new ChangeListener() {
      public void onChange(Widget sender) {
        setConnectionEstablished(false);
        connectBtn.setEnabled(!isConnectionEstablished());
      }    
    });
    connectionText.setWidth("300px"); //$NON-NLS-1$
    connectionText.setHeight("100px"); //$NON-NLS-1$
    connectionText.setText("jdbc:mondrian:Jdbc=jdbc:mysql://localhost:3306/foodmart?user=foodmart&password=foodmart;"+ //$NON-NLS-1$
                           "Catalog=/Users/wseyler/Downloads/mondrian-2.4.2.9831/demo/FoodMart.xml"); //$NON-NLS-1$
    this.setWidget(0, 1, connectionText);
    connectBtn = new Button(MessageFactory.getInstance().connect());
    connectBtn.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        if (connectBtn.getHTML().equals(MessageFactory.getInstance().connect())) {
          connect(connectionText.getText());
        } else if (connectBtn.getHTML().equals(MessageFactory.getInstance().disconnect())) {
          disconnect();
        }               
      }     
    });
    this.setWidget(0, 2, connectBtn);
  }

  /* (non-Javadoc)
   * @see com.google.gwt.user.client.ui.TabListener#onBeforeTabSelected(com.google.gwt.user.client.ui.SourcesTabEvents, int)
   */
  public boolean onBeforeTabSelected(SourcesTabEvents sender, int tabIndex) {
    return isConnectionEstablished();
  }

  /* (non-Javadoc)
   * @see com.google.gwt.user.client.ui.TabListener#onTabSelected(com.google.gwt.user.client.ui.SourcesTabEvents, int)
   */
  public void onTabSelected(SourcesTabEvents sender, int tabIndex) {
    // TODO Auto-generated method stub
    
  }

  public boolean isConnectionEstablished() {
    return connectionEstablished;
  }

  public void setConnectionEstablished(boolean connectionEstablished) {
    this.connectionEstablished = connectionEstablished;
  }
  
  public void connect(String connectionStr) {
    if (!isConnectionEstablished()) {
      ServiceFactory.getInstance().connect(connectionStr, GuidFactory.getGuid(), new AsyncCallback() {
        public void onSuccess(Object result) {
          Boolean booleanResult = (Boolean)result;
          if (booleanResult.booleanValue()) {
            setConnectionEstablished(true);
            connectionListeners.fireConnectionMade(ConnectionPanel.this);
          } else {
            setConnectionEstablished(false);
            connectionListeners.fireConnectionBroken(ConnectionPanel.this);
          }
          connectBtn.setHTML(isConnectionEstablished() ? MessageFactory.getInstance().disconnect() : MessageFactory.getInstance().connect());
        }
        public void onFailure(Throwable caught) {
          Window.alert(MessageFactory.getInstance().no_connection_param(caught.getLocalizedMessage()));
          setConnectionEstablished(false);
          connectBtn.setHTML(isConnectionEstablished() ? MessageFactory.getInstance().disconnect() : MessageFactory.getInstance().connect());
        }      
      });
    }
  }
  
  public void disconnect() {
    if (isConnectionEstablished()) {
      ServiceFactory.getInstance().disconnect(GuidFactory.getGuid(), new AsyncCallback() {
        public void onFailure(Throwable caught) {
          Window.alert(MessageFactory.getInstance().no_connection_param(caught.getLocalizedMessage()));
          setConnectionEstablished(false);
          connectionListeners.fireConnectionBroken(ConnectionPanel.this);
          connectBtn.setHTML(isConnectionEstablished() ? MessageFactory.getInstance().disconnect() : MessageFactory.getInstance().connect());
        }
        public void onSuccess(Object result) {
          setConnectionEstablished(false);
          connectionListeners.fireConnectionBroken(ConnectionPanel.this);
          connectBtn.setHTML(isConnectionEstablished() ? MessageFactory.getInstance().disconnect() : MessageFactory.getInstance().connect());
        }       
      });
    }
  }

  /* (non-Javadoc)
   * @see org.pentaho.halogen.client.listeners.SourcesConnectionEvents#addConnectionListener(org.pentaho.halogen.client.listeners.ConnectionListener)
   */
  public void addConnectionListener(ConnectionListener listener) {
    if (connectionListeners == null) {
      connectionListeners = new ConnectionListenerCollection();
    }
    connectionListeners.add(listener);
  }

  /* (non-Javadoc)
   * @see org.pentaho.halogen.client.listeners.SourcesConnectionEvents#removeClickListener(org.pentaho.halogen.client.listeners.ConnectionListener)
   */
  public void removeClickListener(ConnectionListener listener) {
    if (connectionListeners != null) {
      connectionListeners.remove(listener);
    }
  }
  
}
