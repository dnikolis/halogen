package org.pentaho.halogen.client;


import org.pentaho.halogen.client.services.Olap4JService;
import org.pentaho.halogen.client.services.Olap4JServiceAsync;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Halogen implements EntryPoint {

  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
    Olap4JServiceAsync olap4JService = (Olap4JServiceAsync) GWT.create(Olap4JService.class);
    Messages messages = (Messages) GWT.create(Messages.class);
    ServiceDefTarget endpoint = (ServiceDefTarget) olap4JService;
    String moduleRelativeURL = GWT.getModuleBaseURL() + "olap4j"; //$NON-NLS-1$
    endpoint.setServiceEntryPoint(moduleRelativeURL);
    
    HalogenTabPanel tabPanel = new HalogenTabPanel(olap4JService, messages);
    RootPanel root = RootPanel.get();
    root.add(tabPanel);
  }
}
