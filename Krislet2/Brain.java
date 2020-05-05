//
//	File:			Brain.java
//	Author:		Krzysztof Langner
//	Date:			1997/04/28
//
//    Modified by:	Paul Marlow

import java.lang.Math;
import java.util.StringTokenizer;

class Brain extends Thread implements SensorInput {
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
        int[] turnRates = {0, -30, -60, -90, 30, 60, 90};
        PlayerInfo lastSeenTeammate = null;
        int lastSeenTeammate_head_deg = 0;
        double teammate_dist_ball = 0;

        // first put it somewhere on my side
        //m_krislet.move(-Math.random() * 52.5, Math.random() * 34.0);
        //m_krislet.move(-Math.random() * 10, Math.random() * 15);

        m_krislet.turn(-40);
        m_memory.waitForNewInfo();
        //m_krislet.move(-6.35, -25.79);
        m_krislet.move(-19, 11);

        while (!m_timeOver) {
            PlayerInfo teammate = (PlayerInfo) m_memory.getObject("player");
            BallInfo ball = (BallInfo) m_memory.getObject("ball");

            // Look for ball and turn toward it. Default behaviour.
            while (ball == null) {
                m_krislet.turn(40);
                m_memory.waitForNewInfo();
                ball = (BallInfo) m_memory.getObject("ball");
                if (ball != null) {
                    m_krislet.turn(ball.m_direction);
                    m_memory.waitForNewInfo();
                }
            }

            // After ball has been found, we look for any players in our radius by turning our head.
            // If no players are found within our radius it must mean we are the closest one to the ball and/or the only player on the field.
            if (lastSeenTeammate == null) {
                Pair<PlayerInfo, Integer> lastSeen = lookForAPlayer();
                lastSeenTeammate = lastSeen.getFirst();
                lastSeenTeammate_head_deg = lastSeen.getSecond();

                if (lastSeenTeammate == null) {
                    // A teammate was not found
                    System.out.println("A player was not found!");
                }
            } else {
                // A teammate was found
                // Determine if we are closer than our teammate to the ball
                double teammate_distance_to_ball = calculateTeammateDistanceToBall(lastSeenTeammate, lastSeenTeammate_head_deg, ball);
                double my_distance_to_ball = ball.m_distance;

                /* Print statements to check distance and determining if closer to ball than teammate */
                System.out.println("Teammate distance: " + teammate_distance_to_ball);
                System.out.println("My distance: " + my_distance_to_ball);
                System.out.println(isClosestToBall(my_distance_to_ball, teammate_distance_to_ball));
                System.out.println("\n");

                if (isClosestToBall(my_distance_to_ball, teammate_distance_to_ball)) {
                    // Dash to ball if we are the closest one
                    m_krislet.dash(10 * ball.m_distance);

                    if (ball.m_distance < 1.0) {
                        // We need to know where to pass the ball, so we look for our teammate
                        System.out.println("Close to ball , looking for player");
                        Pair<PlayerInfo, Integer> lastSeen = lookForAPlayer();
                        if (lastSeen.getFirst() != null && lastSeen.getSecond() != null) {
                            lastSeenTeammate = lastSeen.getFirst();
                            lastSeenTeammate_head_deg = lastSeen.getSecond();
                        }

                        m_krislet.kick(calculatePower(lastSeenTeammate.m_distance, ball), lastSeenTeammate.m_direction + lastSeenTeammate_head_deg);
                    }

                }
            }
			// sleep one step to ensure that we will not send
			// two commands in one cycle.
			try {
				Thread.sleep(SoccerParams.simulator_step);
			} catch (
					Exception e) {
			}
        }
    }


    //===========================================================================
// Here are suporting functions for implement logic

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
        return new Pair<>(null, 0);
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
    private Memory m_memory;                // place where all information is stored
    private char m_side;
    volatile private boolean m_timeOver;
}

