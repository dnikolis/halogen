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

import javax.servlet.http.HttpSession;

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
import org.pentaho.halogen.client.util.CellInfo;
import org.pentaho.halogen.client.util.StringTree;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * @author wseyler
 *
 */
public class Olap4JServiceImpl extends RemoteServiceServlet implements Olap4JService {
  protected static final Double ZERO_THRESHOLD = 1.2346E-8;
  protected static HashMap<HttpSession, OlapConnection> connectionCache = new HashMap<HttpSession, OlapConnection>();
  protected static HashMap<HttpSession, Cube> cubeCache = new HashMap<HttpSession, Cube>();
  protected static HashMap<HttpSession, Query> queryCache = new HashMap<HttpSession, Query>();
  
  /* (non-Javadoc)
   * @see org.pentaho.halogen.client.services.Olap4JService#getServerInfo()
   */
  public String getServerInfo() {
    return DateFormat.getInstance().format(new Date());
  }
  
  public Boolean connect(String connectStr) {
    OlapConnection connection;
    try {
      Class.forName("mondrian.olap4j.MondrianOlap4jDriver"); //$NON-NLS-1$
      connection = (OlapConnection) DriverManager.getConnection(connectStr);
      OlapWrapper wrapper = connection;
      OlapConnection olapConnection = wrapper.unwrap(OlapConnection.class);
      if (olapConnection != null) {
        connectionCache.put(this.getThreadLocalRequest().getSession(), olapConnection);
        cubeCache.clear();
        queryCache.clear();
        return true;
      } else {
        return false;
      }
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return false;
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return false;
    }
  }
  
  public String[] getCubes() {
    OlapConnection connection = connectionCache.get(this.getThreadLocalRequest().getSession());
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
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.halogen.client.services.Olap4JService#setCube(java.lang.String)
   */
  public Boolean setCube(String cubeName) {
    OlapConnection connection = connectionCache.get(this.getThreadLocalRequest().getSession());
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
        cubeCache.put(this.getThreadLocalRequest().getSession(), cube);
        queryCache.clear();
        return new Boolean(true);
      }
      return new Boolean(false);
    } catch (OlapException e) {
      e.printStackTrace();
      return new Boolean(false);
    }
  }
  
  public String[] getDimensions(String axis) {
    Query query = queryCache.get(this.getThreadLocalRequest().getSession());
    if (query == null) {
      Cube cube = cubeCache.get(this.getThreadLocalRequest().getSession());
      if (cube == null) {
        return new String[0];
      }
      try {
        query = new Query(this.getThreadLocalRequest().getSession().toString(), cube);
        queryCache.put(this.getThreadLocalRequest().getSession(), query);
      } catch (SQLException e) {
        // TODO Auto-generated catch block
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
  
  public Boolean moveDimension(String axisName, String DimName) {
    Query query = queryCache.get(this.getThreadLocalRequest().getSession());
    if (query == null) {
      Cube cube = cubeCache.get(this.getThreadLocalRequest().getSession());
      if (cube == null) {
        return new Boolean(false);
      }
      try {
        query = new Query(this.getThreadLocalRequest().getSession().toString(), cube);
        queryCache.put(this.getThreadLocalRequest().getSession(), query);
      } catch (SQLException e) {
        // TODO Auto-generated catch block
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

  public StringTree getMembers(String dimName) {
    Query query = queryCache.get(this.getThreadLocalRequest().getSession());
    
    List<String> uniqueNameList = new ArrayList<String>();
    NamedList<Level> levels = query.getDimension(dimName).getDimension().getHierarchies().get(dimName).getLevels();
    for(Level level : levels) {
      try {
        List<Member> levelMembers = level.getMembers();
        for(Member member : levelMembers) {
          uniqueNameList.add(member.getUniqueName());
        }
      } catch (OlapException e) {
        // TODO Auto-generated catch block
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
  
  public Boolean validateQuery() {
    Query query = queryCache.get(this.getThreadLocalRequest().getSession());
    return new Boolean(query != null && query.validate());
  }
  
  public CellInfo[][] executeQuery() {
    Query query = queryCache.get(this.getThreadLocalRequest().getSession());
    if (query == null) {
      return new CellInfo[0][0];
    }
    
    try {
      CellSet results = query.execute();
      return CellSet2CellInfo(results);  
    } catch (OlapException e) {
      e.printStackTrace();
      return null;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  
  public CellInfo[][] executeMDXStr(String mdx) {
    OlapConnection connection = connectionCache.get(this.getThreadLocalRequest().getSession());

    try {
      CellSet results = connection.prepareOlapStatement(mdx).executeQuery();

      return CellSet2CellInfo(results);
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }
  
  public Boolean createSelection(String dimName, String memberName, Integer selectionType) {
    Query query = queryCache.get(this.getThreadLocalRequest().getSession());
    Cube cube = cubeCache.get(this.getThreadLocalRequest().getSession());
    try {
      Member member = cube.lookupMember(dimName, memberName);
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

  public CellInfo[][] swapAxis() {
    Query query = queryCache.get(this.getThreadLocalRequest().getSession());
    query.swapAxes();
    
    return executeQuery();
  }
  /**
   * @param dimName
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
  
  private CellInfo[][] CellSet2CellInfo(CellSet cellSet) {
    CellSetAxis rowCells = cellSet.getAxes().get(Axis.ROWS.axisOrdinal());
    CellSetAxis colCells = cellSet.getAxes().get(Axis.COLUMNS.axisOrdinal());
    
    int rowCount = rowCells.getPositionCount();
    int colCount = colCells.getPositionCount();
    List<Position> rowPositions = rowCells.getPositions();
    List<Position> colPositions = colCells.getPositions();
    
    int rowMemberWidth = 0;
    for (Position position : rowPositions) {
      rowMemberWidth = Math.max(rowMemberWidth, position.getMembers().size());
    }
    colCount += rowMemberWidth;
    
    int colMemberHeight = 0;
    for (Position position : colPositions) {
      colMemberHeight = Math.max(colMemberHeight, position.getMembers().size());
    }
    rowCount += colMemberHeight;

    CellInfo[][] values = new CellInfo[rowCount][colCount];

    // Populate the column members
    for (int c=0; c<colPositions.size(); c++) {
      Position colPosition = colPositions.get(c);
      for (int r=0; r<colPosition.getMembers().size(); r++) {
        Member mbr = colPosition.getMembers().get(r);
        
        CellInfo cellInfo = new CellInfo();
        cellInfo.setFormattedValue(mbr.getName());
        cellInfo.setColumnHeader(true);
        
        values[r][c+rowMemberWidth] = cellInfo;
      }
    }
    
    // Populate the row members
    for (int r=0; r<rowPositions.size(); r++) {
      Position rowPosition = rowPositions.get(r);
      for (int c=0; c<rowPosition.getMembers().size(); c++) {
        Member mbr = rowPosition.getMembers().get(c);
        
        CellInfo cellInfo = new CellInfo();
        cellInfo.setFormattedValue(mbr.getName());
        cellInfo.setRowHeader(true);
        
        values[r+colMemberHeight][c] = cellInfo;
      }
    }
    
    // Populate the cells
    for (int r=0; r<rowPositions.size(); r++) {
      for (int c=0; c<colPositions.size(); c++) {
        Cell cell = cellSet.getCell(colPositions.get(c), rowPositions.get(r));
        CellInfo cellInfo = new CellInfo();
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
        values[r+colMemberHeight][c+rowMemberWidth] = cellInfo;
      }          
    }
    return values;
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
}
