// Keenen Wood 1510551

import java.net.*;
import java.io.*;

class SimpleClient {
// ###################################################
// Public Methods
   public static void main(String[] args) {
      // Tells user how to use if not enough input is given at the command line
      if(args.length <= 1) {
         System.out.println("Usage: SimpleClient <hostName> <portNumber>");
      } else {
         // Declare variables
         String hostName;
         int portNo;
         try {
            // Stores input into declared variables
            hostName = args[0];
            portNo = Integer.parseInt(args[1]);
            // Creates and InetAddress to echo the servers IP and prints to the terminal
            InetAddress echoServerIP = InetAddress.getByName(hostName);         
            Socket clientSocket = new Socket(echoServerIP, portNo);
            // Creates a reader to read the text sent from the server to this clients socket
            BufferedReader reader = new BufferedReader(new  InputStreamReader(clientSocket.getInputStream()));
            String line;
            // reads and prints the messages sent from the server until no more lines exist
            while ((line = reader.readLine()) != null){
               System.out.println(line);
            }
            // closes the reader
            reader.close();
         // catches any input/output exceptions
         } catch(IOException e) {
            System.out.println("Cannot conneect to server");
         }
      }
   }
}
