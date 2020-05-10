package Krislet;//
//	File:			Memory.java
//	Author:		Krzysztof Langner
//	Date:			1997/04/28
//

class Memory 
{
	//---------------------------------------------------------------------------
	// This constructor:
	// - initializes all variables
	public Memory()
	{
	}


	//---------------------------------------------------------------------------
	// This function puts see information into our memory
	public void store(VisualInfo info)
	{
		m_info = info;
	}

	public double getScore(){return m_info.score;}

	public void setScore(double score){
		m_info.score = score;
	}

	public void setUsed(boolean used){m_info.used = used;}


	public boolean getUsed(){return m_info.used;}

	//---------------------------------------------------------------------------
	// This function looks for specified object
	public ObjectInfo getObject(String name) 
	{
		if( m_info == null ) {
			waitForNewInfo();
		}

		for (java.lang.Object o: m_info.m_objects)
		{
			if( ((ObjectInfo)o).m_type.equals(name))
				return ((ObjectInfo)o);
		}

		return null;
	}

	public Pair<PlayerInfo, PlayerInfo> getPlayers(){
		PlayerInfo teammate = null;
		PlayerInfo enemy = null;
			for (java.lang.Object o : m_info.m_objects) {
				if (((ObjectInfo) o).m_type.equals("player")) {
					if (((PlayerInfo) o).m_teamName.equals("NEAT")) {
						teammate = (PlayerInfo) o;
					} else {
						enemy = (PlayerInfo) o;
					}
				}
			}
			if((teammate != null) && (enemy != null)){
			}
		return new Pair(teammate, enemy);
	}

	//---------------------------------------------------------------------------
	// This function waits for new visual information
	public void waitForNewInfo() 
	{

		// first remove old info
		m_info = null;
		// now wait until we get new copy
		while(m_info == null)
		{
			// We can get information faster then 75 miliseconds
			try
			{
				Thread.sleep(SIMULATOR_STEP);
			}
			catch(Exception e)
			{
			}
		}
	}


//===========================================================================
// Private members
	volatile private VisualInfo	m_info;	// place where all information is stored
	final static int SIMULATOR_STEP = 100;
}

