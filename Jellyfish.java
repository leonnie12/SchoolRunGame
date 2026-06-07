import java.util.ArrayList;

public class Jellyfish {
    double x;
    double y;

    int width = 60;
    int height = 80;

    int cooldown = 180; 
    int timer = 0;

    boolean attacking = false;
    double waveRadius = 0;
    double waveSpeed = 4;

    double maxRadius = 100;

    public Jellyfish(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void update(ArrayList<Fish> school) {
        timer++;

        if (!attacking && timer >= cooldown) {
            attacking = true;
            waveRadius = 0;
            timer = 0;
        }

        if (attacking) {
            waveRadius += waveSpeed;

            // hit detection while wave expands
            for (int i = school.size() - 1; i >= 0; i--) {
                Fish f = school.get(i);

                double dx = f.x - x;
                double dy = f.y - y;
                double dist = Math.sqrt(dx * dx + dy * dy);

                // ring thickness (~10px)
                if (dist < waveRadius && dist > waveRadius - 10) {
                    school.remove(i);
                }
            }

            // end attack
            if (waveRadius >= maxRadius) {
                attacking = false;
                waveRadius = 0;
            }
        }
    }
}