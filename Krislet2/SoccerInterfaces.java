//
//	File:			SoccerInterfaces.java
//	Author:		Krzysztof Langner
//	Date:			1997/04/28
//





//***************************************************************************
//
//	This interface declares functions which are used to send
//	command to player
//
//***************************************************************************
interface SendCommand
{
	void moveObject(String object, double x, double y);
	public void sendGameScore(double distanceTraveled);

	public void signalEndOfGame(int type);
}




interface SensorInput
{
	//---------------------------------------------------------------------------
	// This function sends see information
	public void see(VisualInfo info);

	//---------------------------------------------------------------------------
	// This function receives hear information from player
	public void hear(int time, int direction, String message);

	//---------------------------------------------------------------------------
	// This function receives hear information from referee
	public void hear(int time, String message);

	public void senseBody(String message);
}
