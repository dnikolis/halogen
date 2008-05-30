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
 * @created May 29, 2008 
 * @author wseyler
 */


package org.pentaho.halogen.client.events;

import org.pentaho.halogen.client.listeners.ChartPrefsListener;

/**
 * @author wseyler
 *
 */
public interface SourcesChartPrefsEvents {
  /**
   * Adds a listener interface to receive chart preference events.
   * 
   * @param listener the listener interface to add
   */
  void addChartPrefsListener(ChartPrefsListener listener);

  /**
   * Removes a previously added listener interface.
   * 
   * @param listener the listener interface to remove
   */
  void removePrefsListener(ChartPrefsListener listener);
}
