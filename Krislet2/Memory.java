//
//	File:			Memory.java
//	Author:		Krzysztof Langner
//	Date:			1997/04/28
//

import java.util.ArrayList;
import java.util.List;

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
		if(info != null) {
			m_info = info;
		}
	}

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

	public ArrayList<PlayerInfo> getPlayerList(){
		if( m_info == null ) {
			waitForNewInfo();
		}
		ArrayList<PlayerInfo> objectList = new ArrayList<>();
		for (java.lang.Object o: m_info.m_objects){
			if(((ObjectInfo)o).m_type.equals("player")){
				objectList.add((PlayerInfo) o);
			}
		}
		return objectList;
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

	public List<PlayerInfo> getPlyerInfo(){
		return m_info.getPlayerList();
	}

	public int getTime(){
		return m_info.m_time;
	}

    public PlayerInfo getPlayer(String teamname, int teamnumber){
        PlayerInfo player = null;
        for (java.lang.Object o : m_info.m_objects) {
            if (((ObjectInfo) o).m_type.equals("player")) {
                if (((PlayerInfo) o).m_teamName.equals(teamname)) {
                    if(((PlayerInfo) o).getTeamNumber() == teamnumber) {
                        player = (PlayerInfo) o;
                    }
                }
            }
        }
        return player;
    }

//===========================================================================
// Private members
	volatile private VisualInfo	m_info;	// place where all information is stored
	final static int SIMULATOR_STEP = 100;
}

