package org.pentaho.halogen.client.panels;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class CellFormatPopup extends PopupPanel {

	public static final String BG_RED = "olap-background-red";
	public static final String BG_GREEN = "olap-background-green";
	public static final String BG_YELLOW = "olap-background-yellow";
	public static final String BG_BLUE = "olap-background-blue";
	public static final String BG_WHITE = "olap-background-white";
	public static final String BG_BLACK = "olap-background-black";
	
	public static final String TXT_BOLD = "olap-text-bold";
	public static final String TXT_NORMAL = "olap-text-normal";
	
	int xPos, yPos;
	String guid;
	Widget sender;  
	MenuBar menuBar;
	String returnStyle = "";
	
	public CellFormatPopup(String guid, int x, int y, Widget sender)
	{
		super(false, true);
		this.sender = sender;
		this.guid = guid;
		xPos = x;
		yPos = y;
		
		this.setPopupPosition(y, x);
		init();
	}
	
	public void init(){
		menuBar = new MenuBar(true);
		menuBar.setAutoOpen(true);
		
		MenuBar bgMenu = new MenuBar(true);
		bgMenu.addItem(new MenuItem ("Red",new CellFormatCommand (BG_RED)));
		bgMenu.addItem(new MenuItem ("Green",new CellFormatCommand (BG_GREEN)));
		bgMenu.addItem(new MenuItem ("Yellow",new CellFormatCommand (BG_YELLOW)));
		bgMenu.addItem(new MenuItem ("Blue",new CellFormatCommand (BG_BLUE)));
		bgMenu.addItem(new MenuItem ("White",new CellFormatCommand (BG_WHITE)));
		bgMenu.addItem(new MenuItem ("Black",new CellFormatCommand (BG_BLACK)));
		
		MenuBar fontMenu = new MenuBar(true);
		fontMenu.addItem(new MenuItem ("Bold",new CellFormatCommand (TXT_BOLD)));
		fontMenu.addItem(new MenuItem ("Normal",new CellFormatCommand (TXT_NORMAL)));
		
		menuBar.addItem(new MenuItem("Background", bgMenu));
		menuBar.addItem(new MenuItem("Font style", fontMenu));
		
		this.setWidget(menuBar);
		
	}
	
	public Widget getSource() {
		return sender;
	}

	public void setSource(Widget sender) {
		this.sender = sender;
	}

	
	
	public class CellFormatCommand implements Command
	{
		String style = "";
		
		public CellFormatCommand(String style){
			this.style = style;
		}
		public void execute() {
			Widget widget = getSource();
			widget.addStyleName(style);
			setReturnStyle(style);
			String oldStyle = new String("");
			
			if (style.contains("olap-text-"))
			{
				if (sender.getStyleName().contains("olap-background-"))
					oldStyle = sender.getStyleName().substring(sender.getStyleName().indexOf("olap-background-"), 
					sender.getStyleName().indexOf(" ", sender.getStyleName().indexOf("olap-background-")));
			}
			else
			{
				if (sender.getStyleName().contains("olap-text-"))
					oldStyle = sender.getStyleName().substring(sender.getStyleName().indexOf("olap-text-"), 
					sender.getStyleName().indexOf(" ", sender.getStyleName().indexOf("olap-text-")));
			}
			sender.setStyleName(oldStyle);
			sender.addStyleName(style);
			CellFormatPopup.this.hide();
		}
		
	}



	public String getReturnStyle() {
		return returnStyle;
	}

	public void setReturnStyle(String returnStyle) {
		this.returnStyle = returnStyle;
	}



}
