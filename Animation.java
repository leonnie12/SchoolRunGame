import java.awt.image.*;


public class Animation {
        BufferedImage[] frames;
        int currentFrame = 0;
        int timer = 0;
        int frameDelay;
        
        public Animation(BufferedImage[] frames, int frameDelay) {
            this.frames = frames;
            this.frameDelay = frameDelay;
        }

        public void update() {
            timer++;
            if (timer >= frameDelay) {
                timer = 0;
                currentFrame++;
                if (currentFrame >= frames.length) {
                    currentFrame = 0;
                }
            }
        }

        public BufferedImage getCurrentFrame() {
            return frames[currentFrame];
        }
    
    }

