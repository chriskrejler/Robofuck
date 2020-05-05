//
//	File:			Brain.java
//	Author:		Krzysztof Langner
//	Date:			1997/04/28
//
//    Modified by:	Paul Marlow

import java.lang.Math;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;


class Brain extends Thread implements SensorInput
{
    States.gameState gameState = States.gameState.BEFORE_KICKOFF;
    BodyInfo bodyInfo = new BodyInfo();
	//---------------------------------------------------------------------------
	// This constructor:
	// - stores connection to krislet
	// - starts thread for this object
	public Brain(SendCommand krislet, String team,
							char side, int number, String playMode)
	{
		m_timeOver = false;
		m_krislet = krislet;
		m_memory = new Memory();
        start();
    }

    public Brain(SendCommand krislet) {
        m_timeOver = false;
        m_krislet = krislet;
        m_memory = new Memory();
        start();
    }


    public void run() {
	    //Initialize the game
        ObjectInfo ballTemp = m_memory.getObject("ball");
	    while(ballTemp == null){
            ballTemp = m_memory.getObject("ball");
        }
        ballX = ballTemp.m_X;
        ballY = ballTemp.m_Y;

        fourMan();

        while (true) {
        	//Collect amount of distance ball traveled
			ObjectInfo ball = m_memory.getObject("ball");
			//Dist traveled is sqrt of a^2 + b^2
			distanceTraveled = Math.sqrt(
					(Math.abs(ballX - ball.m_X) * Math.abs(ballX - ball.m_X)) +
					(Math.abs(ballX - ball.m_X) * Math.abs(ballX - ball.m_X)));

			//Check if ball is in possession, or time is over
        	if(!inPossession() || !(m_memory.getTime() < timelimit)){
        	    //Send the distance the ball has traveled, and set game state to before_kick_off
        		m_krislet.sendGameScore(distanceTraveled);
        		m_krislet.signalEndOfGame(1);
        		//Wait for the NEAT algorithm to mutate
        		try{
                    currentThread().sleep(100);
                }catch(Exception e){
                    System.out.println("cry");
                }
        		//Reset ball, and set game state to play_on
                m_krislet.moveObject("ball", -19.5, 0);
                m_krislet.signalEndOfGame(0);
            }
        }
    }

    public void fourMan(){
	    //Get team name from a random player
        PlayerInfo player1 = (PlayerInfo) m_memory.getObject("player");
        if (player1 != null){
            String team1 = player1.getTeamName();
            m_krislet.moveObject("player " + team1 + " 1", -getRandomInRange(20), getRandomInRange(0));
            m_krislet.moveObject("player " + team1 + " 2", getRandomInRange(20), 0);
            m_krislet.moveObject("player " + team1 + " 3", getRandomInRange(0), -getRandomInRange(20));
            m_krislet.moveObject("player " + team1 + " 4", getRandomInRange(0), getRandomInRange(20));
            m_krislet.moveObject("ball", -19.5, 0);
        }

    }

    public int getRandomInRange(int number){
        Random rand = new Random();
        int max = number + 5;
        int min = number - 5;
        return rand.nextInt(max - min + 1) + min;
    }

    public boolean inPossession(){
        BallInfo ball = (BallInfo) m_memory.getObject("ball");
		List<PlayerInfo> players = m_memory.getPlyerInfo();

		if (ball != null && !players.isEmpty()) {
            if (ball.m_deltaX + ball.m_deltaY < 0.1) {
                for (PlayerInfo player : players) {
                    if (Math.abs(player.m_X - ball.m_X) + Math.abs(player.m_Y - ball.m_Y) < 3) {
                        return true;
                    }
                }
                return false;
            }
        }
        return true;
    }







	public void setGameState(States.gameState gs){
		gameState = gs;
	}

	public void senseBody(String message){
		StringTokenizer	tokenizer = new StringTokenizer(message,"() ");
		tokenizer.nextToken();
		bodyInfo.setFrame(Integer.parseInt(tokenizer.nextToken()));
		tokenizer.nextToken();
		bodyInfo.setViewMode(tokenizer.nextToken() + " " + tokenizer.nextToken());
		tokenizer.nextToken();
		bodyInfo.setStamina(Double.parseDouble(tokenizer.nextToken()));
		bodyInfo.setEffort(Double.parseDouble(tokenizer.nextToken()));
		tokenizer.nextToken();
		bodyInfo.setSpeed(Double.parseDouble(tokenizer.nextToken()));
		tokenizer.nextToken();
		bodyInfo.setKickCount(Integer.parseInt(tokenizer.nextToken()));
		tokenizer.nextToken();
		bodyInfo.setDashCount(Integer.parseInt(tokenizer.nextToken()));
		tokenizer.nextToken();
		bodyInfo.setTurnCount(Integer.parseInt(tokenizer.nextToken()));
		tokenizer.nextToken();
		bodyInfo.setSayCount(Integer.parseInt(tokenizer.nextToken()));

		System.out.println(bodyInfo.toString());

	}




//===========================================================================
// Implementation of SensorInput Interface

    //---------------------------------------------------------------------------
    // This function sends see information
    public void see(VisualInfo info) {
        m_memory.store(info);
    }


	//---------------------------------------------------------------------------
	// This function receives hear information from player
	public void hear(int time, int direction, String message)
	{
	}

    //---------------------------------------------------------------------------
    // This function receives hear information from referee
    public void hear(int time, String message) {

		StringTokenizer tokenizer = new StringTokenizer(message,"() ", true);
		String token;

		// First is referee token and time token
		token = tokenizer.nextToken();

		//set states
		if(token.contains("goal_l_")){
			setGameState(States.gameState.GOAL_L);
		}
		else if (token.contains("goal_r_")){
			setGameState(States.gameState.GOAL_R);
		}
		else {
			switch (token) {
				case "time_over":
					m_timeOver = true;
					break;
				case "kick_off_l":
					gameState = States.gameState.KICKOFF_L;
					break;
				case "kick_off_r":
					gameState = States.gameState.KICKOFF_R;
					break;
				case "play_on":
					gameState = States.gameState.PLAY_ON;
					break;
				case "kick_in_l":
					gameState = States.gameState.KICK_IN_L;
					break;
				case "kick_in_r":
					gameState = States.gameState.KICK_IN_R;
					break;
				case "corner_kick_l":
					gameState = States.gameState.CORNER_KICK_L;
					break;
				case "corner_kick_r":
					gameState = States.gameState.CORNER_KICK_R;
					break;
				case "goal_kick_l":
					gameState = States.gameState.GOAL_KICK_L;
					break;
				case "goal_kick_r":
					gameState = States.gameState.GOAL_KICK_R;
					break;
				case "foul_charge_l":
					gameState = States.gameState.FOUL_CHARGE_L;
					break;
				case "foul_charge_r":
					gameState = States.gameState.FOUL_CHARGE_R;
					break;
				case "back_pass_l":
					gameState = States.gameState.BACK_PASS_L;
					break;
				case "back_pass_r":
					gameState = States.gameState.BACK_PASS_R;
					break;
				case "indirect_free_kick_l":
					gameState = States.gameState.INDIRECT_FREE_KICK_L;
					break;
				case "indirect_free_kick_r":
					gameState = States.gameState.INDIRECT_FREE_KICK_R;
					break;
				case "illegal_defense_l":
					gameState = States.gameState.ILLEGAL_DEFENSE_L;
					break;
				case "illegal_defense_r":
					gameState = States.gameState.ILLEGAL_DEFENSE_R;
					break;
			}
		}

	}


    //===========================================================================
// Private members
    private SendCommand m_krislet;            // robot which is controled by this brain
    private Memory m_memory;                // place where all information is stored
    private char m_side;
    volatile private boolean m_timeOver;
    private int timelimit = 2000;
    public double distanceTraveled = 0;
    private double ballX = 0;
	private double ballY = 0;

}

