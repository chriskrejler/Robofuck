package Krislet;//
//	File:			Brain.java
//	Author:		Krzysztof Langner
//	Date:			1997/04/28
//
//    Modified by:	Paul Marlow

import NEAT.Genome;
import NEAT.Implementation.Player;

import java.lang.Math;
import java.util.StringTokenizer;

public class Brain extends Thread implements SensorInput {
	States.gameState gameState = States.gameState.BEFORE_KICKOFF;
	BodyInfo bodyInfo = new BodyInfo();
    //---------------------------------------------------------------------------
    // This constructor:
    // - stores connection to krislet
    public Brain(SendCommand krislet, String team,
                 char side, int number, String playMode) {
        m_timeOver = false;
        m_krislet = krislet;
        m_memory = new Memory();
//		m_team = team;
        m_side = side;
//		m_number = number;
//		m_playMode = playMode;
    }



    public float run(Genome gene) {
    	boolean running = true;
    	double shots = 0;
		Pair<PlayerInfo, PlayerInfo> teammateEnemy;
		float[] inputs = new float[4];
		float[] outputs;


		while(running) {
			m_krislet.parseSensorInformation(m_krislet.receive());
			if (gameState == States.gameState.PLAY_ON) {

				do {
					m_krislet.parseSensorInformation(m_krislet.receive());
					teammateEnemy = m_memory.getPlayers();
				}while(teammateEnemy.first == null || teammateEnemy.second == null);

				inputs[0] = teammateEnemy.first.m_direction;
				inputs[1] = teammateEnemy.first.m_distance;
				inputs[2] = teammateEnemy.second.m_direction;
				inputs[3] = teammateEnemy.second.m_distance;

				outputs = gene.evaluateNetwork(inputs);
				System.out.println("Angle: " + outputs[0] + " Power: " + outputs[1]);

				m_krislet.kick(outputs[0], outputs[1]);

				while (true) {
					m_krislet.parseSensorInformation(m_krislet.receive());
					if (gameState == States.gameState.KICK_IN_R && !m_memory.getUsed()) {
						shots += m_memory.getScore();
						m_memory.setUsed(true);
						break;
					} else if (gameState == States.gameState.KICK_IN_L && !m_memory.getUsed()) {
						shots += m_memory.getScore();
						m_memory.setUsed(true);
						running = false;
						break;
					}
				}
				System.out.println(shots);
			}
		}
		return (float)shots;
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

    public void save(double score, boolean used){
    	m_memory.setScore(score);
    	m_memory.setUsed(used);
		System.out.println("Score: " + score + " Used: " + used);
	}


    //---------------------------------------------------------------------------
    // This function receives hear information from player
    public void hear(int time, int direction, String message) {
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
    public SendCommand m_krislet;            // robot which is controled by this brain
    public Memory m_memory;                // place where all information is stored
    private char m_side;
    volatile private boolean m_timeOver;
    public Player player;
}

