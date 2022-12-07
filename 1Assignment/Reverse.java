// Keenen Wood 1510551

import java.net.InetAddress;
import java.io.*;
import java.net.*;

public class Reverse {
// ###################################################
// Public Methods
   public static void main(String[] args) {
      // Declares host name and host address variables and initialises them
      String hostAddress = "";
      // Handles user not providing any website names to resolve
      if(args.length == 0) {
         System.out.println("Usage: Reverse <IP1> <IP2>...<IPN>");
      } else {
         // Handles problematic input
         try {
            // This for-loop helps with multiple website names to resolve
            for (int i = 0; i < args.length; i++) {
               // assigns the value in the command line input at position i in the loop to the hostname
               hostAddress = args[i];
               InetAddress IP = InetAddress.getByName(hostAddress);
               // checks to see if the IP address has a name (by checking if it is not equal to nothing)
               if (!IP.getHostName().equals(hostAddress)) {
                  System.out.println(hostAddress + " : " + IP.getHostName());
               // else print out that the ip address doesnt resolve to a DNS name
               } else {
                  System.out.println(hostAddress + " : no name");
               }
            }
         // catches any bad input and throws exception
         } catch (UnknownHostException e) {
            System.out.println(hostAddress + " : unknown host");               
         }
      }
   }   
}
