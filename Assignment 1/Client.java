import java.net.Socket;
import java.net.InetAddress;
import java.io.*;

class Client {

// ###################################################
// Public Methods

   public static void main(String[] args) {
      try {
         String hostName = args[0];
         InetAddress echoServerIP = InetAddress.getByName(hostName);
// connects the client to the server by creating the servers socket
         Socket serverSocket = new Socket(echoServerIP, 0);
// creates a reader object to read lines from the server   
         BufferedReader reader = new BufferedReader(
new InputStreamReader(serverSocket.getInputStream()));
         String line = reader.readLine();
// prints whatever it has read from the server
      System.out.println(line);
      } catch(Exception e) {}
   }
}
