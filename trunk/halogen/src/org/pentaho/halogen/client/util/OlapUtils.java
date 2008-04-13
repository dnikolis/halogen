package org.pentaho.halogen.client.util;

import java.util.ArrayList;
import java.util.List;

public class OlapUtils {
	public static List getCellSpans(CellInfo[] cellInfos) {
		List spans = new ArrayList();
		CellInfo holdValue = cellInfos != null && cellInfos.length > 0 ? cellInfos[0] : null;
		int span = 1;
		
		for (int i=1; i<cellInfos.length; i++) {
			if (cellInfos[i].getFormattedValue().equals(holdValue == null ? null : holdValue.getFormattedValue())) {
				span ++;
			} else {
				spans.add(new CellSpanInfo(holdValue, span));
				span = 1;
				holdValue = cellInfos[i];
			}
		}
		spans.add(new CellSpanInfo(holdValue, span));
		
		return spans;
	}

	public static CellInfo[] extractRow(CellInfo[][] cellInfoGrid, int row) {
		return cellInfoGrid[row];
	}
	
	public static CellInfo[] extractColumn(CellInfo[][] cellInfoGrid, int column) {
		CellInfo[] values = new CellInfo[cellInfoGrid.length];
		for (int row = 0; row < cellInfoGrid.length; row++) {
			values[row] = cellInfoGrid[row][column];
		}
		return values;
	}
}
