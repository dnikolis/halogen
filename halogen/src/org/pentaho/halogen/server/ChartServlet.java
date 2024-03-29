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
 * @created Apr 30, 2008 
 * @author wseyler
 */

package org.pentaho.halogen.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.pentaho.halogen.client.util.ChartPrefs;
import org.pentaho.halogen.client.util.OlapData;
import org.pentaho.halogen.server.services.OlapUtil;
import org.pentaho.halogen.server.util.ChartPackage;

/**
 * @author wseyler
 *
 */
public class ChartServlet extends HttpServlet {
  /* (non-Javadoc)
  * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
  */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    // TODO Auto-generated method stub

    String olapDataGuid = req.getParameter("guid"); //$NON-NLS-1$
    ChartPackage chartPackage = (ChartPackage) req.getSession().getAttribute(olapDataGuid);
    OlapData olapData = chartPackage.getOlapData();
    ChartPrefs chartPrefs = chartPackage.getChartPrefs();

    try {
      CategoryDataset categoryDataset = OlapUtil.createCategoryDataset(olapData);
      String categoryAxisName = olapData.getRowHeaders().getCell(0, 0).getFormattedValue();
      String valueAxisName = olapData.getColumnHeaders().getCell(0, 0).getFormattedValue();
      final JFreeChart chart = ChartFactory.createBarChart(chartPrefs.getChartTitle(), categoryAxisName, valueAxisName, categoryDataset, PlotOrientation.VERTICAL, true, true, false); //$NON-NLS-1$
      resp.setContentType("image/png"); //$NON-NLS-1$
      ChartUtilities.writeChartAsPNG(resp.getOutputStream(), chart, chartPrefs.getChartWidth(), chartPrefs.getChartHeight());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    req.getSession().removeAttribute(olapDataGuid);
  }
}
