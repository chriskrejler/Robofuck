//
//	File:			Brain.java
//	Author:		Krzysztof Langner
//	Date:			1997/04/28
//
//    Modified by:	Paul Marlow

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;
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
//		m_team = team;
        m_side = side;
//		m_number = number;
//		m_playMode = playMode;
        start();
    }

    public Brain(SendCommand krislet) {
        m_timeOver = false;
        m_krislet = krislet;
        m_memory = new Memory();
        start();
    }


    public void run() {
        while (!m_timeOver) {

        }

    }

    public boolean inPossesion(){
        return false;
    }

    private double calculateTeammateDistanceToBall(PlayerInfo teammate, int player_head_degrees, BallInfo ball) {
        // https://da.wikipedia.org/wiki/Cosinusrelation

        double degrees = Math.abs(player_head_degrees) - Math.abs(teammate.m_direction);
        return Math.sqrt(
                Math.pow(ball.m_distance, 2) +
                        Math.pow(teammate.m_distance, 2) - 2 *
                        ball.m_distance *
                        teammate.m_distance *
                        Math.cos(Math.toRadians(degrees))
        );
    }


    private double calculatePower(float teammate_distance, BallInfo ball) {
        return teammate_distance + ((teammate_distance * (1 - 0.25 * ((ball.m_direction) / 180) - 0.25 * (ball.m_distance / 0.7))) / 45) * 100;
    }

    public int timeToArrival(BallInfo ball, PlayerInfo teammate){
        double ax;
        double px;
        double vx;

        int tick = 1;
        double position = teammate.m_distance;
        double traveled = 0;

        ax = calculatePower(teammate.m_distance, ball) * 0.027;
        px = ax;
        traveled += px;
        vx = 0.94 * ax;

        while(position - traveled > 0.5){
            px = px+vx;
            traveled+=px;
            vx = 0.94 * ax;
            tick++;
        }
        return tick;
    }

    public double distanceDashed(int time){

        double ax;
        double px;
        double vx;
        int tick = 1;
        double traveled = 0;

        ax = 100 * 0.006;
        px = ax;
        traveled += px;
        vx = 0.94 * ax;

        while(tick < time){
            px = px+vx;
            traveled+=px;
            vx = 0.94 * ax;
            tick += 2;
        }
        return traveled;
    }

    public boolean isPlayerFree(BallInfo ball, PlayerInfo teammate, List<PlayerInfo> enemies, double radius){
        double currentRadius = -radius;
        double playerLength = teammate.m_distance;
        double a, c, height;
        List<Double> freeLengths = new ArrayList<>();

        while(currentRadius < radius){
            a = currentRadius/playerLength;
            c = currentRadius;

            for(PlayerInfo enemy: enemies){
                if(enemy.m_distance < playerLength){
                    height = calculateHeight(enemy.m_direction, enemy.m_distance);
                    if(checkCollision(a, 0, c, playerLength, 0, radius) &&
                            !checkCollision(a, 0, c, enemy.m_distance, height, radius)){
                        freeLengths.add(currentRadius);
                    }
                }
            }
            currentRadius++;
        }
        return false;
    }

    private double calculateHeight(double angle, double length){
        return Math.sin(Math.toRadians(angle))*length;
    }

    private boolean checkCollision(double a, double b, double c,
                                           double x, double y, double radius)
    {
        // Finding the distance of line from center.
        double dist = (Math.abs(a * x + b * y + c)) /
                Math.sqrt(a * a + b * b);
        // Checking if the distance is less than,
        // greater than or equal to radius.
        return radius > dist;
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

	public double calculateBallArrival(BallInfo ball, PlayerInfo teammate, PlayerInfo enemy){

		double oBall = 0;
		double velocity = 10;
		double kp = teammate.m_distance + ((teammate.m_distance * (1 - 0.25 * ((ball.m_direction)/180) - 0.25 * (ball.m_distance/0.7))) / 45) * 100;

		while(velocity > 0.1){
			oBall = teammate.m_direction;
		}
		return 2.0;
	}


    //===========================================================================
// Private members
    private SendCommand m_krislet;            // robot which is controled by this brain
    private Memory m_memory;                // place where all information is stored
    private char m_side;
    volatile private boolean m_timeOver;
}

