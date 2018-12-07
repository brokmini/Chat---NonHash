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
