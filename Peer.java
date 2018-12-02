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
        //PrintWriter pw = new PrintWriter(new File("peers.csv"));
        String line = reader.readLine();
        //System.out.println(line);
        if (line==null || line.equals(""))
        {
          //If it is the first node to join the p2p network
          System.out.println("Hey! Choose username");
          Scanner scanner = new Scanner(System.in);
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
                Scanner scanner = new Scanner(System.in);
                name=scanner.nextLine();
                BufferedReader reader1 = new BufferedReader(new FileReader("peers.csv"));
                portNumbers="";
                while ((line = reader1.readLine()) != null)
                {
                  userList = userList+ line.split(",")[0]+",";
                  portNumbers= portNumbers + line.split(",")[1]+",";
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

          //System.out.println("Port chosen "+result);

          //System.out.println("Users already in the chat room are: "+userList);
          reader.close();
          pw.write("\n"+name+","+Integer.toString(listen_port));
          pw.close();
          //System.out.println("Used port numbers ");
          //System.out.println(portNumbers);

          Integer connect_port = 9000;
          Listener  listener= new Listener(listen_port);
          listener.start();
          connect(connect_port,listen_port);
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
