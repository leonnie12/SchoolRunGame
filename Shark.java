public class Shark { 
    double x; 
    double y; 
    double speedX; 
    double speedY; 
    int width = 450; 
    int height = 225; 
    
    int biteRange = 130; 
    Animation sharkAnimation; 
    public Shark(double x, double y, double speedX, double speedY) { 
        this.x = x; 
        this.y = y; 
        this.speedX = speedX; 
        this.speedY = speedY; 
        sharkAnimation = new Animation(SchoolRun.loadAnimationStrip(SchoolRun.sharkStrip, 9), 7); 
    } 
    int sharkMouthX = speedX>0 ? (int)(x+width-50) : (int)(x+50);
    int sharkMouthY = (int)(y + height / 2);    
    public void updateShark() {
        sharkMouthX = speedX<0 ? (int)(x+50) : (int)(x+width-50);
        sharkMouthY = (int)(y+height/2);
    }
    public boolean isFishInBiteRange(Fish fish) {
        double dx = fish.x - sharkMouthX; 
        double dy = fish.y - sharkMouthY; 
        double dist = Math.sqrt(dx * dx + dy * dy); 
        return dist <= biteRange;
    } 
}