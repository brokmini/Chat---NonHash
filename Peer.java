import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.*;
import java.util.Random;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;

public class Peer
{
  static Hashtable<String,Socket> hashtable = new Hashtable<String,Socket>();
  static String name;
  public static void main(String[] args) throws IOException
  {
        File file = new File("peers.csv");
        BufferedReader reader = new BufferedReader(new FileReader("peers.csv"));
        PrintWriter pw = new PrintWriter(new FileWriter(file, true));
        Scanner scanner = new Scanner(System.in);
        //PrintWriter pw = new PrintWriter(new File("peers.csv"));
        String line = reader.readLine();
        //System.out.println(line);
        if (line==null || line.equals(""))
        {
          //If it is the first node to join the p2p network
          System.out.println("Hey! Choose username");
          name=scanner.nextLine();
          pw.write(name+",9000");
          pw.close();
          Integer listen_port = 9000;
          Listener  listener= new Listener(listen_port);
          listener.start();
        }
        else
        {
          //When peer is not the first to join the network
          Boolean nameSet = false;
          String userList="";
          String portNumbers="";
          while (!nameSet)
          {
                //Do until a unique name is chosen
                System.out.println("Choose username");
                //Scanner scanner = new Scanner(System.in);
                name=scanner.nextLine();
                BufferedReader reader1 = new BufferedReader(new FileReader("peers.csv"));
                portNumbers="";
                while ((line = reader1.readLine()) != null)
                {
                  userList = userList+ line.split(",")[0]+",";
                  portNumbers= portNumbers + line.split(",")[0]+":"+line.split(",")[1]+",";
                  if(line.split(",")[0].equals(name))
                  {
                    System.out.println("username already exists");
                    userList="";
                    nameSet = false;
                    break;
                  }
                  nameSet = true;
                }
          }

          Integer listen_port = null;
          Boolean portSet = false;
          while(!portSet)
          {
            Random r = new Random();
            listen_port = r.nextInt(9000-6000) + 6000;
            if (!(portNumbers.contains(Integer.toString(listen_port))))
              portSet=true;
          }

          reader.close();
          pw.write("\n"+name+","+Integer.toString(listen_port));
          pw.close();

          Listener  listener= new Listener(listen_port);
          listener.start();

          System.out.println("Users already in the chat room are: "+userList);
          System.out.println("How many of them do you want to connect with?");
          int n =scanner.nextInt();
          String catchEmpty = scanner.nextLine();
          for (int i=0;i<n;i++)
          {
            System.out.println("Enter a name you want to connect with");
            String friend = scanner.nextLine();
            if(portNumbers.contains(friend))
            {
              String temp = portNumbers.split(friend+":")[1];
              String friendPort = temp.split(",")[0];
              Integer connect_port = Integer.parseInt(friendPort);
              System.out.println("Your friend "+friend+ " is at "+friendPort);
              connect(connect_port,listen_port);
            }
          }
        }

  PingSender ps = new PingSender();
  ps.start();

  } //End of Main function

  //**********************************************************************************************************************************************
	//function that is called when a new node connects to existing node in the network
			public static void connect(Integer port,Integer myPort)
			{
				try
				{
				Socket socket = new Socket("localhost", port);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				out.write("CONNECTED WITH "+Peer.name+"\n");
				out.flush();

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String recdData = in.readLine();
        System.out.println(recdData);
				String username = recdData.split("CONNECTED WITH")[1];
        Peer.myHash(username,socket);

				PingListener p = new PingListener(socket,myPort,username);
				p.start();
				}
				catch (IOException e)
				{
					System.out.println("Connect Failed"); System.exit(-1);
				}
			}

//**********************************************************************************************************************************************
//This function handles the sending of Ping messages to all neighbors
public static int pingStatus()
{
          String connectedUsers = Peer.readHash();
          System.out.println("The connected users are - "+connectedUsers);
          String[] friendList = connectedUsers.split(",");
          for(int i =0;i<friendList.length;i++)
          {
            //System.out.println(fetchSocket(friendList[i]));
            Socket t = fetchSocket(friendList[i]);
            try
            {
                PrintWriter pOut = new PrintWriter(t.getOutputStream(), true);
                pOut.write("Hello from "+Peer.name+"\n");
                pOut.flush();

            }
            catch (NullPointerException e)
            {
              System.out.println("PING STATUS - "+friendList[i] +" has vanished");
              Peer.hashtable.remove(friendList[i]);
              //myList=Agent.readHash();
            }
            catch (IOException e) {}
          }
          return 1;
}
//**********************************************************************************************************************************************
//function to hash neighbor details
public static void myHash(String username,Socket s)
{
      				hashtable.put(username,s);
}
//**********************************************************************************************************************************************
//function to read neighbor details from hash
public static String readHash()
  {
              Set<String> keys = hashtable.keySet();
              Iterator<String> itr = keys.iterator();
              String availableUsers =""; int first=1;
              while (itr.hasNext())
              {
                // Getting Key
                String key = itr.next();
                if(first==1)
                {
                  availableUsers = availableUsers + key;
                  first = 0;
                }
                else
                {
                  availableUsers = availableUsers + ","+key;
                }
              }
      				return availableUsers;
  }
//**********************************************************************************************************************************************
//function to read neighbor details from hash
 public static Socket fetchSocket(String username)
 {
              Socket s = null;
              Set<String> keys = hashtable.keySet();
              Iterator<String> itr = keys.iterator();
              while (itr.hasNext())
              {
                // Getting Key
                String key = itr.next();
                if(key.equals(username))
                {
                  s= hashtable.get(key);
                }
              }
        return s;
  }
} // End of class Peer
//**********************************************************************************************************************************************
//This thread actively listens for any nodes connecting to it
class Listener extends Thread
{
			Integer port;
			public Listener(Integer p)
			{
				port = p;
			}
			public void run()
			{
						ServerSocket serverSocket = null;
						Socket clientSocket = null;
						try
						{
							serverSocket = new ServerSocket(port);
						}
						catch (IOException e)
						{
							System.out.println("Could not listen on port"); System.exit(-1);
						}

						while(true)
						{
									try
									{
										System.out.println("Looking out for new friends...");
										clientSocket = serverSocket.accept();

                    PongListener l=new PongListener(clientSocket);
										l.start();

                    //To advertise about self
										PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
										out.write("CONNECTED WITH "+Peer.name+"\n");
										out.flush();


									}
									catch (IOException e)
									{
										System.out.println("Accept failed"); System.exit(-1);
									}
						}
			}

} //End of Listener class
//***********************************************************************************************************************************************
class PingSender extends Thread
{
			public void run()
			{
				while(true)
				{
						try
						{
							Thread.sleep(15000);
							int pingDone=Peer.pingStatus();
						}
						catch(InterruptedException e)
		        {
		               System.err.println("IOException " + e);
		        }
				}
			}
}
//**********************************************************************************************************************************************
class PingListener extends Thread
{
				Socket socket;
				Integer hostPort;
        String friend;
				//Integer backupHost,my_no_files;

				public PingListener(Socket s,Integer l,String friend)
				{
					socket = s;
					hostPort=l;
          friend = friend;
				}

				public void run()
				{
								while(true)
								{
									try
									{
										BufferedReader pIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
										String ping=pIn.readLine();
										if (ping!= null && !ping.isEmpty())

												{
															//String myList= Agent.readHash();
															String[] pingMsg=ping.split("#");
															/*if (pingMsg.length>1)
															{
																//Identifies node to connect to incase of loss of neighbor
																backupHost=Integer.parseInt(pingMsg[1]);
															}*/
															if(pingMsg[0].contains("Hello from"))
															{
                                System.out.println(pingMsg[0]);
																PrintWriter pong = new PrintWriter(socket.getOutputStream(), true);
																pong.write("HelloAck from "+Peer.name+"\n");
																pong.flush();
															}


												}
									}//End of Try
									catch (Exception e)
									{
										//If the node to which I initially connected is dead, I maintain my presence in the sytem by reconnecting to another host
										System.out.println("Neighbor I initially connected with has vanished");
										//Peer.hashtable.remove(friend);
										/*Agent.myList=Agent.readHash();
										Reconnector r = new Reconnector(backupHost,Agent.listen_port,my_no_files);
										r.start();
										try
										{
											Thread.sleep(Long.MAX_VALUE);
										}
										catch(InterruptedException q)
										{
											System.out.println("Cdnt Sleep "+q);

										}*/

									}

								}
				}
}
//******************************************************************************************************************************************************************
class PongListener extends Thread
{

			Socket socket;
      String friend;
			public PongListener(Socket s)
				{
				socket=s;
				}
			public void run()
			{
				while(true)
				{
					try
					{
						BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						String recdData=in.readLine();
            //System.out.println("PONG LISTEN: "+recdData);
						if(recdData.contains("CONNECTED WITH "))
						{
							//Fetches details about a new node that connects with it and hashes the information
      				friend = recdData.split("CONNECTED WITH ")[1];
              Peer.myHash(friend,socket);
						}

						else if(recdData.contains("HelloAck"))
						{
							System.out.println(recdData);
						}
					} // End of Try
					catch(IOException e)
					{
							//In case a node that connected to it is dead
							System.out.println("PongListener - "+friend +" has vanished");
							Peer.hashtable.remove(friend);
							//Agent.myList=Agent.readHash();
							try
							{
								Thread.sleep(Long.MAX_VALUE);
							}
							catch(InterruptedException q)
							{
								System.out.println("Cdnt Sleep "+q);

							}
					}
				}
			}
}
//****************************************************************************************************************************************************
