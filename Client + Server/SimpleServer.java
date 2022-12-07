// Keenen Wood 1510551

import java.net.*;
import java.io.*;

class SimpleServer {
// ###################################################
// Public Methods
   public static void main(String[] args) {
      try {
         // creates a server socket that will bind to the first free port
         ServerSocket echoServer = new ServerSocket (0);
         // prints out the relevant information to the terminal
         System.out.println("Listening on port - " + echoServer.getLocalPort());
         InetAddress IP = InetAddress.getLocalHost();
         // creates an infinite loop to turn the server "on", clients can connect 
         while(true) {
            // creates an instance of the client to form the connection
            Socket client =  echoServer.accept();
            InetAddress clientIP = client.getInetAddress();
            String IPClient = clientIP.getHostAddress();
            // Created a writing object to send messages through to the client
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            //Write a message to send to the echo client
            writer.write("Hello, " + clientIP.getHostName() + "\nYour IP address is " + IPClient + "\n");
            //Close the client after sending the message
            writer.flush();
            writer.close();
            client.close();
         }
      } catch(Exception e) {
         System.out.println("Error! try again");
      }
   }
}
