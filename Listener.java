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
                    PingListener l=new PingListener(clientSocket,"");
										l.start();

                    //To advertise about self
										PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
										out.write("CONNECTED WITH "+Peer.name+" #### "+Peer.savePublicKey(Peer.publicKey)+"\n");
										out.flush();

									}
									catch (IOException e)
									{
										System.out.println("Accept failed"); System.exit(-1);
									}
						}
			}

} //End of Listener class
