package Krislet;//
//	File:			Brain.java
//	Author:		Krzysztof Langner
//	Date:			1997/04/28
//
//    Modified by:	Paul Marlow

import java.lang.Math;
import java.util.StringTokenizer;

public class Brain extends Thread implements SensorInput {
	States.gameState gameState = States.gameState.BEFORE_KICKOFF;
	BodyInfo bodyInfo = new BodyInfo();
    //---------------------------------------------------------------------------
    // This constructor:
    // - stores connection to krislet
    // - starts thread for this object
    public Brain(SendCommand krislet, String team,
                 char side, int number, String playMode) {
        m_timeOver = false;
        m_krislet = krislet;
        m_memory = new Memory();
//		m_team = team;
        m_side = side;
//		m_number = number;
//		m_playMode = playMode;
        start();
    }


    //---------------------------------------------------------------------------
    // This is main brain function used to make decision
    // In each cycle we decide which command to issue based on
    // current situation. the rules are:
    //
    //	1. If you don'first know where is ball then turn right and wait for new info
    //
    //	2. If ball is too far to kick it then
    //		2.1. If we are directed towards the ball then go to the ball
    //		2.2. else turn to the ball
    //
    //	3. If we dont know where is opponent goal then turn wait
    //				and wait for new info
    //
    //	4. Kick ball
    //
    //	To ensure that we don'first send commands to often after each cycle
    //	we waits one simulator steps. (This of course should be done better)
    public void run() {
        updatePlayers();




    }


    //===========================================================================
// Here are suporting functions for implement logic

    public void updatePlayers(){
        Pair<PlayerInfo, PlayerInfo> players = m_memory.getPlayers();
        teammate = new Pair(players.first.m_direction, players.first.m_distance);
        enemy = new Pair(players.second.m_direction, players.second.m_distance);
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

    private boolean isClosestToBall(double my_distance, double teammate_distance) {
        return my_distance < teammate_distance;
    }

    private double calculateTeammateDistanceToBall(PlayerInfo teammate, int player_head_degrees, BallInfo ball) {
        // https://da.wikipedia.org/wiki/Cosinusrelation


        return Math.sqrt(
                Math.pow(ball.m_distance, 2) + Math.pow(teammate.m_distance, 2) - 2 * ball.m_distance * teammate.m_distance * Math.cos(Math.toRadians(Math.abs(player_head_degrees) - teammate.m_direction))
        );
    }

    private Pair<PlayerInfo, Integer> lookForAPlayer() {
        PlayerInfo last_teammate;
        int[] turnRates = {0, -90, 180};

        for (int turnRate : turnRates) {
            m_krislet.turn_neck((turnRate));
            m_memory.waitForNewInfo();
            PlayerInfo teammate = (PlayerInfo) m_memory.getObject("player");

            // A teammate was found within our radius
            // Save teammate to global and turn back.
            if (teammate != null) {
                last_teammate = teammate;

                // Temp. until body sensor parser has been implemented
                if (turnRate > 0) {
                    m_krislet.turn_neck(-90);
                } else {
                    m_krislet.turn_neck(Math.abs(turnRate));
                }
                m_memory.waitForNewInfo();

                return new Pair<>(last_teammate, turnRate);
            }

        }
        m_krislet.turn_neck(-90);
        m_memory.waitForNewInfo();
        return new Pair<>((PlayerInfo)null, 0);
    }

    private double calculatePower(float teammate_distance, BallInfo ball) {
        return teammate_distance + ((teammate_distance * (1 - 0.25 * ((ball.m_direction) / 180) - 0.25 * (ball.m_distance / 0.7))) / 45) * 100;
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
    public void hear(int time, int direction, String message) {
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
    public Memory m_memory;                // place where all information is stored
    private char m_side;
    volatile private boolean m_timeOver;
    public Pair<Double, Double> teammate;
    public Pair<Double, Double> enemy;
}

