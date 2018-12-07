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

class PingListener extends Thread
{
				Socket socket;
        String friend;
				//Integer backupHost,my_no_files;

				public PingListener(Socket s,String username)
				{
					socket = s;
          friend = username;
				}

				public void run()
				{
								while(true)
								{
									try
									{
										BufferedReader pIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    InputStream is = socket.getInputStream();
                    //OutputStream out = null;
                    //byte[] bytes = new byte[16*1024];
                    //System.out.println("Number of bytes - "+ in.read(bytes));
										String ping=pIn.readLine();
										if (ping!= null && !ping.isEmpty())
												{
                            if(ping.contains("CONNECTED WITH "))
                              {
                                //Fetches details about a new node that connects with it and hashes the information
                                String meta = ping.split("CONNECTED WITH ")[1];
                                friend = meta.split(" #### ")[0];
                                System.out.println(ping.split(" #### ")[0]);
                                String recdkey=ping.split(" #### ")[1];
                                PeerDetails p = new PeerDetails();
                                p.socket = socket;
                                p.pubKey = Peer.retrievePublicKey(recdkey);
                                Peer.myHash(friend,p);
                              }

														if(ping.contains("Hello from"))
															{
                                //friend = ping.split("Hello from")[1]
                                System.out.println(ping);
																PrintWriter pong = new PrintWriter(socket.getOutputStream(), true);
																pong.write("HelloAck from "+Peer.name+"\n");
																pong.flush();
															}

                              if(ping.contains("Broadcast from ")||ping.contains("HelloAck from"))
                              {
                                System.out.println(ping);
                              }

                              if(ping.contains("Message from"))
                              {
                                String encryptedMsg = (ping.split(":")[1]).replaceAll(" ","");
                                try
                                {
                                  PublicKey pkey = Peer.fetchPublicKey(friend);
                                  String decryptMessage = Peer.decryptMessage(encryptedMsg, pkey);
                                  System.out.println(friend+":"+decryptMessage);
                                }
                                catch(Exception e)
                              {
                                System.out.println("Error while decrypting");
                                e.printStackTrace();
                              }

                              }

                              if(ping.contains("$"))
                              {
                                String directoryName = "recv/"+Peer.name;
                                File directory = new File(directoryName);
                                if (! directory.exists()){
                                  directory.mkdir();
                                }
                                PrintWriter writer = new PrintWriter("recv\\"+Peer.name+"\\"+friend+".txt", "UTF-8");
                                String[] fileContents =ping.split("#");
                                for(int i =0;i<fileContents.length;i++)
                                {
                                  writer.println(fileContents[i]);
                                }
                                writer.close();
                              }

												}
									}//End of Try
									catch (Exception e)
									{
										//If the node to which I initially connected is dead, I maintain my presence in the sytem by reconnecting to another host
										System.out.println(friend+ " has Disappeared");
										Peer.hashtable.remove(friend);
										/*Reconnector r = new Reconnector(backupHost,Agent.listen_port,my_no_files);
										r.start();*/
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
