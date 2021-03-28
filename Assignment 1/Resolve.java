// Keenen Wood 1510551

// Q3 Resolve.class

import java.net.InetAddress;

class Resolve {
// ###############################################################
// Global Variables
   String hostName = "";

// ###############################################################
// Public Methods

/* this tells the compiler where to start */
   public static void main (String[] args) {
      Resolve program = new Resolve();
      program.run(args);
   }

/* overcomes the non static variable error in the main method*/
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
         InetAddress IP = InetAddress.getByName(hostName);
// prints the host name and the IP address (in a string format) to the user
         System.out.println(hostName + " : " + IP.getHostAddress());
// catches any errors in the host name
      } catch (Exception e) {
         System.out.println(hostName + " : unknown host");   
      }   
      return;
   }
}
