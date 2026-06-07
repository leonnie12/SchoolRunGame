public class Fish {
    double x;
    double y;
    double speedX;
    double speedY;
    boolean isActive;
    int size = SchoolRun.FISH_SIZE;

    double randomOffsetX = Math.random() * 100;
    double randomOffsetY = Math.random() * 100;

    double knockbackX = 0;
    double knockbackY = 0;

    int stunTimer = 0;
    Animation fishAnimation;

    public Fish(double x, double y, boolean isActive) {
        this.x = x;
        this.y = y;
        this.isActive = isActive;

        fishAnimation = new Animation(SchoolRun.loadAnimationStrip(SchoolRun.fishStrip, 6), 7);
    }

    void setSpeed(double speedX, double speedY) {
        this.speedX = speedX;
        this.speedY = speedY;
    }

    public void updatePosition() {
        x += speedX;
        y += speedY;
    }

    public void bobbingMotion() {
        double t = System.currentTimeMillis() * 0.002;
        x += Math.sin(t + randomOffsetX) * 0.3;
        y += Math.cos(t + randomOffsetY) * 0.3;
    }
}