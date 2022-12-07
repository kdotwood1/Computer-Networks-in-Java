//		Keenen Wood
//		  1510551

class HttpServerRequest{
//################################################################
// Private Variables
   private String file = null;
   private String host = null;
   private boolean done = false;
   
//################################################################
// Getters
   public boolean isDone() { return done; }
   public String getFile() { return file; }
   public String getHost() { return host; }

//################################################################
//Public Methods
/*
 * Step 5 Parse the request
 * The 'process' method takes a single line of a Http request header and 
 * checks to see if it starts with 1."GET", 2."Host: ", or 3."" and processes 
 * accordingly
 *
 */
   public void process(String request){
      if(request==null){
         done = true;
         return;
      }
      if(request.startsWith("GET")){			// 1.
         System.out.println(request);							
         String parts[] = request.split(" ");		// splits the GET line by it's three parts and stores it in an array
         if (parts[1].equals("")) {
            return;
         }
         if (parts.length == 3){			// checks that there is three parts        
            file = parts[1].substring(1);		// removes the "/" at the beginning of part #2 and stores it as the filename
            if(file.equals("")){			// if there is no specified file
               file = "/index.html";			// store "/index.html" which acts as my homepage 
               System.out.println(file);		// E = (error checking console print)
               return;
            } else if(file.endsWith("/")){		// if the file ends with a "/"
               file += "index.html";			// append "index.html" to the filename
               System.out.println(file);		// E
               return;	
            } else {
               System.out.println(file);		// E
               return;					// otherwise keep file as is and return
            }
         } else { return; } // ill-formed get
      } else if(request.startsWith("Host: ")){	// 2.
         System.out.println(request);			// E
         if (request.substring(6).equals("")) {	// if there is no host specified leave as null
            host = "";
            return; 
         } else {
            host = request.substring(6);		// otherwise remove "Host: " from the request and store what's left as the host
            return;
         }
      } else if(request.equals("")){			// 3.
         System.out.println(request);			// E
         done = true;					// An empty line in the header means we are done so we change done to true so isDone() returns true
         return;      
      } else { return; }				// 4. it is not a line from the Http request header that we care about so just return
   }
}
