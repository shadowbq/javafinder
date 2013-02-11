/**
 * Java Version & Vendor Display
 *
 * Author: http://javatester.org/about.html 
 *****************************************************************************/

import java.applet.*;
import java.awt.*;

public class JavaVersionDisplayApplet extends Applet { 
   private Label m_labVersionVendor; 
   
   //constructor
   public JavaVersionDisplayApplet()  { 
	 Color colFrameBackground = Color.pink;
     this.setBackground(colFrameBackground);
     m_labVersionVendor = new Label (" Java Version: " +
                                    System.getProperty("java.version") +
                                    " from " + 
									System.getProperty("java.vendor"));
     this.add(m_labVersionVendor);
   }
 
}