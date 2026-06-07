public class Piranha {
    double x;
    double y;
    double speedX;
    double speedY;
    int size = 30;
    boolean isActive;
    int homingTimer;

    Animation piranhaAnimation;

    public Piranha(double x, double y,double speedX, double speedY, boolean isActive) {
        this.x = x;
        this.y = y;
        this.speedX = speedX;
        this.speedY = speedY;
        this.isActive = isActive;
        piranhaAnimation = new Animation(SchoolRun.loadAnimationStrip(SchoolRun.piranhaStrip, 7), 7);
    }
    public void updateEnemy() {
        x += speedX;
        y += speedY;
    }
}