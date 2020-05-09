//
//	File:			Brain.java
//	Author:		Krzysztof Langner
//	Date:			1997/04/28
//
//    Modified by:	Paul Marlow

import java.lang.Math;
import java.util.ArrayList;
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

        ArrayList<PlayerInfo> players = (ArrayList<PlayerInfo>) m_memory.getPlayerList();

		while(players.isEmpty()){
			players = (ArrayList<PlayerInfo>) m_memory.getPlayerList();
		}

		passSituation(players);

        while (true) {
        	//m_krislet.send("hear referee 0 play_on");
        	//Collect amount of distance ball traveled
			ObjectInfo ball = m_memory.getObject("ball");
			players = (ArrayList<PlayerInfo>) m_memory.getPlayerList();
			//Dist traveled is sqrt of a^2 + b^2
			/*
			distanceTraveled = Math.sqrt(
					(Math.abs(ballX - ball.m_X) * Math.abs(ballX - ball.m_X)) +
					(Math.abs(ballX - ball.m_X) * Math.abs(ballX - ball.m_X)));
					*/

			//Check if a pass has been made, determine if its successful, and reset
            checkForPass(ball, players);
			System.out.println(gameState);
        }
    }

    public void threeManNoVision(){
	    //Get team name from a random player
        PlayerInfo player1 = (PlayerInfo) m_memory.getObject("player");
        if (player1 != null){
            String team1 = player1.getTeamName();
            m_krislet.moveObject("player " + team1 + " 1", 0,0);
            m_krislet.moveObject("player " + team1 + " 2", getRandomInRange(0, 20), getRandomInRange(0,20));
            m_krislet.moveObject("player " + team1 + " 3", getRandomInRange(0, 20), getRandomInRange(0,20));
            m_krislet.moveObject("ball", 0, 0);
        }
    }

    public void threeManVision(){
		//Get team name from a random player
		PlayerInfo player1 = (PlayerInfo) m_memory.getObject("player");
		if (player1 != null){
			String team1 = player1.getTeamName();
			m_krislet.moveObject("player " + team1 + " 1", 0,0);
			m_krislet.moveObject("player " + team1 + " 2", getRandomInRange(25, 15), getRandomInRange(0,10));
			m_krislet.moveObject("player " + team1 + " 3", getRandomInRange(25, 15), getRandomInRange(0,10));
			m_krislet.moveObject("ball", 0, 0);
		}
	}

	public void passSituation(ArrayList<PlayerInfo> players){
		PlayerInfo player1 = (PlayerInfo) m_memory.getObject("player");
		if(player1 != null) {
			team1 = players.get(0).getTeamName();
			for (PlayerInfo player : players) {
				if (!team1.equals(player.getTeamName())) {
					team2 = player.getTeamName();
					break;
				}
			}
			m_krislet.moveObject("player " + team1 + " 1", 0, 0);
			m_krislet.moveObject("player " + team1 + " 2", getRandomInRange(30, 20), getRandomInRange(0, 10));
			m_krislet.moveObject("player " + team2 + " 1", getRandomInRange(30, 20), getRandomInRange(0, 10));
			m_krislet.moveObject("ball", 0, 0);
		}
	}

    public void checkForPass(ObjectInfo ball, ArrayList<PlayerInfo> players){
		//check if a pass was successful or not
		if(ball.m_deltaX < Math.abs(0.1) &&  ball.m_deltaY < Math.abs(0.1) && ball.m_X != 0 && ball.m_Y != 0){
			boolean pass = false;
			for(PlayerInfo player : players){
				if(player.getTeamNumber() != 1 && player.getTeamName().equals(team1)){
					if(Math.abs(player.m_X - ball.m_X) + Math.abs(player.m_Y - ball.m_Y) < 2){
						pass = true;
						break;
					}
				}
			}
			if (pass){
				succesfulPasses += 1;
				threeManNoVision();
			}
			else {
				//Send the amount of successful passes, and set game state to before_kick_off
				m_krislet.sendGameScore(succesfulPasses);
				m_krislet.signalEndOfGame(1);
				//Wait for NEAT algorithm to mutate
				try{
					currentThread().sleep(100);
				}catch(Exception e){
					System.out.println("cry");
				}
				//Reset ball and players, and set game state to play_on
				passSituation(players);
				m_krislet.signalEndOfGame(0);
			}
		}
	}

    public int getRandomInRange(int number, int deviation){
        Random rand = new Random();
        int max = number + deviation;
        int min = number - deviation;
        int returnnumber = 0;
        while(returnnumber == 0) {
			returnnumber = rand.nextInt(max - min + 1) + min;
		}
        return returnnumber;
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
		System.out.println(message);

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
	private String team1 = "temp1";
	private String team2 = "temp2";
	private int succesfulPasses = 0;

}

