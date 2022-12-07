//		Keenen Wood
//		  1510551

import java.net.*;
import java.io.*;
import java.util.concurrent.*; // wouldn't work with just "java.util*" not sure why

class HttpServer{
//################################################################
// Entry Point
   public static void main(String[] args){
      System.out.println("Searching");
      ExecutorService exec = Executors.newFixedThreadPool(10);
      try{
         ServerSocket httpServer = new ServerSocket(50505);			// Step 1 binds serversocket to port 50505
         Socket client = httpServer.accept();					// Step 2 Accepts a connection
         System.out.println("Connection Made");
         HttpServerSession session = new HttpServerSession(client);
         session.run();
         client.close();
      } catch(Exception e){ 
         System.out.println("Woopsie! That didn't work"); 
      }
   }
}



class HttpServerSession implements Runnable {
//################################################################
// Global Variables
   private Socket client;
   private HttpServerRequest hsrq;
   private String requ;
   private String resp;
   private String filename;
   private String hostname;
   
//################################################################
// Constructor   
   public HttpServerSession(Socket accptd){
      client = accptd;
   }
   
//################################################################
// Public Methods
   @Override
   public void run(){
      try{
         BufferedReader bufr = new BufferedReader(new InputStreamReader(client.getInputStream()));
         BufferedOutputStream bost = new BufferedOutputStream(client.getOutputStream());
         requ = bufr.readLine();					// Step 3 Read a line from the Http Request Header
         resp = "";
         hsrq = new HttpServerRequest();
         while(true){ 							// Step 7 of the Assignment: Open a loop
            hsrq.process(requ);					// Process the current request line
            if(!hsrq.isDone()){					// if the request has not been fully processed
               requ = bufr.readLine();				// read the next line of the request header
            } else { break; }						// otherwise break from the loop
         }
         filename = hsrq.getFile();					// Step 8 Return the requested file; get filename requested
         hostname = hsrq.getHost();					// get the hostname
         if(hostname.equals(null)){					// if a null value was returned,
            hostname = "localhost:50505";				// use local host
         }
         File file = new File(hostname + "/" + filename);		// Creates a file for the inputstream to read
         System.out.println(filename + "\n" + hostname);		// error checkiing
         if(file.isFile() == true){					// if there actually is a file 
            FileInputStream fstr = new FileInputStream(file);	// create an inputstream to open the file
            byte[] buff = new byte[1024];				// create a buffer to process the stream
            int reco = fstr.read(buff);				// read the file
            resp = "HTTP/1.1 200 OK\r\n";				// write a response header and
            println(bost, resp);					// send it
            while (reco != -1) {					// while there is more to read
               //Thread.sleep(1000);  // Step 10 slow connection
               bost.write(buff, 0, reco);				// send what has been read
               bost.flush();						// flush the outputstream
               reco = fstr.read(buff);				// read the next part of the file
            }
            bost.flush();						// flush the outputstream again
            fstr.close();						// close the file
         } else {							// Step 9 deal with missing files
            resp = "HTTP/1.1 404 Not Found\r\n";			// write a response header and
            println(bost, resp); 					// send it
            bost.write("404 File not found".getBytes());		// send text to let user know what went wrong
            System.out.println(resp);					// error checking
         }
         bost.flush();							// flush and close what's still running
         bufr.close();
         client.close();
      } catch(Exception e) { System.out.println("ERRR! Incorrect, try again "); }
   }
   
//################################################################
// Private Methods
/*
 * The "println" method makes a buffered outputstream comply with http requirements of
 * which a printwriter otherwise wouldn't
 *
 */
   private boolean println(BufferedOutputStream bost, String s) throws IOException {
      String send = s + "\r\n";				// adds CR and LF pair to response header
      byte[] buff = send.getBytes();				// converts the message into bytes and stores in an array for transmission
      System.out.println("\nResponse header : " + s);	// print response to console
      try{
         bost.write(buff, 0, buff.length); 			// send header
      }
      catch(IOException e){ return false; }
      return true;
   }   
}
