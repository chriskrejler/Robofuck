//
//	File:			Brain.java
//	Author:		Krzysztof Langner
//	Date:			1997/04/28
//
//    Modified by:	Paul Marlow

import java.lang.Math;
import java.util.StringTokenizer;


class Brain extends Thread implements SensorInput
{
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


	//---------------------------------------------------------------------------
	// This is main brain function used to make decision
	// In each cycle we decide which command to issue based on
	// current situation. the rules are:
	//
	//	1. If you don't know where is ball then turn right and wait for new info
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
	//	To ensure that we don't send commands to often after each cycle
	//	we waits one simulator steps. (This of course should be done better)
	public void run()
	{

		int head_deg = 0;
		boolean foundTeammate = false;
		float ln_team_dist = 0;
		float ln_team_dir = 0;
		double team_ball_dist = 0;

		// first put it somewhere on my side
		//m_krislet.move(-Math.random() * 52.5, Math.random() * 34.0);
		//m_krislet.move(-Math.random() * 10, Math.random() * 15);

		m_krislet.turn(-40);
		m_memory.waitForNewInfo();
		//m_krislet.move(-6.35, -25.79);
		//m_krislet.move(-19, 11);

		while (!m_timeOver) {
			int angle = 0;
			boolean up = true;

			PlayerInfo teammate = (PlayerInfo) m_memory.getObject("player");
			BallInfo ball = (BallInfo) m_memory.getObject("ball");

			// Find bold og drej krop mod bold
			while (ball == null) {
				m_krislet.turn(40);
				m_memory.waitForNewInfo();
				ball = (BallInfo) m_memory.getObject("ball");
				if (ball != null) {
					m_krislet.turn(ball.m_direction);
				}
			}

			if (teammate != null) {
				foundTeammate = true;
			}

			head_deg = 0;
			// Find medspiller og knæk nakken til vi finder ham
			while (!foundTeammate) {
				if (up) {
					if (head_deg != -90) {
						m_krislet.turn_neck(-30);
						head_deg -= 30;
					} else {
						up = false;
					}
				} else {
					if (head_deg != 90) {
						m_krislet.turn_neck(30);
						head_deg += 30;
					} else {
						up = true;
					}

				}
				m_memory.waitForNewInfo();
				teammate = (PlayerInfo) m_memory.getObject("player");
				if (teammate != null) {
					ln_team_dir = teammate.m_direction;
					ln_team_dist = teammate.m_distance;
					team_ball_dist = Math.sqrt(
							Math.pow(ball.m_distance, 2) + Math.pow(ln_team_dist, 2) - 2 * ball.m_distance * ln_team_dist * Math.cos(Math.toRadians(Math.abs(ln_team_dir) + Math.abs(head_deg)))
					);
					m_krislet.turn_neck(-head_deg);
					foundTeammate = true;
				}
			}


			System.out.println(team_ball_dist);
			System.out.println(ln_team_dist);
			System.out.println(ln_team_dir);
			System.out.println(head_deg);
			System.out.println(ball.m_distance);
			System.out.println(ball.m_direction);

			if (ball.m_distance < team_ball_dist) {
				System.out.println("I am closest to the ball");

				m_krislet.dash(10 * ball.m_distance);
			}

			if (ball.m_distance < 1.0) {
				foundTeammate = false;

				head_deg = 0;
				while (!foundTeammate) {
					if (up) {
						if (head_deg != -90) {
							m_krislet.turn_neck(-30);
							head_deg -= 30;
						} else {
							up = false;
						}
					} else {
						if (head_deg != 90) {
							m_krislet.turn_neck(30);
							head_deg += 30;
						} else {
							up = true;
						}

					}
					m_memory.waitForNewInfo();
					teammate = (PlayerInfo) m_memory.getObject("player");
					if (teammate != null) {
						ln_team_dir = teammate.m_direction;
						ln_team_dist = teammate.m_distance;
						team_ball_dist = Math.sqrt(
								Math.pow(ball.m_distance, 2) + Math.pow(ln_team_dist, 2) - 2 * ball.m_distance * ln_team_dist * Math.cos(Math.toRadians(Math.abs(ln_team_dir) + Math.abs(head_deg)))
						);
						m_krislet.turn_neck(-head_deg);
						foundTeammate = true;
					}
				}

				double kp = ln_team_dist + ((ln_team_dist * (1 - 0.25 * ((ball.m_direction)/180) - 0.25 * (ball.m_distance/0.7))) / 45) * 100;
				m_krislet.kick(kp, ln_team_dir + head_deg);
			}
			// sleep one step to ensure that we will not send
			// two commands in one cycle.
			try{
				Thread.sleep(2*SoccerParams.simulator_step);
			}catch(Exception e){}
		}

	}





//===========================================================================
// Here are suporting functions for implement logic


//===========================================================================
// Implementation of SensorInput Interface

	//---------------------------------------------------------------------------
	// This function sends see information
	public void see(VisualInfo info)
	{
		m_memory.store(info);
	}


	//---------------------------------------------------------------------------
	// This function receives hear information from player
	public void hear(int time, int direction, String message)
	{
	}

	//---------------------------------------------------------------------------
	// This function receives hear information from referee
	public void hear(int time, String message)
	{						 /*
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


//===========================================================================
// Private members
	private SendCommand	m_krislet;			// robot which is controled by this brain
	private Memory			m_memory;				// place where all information is stored
	private char				m_side;
	volatile private boolean		m_timeOver;
}
