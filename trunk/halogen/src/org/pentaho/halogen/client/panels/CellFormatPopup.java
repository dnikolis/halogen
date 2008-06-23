package org.pentaho.halogen.client.panels;
 
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class CellFormatPopup extends PopupPanel {
		
	public static final String COLOR_RED = "red";
	public static final String COLOR_GREEN = "green";
	public static final String COLOR_YELLOW = "yellow";
	public static final String COLOR_BLUE = "blue";
	public static final String COLOR_WHITE = "white";
	public static final String COLOR_BLACK = "black";
	
	public static final String STYLE_BOLD = "bold";
	public static final String STYLE_NORMAL = "normal";
	
	public static final String DECORATION_OUTLINE = "outline";
	public static final String DECORATION_UNDERLINE = "underline";
	
	
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
		
//		com.google.gwt.user.client.Element rootNode = DOM.getElementById("rootNode");
//		com.google.gwt.user.client.Element caption = DOM.createElement("<P>");
//		com.google.gwt.user.client.DOM.setInnerText(caption, "MERDA!");
//		com.google.gwt.user.client.DOM.appendChild(rootNode, caption);
		
		
		
		MenuBar bgMenu = new MenuBar(true);
		bgMenu.addItem(new MenuItem ("Red",new CellFormatCommand (CellFormatType.BACKGROUND, COLOR_RED)));
		bgMenu.addItem(new MenuItem ("Green",new CellFormatCommand (CellFormatType.BACKGROUND, COLOR_GREEN)));
		bgMenu.addItem(new MenuItem ("Yellow",new CellFormatCommand (CellFormatType.BACKGROUND, COLOR_YELLOW)));
		bgMenu.addItem(new MenuItem ("Blue",new CellFormatCommand (CellFormatType.BACKGROUND, COLOR_BLUE)));
		bgMenu.addItem(new MenuItem ("White",new CellFormatCommand (CellFormatType.BACKGROUND, COLOR_WHITE)));
		bgMenu.addItem(new MenuItem ("Black",new CellFormatCommand (CellFormatType.BACKGROUND, COLOR_BLACK)));
		
		MenuBar fgMenu = new MenuBar(true);
		fgMenu.addItem(new MenuItem ("Red",new CellFormatCommand (CellFormatType.FOREGROUND, COLOR_RED)));
		fgMenu.addItem(new MenuItem ("Green",new CellFormatCommand (CellFormatType.FOREGROUND, COLOR_GREEN)));
		fgMenu.addItem(new MenuItem ("Yellow",new CellFormatCommand (CellFormatType.FOREGROUND, COLOR_YELLOW)));
		fgMenu.addItem(new MenuItem ("Blue",new CellFormatCommand (CellFormatType.FOREGROUND, COLOR_BLUE)));
		fgMenu.addItem(new MenuItem ("White",new CellFormatCommand (CellFormatType.FOREGROUND, COLOR_WHITE)));
		fgMenu.addItem(new MenuItem ("Black",new CellFormatCommand (CellFormatType.FOREGROUND, COLOR_BLACK)));
		
		MenuBar styleMenu = new MenuBar(true);
		styleMenu.addItem(new MenuItem ("Bold", new CellFormatCommand (CellFormatType.STYLE, STYLE_BOLD)));
		styleMenu.addItem(new MenuItem ("Normal", new CellFormatCommand (CellFormatType.STYLE, STYLE_NORMAL)));
		styleMenu.addItem(new MenuItem ("Underline", new CellFormatCommand (CellFormatType.DECORATION, DECORATION_UNDERLINE)));
		styleMenu.addItem(new MenuItem ("Outline", new CellFormatCommand (CellFormatType.DECORATION, DECORATION_OUTLINE)));
		
		MenuBar sizeMenu = new MenuBar(true);
		sizeMenu.addItem(new MenuItem ("70%", new CellFormatCommand (CellFormatType.SIZE, "70%")));
		sizeMenu.addItem(new MenuItem ("80%", new CellFormatCommand (CellFormatType.SIZE, "80%")));
		sizeMenu.addItem(new MenuItem ("90%", new CellFormatCommand (CellFormatType.SIZE, "90%")));
		sizeMenu.addItem(new MenuItem ("100%", new CellFormatCommand (CellFormatType.SIZE, "100%")));
		sizeMenu.addItem(new MenuItem ("110%", new CellFormatCommand (CellFormatType.SIZE, "110%")));
		sizeMenu.addItem(new MenuItem ("120%", new CellFormatCommand (CellFormatType.SIZE, "120%")));
		sizeMenu.addItem(new MenuItem ("130%", new CellFormatCommand (CellFormatType.SIZE, "130%")));
		
		MenuBar familyMenu = new MenuBar(true);
		familyMenu.addItem(new MenuItem ("Monospace", new CellFormatCommand (CellFormatType.FAMILY, "monospace")));
		familyMenu.addItem(new MenuItem ("Arial", new CellFormatCommand (CellFormatType.FAMILY, "arial")));
		familyMenu.addItem(new MenuItem ("Sans-serif", new CellFormatCommand (CellFormatType.FAMILY, "sans-serif")));
		familyMenu.addItem(new MenuItem ("Courier", new CellFormatCommand (CellFormatType.FAMILY, "courier")));
		
		
		

		menuBar.addItem(new MenuItem("Size", sizeMenu));
		menuBar.addItem(new MenuItem("Foreground", fgMenu));
		menuBar.addItem(new MenuItem("Background", bgMenu));
		menuBar.addItem(new MenuItem("Font style", styleMenu));
		menuBar.addItem(new MenuItem("Family", familyMenu));
		
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
		String value = "";
		CellFormatType type;
		
		public CellFormatCommand(CellFormatType type, String value){
			this.value = value;
			this.type = type;
			
		}
		public void execute() {
			//Widget widget = getSource();
			
			if (type == CellFormatType.FOREGROUND)
				DOM.setStyleAttribute(sender.getElement(), "color", value);
			else if (type == CellFormatType.BACKGROUND)
				DOM.setStyleAttribute(sender.getElement(), "background", value);
			else if (type == CellFormatType.DECORATION)
				DOM.setStyleAttribute(sender.getElement(), "textDecoration", value);
			else if (type == CellFormatType.STYLE)
				DOM.setStyleAttribute(sender.getElement(), "fontWeight", value);
			else if (type == CellFormatType.SIZE)
				DOM.setStyleAttribute(sender.getElement(), "fontSize", value);
			else if (type == CellFormatType.FAMILY)
				DOM.setStyleAttribute(sender.getElement(), "fontFamily", value);

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
