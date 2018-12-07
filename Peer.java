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

public class Peer
{
  static Hashtable<String,PeerDetails> hashtable = new Hashtable<String,PeerDetails>();
  static String name;
  static int myListenPort;
  static PublicKey publicKey;
  static PrivateKey privateKey;
  public static void main(String[] args) throws IOException
  {
        File file = new File("peers.csv");
        BufferedReader reader = new BufferedReader(new FileReader("peers.csv"));
        PrintWriter pw = new PrintWriter(new FileWriter(file, true));
        Scanner scanner = new Scanner(System.in);
        String line = reader.readLine();

        privateKey = null;
        publicKey = null;
        try
        {
        Map<String, Object> keys = getRSAKeys();
        privateKey = (PrivateKey) keys.get("private");
        publicKey = (PublicKey) keys.get("public");
        }
        catch(Exception e) {}

        /*try
        {
        String plainText = "Hello World!";
        String encryptedText = encryptMessage(plainText, privateKey);
        String descryptedText = decryptMessage(encryptedText, newPub);
        System.out.println("input:" + plainText);
        System.out.println("encrypted:" + encryptedText);
        System.out.println("decrypted:" + descryptedText);
      } catch(Exception e) {}*/

        if (line==null || line.equals(""))
        {
          //If it is the first node to join the p2p network
          System.out.println("Hey! Choose username");
          name=scanner.nextLine();
          pw.write(name+",9000");
          pw.close();
          Integer listen_port = 9000;
          myListenPort = 9000;
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
          myListenPort = listen_port;
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

  CommandListener cl = new CommandListener();
  cl.start();

  } //End of Main function
  //******************************************************************************************************************************************
  private static Map<String,Object> getRSAKeys() throws Exception
  {
  KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
  keyPairGenerator.initialize(2048);
  KeyPair keyPair = keyPairGenerator.generateKeyPair();
  PrivateKey privateKey = keyPair.getPrivate();
  PublicKey publicKey = keyPair.getPublic();
  Map<String, Object> keys = new HashMap<String,Object>();
  keys.put("private", privateKey);
  keys.put("public", publicKey);
  return keys;
  }

public static String decryptMessage(String encryptedText, PublicKey publicKey) throws Exception
{
    Cipher cipher = Cipher.getInstance("RSA");
    cipher.init(Cipher.DECRYPT_MODE, publicKey);
    return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedText)));
}

// Encrypt using RSA private key

public static String encryptMessage(String plainText, PrivateKey privateKey) throws Exception
{
    Cipher cipher = Cipher.getInstance("RSA");
    cipher.init(Cipher.ENCRYPT_MODE, privateKey);
    return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes()));
}

public static String savePublicKey(PublicKey publicKey) {
    byte[] encodedPublicKey = publicKey.getEncoded();
    String b64PublicKey = Base64.getEncoder().encodeToString(encodedPublicKey);
    return b64PublicKey;
}

public static PublicKey retrievePublicKey(String keyString)
{
  PublicKey pubKey = null;
  try
  {
    KeyFactory kf = KeyFactory.getInstance("RSA");
    X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(keyString));
    pubKey = (PublicKey) kf.generatePublic(keySpecX509);
  } catch(Exception e) { System.out.println("Error in converting back to key");}


  return pubKey;
}


  //**********************************************************************************************************************************************
	//function that is called when a new node connects to existing node in the network
			public static void connect(Integer port,Integer myPort)
			{
				try
				{
				Socket socket = new Socket("localhost", port);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.write("CONNECTED WITH "+Peer.name+" #### "+Peer.savePublicKey(Peer.publicKey)+"\n");
				out.flush();


        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String recdData = in.readLine();
        //System.out.println(recdData);

        String meta = recdData.split("CONNECTED WITH ")[1];
        String username= meta.split(" #### ")[0];
        String recdkey = meta.split(" #### ")[1];
        System.out.println(recdData.split(" #### ")[0]);

        PeerDetails pd = new PeerDetails();
        pd.socket=socket;
        pd.pubKey=Peer.retrievePublicKey(recdkey);
        Peer.myHash(username,pd);
				PingListener p = new PingListener(socket,username);
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
          String connectedUsers;
          connectedUsers = Peer.readHash();
          if(!connectedUsers.equals(""))
          {
            //System.out.println("The connected users are - "+connectedUsers);
            String[] friendList = connectedUsers.split(",");
            for(int i =0;i<friendList.length;i++)
            {
              //System.out.println(fetchSocket(friendList[i]));
              Socket t = fetchSocket(friendList[i]);
              String f = friendList[i];
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
        return 0;
}
//**********************************************************************************************************************************************
//function to hash neighbor details
public static void myHash(String username,PeerDetails p)
{
      				hashtable.put(username,p);
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
              PeerDetails p = new PeerDetails();
              Set<String> keys = hashtable.keySet();
              Iterator<String> itr = keys.iterator();
              while (itr.hasNext())
              {
                // Getting Key
                String key = itr.next();

                String key1 = key.replaceAll(" ","");
                String username1=username.replaceAll(" ","");
                if(key1.equals(username1))
                {
                  p= hashtable.get(key);
                  s= p.socket;
                }
              }
        return s;
  }
  //**********************************************************************************************************************************************
  //function to read neighbor details from hash
   public static PublicKey fetchPublicKey(String username)
   {
                PublicKey pkey = null;
                PeerDetails p = new PeerDetails();
                Set<String> keys = hashtable.keySet();
                Iterator<String> itr = keys.iterator();
                while (itr.hasNext())
                {
                  // Getting Key
                  String key = itr.next();

                  String key1 = key.replaceAll(" ","");
                  String username1=username.replaceAll(" ","");
                  if(key1.equals(username1))
                  {
                    p= hashtable.get(key);
                    pkey= p.pubKey;
                  }
                }
          return pkey;
    }
  //**********************************************************************************************************************************************
   public static int isFriend(String username)
   {
                Set<String> keys = hashtable.keySet();
                Iterator<String> itr = keys.iterator();
                while (itr.hasNext())
                {
                  // Getting Key
                  String key = (itr.next()).replaceAll(" ","");
                  String username1 = username.replaceAll(" ","");
                  if(key.equals(username))
                  {
                    return 1;
                  }
                }
                return 0;

    }
} // End of class Peer
//**********************************************************************************************************************************************
class PeerDetails
{
  Socket socket;
  PublicKey pubKey;
}
