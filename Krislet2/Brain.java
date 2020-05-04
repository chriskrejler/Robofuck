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


class Brain extends Thread implements SensorInput {
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
    public void hear(int time, String message) {						 /*
		StringTokenizer tokenizer = new StringTokenizer(message,"() ", true);
		String token;

		// First is referee token and time token
		tokenizer.nextToken();
		tokenizer.nextToken();
		tokenizer.nextToken();
		token = tokenizer.nextToken();

		if(token.compareTo("time_over") == 0)
			m_timeOver = true;
			*/
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
