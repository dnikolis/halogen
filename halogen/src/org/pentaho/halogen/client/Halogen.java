package org.pentaho.halogen.client;


import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Halogen implements EntryPoint {

  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
    HalogenTabPanel tabPanel = new HalogenTabPanel();
    RootPanel root = RootPanel.get();
    root.add(tabPanel);
  }
}
