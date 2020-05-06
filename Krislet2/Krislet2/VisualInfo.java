package Krislet2;//
//	File:			VisualInfo.java
//	Author:		Krzysztof Langner
//	Date:			1997/04/28

//  Modified by:  Paul Marlow, Amir Ghavam, Yoga Selvaraj
//  Course:       Software Agents
//  Date Due:     November 30, 2000 

//  Modified by:  Tarek Hassan
//  Date:         015 June 2001

//  Modified by:	Paul Marlow
//  Date:		February 22, 2004

import java.util.*;

class VisualInfo
{
	private int	m_time;
	public Vector m_objects;
	public StringTokenizer m_tokenizer;
	public String m_message;

      // Split objects into specific lists
      private Vector m_ball_list;
      private Vector m_player_list;
      private Vector m_flag_list;
      private Vector m_goal_list;
      private Vector m_line_list;

  	// Constructor for 'see' information
	public VisualInfo(String info)
	{
		info.trim();
	 	m_message = info;
		m_tokenizer = new StringTokenizer(info,"() ", true);
            m_player_list = new Vector(22);
            m_ball_list = new Vector(1);
            m_goal_list = new Vector(10);
            m_line_list = new Vector(20);
            m_flag_list = new Vector(60);
		m_objects = new Vector<>(113);
	}

      public Vector getBallList()
      {
          return m_ball_list;
      }

      public Vector getPlayerList()
      {
          return m_player_list;
      }

      public Vector getGoalList()
      {
          return m_goal_list;
      }

      public Vector getLineList()
      {
          return m_line_list;
      }

      public Vector getFlagList()
      {
          return m_flag_list;
      }

	//---------------------------------------------------------------------------
	// This function parses visual information from the server
	public void parse()
	{
		String token = null;
		ObjectInfo objInfo;

            m_player_list.clear();
            m_ball_list.clear();
            m_goal_list.clear();
            m_line_list.clear();
            m_flag_list.clear();
		m_objects.clear();
		m_tokenizer = new StringTokenizer(m_message,"() ", true);

		try
		{
			m_tokenizer.nextToken();	// '('
      		// Don'first parse information if it's not 'see' information
			if( m_tokenizer.nextToken().compareTo("see") != 0 )
				return;

			m_tokenizer.nextToken();	// ' '
			m_time = Integer.parseInt( m_tokenizer.nextToken() ); // TIME
			m_tokenizer.nextToken();	// ' '
			token = m_tokenizer.nextToken();

			while(token.compareTo("(") == 0)// '('
			{
        			// Create soccer object for reference - can be player, ball, flag or line
				objInfo = createNewObject();

                        if (objInfo.getType().startsWith("player")) {
                            m_objects.addElement(objInfo);
                        }
                        else if (objInfo.getType().startsWith("ball")) {
							m_objects.addElement(objInfo);
                        }
                        else if (objInfo.getType().startsWith("goal")) {
                            m_objects.addElement(objInfo);
                        }
                        else if (objInfo.getType().startsWith("line")) {
                            m_objects.addElement(objInfo);
                        }
                        else if (objInfo.getType().startsWith("flag")) {
                            m_objects.addElement(objInfo);
                        }

				token = m_tokenizer.nextToken();
				if( token.compareTo(")") == 0 )
				{
					token = m_tokenizer.nextToken();
					if(token.compareTo(" ") == 0) token = m_tokenizer.nextToken();
					continue;
				}

				token = m_tokenizer.nextToken();
        			// Get object's approximate distance from agent - actual distance
        			// becomes more difficult to determine as the distance increases
				objInfo.m_distance = Float.valueOf(token).floatValue();
				token = m_tokenizer.nextToken();

				if( token.compareTo(")") == 0 )
				{
					token = m_tokenizer.nextToken();
					if(token.compareTo(" ") == 0) token = m_tokenizer.nextToken();
					continue;
				}

		        	// Get object direction relative to the agent
				objInfo.m_direction = Float.valueOf(m_tokenizer.nextToken()).floatValue();
				token = m_tokenizer.nextToken();

				if( token.compareTo(")") == 0 )
				{
					token = m_tokenizer.nextToken();
					if(token.compareTo(" ") == 0) token = m_tokenizer.nextToken();
					continue;
				}

        			// Get the distance change since last 'see' message
				objInfo.m_distChange = Float.valueOf(m_tokenizer.nextToken()).floatValue();
				token = m_tokenizer.nextToken();

				if( token.compareTo(")") == 0 )
				{
					token = m_tokenizer.nextToken();
					if(token.compareTo(" ") == 0) token = m_tokenizer.nextToken();
					continue;
				}

        			// Get direction change since last 'see' message
				objInfo.m_dirChange = Float.valueOf(m_tokenizer.nextToken()).floatValue();
				token = m_tokenizer.nextToken();

				if( token.compareTo(")") == 0 )
				{
					token = m_tokenizer.nextToken();
					if(token.compareTo(" ") == 0) token = m_tokenizer.nextToken();
					continue;
				}

				// Player's bodyDir parameter
		        	((PlayerInfo)(objInfo)).m_bodyDir = Float.valueOf(m_tokenizer.nextToken()).floatValue();
				token = m_tokenizer.nextToken();

				if( token.compareTo(")") == 0 )
				{
					token = m_tokenizer.nextToken();
					if(token.compareTo(" ") == 0) token = m_tokenizer.nextToken();
					continue;
				}

				// Player's headDir parameter
        			((PlayerInfo)(objInfo)).m_headDir = Float.valueOf(m_tokenizer.nextToken()).floatValue();

				token = m_tokenizer.nextToken();
				token = m_tokenizer.nextToken();
				if(token.compareTo(" ") == 0) token = m_tokenizer.nextToken();
			}
		}
		catch(Exception e)
		{
			System.out.println("Error parsing see information!");
			System.out.println(token);
			while( m_tokenizer.hasMoreTokens() )
      		{
        			token = m_tokenizer.nextToken();
        			System.out.print(token);//m_tokenizer.nextToken());
      		}
                  System.out.println(m_message);
			System.out.println("");
		}
	}

	//===========================================================================
	// Private implementations

	//---------------------------------------------------------------------------
	// This function creates new object based on the see message sent from the
	// server
	private ObjectInfo createNewObject()
	{
		String token = null;
		ObjectInfo objInfo = null;

		m_tokenizer.nextToken();			// '('
		token = m_tokenizer.nextToken();
		if(token.compareTo("player") == 0 || token.compareTo("p") == 0)  // player object
		{
			String team = new String();
			int uniformNumber = 0;
                  boolean goalie = false;
			token = m_tokenizer.nextToken();	// ' '
			if(token.compareTo(" ") == 0)
			{
				team = m_tokenizer.nextToken();	// teamname
				token = m_tokenizer.nextToken();// ' '
				if(token.compareTo(" ") == 0)
        			{
		          		// uniform number
                              token = m_tokenizer.nextToken();
          				uniformNumber = Integer.parseInt(token);
                              token = m_tokenizer.nextToken();
                              if(token.compareTo(" ") == 0)
                              {
                                  // Player is a goalie?
                                  token = m_tokenizer.nextToken();
                                  if(token.compareTo("goalie") == 0)
                                  {
                                      goalie = true;
                                  }
                              }
        			}
			}
			objInfo = new PlayerInfo(team, uniformNumber, goalie);
		}
		else if(token.compareTo("goal") == 0 || token.compareTo("g") == 0) // goal object
		{
			token = m_tokenizer.nextToken();	// space
			if(token.compareTo(" ") == 0) token = m_tokenizer.nextToken();	// side
			objInfo = new GoalInfo(token.charAt(0));
		}
		else if(token.compareTo("ball") == 0 || token.compareTo("b") == 0) // ball object
		{
			objInfo = new BallInfo();
		}
		else if(token.compareTo("flag") == 0 || token.compareTo("f") == 0) // flag object
		{
			char type = ' ';
			char pos1 = ' ';
			char pos2 = ' ';
			int num = 0;
			boolean out = true;

			token = m_tokenizer.nextToken();	// space
			token = m_tokenizer.nextToken();	// p or g or [l|c|r] or [b|first]
			if(( token.compareTo("p") == 0 )||( token.compareTo("g") == 0 ))
			{
				type = token.charAt(0);
				token = m_tokenizer.nextToken();	// space
				token = m_tokenizer.nextToken();	// [l|r]
				pos1 = token.charAt(0);
				token = m_tokenizer.nextToken();	// space
				token = m_tokenizer.nextToken();	// [first|c|b]
				pos2 = token.charAt(0);
				out = false;
			}
			else if(( token.compareTo("l") == 0 )||( token.compareTo("r") == 0 ))
			{
				pos1 = token.charAt(0);
				token = m_tokenizer.nextToken();	// space
				token = m_tokenizer.nextToken();	// [first|b] or 0
				pos2 = token.charAt(0);

				if (pos2 == '0')
				{
					num=0;
					pos2=' ';
					out=true;
				}
				else
				{
					token = m_tokenizer.nextToken(); 	// space or )

					if ( token.compareTo(")") == 0 ) out=false;
					else
					{
						num = Integer.parseInt( m_tokenizer.nextToken() );
						out=true;
					}
				}
			}
			else if( token.compareTo("c") == 0 )
			{
				pos1 = token.charAt(0);
				token = m_tokenizer.nextToken();	// ) or space
				if ( token.compareTo(")") != 0)
				{
					token = m_tokenizer.nextToken();	// [first|b]
					pos2 = token.charAt(0);
				}
				out=false;
			}
			else if(( token.compareTo("b") == 0 )||( token.compareTo("first") == 0 ))
			{
				pos1 = token.charAt(0);
				token = m_tokenizer.nextToken();	// space
				token = m_tokenizer.nextToken();	// [r|l] or 0
				pos2 = token.charAt(0);

				if ( pos2 == '0')
				{
					pos2=' ';
					num=0;
				}
				else
				{
					token = m_tokenizer.nextToken();			// space
					num = Integer.parseInt(m_tokenizer.nextToken());	// number
					token = m_tokenizer.nextToken();			// )
				}
				out=true;
			}

      		String flagType = "flag";
      		if (type != ' ') flagType = flagType + " " + type;
      		if (pos1 != ' ') flagType = flagType + " " + pos1;
      		if (pos2 != ' ') flagType = flagType + " " + pos2;

      		// Implementing flags like this, allows one to specifically find a
      		// particular flag (i.e. "flag c", or "flag p l first")
			objInfo = new FlagInfo(flagType, type, pos1, pos2, num, out);
		}
		else if(token.compareTo("line") == 0 || token.compareTo("l") == 0) // line object
		{
			token = m_tokenizer.nextToken();	// space
			token = m_tokenizer.nextToken();	// [l|r|first|b]
			objInfo = new LineInfo(token.charAt(0));
		}
		else if(token.compareTo("Player") == 0 || token.compareTo("P") == 0)
		{
			objInfo = new PlayerInfo();
		}
		else if(token.compareTo("Goal") == 0 || token.compareTo("G") == 0)
		{
			objInfo = new GoalInfo();
		}
		else if(token.compareTo("Ball") == 0 || token.compareTo("B") == 0)
		{
			objInfo = new BallInfo();
		}
		else if(token.compareTo("Flag") == 0 || token.compareTo("F") == 0)
		{
			objInfo = new FlagInfo();
		}
		else if(token.compareTo("Line") == 0 || token.compareTo("L") == 0)
		{
			objInfo = new LineInfo();
		}

    		while (token.compareTo(")") != 0)
    		{
      		token = m_tokenizer.nextToken();
    		}

		return objInfo;
	}
}

