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
 * @created Dec 11, 2007 
 * @author wseyler
 */


package org.pentaho.halogen.server.services;

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.olap4j.Axis;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.OlapWrapper;
import org.olap4j.Position;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.NamedList;
import org.olap4j.query.Query;
import org.olap4j.query.QueryAxis;
import org.olap4j.query.QueryDimension;
import org.olap4j.query.Selection;
import org.pentaho.halogen.client.services.Olap4JService;
import org.pentaho.halogen.client.util.CellData;
import org.pentaho.halogen.client.util.CellInfo;
import org.pentaho.halogen.client.util.ColumnHeaders;
import org.pentaho.halogen.client.util.OlapData;
import org.pentaho.halogen.client.util.RowHeaders;
import org.pentaho.halogen.client.util.StringTree;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * @author wseyler
 *
 */
@SuppressWarnings("serial")
public class Olap4JServiceImpl extends RemoteServiceServlet implements Olap4JService {

  protected static final Double ZERO_THRESHOLD = 1.2346E-8;
  protected static String chartDirectory;
  protected static HashMap<String, OlapConnection> connectionCache = new HashMap<String, OlapConnection>();
  protected static HashMap<OlapConnection, Cube> cubeCache = new HashMap<OlapConnection, Cube>();
  protected static HashMap<Cube, Query> queryCache = new HashMap<Cube, Query>();

  public Olap4JServiceImpl() {
    super();
    chartDirectory = System.getProperty("user.dir") + File.separator + "www" + File.separator + "org.pentaho.halogen.Halogen" + File.separator + "charts";
    File chartDirectoryFile = new File(chartDirectory);
    if (!chartDirectoryFile.exists()) {
      chartDirectoryFile.mkdirs();
    }
  }

  /* (non-Javadoc)
   * @see org.pentaho.halogen.client.services.Olap4JService#getServerInfo()
   */
  public String getServerInfo() {
    return DateFormat.getInstance().format(new Date());
  }
  
  public Boolean connect(String connectStr, String guid) {
    OlapConnection connection;
    try {
      Class.forName("mondrian.olap4j.MondrianOlap4jDriver"); //$NON-NLS-1$
      connection = (OlapConnection) DriverManager.getConnection(connectStr);
      OlapWrapper wrapper = connection;
      OlapConnection olapConnection = wrapper.unwrap(OlapConnection.class);
      if (olapConnection != null) {
        connectionCache.put(guid, olapConnection);
        return true;
      } else {
        return false;
      }
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      return false;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }
  
  public String[] getCubes(String guid) {
    OlapConnection connection = connectionCache.get(guid);
    if (connection == null) {
      return new String[0];
    }
    try {
      NamedList<Cube> cubes = connection.getSchema().getCubes();
      String[] cubeNames = new String[cubes.size()];
      for (int i=0; i<cubes.size(); i++) {
        Cube cube = cubes.get(i);
        cubeNames[i] = cube.getName();
      }
      return cubeNames;
    } catch (OlapException e) {
      e.printStackTrace();
    }
    return null;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.halogen.client.services.Olap4JService#setCube(java.lang.String)
   */
  public Boolean setCube(String cubeName, String guid) {
    OlapConnection connection = connectionCache.get(guid);
    if (connection == null) {
      return new Boolean(false);
    }
    
    try {
      NamedList<Cube> cubes = connection.getSchema().getCubes();
      Cube cube = null;
      Iterator<Cube> iter = cubes.iterator();
      while(iter.hasNext() && cube == null) {
        Cube testCube = iter.next();
        if (cubeName.equals(testCube.getName())) {
          cube = testCube;
        }
      }
      if (cube != null) {
        cubeCache.put(connection, cube);
        return new Boolean(true);
      }
      return new Boolean(false);
    } catch (OlapException e) {
      e.printStackTrace();
      return new Boolean(false);
    }
  }
  
  public String[] getDimensions(String axis, String guid) {
    
    Cube cube;
    try {
      cube = getCube4Guid(guid);
    } catch (ObjectNotInCacheException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
      return new String[0];
    }

    Query query = queryCache.get(cube);
    if (query == null) {
      try {
        query = new Query(guid, cube);
        queryCache.put(cube, query);
      } catch (SQLException e) {
        e.printStackTrace();
        return new String[0];
      }
    }
    
    Axis targetAxis = null;
    if (!axis.equalsIgnoreCase("none")) { //$NON-NLS-1$
      targetAxis = Axis.valueOf(axis);
    }
    
    List<QueryDimension> dimList = query.getAxes().get(targetAxis).getDimensions();  
    String[] dimNames = new String[dimList.size()];
    for (int i=0; i<dimList.size(); i++) {
      dimNames[i] = dimList.get(i).getName();
    }
    return dimNames;
  }
  
  public Boolean moveDimension(String axisName, String DimName, String guid) {
    Cube cube;
    try {
      cube = getCube4Guid(guid);
    } catch (ObjectNotInCacheException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
      return new Boolean(false);
    }
    
    Query query = queryCache.get(cube);
    if (query == null) {
      try {
        query = new Query(guid, cube);
        queryCache.put(cube, query);
      } catch (SQLException e) {
        e.printStackTrace();
        return new Boolean(false);
      }
    }
    Axis targetAxis = null;
    if (!axisName.equalsIgnoreCase("none")) { //$NON-NLS-1$
      targetAxis = Axis.valueOf(axisName);
    }
    
    query.getAxes().get(targetAxis).getDimensions().add(query.getDimension(DimName));

    return new Boolean(true);
  }

  public StringTree getMembers(String dimName, String guid) {

    Query query = null;
    try {
      query = getQuery4Guid(guid);
    } catch (ObjectNotInCacheException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    
    List<String> uniqueNameList = new ArrayList<String>();
    NamedList<Level> levels = query.getDimension(dimName).getDimension().getHierarchies().get(dimName).getLevels();
    for(Level level : levels) {
      try {
        List<Member> levelMembers = level.getMembers();
        for(Member member : levelMembers) {
          uniqueNameList.add(member.getUniqueName());
        }
      } catch (OlapException e) {
        e.printStackTrace();
      }
    }
    StringTree result = new StringTree(dimName, null);
    for(int i=1; i<uniqueNameList.size(); i++) {
      String[] memberNames = uniqueNameList.get(i).split("\\."); //$NON-NLS-1$
      for (int j=0; j<memberNames.length; j++) {  // Trim off the brackets
        memberNames[j] = memberNames[j].substring(1, memberNames[j].length() - 1);
      }
      result = parseMembers(memberNames, result);
    }
    
    return result;
  }
  
  private StringTree findOrCreateNode(StringTree parent, String srchString) {
    StringTree found = null;
    for (int i=0; i<parent.getChildren().size() && found == null; i++) {
      StringTree targetNode = (StringTree)parent.getChildren().get(i);
      if (targetNode.getValue().equals(srchString)) {
        found = targetNode;
      }
    }
    if (found == null) {  // couldn't find it in the children so we'll create it
      found = new StringTree(srchString, parent);
    }
    return found;
  }
  
  private StringTree parseMembers(String[] uniqueMemberNames, StringTree parentNode) {
    StringTree currentNode = parentNode;
    for (int i=1; i<uniqueMemberNames.length; i++) {
      currentNode = findOrCreateNode(currentNode, uniqueMemberNames[i]);
    }
    return parentNode;
  }
  
  public Boolean validateQuery(String guid) {
    try {
      return new Boolean(getQuery4Guid(guid).validate());
    } catch (OlapException e) {
      e.printStackTrace();
      return new Boolean(false);
    } catch (ObjectNotInCacheException e) {
      e.printStackTrace();
      return new Boolean(false);
    }
  }
  
  public OlapData executeQuery(String guid) {
    CellSet results = null;
    try {
      results = getQuery4Guid(guid).execute();
    } catch (OlapException e) {
      e.printStackTrace();
    } catch (ObjectNotInCacheException e) {
      e.printStackTrace();
    }
    return cellSet2OlapData(results);
  }
  
  public OlapData executeMDXStr(String mdx, String guid) {
    OlapConnection connection = connectionCache.get(guid);

    try {
      CellSet results = connection.prepareOlapStatement(mdx).executeQuery();

      return cellSet2OlapData(results);
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }
  
  public Boolean createSelection(String dimName, String[] memberNames, Integer selectionType, String guid) {    
    Cube cube = null;
    try {
      cube = getCube4Guid(guid);
    } catch (ObjectNotInCacheException e1) {
      e1.printStackTrace();
      return new Boolean(false);
    }
    
    Query query = queryCache.get(cube);
    if (query == null) {
      return new Boolean(false);
    }
    try {
      Member member = cube.lookupMember(memberNames);
      QueryDimension qDim = getQueryDimension(query, dimName);
      Selection.Operator selectionMode = Selection.Operator.values()[selectionType.intValue()];
      Selection selection = qDim.createSelection(member, selectionMode);
      qDim.getSelections().add(selection);
    } catch (OlapException e) {
      e.printStackTrace();
      return new Boolean(false);
    }
    return new Boolean(true);
  }

  /* (non-Javadoc)
   * @see org.pentaho.halogen.client.services.Olap4JService#clearSelection(java.lang.String, java.lang.String[])
   */
  public Boolean clearSelection(String dimName, String[] memberNames, String guid) {
    Query query = null;
    try {
      query = getQuery4Guid(guid);
    } catch (ObjectNotInCacheException e) {
      e.printStackTrace();
      return new Boolean(false);
    }

    QueryDimension qDim = getQueryDimension(query, dimName);
    String path = normalizeMemberNames(memberNames);
    Selection selection = findSelection(path, qDim);
    if (selection == null) {
      return new Boolean(false);
    }
    qDim.getSelections().remove(selection);
    return new Boolean(true);
  }

  /**
   * @param path
   * @param dim
   */
  private Selection findSelection(String path, QueryDimension dim) {
    path = "[" + dim.getName() + "]." + path; //$NON-NLS-1$ //$NON-NLS-2$
    return findSelection(path, dim.getSelections());
  }

  /**
   * @param path
   * @param selections
   * @return
   */
  private Selection findSelection(String path, List<Selection> selections) {
    for (Selection selection : selections) {
      if (selection.getName().equals(path)) {
        return selection;
      }
    }
    return null;
  }

  public OlapData swapAxis(String guid) {
    Query query = null;
    try {
      query = getQuery4Guid(guid);
    } catch (ObjectNotInCacheException e) {
      e.getLocalizedMessage();  // We don't need a stack trace here since query.swapAxes() will blow
    }
    
    query.swapAxes();
    
    return executeQuery(guid);
  }
  
  /**
   * @param dimName
   * @param query
   * @return
   */
  private QueryDimension getQueryDimension(Query query, String dimName) {
    Map<Axis, QueryAxis> axes = query.getAxes();
    Set<Axis> keySet = axes.keySet();
    QueryDimension result = null;
    for (Axis axi : keySet) {
      QueryAxis qAxis = axes.get(axi);
      for (QueryDimension testQDim : qAxis.getDimensions()) {
        if (testQDim.getName().equals(dimName)) {
          result = testQDim;
          break;
        }
      }
      if (result != null) {
        break;
      }
    }
    return result;
  }
  
  private OlapData cellSet2OlapData(CellSet cellSet) {
    if (cellSet == null) {
      return null;
    }
    CellSetAxis rowCells = cellSet.getAxes().get(Axis.ROWS.axisOrdinal());
    CellSetAxis colCells = cellSet.getAxes().get(Axis.COLUMNS.axisOrdinal());
    
    List<Position> rowPositions = rowCells.getPositions();
    List<Position> colPositions = colCells.getPositions();
    
    // Populate the column members
    CellInfo[][] cellGrid = new CellInfo[getMaxDepth4Positions(colPositions)+1][colPositions.size()];
    for (int c=0; c<colPositions.size(); c++) {
      Position colPosition = colPositions.get(c);
      for (int r=0; r<colPosition.getMembers().size(); r++) {
        Member mbr = colPosition.getMembers().get(r);
        
        while (mbr != null) {
          CellInfo cellInfo = new CellInfo();
          cellInfo.setFormattedValue(mbr.getName());
          cellInfo.setColumnHeader(true);    
          cellGrid[r + mbr.getLevel().getDepth()][c] = cellInfo;
          
          mbr = mbr.getParentMember();
        }
      }
    }
    ColumnHeaders columnHeaders = new ColumnHeaders(cellGrid);
    
    // Populate the row members
    cellGrid = new CellInfo[rowPositions.size()][getMaxDepth4Positions(rowPositions)+1];
    for (int r=0; r<rowPositions.size(); r++) {
      Position rowPosition = rowPositions.get(r);
      for (int c=0; c<rowPosition.getMembers().size(); c++) {
        Member mbr = rowPosition.getMembers().get(c);
        
        while (mbr != null) {
          CellInfo cellInfo = new CellInfo();
          cellInfo.setFormattedValue(mbr.getName());
          cellInfo.setRowHeader(true);
          cellGrid[r][c + mbr.getLevel().getDepth()] = cellInfo;
          
          mbr = mbr.getParentMember();
        }
      }
    }
    RowHeaders rowHeaders = new RowHeaders(cellGrid);
    
    // Populate the cells
    cellGrid = new CellInfo[rowPositions.size()][colPositions.size()];
    for (int r=0; r<rowPositions.size(); r++) {
      for (int c=0; c<colPositions.size(); c++) {
        Cell cell = cellSet.getCell(colPositions.get(c), rowPositions.get(r));
        CellInfo cellInfo = new CellInfo();
        cellInfo.setRawValue(cell.getFormattedValue());
        String cellValue = cell.getFormattedValue();  // First try to get a formatted value
        if (cellValue.length()<1) {
          Number value = (Number) cell.getValue();
          if (value.doubleValue() < 1.23457E08) {
            cellValue = "null"; //$NON-NLS-1$
          } else {
            cellValue = cell.getValue().toString();   // Otherwise return the raw value
          }
        }
        cellInfo.setFormattedValue(getValueString(cellValue));
        cellInfo.setColorValue(getColorValue(cell.getFormattedValue()));
        cellGrid[r][c] = cellInfo;
      }          
    }
    CellData cellData = new CellData(cellGrid);
    
    OlapData olapData = new OlapData(rowHeaders, columnHeaders, cellData);
    return olapData;
  }

  /**
   * @param formattedValue
   * @return
   */
  private String getColorValue(String formattedValue) {
    String[] values = formattedValue.split("\\|"); //$NON-NLS-1$
    String color = null;

    if (values.length > 2) {  // We've got attributes
      for (int i=2; i<values.length; i++) {
        if (values[i].startsWith("style")) { //$NON-NLS-1$
          String colorString = values[i].split("=")[1]; //$NON-NLS-1$
          if (colorString.equalsIgnoreCase("black")) { //$NON-NLS-1$
            color = "#000000"; //$NON-NLS-1$
          } else if (colorString.equalsIgnoreCase("blue")) { //$NON-NLS-1$
            color = "#0000FF"; //$NON-NLS-1$
          } else if (colorString.equalsIgnoreCase("cyan")) { //$NON-NLS-1$
            color = "#00FFFF"; //$NON-NLS-1$
          } else if (colorString.equalsIgnoreCase("dark-gray")) { //$NON-NLS-1$
            color = "#A9A9A9"; //$NON-NLS-1$
          } else if (colorString.equalsIgnoreCase("gray")) { //$NON-NLS-1$
            color = "#808080"; //$NON-NLS-1$
          } else if (colorString.equalsIgnoreCase("green")) { //$NON-NLS-1$
            color = "#008000"; //$NON-NLS-1$
          } else if (colorString.equalsIgnoreCase("light-gray")) { //$NON-NLS-1$
            color = "#D3D3D3"; //$NON-NLS-1$
          } else if (colorString.equalsIgnoreCase("magenta")) { //$NON-NLS-1$
            color = "#FF00FF"; //$NON-NLS-1$
          } else if (colorString.equalsIgnoreCase("orange")) { //$NON-NLS-1$
            color = "#FFA500"; //$NON-NLS-1$
          } else if (colorString.equalsIgnoreCase("pink")) { //$NON-NLS-1$
            color = "#FFC0CB"; //$NON-NLS-1$
          } else if (colorString.equalsIgnoreCase("red")) { //$NON-NLS-1$
            color = "#FF0000"; //$NON-NLS-1$
          } else if (colorString.equalsIgnoreCase("white")) { //$NON-NLS-1$
            color = "#FFFFFF"; //$NON-NLS-1$
          } else if (colorString.equalsIgnoreCase("yellow")) { //$NON-NLS-1$
            color = "#FFFF00"; //$NON-NLS-1$
          } else {
            color = colorString;
          }
        }
      }
    }
    
    return color;
  }

  /**
   * @param formattedValue
   * @return
   */
  private String getValueString(String formattedValue) {
    String[] values = formattedValue.split("\\|"); //$NON-NLS-1$
    if (values.length > 1) {
      return values[1];
    }
    return values[0];
  }
  
  private int getMaxDepth4Positions(List<Position> positions) {
  	int depth = 0;
    for (Position position : positions) {
      for (Member member: position.getMembers()) {
        depth = Math.max(depth, member.getLevel().getDepth());
      }
    }
    
    return depth;
  }

  /**
   * @param memberNames  in the form memberNames[0] = "All Products", memberNames[1] = "Food", memberNames[2] = "Snacks"
   * @return a String in the following format "[All Products].[Food].[Snacks]
   */
  private String normalizeMemberNames(String[] memberNames) {
    StringBuffer buffer = new StringBuffer();
    for (String name : memberNames) {
      buffer.append("[").append(name).append("]."); //$NON-NLS-1$ //$NON-NLS-2$
    }
    if (buffer.length() > 0) {
      buffer.deleteCharAt(buffer.length()-1); // Remove the last "."
    }
    
    return buffer.toString();
  }
  
  private Cube getCube4Guid(String guid) throws ObjectNotInCacheException {
    OlapConnection connection = connectionCache.get(guid);
    if (connection == null) {
      throw new ObjectNotInCacheException(Messages.getString("Olap4JServiceImpl.OBJECT_NOT_IN_CACHE") + OlapConnection.class.toString() + Messages.getString("Olap4JServiceImpl.NO_KEY_FOUND") + guid); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    Cube cube = cubeCache.get(connection);
    if (cube == null) {
      throw new ObjectNotInCacheException(Messages.getString("Olap4JServiceImpl.OBJECT_NOT_IN_CACHE") + Cube.class.toString() + Messages.getString("Olap4JServiceImpl.NO_KEY_FOUND") + connection.toString()); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    return cube;
  }
  
  private Query getQuery4Guid(String guid) throws ObjectNotInCacheException {
    Cube cube;
    try {
      cube = getCube4Guid(guid);
    } catch (ObjectNotInCacheException e) {
      throw e;
    }
    
    Query query = queryCache.get(cube);
    if (query == null) {
      throw new ObjectNotInCacheException(Messages.getString("Olap4JServiceImpl.OBJECT_NOT_IN_CACHE") + Query.class.toString() + Messages.getString("Olap4JServiceImpl.NO_KEY_FOUND") + cube.toString()); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    return query;
  }

  /* (non-Javadoc)
   * @see org.pentaho.halogen.client.services.Olap4JService#createChart(org.pentaho.halogen.client.util.OlapData)
   */
  public String createChart(OlapData olapData) {
    CategoryDataset categoryDataset = createCategoryDataset(olapData);
    String categoryAxisName = olapData.getRowHeaders().getCell(0, 0).getFormattedValue();
    String valueAxisName = olapData.getColumnHeaders().getCell(0, 0).getFormattedValue();
    JFreeChart chart = ChartFactory.createBarChart("Olap Chart", categoryAxisName, valueAxisName, categoryDataset, PlotOrientation.VERTICAL, true, true, false);
    File chartFile = null;
    try {
      chartFile = File.createTempFile("chart",".png", new File(chartDirectory));
      ChartUtilities.saveChartAsPNG(chartFile, chart, 800, 800);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    HttpServletRequest req = getThreadLocalRequest();
    StringBuffer buffer = new StringBuffer(req.getScheme()).append("://").append(req.getServerName()).append(":").append(req.getServerPort()).append("/org.pentaho.halogen.Halogen").append("/charts/").append(chartFile.getName());
    System.out.println(buffer.toString());
    return buffer.toString();
  }

  /**
   * @param olapData
   * @return
   */
  private CategoryDataset createCategoryDataset(OlapData olapData) {
    DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset();
    
    for (int row = 0; row < olapData.getCellData().getDownCount(); row++ ) {
      for (int column = 0; column < olapData.getCellData().getAcrossCount(); column++ ) {
        if (olapData.getCellData().getCell(row, column).getFormattedValue() != null && !olapData.getCellData().getCell(row, column).getFormattedValue().equalsIgnoreCase("null") ) {
          try {
            Double value = new Double(olapData.getCellData().getCell(row, column).getFormattedValue());
            String rowName = olapData.getRowHeaders().getCell(row, olapData.getRowHeaders().getAcrossCount()-1).getFormattedValue();
            String columnName = olapData.getColumnHeaders().getCell(olapData.getColumnHeaders().getDownCount()-1, column).getFormattedValue();
            categoryDataset.addValue(value, rowName, columnName);
          } catch (NumberFormatException ex) {
            // Do Nothing
          }
        }
      }
    }
    
    return categoryDataset;
  }
}

