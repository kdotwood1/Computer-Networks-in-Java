// Keenen Wood 1510551

import java.net.InetAddress;

class Reverse {
// ###############################################################
// Global Variables
   String hostAddress = "";

// ###############################################################
// Public Methods

/* this tells the compiler where to start */
   public static void main (String[] args) {
      Reverse program = new Reverse();
      program.run(args);
   }

/* overcomes the non static variable error in the main method*/
   public void run (String[] args) {
      for (int i = 0; i < args.length; i++) {
         resolveHostName(args[i]);
      }
      return;
   }

// ###############################################################
// Private Methods

/* this method takes a string variable of the IP address and searches for its corresponding host name */
   private void resolveHostName (String hostAddress){
      try {
// gets the IP address associated with InetAddress
         InetAddress IP = InetAddress.getByName(hostAddress);
// prints the host name and the IP address (in a string format) to the user
         if (!IP.getHostName().equals(hostAddress)) {
            System.out.println(hostAddress + " : " + IP.getHostName());
         } else {
            System.out.println(hostAddress + " : no name");
         }
// catches any errors in the host name
      } catch (Exception e) {
         System.out.println(hostAddress + " : unknown IP address");   
      }   
      return;
   }
}
