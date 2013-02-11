/**
 * Java Version & Vendor Display
 *
 * Author: http://javatester.org/about.html 
 *****************************************************************************/

import java.util.*;
import java.io.*;
import java.text.*;


public class JavaVersionDisplay  { 
   
   public static void main(String[] args) {
       System.out.println("Java Version: " + System.getProperty("java.version") + " from " + System.getProperty("java.vendor"));
   }

}