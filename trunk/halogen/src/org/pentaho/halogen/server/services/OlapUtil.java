package org.pentaho.halogen.server.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.olap4j.Axis;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.Position;
import org.olap4j.metadata.Member;
import org.olap4j.query.Query;
import org.olap4j.query.QueryAxis;
import org.olap4j.query.QueryDimension;
import org.olap4j.query.Selection;
import org.pentaho.halogen.client.util.CellData;
import org.pentaho.halogen.client.util.CellInfo;
import org.pentaho.halogen.client.util.ColumnHeaders;
import org.pentaho.halogen.client.util.OlapData;
import org.pentaho.halogen.client.util.RowHeaders;
import org.pentaho.halogen.client.util.StringTree;

public class OlapUtil {
	
	public static List<Member> getAncestors4Member(Member member) {
		List<Member> ancestors = new ArrayList<Member>();
		while (member != null) {
			ancestors.add(member);
			member = member.getParentMember();
		}
		List<Member> invertedAncestors = new ArrayList<Member>();
		for (int i=ancestors.size(); i>0; i--) {
			invertedAncestors.add(ancestors.get(i-1));
		}
		return invertedAncestors;
	}
	
  public static StringTree findOrCreateNode(StringTree parent, String srchString) {
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

  public static StringTree parseMembers(String[] uniqueMemberNames, StringTree parentNode) {
    StringTree currentNode = parentNode;
    for (int i=1; i<uniqueMemberNames.length; i++) {
      currentNode = OlapUtil.findOrCreateNode(currentNode, uniqueMemberNames[i]);
    }
    return parentNode;
  }

  /**
   * @param path
   * @param selections
   * @return
   */
  public static Selection findSelection(String path, List<Selection> selections) {
    for (Selection selection : selections) {
      if (selection.getName().equals(path)) {
        return selection;
      }
    }
    return null;
  }

  /**
   * @param path
   * @param dim
   */
  public static Selection findSelection(String path, QueryDimension dim) {
    path = "[" + dim.getName() + "]." + path; //$NON-NLS-1$ //$NON-NLS-2$
    return findSelection(path, dim.getSelections());
  }
  
  /**
   * @param dimName
   * @param query
   * @return
   */
  public static QueryDimension getQueryDimension(Query query, String dimName) {
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

  public static OlapData cellSet2OlapData(CellSet cellSet) {
    if (cellSet == null) {
      return null;
    }
    CellSetAxis rowCells = cellSet.getAxes().get(Axis.ROWS.axisOrdinal());
    CellSetAxis colCells = cellSet.getAxes().get(Axis.COLUMNS.axisOrdinal());
    
    List<Position> rowPositions = rowCells.getPositions();
    List<Position> colPositions = colCells.getPositions();
    
    // DEBUG for positiions
//    System.out.println("---- Row Positions ----");
//    for (Position position : rowPositions) {
//    	for (Member member : position.getMembers()) {
//    		System.out.print("[" + member.getName() + "][" + member.getLevel().getDepth() + "]");
//    	}
//    	System.out.println();
//    }
//    
//    System.out.println("\n---- Column Positions ----");
//    for (Position position : colPositions) {
//    	for (Member member : position.getMembers()) {
//    		System.out.print("[" + member.getName() + "][" + member.getLevel().getDepth() + "]");
//    	}
//    	System.out.println();
//    }
    // End DEBUG positions
    
    // Populate the column members
    CellInfo[][] cellGrid = new CellInfo[getMaxDepth4Positions(colPositions)][colPositions.size()];
    for (int c=0; c<colPositions.size(); c++) {
      Position colPosition = colPositions.get(c);
      List<CellInfo> mbrInfoList = new ArrayList<CellInfo>();
      for (int r=0; r<colPosition.getMembers().size(); r++) {
        Member mbr = colPosition.getMembers().get(r);
        for (Member member : OlapUtil.getAncestors4Member(mbr)) {
        	CellInfo cellInfo = new CellInfo();
        	cellInfo.setFormattedValue(member.getName());
        	cellInfo.setColumnHeader(true);
        	mbrInfoList.add(cellInfo);
        }
        for (int i=0; i<mbrInfoList.size(); i++) {
        	CellInfo cellInfo = mbrInfoList.get(i);
        	cellGrid[i][c] = cellInfo;
        }
      }
    }
    ColumnHeaders columnHeaders = new ColumnHeaders(cellGrid);
    
    // Populate the row members
    cellGrid = new CellInfo[rowPositions.size()][];
    for (int r=0; r<rowPositions.size(); r++) {
      Position rowPosition = rowPositions.get(r);
      List<CellInfo> mbrInfoList = new ArrayList<CellInfo>();
      for (int c=0; c<rowPosition.getMembers().size(); c++) {
        Member mbr = rowPosition.getMembers().get(c);
        for (Member member : OlapUtil.getAncestors4Member(mbr)) {
        	CellInfo cellInfo = new CellInfo();
        	cellInfo.setFormattedValue(member.getName());
        	cellInfo.setColumnHeader(true);
        	mbrInfoList.add(cellInfo);
        }
      }
      CellInfo[] cellInfoRow = new CellInfo[mbrInfoList.size()];
      cellInfoRow = mbrInfoList.toArray(cellInfoRow);
      cellGrid[r] = cellInfoRow;
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
  
  public static int getMaxDepth4Positions(List<Position> positions) {
  	int depth = 0;
    for (Position position : positions) {
    	depth = Math.max(depth, getMaxDepth4Position(position));
    }
    
    return depth;
  }

  public static int getMaxDepth4Position(Position position) {
  	int depth = 0;
    for (Member member: position.getMembers()) {
      depth += member.getLevel().getDepth()+1;
    }
    
    return depth;
  }

  /**
   * @param formattedValue
   * @return
   */
  public static String getColorValue(String formattedValue) {
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
  public static String getValueString(String formattedValue) {
    String[] values = formattedValue.split("\\|"); //$NON-NLS-1$
    if (values.length > 1) {
      return values[1];
    }
    return values[0];
  }
  

  /**
   * @param memberNames  in the form memberNames[0] = "All Products", memberNames[1] = "Food", memberNames[2] = "Snacks"
   * @return a String in the following format "[All Products].[Food].[Snacks]
   */
  public static String normalizeMemberNames(String[] memberNames) {
    StringBuffer buffer = new StringBuffer();
    for (String name : memberNames) {
      buffer.append("[").append(name).append("]."); //$NON-NLS-1$ //$NON-NLS-2$
    }
    if (buffer.length() > 0) {
      buffer.deleteCharAt(buffer.length()-1); // Remove the last "."
    }
    
    return buffer.toString();
  }

  /**
   * @param olapData
   * @return
   */
  public static CategoryDataset createCategoryDataset(OlapData olapData) {
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
