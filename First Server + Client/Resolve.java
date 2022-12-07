// Keenen Wood 1510551

import java.net.Socket;
import java.net.InetAddress;
import java.io.*;

class Resolve {
// ###################################################
// Main Method
   public static void main(String[] args) {
      // Declares host name variable and initialises it
      String hostName = null;
      // Handles user not providing any website names to resolve
      if(args.length == 0) {
         System.out.println("Usage: Resolve <name1> <name2>...<nameN>");
      } else {
         // Handles problematic input
         try {
            // This for-loop helps with multiple website names to resolve
            for (int i = 0; i < args.length; i++) {
               // Assigns the value in the command line input at position i in the loop to the hostname
               hostName = args[i];
               // Resolve the IPv4 and IPv6 addresses and store it in array
               InetAddress[] IP = InetAddress.getAllByName(hostName);
               // Loop through, possibly, multiple ip addresses from the same DNS name
               for (int j = 0; j < IP.length; j++) {
                  System.out.println(hostName + " : " + IP[j].getHostAddress());
               }
            }
         } catch(Exception e){
            System.err.println(hostName + " : unknown host");
         }
      }
   }
}
