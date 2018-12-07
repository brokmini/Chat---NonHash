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
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.lang.StringBuffer;

class CommandListener extends Thread
{
			//Integer myID;

			public CommandListener()
			{
				//myID=a;
			}

			public void run()
			{
				while(true)
				{
					Scanner scanner = new Scanner(System.in);
					while(scanner.hasNextLine())
					{
								String command=scanner.nextLine();

								if(command.equals("find friend"))
										{
                      String line="";
                      try
                      {
                        BufferedReader reader2 = new BufferedReader(new FileReader("peers.csv"));
                        String newPortNumbers=""; String newUserList="";
                        while ((line = reader2.readLine()) != null)
                        {
                          String readname= line.split(",")[0];
                          int flag = Peer.isFriend(readname);
                          if(flag==0) //check if not one of your existing connections
                          {
                            if(!(readname.equals(Peer.name)))
                            {
                              newUserList = newUserList+ readname+",";
                              newPortNumbers= newPortNumbers + readname+":"+line.split(",")[1]+",";
                            }
                          }
                        }
                        if (!newUserList.equals(""))
                        {
                          System.out.println("NEW USERS IN THE CHAT ROOM ARE - "+newUserList);
                          System.out.println("How many of them do you want to connect with?");
                          int n =scanner.nextInt();
                          String catchEmpty = scanner.nextLine();
                          for (int i=0;i<n;i++)
                          {
                            System.out.println("Enter a name you want to connect with");
                            String friend = scanner.nextLine();
                            if(newPortNumbers.contains(friend))
                            {
                              String temp = newPortNumbers.split(friend+":")[1];
                              String friendPort = temp.split(",")[0];
                              Integer connect_port = Integer.parseInt(friendPort);
                              System.out.println("Your friend "+friend+ " is at "+friendPort);
                              Peer.connect(connect_port,Peer.myListenPort);
                            }
                          }
                        }
                        else
                        {
                          System.out.println("No new users");
                        }
                      }
                      catch(IOException e) {}


										} // End of IF for find friend

                    if(command.contains("broadcast"))
                    {
                      String message = command.split("broadcast ")[1];
                      String connectedUsers;
                      connectedUsers = Peer.readHash();
                      //if(connectedUsers.equals(""))
                      String[] friendList = connectedUsers.split(",");
                      for(int i =0;i<friendList.length;i++)
                      {
                        Socket t = Peer.fetchSocket(friendList[i]);
                        try
                        {
                            PrintWriter pOut = new PrintWriter(t.getOutputStream(), true);
                            pOut.write("Broadcast from "+Peer.name+": "+message+"\n");
                            pOut.flush();

                        }
                        catch (NullPointerException e){}
                        catch (IOException e) {}
                      }

                    } // End of If for broadcast

                    if(command.contains("chat"))
                    {
                      String details = command.split("chat ")[1];
                      String meta = details.split("@ ")[1];
                      String friend = meta.split(" ",2)[0];
                      String message = meta.split(" ",2)[1];
                      Socket t = Peer.fetchSocket(friend);

                        try
                        {
                            String encryptedMsg = Peer.encryptMessage(message, Peer.privateKey);
                            PrintWriter pOut = new PrintWriter(t.getOutputStream(), true);
                            pOut.write("Message from "+Peer.name+": "+encryptedMsg+"\n");
                            pOut.flush();

                        }
                        catch (NullPointerException e){}
                        catch (IOException e) {}
                        catch (Exception e) {System.out.println("Error while encrypting");}

                    } // End of If for broadcast

                    if (command.contains("send file"))
                    {
                      System.out.println("Whom do you want to send file to?");
                      String friend = scanner.nextLine();
                      Socket s = Peer.fetchSocket(friend);

                      try
                      {
                        File file = new File("send\\apple.txt");
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        StringBuilder sb = new StringBuilder();
                        String line = br.readLine();
                        while (line != null)
                        {
                          sb.append(line).append("#");
                          line = br.readLine();
                        }
                        String fileAsString = sb.toString();
                        PrintWriter pOut = new PrintWriter(s.getOutputStream(), true);
                        pOut.write(fileAsString+"\n");
                        pOut.flush();

                      }
                      catch(FileNotFoundException e){}
                      catch(IOException e){}


                    }
				}
				}

			}
}
