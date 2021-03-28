import java.net.*;
import java.io.*;
import java.util.*;
class HttpServer
{
   public static void main(String args[])
   {
      try {
         ServerSocket httpServer = new ServerSocket (8080);
         Socket client =  httpServer.accept();
         System.out.println("Connection made");
      } catch(Exception e) {}
   }
} 
