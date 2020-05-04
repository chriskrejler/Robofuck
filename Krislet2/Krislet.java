//
//	File:			Krislet.java
//	Author:		Krzysztof Langner
//	Date:			1997/04/28
//


import java.io.*;
import java.net.*;
import java.util.StringTokenizer;


//***************************************************************************
//
//	This is main object class
//
//***************************************************************************
class Krislet implements SendCommand
{
//===========================================================================
// Initialization member functions

	//---------------------------------------------------------------------------
	// The main appllication function.
	// Command line format:
	//
	// krislet [-parameter value]
	//
	// Parameters:
	//
	//	host (default "localhost")
	//		The host name can either be a machine name, such as "java.sun.com" 
	//		or a string representing its IP address, such as "206.26.48.100."
	//
	//	port (default 6000)
	//		Port number for communication with server
	//
	//	team (default Kris)
	//		Team name. This name can not contain spaces.
	//
	//	
	public static void main(String a[])	
		throws SocketException, IOException
	{
	String	hostName = new String("");
	int			port = 6001;
	String	team = new String("Poland");

		try
		{											
			// First look for parameters
			for( int c = 0 ; c < a.length ; c += 2 )
			{
				if( a[c].compareTo("-host") == 0 )
				{
					hostName = a[c+1];
				}
				else if( a[c].compareTo("-port") == 0 )
				{
					port = Integer.parseInt(a[c+1]);
				}
				else if( a[c].compareTo("-team") == 0 )
				{
					team = a[c+1];
				}
				else
				{
					throw new Exception();
				}
			}
		}
		catch(Exception e)
		{
			System.err.println("");
			System.err.println("USAGE: krislet [-parameter value]");
			System.err.println("");
			System.err.println("    Parameters  value        default");
			System.err.println("   ------------------------------------");
			System.err.println("    host        host_name    localhost");
			System.err.println("    port        port_number  6000");
			System.err.println("    team        team_name    Kris");
			System.err.println("");
			System.err.println("    Example:");
			System.err.println("      krislet -host www.host.com -port 6000 -team Poland");
			System.err.println("    or");
			System.err.println("      krislet -host 193.117.005.223");
			return;
		}

		Krislet player = new Krislet(InetAddress.getByName(hostName),
																	port, team);

		// enter main loop
		player.mainLoop();							
	}  

	//---------------------------------------------------------------------------
	// This constructor opens socket for  connection with server
	public Krislet(InetAddress host, int port, String team) 
		throws SocketException
	{
		m_socket = new DatagramSocket();
		m_host = host;
		m_port = port;
		m_team = team;
	}
																 
	//---------------------------------------------------------------------------
	// This destructor closes socket to server
	public void finalize()
	{
		m_socket.close();
	}


//===========================================================================
// Protected member functions

	//---------------------------------------------------------------------------
	// This is main loop for player
	protected void mainLoop() throws IOException
	{
		byte[] buffer = new byte[MSG_SIZE];
		DatagramPacket packet = new DatagramPacket(buffer, MSG_SIZE);

		// first we need to initialize connection with server
		init();

		m_socket.receive(packet);
		parseInitCommand(new String(buffer,0));
		m_port = packet.getPort();

		// Now we should be connected to the server
		// and we know side, player number and play mode
		while( true ){
			send("(look)");
			parseSensorInformation(receive());
		}
	}


//===========================================================================
// Implementation of SendCommand Interface

	//---------------------------------------------------------------------------
	// This function sends move command to the server


	//---------------------------------------------------------------------------
	// This function sends say command to the server


	public void moveObject(String object, int x, int y){
		send("(move (" + object + ") " + x + " " + y + ")");
	}

	public void sendGameScore(double distanceTraveled){
		send("(say " + distanceTraveled + ")");
	}

	//---------------------------------------------------------------------------
	// This function parses initial message from the server
	protected void parseInitCommand(String message)	throws IOException
	{

		StringTokenizer	tokenizer = new StringTokenizer(message,"() ");

		// We need init token
		if( tokenizer.nextToken().compareTo("init") != 0)
		{
			throw new IOException(message);
		}

		// initialize player's brain

		m_brain = new Brain(this);

	}



//===========================================================================
// Here comes collection of communication function
	//---------------------------------------------------------------------------
	// This function sends initialization command to the server
	private void init()
	{
		send("(init " + m_team + ")");
	}

	//---------------------------------------------------------------------------
	// This function parses sensor information
	private void parseSensorInformation(String message)
	{
		/*
		// First check kind of information
		if( message.charAt(1) == 's' && message.charAt(3) == 'e')
		{
			VisualInfo	info = new VisualInfo(message);
			info.parse();
			m_brain.see(info);
		}
		else if( message.charAt(1) == 'h' )
			parseHear(message);

		 */
		//check for 'ok listen'
		if(message.charAt(1) == 'o' && message.charAt(2) == 'k' && message.charAt(4) == 'l'){
			VisualInfo info = new VisualInfo(message);
			info.parse();
			m_brain.see(info);
		}

        if (message.charAt(1) == 's' && message.charAt(3) == 'n'){
            m_brain.senseBody(message);
        }
	}

	//---------------------------------------------------------------------------
	// This function parses hear information
	private void parseHear(String message)
	{
		// get hear information
		StringTokenizer	tokenizer = new StringTokenizer(message,"() ");
		int	time;
		String sender;

		// skipp hear token
		tokenizer.nextToken();
		time = Integer.parseInt( tokenizer.nextToken() );
		sender = tokenizer.nextToken();
		if( sender.compareTo("referee") == 0 )
			m_brain.hear(time, tokenizer.nextToken());
		else if( sender.compareTo("self") != 0 )
			m_brain.hear(time, Integer.parseInt(sender), tokenizer.nextToken());
	}


	//---------------------------------------------------------------------------
	// This function sends via socket message to the server
	public void send(String message)
	{
		byte[] buffer = new byte[MSG_SIZE];
		message.getBytes(0, message.length(), buffer, 0);

		DatagramPacket packet = new DatagramPacket(buffer, MSG_SIZE, m_host, m_port);

		try{
			m_socket.send(packet);
		}catch(IOException e){
			System.err.println("socket sending error " + e);
		}
	}

	//---------------------------------------------------------------------------
	// This function waits for new message from server
	private String receive() 
	{
		byte[] buffer = new byte[MSG_SIZE];
		DatagramPacket packet = new DatagramPacket(buffer, MSG_SIZE);
		try{
			m_socket.receive(packet);
		}catch(IOException e){
			System.err.println("socket receiving error " + e);
		}
		return new String(buffer,0);
	}

				
								 
//===========================================================================
// Private members
	// class memebers
	private DatagramSocket	m_socket;		// Socket to communicate with server
	private	InetAddress			m_host;			// Server address
	private int							m_port;			// server port
	private String					m_team;			// team name
	private SensorInput			m_brain;		// input for sensor information

	// constants
	private static final int	MSG_SIZE = 4096;	// Size of socket buffer

}
