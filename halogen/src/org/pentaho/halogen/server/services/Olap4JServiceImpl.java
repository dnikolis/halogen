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

import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.OlapWrapper;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.NamedList;
import org.olap4j.query.Query;
import org.olap4j.query.QueryDimension;
import org.olap4j.query.Selection;
import org.pentaho.halogen.client.services.Olap4JService;
import org.pentaho.halogen.client.util.GuidFactory;
import org.pentaho.halogen.client.util.OlapData;
import org.pentaho.halogen.client.util.StringTree;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * @author wseyler
 *
 */
@SuppressWarnings("serial")
public class Olap4JServiceImpl extends RemoteServiceServlet implements Olap4JService {

  protected static final Double ZERO_THRESHOLD = 1.2346E-8;

  protected static HashMap<String, OlapConnection> connectionCache = new HashMap<String, OlapConnection>();

  protected static HashMap<OlapConnection, Cube> cubeCache = new HashMap<OlapConnection, Cube>();

  protected static HashMap<Cube, Query> queryCache = new HashMap<Cube, Query>();

  public Olap4JServiceImpl() {
    super();
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

  public Boolean disconnect(String guid) {
    OlapConnection connection = connectionCache.get(guid);
    if (connection != null) {
      try {
        connection.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    queryCache.remove(cubeCache.remove(connectionCache.remove(guid)));
    return true;
  }

  public String[] getCubes(String guid) {
    OlapConnection connection = connectionCache.get(guid);
    if (connection == null) {
      return new String[0];
    }
    try {
      NamedList<Cube> cubes = connection.getSchema().getCubes();
      String[] cubeNames = new String[cubes.size()];
      for (int i = 0; i < cubes.size(); i++) {
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
      while (iter.hasNext() && cube == null) {
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
    for (int i = 0; i < dimList.size(); i++) {
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
    for (Level level : levels) {
      try {
        List<Member> levelMembers = level.getMembers();
        for (Member member : levelMembers) {
          uniqueNameList.add(member.getUniqueName());
        }
      } catch (OlapException e) {
        e.printStackTrace();
      }
    }
    StringTree result = new StringTree(dimName, null);
    for (int i = 0; i < uniqueNameList.size(); i++) {
      String[] memberNames = uniqueNameList.get(i).split("\\."); //$NON-NLS-1$
      for (int j = 0; j < memberNames.length; j++) { // Trim off the brackets
        memberNames[j] = memberNames[j].substring(1, memberNames[j].length() - 1);
      }
      result = OlapUtil.parseMembers(memberNames, result);
    }

    return result;
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
    return OlapUtil.cellSet2OlapData(results);
  }

  public OlapData executeMDXStr(String mdx, String guid) {
    OlapConnection connection = connectionCache.get(guid);

    try {
      CellSet results = connection.prepareOlapStatement(mdx).executeQuery();

      return OlapUtil.cellSet2OlapData(results);
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
      QueryDimension qDim = OlapUtil.getQueryDimension(query, dimName);
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

    QueryDimension qDim = OlapUtil.getQueryDimension(query, dimName);
    String path = OlapUtil.normalizeMemberNames(memberNames);
    Selection selection = OlapUtil.findSelection(path, qDim);
    if (selection == null) {
      return new Boolean(false);
    }
    qDim.getSelections().remove(selection);
    return new Boolean(true);
  }

  public OlapData swapAxis(String guid) {
    Query query = null;
    try {
      query = getQuery4Guid(guid);
    } catch (ObjectNotInCacheException e) {
      e.getLocalizedMessage(); // We don't need a stack trace here since query.swapAxes() will blow
    }

    query.swapAxes();

    return executeQuery(guid);
  }

  /* (non-Javadoc)
   * @see org.pentaho.halogen.client.services.Olap4JService#createChart(org.pentaho.halogen.client.util.OlapData)
   */
  public String createChart(OlapData olapData) {
    String olapDataGuid = "olapData"+GuidFactory.getGuid();
    getThreadLocalRequest().getSession().setAttribute(olapDataGuid, olapData);
    return olapDataGuid;
  }

  // Private utility methods
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

}
