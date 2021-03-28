import java.net.Socket;
import java.net.InetAddress;
import java.io.*;

class ChatClient {
   public static void main(String[] args) {   
      try {
//         InetAddress group = InetAddress.getByName(239.0.202.1);
// Initialises a multicast socket listening
         MulticastSocket multi = new MulticastSocket(40202);
// connects a new chat to the multicast socket
         MulticastSocket chat = multi.accept();
// creates an object to read from the keyboard
         BufferedReader reader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
         String line = reader.readLine();
// packages up the message and reads it
         DatagramPacket dp = new DatagramPacket(line.getBytes(), line.length);
         ms.receive(dp); 
// creates a reader object to read lines from the server   
         String msg = "SUB UCE";
         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
         writer.write("0800-CHUR-TO-DACUZZIES")      DatagramPacket packet = new DatagramPacket(msg.getBytes, msg.length);
         multi.send(packet);
      } catch(Exception e) { System.out.println("ERROR") } 
   }
}
