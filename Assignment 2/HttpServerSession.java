import java.net.*;
import java.io.*;
import java.util.*;
class HttpServerSession implements Runnable
{
// ###############################################
// Private Global Variables
private Socket _accepted;

// ###############################################
// Constructor
   public void HttpServerSession(Socket accepted)
   {
      _accepted = new Socket(accepted);
   } 

}
