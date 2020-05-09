package Krislet;

public class States {
    enum playerState{
        THINKING,
        DRIBBLING,
        SHOOTING,
        PASSING,
        RUNNING
    }
    enum gameState{
        KICKOFF_L,
        KICKOFF_R,
        BEFORE_KICKOFF,
        PLAY_ON,
        KICK_IN_L,
        KICK_IN_R,
        CORNER_KICK_L,
        CORNER_KICK_R,
        GOAL_KICK_L,
        GOAL_KICK_R,
        FOUL_CHARGE_L,
        FOUL_CHARGE_R,
        BACK_PASS_L,
        BACK_PASS_R,
        INDIRECT_FREE_KICK_L,
        INDIRECT_FREE_KICK_R,
        ILLEGAL_DEFENSE_L,
        ILLEGAL_DEFENSE_R,
        GOAL_R,
        GOAL_L

    }
}
