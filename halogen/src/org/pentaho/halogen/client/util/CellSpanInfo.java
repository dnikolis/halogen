package org.pentaho.halogen.client.util;

public class CellSpanInfo {
	private CellInfo info;
	private int span = 1;
	
	public CellSpanInfo(CellInfo info, int span) {
		this.info = info;
		this.span = span;
	}

	public CellInfo getInfo() {
		return info;
	}

	public int getSpan() {
		return span;
	}

}
