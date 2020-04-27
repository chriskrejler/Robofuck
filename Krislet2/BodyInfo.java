public class BodyInfo {
    public double speed = 0;
    public int frame = 0, stamina = 8000, kickCount = 0, dashCount = 0, turnCount = 0, sayCount = 0;
    public String viewMode = "high medium";

    public double getSpeed() {
        return speed;
    }

    public int getFrame() {
        return frame;
    }

    public int getStamina() {
        return stamina;
    }

    public int getKickCount() {
        return kickCount;
    }

    public int getDashCount() {
        return dashCount;
    }

    public int getTurnCount() {
        return turnCount;
    }

    public int getSayCount() {
        return sayCount;
    }

    public String getViewMode() {
        return viewMode;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setFrame(int frame) {
        this.frame = frame;
    }

    public void setStamina(int stamina) {
        this.stamina = stamina;
    }

    public void setKickCount(int kickCount) {
        this.kickCount = kickCount;
    }

    public void setDashCount(int dashCount) {
        this.dashCount = dashCount;
    }

    public void setTurnCount(int turnCount) {
        this.turnCount = turnCount;
    }

    public void setSayCount(int sayCount) {
        this.sayCount = sayCount;
    }

    public void setViewMode(String viewMode){
        this.viewMode = viewMode;
    }

    @Override
    public String toString() {
        return "BodyInfo{" +
                "speed=" + speed +
                ", frame=" + frame +
                ", stamina=" + stamina +
                ", kickCount=" + kickCount +
                ", dashCount=" + dashCount +
                ", turnCount=" + turnCount +
                ", sayCount=" + sayCount +
                ", viewMode='" + viewMode + '\'' +
                '}';
    }
}
