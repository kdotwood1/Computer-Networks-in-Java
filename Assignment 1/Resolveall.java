// Keenen Wood 1510551

// Q5 I tried www.netflix.com & www.youtube.com which 8 and 6 IPv4 respectively

import java.net.InetAddress;

class Resolveall {
// ###############################################################
// Global Variables
   String hostName = "";

// ###############################################################
// Public Methods

/* this tells the compiler where to start */
   public static void main (String[] args) {
      Resolveall program = new Resolveall();
      program.run(args);
   }

/* overcomes the non static variable error in the main method */
   public void run (String[] args) {
      for (int i = 0; i < args.length; i++) {
         resolveIPAddress(args[i]);
      }
      return;
   }

// ###############################################################
// Private Methods

/* this method takes a string variable and searches for its corresponding IP address */
   private void resolveIPAddress (String hostName){
      try {
// gets the IP address associated with InetAddress
         InetAddress[] IP = InetAddress.getAllByName(hostName);
// prints the host name and the IP address (in a string format) to the user
         for (int i = 0; i < IP.length; i++) {
            System.out.println(hostName + " : " + IP[i].getHostAddress());
         }
// catches any errors in the host name
      } catch (Exception e) {
         System.out.println(hostName + " : unknown host");   
      }   
      return;
   }
}
