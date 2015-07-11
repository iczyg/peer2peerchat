
import java.io.*;
import java.net.*;
import java.util.*;

class Global {
        public static int color;
        public static String mycolor;
      	public static ArrayList<String>IPlist = new ArrayList<String>();
	    public static ArrayList<String> nameList = new ArrayList<String>();
	    public static ArrayList<Integer> portList = new ArrayList<Integer>();
}

public class Peer{ 
    // main
   static String name;
  static int selfPort;
  
    public static void main (String argv[]) throws Exception{
	
	if(argv.length != 4){
	if(argv.length == 3){
		Global.color = 0;
		}else{
            System.out.println("Usage: IP NAME ListenPort [ChatColor]");
            System.exit(1);
		}
        }

	name=argv[1];
	if(argv.length == 4){
      Global.color = Integer.parseInt(argv[3]);  
		}  
    selfPort=Integer.parseInt(argv[2]);
	//create outBound Socket+Handler and spawn thread for it
	OutboundHandler outHandler = new OutboundHandler();
	//create a Thread by passing it an OuboundHandler object
	Thread outThread = new Thread(outHandler);
	//start the thread, aka start run() method of that thread
	outThread.start();
	//create server socket
	ServerSocket listenSock = new ServerSocket(Integer.parseInt(argv[2]), 0, InetAddress.getByName(null));
	System.out.println("Welcome to the Peer to Peer Chat");
	//Create Server loop to process inbound connections
	while(true){
	    //accept an incoming client by saving it to a socket to use to communicate with it
	    Socket clientSock = listenSock.accept();
	        
	    //create a thread to handle the inbound client  so that we can return to accept()
	    InboundHandler inboundMsg = new InboundHandler(clientSock); // listens to the peers outbound socket
	    Thread inThread = new Thread(inboundMsg);
	    inThread.start();
	    // add the peer to the list. 
	}
    }
}

    final class OutboundHandler implements Runnable{
	String Username=Peer.name;
    int ownPort=Peer.selfPort;

	public void run(){
	    String line;     
	    Scanner fileIn=null;
	    try{
		fileIn = new Scanner(new File("peers.txt")); // client opens the list of known 
	    }
	    catch(FileNotFoundException e){System.out.println("The source file does not exist. " + e);} 
	    while(fileIn.hasNextLine()) {
		line=fileIn.nextLine();
                String[] inputs=line.split(" ");
		Global.IPlist.add(inputs[0]);
		Global.nameList.add(inputs[1]);
		Global.portList.add(Integer.parseInt(inputs[2]));
	    }
	    Scanner s=null;
    s=new Scanner(System.in);
    String in = Username + " has joined the chat!\n";
    for(int i=0; i<Global.portList.size(); ++i){
      try{
        in = "127.0.0.1" + " " + Username + " " + ownPort ;
        Socket outSock=new Socket("localhost",Global.portList.get(i));
        PrintWriter out= new PrintWriter(outSock.getOutputStream(), true);
        try {
            // thread to sleep for  buffer
            Thread.sleep(50);
         } catch (Exception e) {
              System.out.println(e);
           }
        out.println(in);
        outSock.close();
        in = Username + " has joined the chat!\n";
        outSock=new Socket("localhost",Global.portList.get(i));
        out= new PrintWriter(outSock.getOutputStream(), true);
        out.println(in);
        outSock.close();
      }
      catch(UnknownHostException e){
      }
      catch(IOException e){
      }
    }
    //send message with colors
      String my_ANSI;
    while(true){
      s=new Scanner(System.in);
      in=s.nextLine();
     switch (Global.color){
   case 1: Global.mycolor = "Red";
           my_ANSI = "\u001B[31m";
           break;
   case 2: Global.mycolor = "Black";
           my_ANSI = "\u001B[30m";
           break;
   case 3: Global.mycolor = "Green";
           my_ANSI = "\u001B[32m";
           break;
   case 4: Global.mycolor = "Yellow";
           my_ANSI = "\u001B[33m";
           break;
   case 5: Global.mycolor = "Blue";
           my_ANSI = "\u001B[34m";
           break;
   case 6: Global.mycolor = "Purple";
           my_ANSI = "\u001B[35m";
           break;
   case 7: Global.mycolor = "Cyan";
           my_ANSI = "\u001B[36m";
           break;
   case 8: Global.mycolor = "White";
           my_ANSI = "\u001B[37m";
           break;
   default: my_ANSI = "\u001B[0m";
           break;
}


      if(in.equals("exit")){
        in = " has left the chat!\n";
        for(int i=0; i<Global.portList.size();++i){
          
          //outbound when chat peer is leaving
          try{
            Socket outSock=new Socket("localhost",Global.portList.get(i));
            PrintWriter out= new PrintWriter(outSock.getOutputStream(), true);
            out.format("%s\n",Username);
            out.println(in);
            outSock.close();
          }
          catch(UnknownHostException e){
          }
          catch(IOException e){
          }
        }
        System.exit(0);
      }else{
        for(int i=0; i<Global.portList.size();++i){
          
          //outbound messages from peer to peer
          try{
            Socket outSock=new Socket("localhost",Global.portList.get(i));
            PrintWriter out= new PrintWriter(outSock.getOutputStream(), true);
            out.format("%s:\n",Username);
            out.println(my_ANSI + in + "\u001B[0m");
            outSock.close();
          }
          catch(UnknownHostException e){
          }
          catch(IOException e){
          }
        }
      }
    }
	}
    }
    
    final class InboundHandler implements Runnable{
  Socket socket;  
  public InboundHandler(Socket socket) throws Exception{
    this.socket=socket;
    int newPort=0;
  }
  public void run(){
    try{
      PrintWriter out=new PrintWriter(this.socket.getOutputStream(), true);
      BufferedReader in= new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
      String s=in.readLine();
      String[] inputs=s.split(" ");// this section checks to see if the connecting peer is a new peer, or an already dcoumeneted peer
      if(inputs[0].equals("127.0.0.1")){
        boolean duplicate=false;
        for(int i=0;i<Global.portList.size();i++){
          if(Global.portList.get(i)==Integer.parseInt(inputs[2])){
            duplicate=true;
          }
        }
        if(duplicate==false){// adds to the list of peers that can be connected to
         Global.IPlist.add(inputs[0]);

		 Global.nameList.add(inputs[1]);
		 Global.portList.add(Integer.parseInt(inputs[2]));
        }
        try {
            // thread to sleep for  buffer
            Thread.sleep(50);
         } catch (Exception e) {
              System.out.println(e);
           }
          PrintWriter fileOut = new PrintWriter(new FileWriter("peers.txt"));// saves the list of known peers as described by peers.txt
          for(int i=0;i<Global.portList.size();i++){
            fileOut.println(Global.IPlist.get(i)+ " " + Global.nameList.get(i) + " " +Global.portList.get(i));
          }
         fileOut.close();
        s=in.readLine();
        inputs=null;
      }
      else{
      System.out.format("%s ",s);
        if(in.ready()){
      s=in.readLine();
      System.out.println(s);
          out.close();
        }
      }
    }
    catch(IOException e){
    }
  }
    }