import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.*;
import java.util.Random;

public class Peer
{
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
          String name=scanner.nextLine();
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
          String userList="",name="";
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


  } //End of Main function

  //**********************************************************************************************************************************************
	//function that is called when a new node connects to existing node in the network
			public static void connect(Integer port,Integer myPort)
			{
				try
				{
				Socket socket = new Socket("localhost", port);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				out.write(String.valueOf(myPort)+" Connect \n");
				out.flush();
				//System.out.println("I am trying to connect: "+socket);

				MessageListener p = new MessageListener(socket);
				p.start();
				}
				catch (IOException e)
				{
					System.out.println("Connect Failed"); System.exit(-1);
				}
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
										System.out.println("Listening...");
										clientSocket = serverSocket.accept();
										MessageListener l=new MessageListener(clientSocket);
										l.start();

                    //To advertise about self
										PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
										out.write(String.valueOf(port)+" Init\n");
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

class MessageListener extends Thread
{

			Socket socket;
			public MessageListener(Socket s)
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
            System.out.println("MESSAGE RECEIVED IS : \n");
            System.out.println(recdData);
          } //End of Try
          catch(IOException e)
  				{
          }
        } //End of while

      } // End of run
} // End of MessageListener Class
