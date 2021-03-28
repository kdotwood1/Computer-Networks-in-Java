import java.net.*;
import java.io.*;

class SimpleServer {
// ###################################################
// Global Variables
 
// ###################################################
// Public Methods

   public static void main(String[] args) {
      try {
// creates a server called "echoserver" bound to a port (the argument is the port number)
         ServerSocket echoServer = new ServerSocket (0);
         System.out.println("Server is waiting for the connection...");
// forces the program to wait for a new connection, then stores it in the socket variable named "client"
         Socket client =  echoServer.accept();
         System.out.println("Here comes the new connection...");
// obtain ip address
      InetAddress clientIP = client.getInetAddress();
// creates a writer to send lines to the the client     
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
// greets the client by resolving the domain name
      writer.write("Hello" + clientIP.getHostName() + "\n" + "Your IP address is " + clientIP + "\n");
      writer.flush();
      client.close();
      } catch(Exception e) {}
   }
}
