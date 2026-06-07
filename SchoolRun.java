import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class SchoolRun extends Canvas implements MouseListener, MouseMotionListener, KeyListener {

    static final int WIDTH = 1280;
    static final int HEIGHT = 720;
    static final int MAP_WIDTH = 5400;
    static final int MAP_HEIGHT = 3100;
    static final int FISH_SIZE = 18;
    static final int FRAME_DELAY = 16;
    
    static final int MENU = 0;
    static final int GAME = 1;
    static final int GAMEOVER = 2;
    static int state = MENU;
    static Rectangle startButton = new Rectangle((WIDTH-200)/2, (HEIGHT-120)/2, 200, 120);
    static Rectangle playAgainButton = new Rectangle((WIDTH-250)/2, 500, 250, 100);
    static final long TIME = 30*1_000_000_000L;
    static long startTime = 0;
    static int highScore = 0;
    static final int STARTING_FISH = 6;

    static BufferedImage startBackground;
    static BufferedImage background;
    static BufferedImage jellyfishImg;
    static BufferedImage piranhaStrip;
    static BufferedImage[] piranhaFrames;
    static BufferedImage fishStrip;
    static BufferedImage[] fishFrames;
    static BufferedImage sharkStrip;
    static BufferedImage[] sharkFrames;

    static ArrayList<Shark> sharks = new ArrayList<>();
    static ArrayList<Piranha> piranhas = new ArrayList<>();
    static ArrayList<Fish> school = new ArrayList<>();
    static ArrayList<Fish> wildFish = new ArrayList<>();
    static ArrayList<Jellyfish> jellyfish = new ArrayList<>();

    static final int electricDiameter = 450;

    static final int PIRANHA_SPEED = 11;
    static int piranhaSpawnTimer = 0;
    static int sharkSpawnTimer = 0;

    static final int GRID_SIZE = 450;
    static int[][] spawnGrid = new int[MAP_WIDTH/GRID_SIZE][MAP_HEIGHT/GRID_SIZE];

    static int camX = (MAP_WIDTH-WIDTH)/2;
    static int camY = (MAP_HEIGHT-HEIGHT)/2;
    static int mouseX = MAP_WIDTH / 2;
    static int mouseY = MAP_HEIGHT;
    static int clickX = 0;
    static int clickY = 0;
    static boolean clicked = false;

    static int playerSpeed = 12;
    static boolean isHeld = false;

    public static void loadImages() {
        try {
            startBackground = ImageIO.read(new File("startScreen1.png"));
            background = ImageIO.read(new File("backgroundfinal.png"));
            piranhaStrip = ImageIO.read(new File("piranha1.png"));
            fishStrip = ImageIO.read(new File("fishSwim1.png"));
            sharkStrip = ImageIO.read(new File("sharkBite1.png"));
        }
        catch (IOException e) {
        }
    }

    public static void initFish() {
        for (int i = 0; i < STARTING_FISH; i++) {
            school.add(new Fish((Math.random() * (MAP_WIDTH - FISH_SIZE) + 1),(Math.random() * FISH_SIZE), true));
        }
        
        for (int row = 0; row < MAP_HEIGHT / GRID_SIZE; row++) {
            for (int col = 0; col < MAP_WIDTH / GRID_SIZE; col++) {
                for (int i = 0; i < (int) (Math.random() * 3 + 1); i++)
                    wildFish.add(new Fish((int) (Math.random() * GRID_SIZE + 1 + GRID_SIZE * col), (int) (Math.random() * GRID_SIZE + 1 + GRID_SIZE * row), true));
                if (row % 2 == 0 && col % 2 == 0)
                    jellyfish.add(new Jellyfish((int) (Math.random() * GRID_SIZE + 1 + GRID_SIZE * col), (int) (Math.random() * GRID_SIZE + 1 + GRID_SIZE * row)));
            }
        }
    }

    public static void spawnPiranha() {
        boolean spawnLeft = Math.random() < 0.5;

        double spawnX = spawnLeft ? camX - 60 : camX + WIDTH + 60;
        double spawnY = camY + Math.random() * HEIGHT;

        double startSpeedX = spawnLeft? 4 : -4;

        piranhas.add(new Piranha(spawnX,spawnY,startSpeedX,0,true));
    }

    public static void spawnShark() {
        boolean spawnLeft = Math.random()<0.5;

        double spawnX = spawnLeft ? -400 : MAP_WIDTH;
        double spawnY = Math.random()*MAP_HEIGHT;

        double speedX = spawnLeft ?  Math.random()*3+5:-(Math.random()*3+5);
        double speedY = Math.random()*4-2;

        sharks.add(new Shark(spawnX,spawnY,speedX, speedY));
    }
        
    public static BufferedImage[] loadAnimationStrip(BufferedImage strip, int numFrames) {
        BufferedImage[] frames = new BufferedImage[numFrames];
        int frameWidth = strip.getWidth() / numFrames;
        int frameHeight = strip.getHeight();
        for(int i = 0; i<numFrames; i++) {
            frames[i] = strip.getSubimage(i*frameWidth, 0, frameWidth, frameHeight);
        }
        return frames;
    }

    public static void reset() {
        school.clear();
        wildFish.clear();
        piranhas.clear();
        sharks.clear();

        initFish();

        camX = (MAP_WIDTH - WIDTH) / 2;
        camY = (MAP_HEIGHT - HEIGHT) / 2;

        mouseX = MAP_WIDTH / 2;
        mouseY = MAP_HEIGHT;

        isHeld = false;
        piranhaSpawnTimer = 0;

        startTime = 0;
    }
    
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("👾");
        SchoolRun game = new SchoolRun();
        game.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        game.addMouseListener(game);
        game.addMouseMotionListener(game);
        game.addKeyListener(game);
        game.requestFocus();

        game.createBufferStrategy(3);
        BufferStrategy bs = game.getBufferStrategy();

        loadImages();
        initFish();

        while (true) {
            update();

            Graphics g = bs.getDrawGraphics();
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

            draw(g2d);

            g.dispose();

            bs.show();

            try {
                Thread.sleep(FRAME_DELAY);
            }
            catch (Exception e) {

            }
        }
    }

    public static void update() {
        switch (state) {
            case MENU -> {
                updateMenu();
            }
            case GAME -> {
                updateGame();
            }
            case GAMEOVER -> {
                updateGameOver();
            }
        }
    }

    public static void updateMenu() {
        if (startButton.contains(clickX, clickY) && clicked) {
            state = GAME;
            startTime = System.nanoTime();
            clicked = false;
        }
    }
    public static void updateGame() {
        Fish leader = school.get(0);
        leaderMovement();
        schoolMovement();
        addWildFish();
        //piranhaMovement();
        wildFishMovement();
        sharkMovement();
                
        camX = (int)(leader.x - WIDTH / 2);
        camY = (int)(leader.y - HEIGHT / 2);

        camX = Math.max(0, camX);
        camY = Math.max(0, camY);

        camX = Math.min(camX, MAP_WIDTH - WIDTH);
        camY = Math.min(camY, MAP_HEIGHT - HEIGHT);
        if (System.nanoTime()-startTime >= TIME) {
            state = GAMEOVER;
            isHeld = false;
        }
    }
    public static void updateGameOver() {
        highScore = Math.max(highScore, school.size());
        if (playAgainButton.contains(mouseX - camX, mouseY - camY)) {
            reset();
            state = MENU;
            isHeld = false;
        }
    }
    public static void draw(Graphics2D g2d) {
        switch (state) {
            case MENU -> {
                drawMenu(g2d);
            }
            case GAME -> {
                drawGame(g2d);
            }
        
            case GAMEOVER -> {
                drawGameOver(g2d);
            }
        }
    }
    public static void drawEntity(Graphics2D g2d, Animation animation, double x, double y, double speedX, double scale, int offset) {
        BufferedImage frame = animation.getCurrentFrame();

        int width = (int) (frame.getWidth() / scale);
        int height = (int) (frame.getHeight() / scale);
        
        if (speedX >= 0) {
            g2d.drawImage(frame, (int)(x - camX - offset), (int)(y - camY - offset), width, height, null);
        } 
        else {
            g2d.drawImage(frame, (int)(x - camX + width - offset), (int)(y - camY - offset), -width, height, null);
        }
    }

    public static void drawMenu(Graphics2D g2d) {
        g2d.drawImage(startBackground, 0, 0,WIDTH, HEIGHT,null);
        g2d.setColor(Color.CYAN);
        g2d.fill(startButton);
    }

    public static void drawGame(Graphics2D g2d) {
        g2d.drawImage(background, -camX, -camY,MAP_WIDTH, MAP_HEIGHT,null);
        for (Fish wildFish : wildFish) {
            drawEntity(g2d, wildFish.fishAnimation, wildFish.x,wildFish.y, wildFish.speedX, 2,20);
        }
        for (Fish fish:school) {
            drawEntity(g2d, fish.fishAnimation, fish.x, fish.y, fish.speedX, 1.8, 20);
        }
        for (Piranha piranha : piranhas) {
            drawEntity(g2d, piranha.piranhaAnimation, piranha.x, piranha.y, piranha.speedX, 1.8, 20);
        }
        for(Shark shark: sharks) {
            drawEntity(g2d, shark.sharkAnimation, shark.x, shark.y-100, shark.speedX, 0.14, 0);
            g2d.setColor(Color.GRAY);
            g2d.fillRect((int)shark.x-camX, (int)shark.y-camY, shark.width, shark.height);
            g2d.setColor(Color.GREEN);
            g2d.drawOval((int)shark.sharkMouthX-camX-shark.biteRange, (int)shark.sharkMouthY-camY-shark.biteRange, shark.biteRange*2, shark.biteRange*2);
        }
        for(Jellyfish jellyfish: jellyfish) {
            g2d.setColor(Color.PINK);
            g2d.fillRect((int)(jellyfish.x)-camX, (int)(jellyfish.y)-camY, jellyfish.width, jellyfish.height);
            g2d.setStroke(new BasicStroke(5));
            g2d.setColor(Color.YELLOW);
            g2d.drawOval((int)(jellyfish.x+(jellyfish.width-electricDiameter)/2)-camX, (int)(jellyfish.y+(jellyfish.height-electricDiameter)/2)-camY, electricDiameter,electricDiameter);
        }
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 40));
        g2d.drawString("Score: "+school.size(), 10, 50);
        g2d.drawString("Time Remaining: "+ (int)(30-1.0/1_000_000_000*(System.nanoTime()-startTime)), 10, 90);
    }
    public static void drawGameOver(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0,0,WIDTH,HEIGHT);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 80));
        g2d.drawString("TIME'S UP!", WIDTH/2 - 250, 180);

        g2d.setFont(new Font("SansSerif", Font.BOLD, 50));
        g2d.drawString("Score: " + school.size(), WIDTH/2 - 150, 300);

        g2d.drawString("High Score: " + highScore, WIDTH/2 - 200, 380);

        g2d.setColor(Color.CYAN);
        g2d.fill(playAgainButton);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 40));
        g2d.drawString("PLAY AGAIN", playAgainButton.x + 15, playAgainButton.y + 65);
    }

    public static void leaderMovement() {
        Fish leader = school.get(0);
        if (leader.stunTimer > 0) {
                leader.stunTimer--;

                leader.x += leader.knockbackX;
                leader.y += leader.knockbackY;
                leader.knockbackX *= 0.1;
                leader.knockbackY *= 0.1;
            }
        if (isHeld && !(leader.stunTimer > 0)) {
            double dx = mouseX - leader.x;
            double dy = mouseY - leader.y;

            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance > 15) {
                leader.speedX += (dx / distance) * playerSpeed * 0.2;
                leader.speedY += (dy / distance) * playerSpeed * 0.2;
            }

            double speed = Math.sqrt(leader.speedX * leader.speedX +leader.speedY * leader.speedY);
            double maxSpeed = playerSpeed;

            if (speed>maxSpeed) {
                leader.speedX =(leader.speedX / speed) * maxSpeed;
                leader.speedY =(leader.speedY / speed) * maxSpeed;
            }
            leader.updatePosition();
        }
        if (!isHeld) {
            leader.bobbingMotion();
        }
    }

    public static void schoolMovement() {
        Fish leader = school.get(0);
        for (int i = 1; i < school.size(); i++) {
            Fish fish = school.get(i);

            double moveX = 0;
            double moveY = 0;
            
            if (fish.stunTimer > 0) {
                fish.stunTimer--;

                fish.x += fish.knockbackX;
                fish.y += fish.knockbackY;

                fish.knockbackX *= 0.7;
                fish.knockbackY *= 0.7;
                continue; 
            }
            
            if (isHeld) {
                double dx = leader.x - fish.x;
                double dy = leader.y - fish.y;
                double distance =Math.sqrt(dx * dx + dy * dy);
                if (distance > 1) {
                    double pull = 1.2;
                    fish.speedX += (dx / distance) * pull;
                    fish.speedY += (dy / distance) * pull;

                    // Momentum sharing: follow the leader's velocity
                    fish.speedX += leader.speedX * 0.15;
                    fish.speedY += leader.speedY * 0.15;

                    // Tight push away to prevent overlapping with leader
                    if (distance < 100) {
                        fish.speedX -= (dx / distance);
                        fish.speedY -= (dy / distance);
                    }
                }
                for (int j = 0; j < school.size(); j++) {
                    if (i == j) {
                        continue;
                    }
                    Fish other = school.get(j);
                    double dxo = fish.x-other.x;
                    double dyo = fish.y-other.y;
                    double interdistance = Math.sqrt(dxo * dxo + dyo * dyo);
                    double separation = 80;
                    if (interdistance < separation) {
                        double pushStrength = (1.0 - (interdistance / separation)); 
                        moveX += (dxo/interdistance) * pushStrength;
                        moveY += (dyo/interdistance) * pushStrength;
                    }   
                }

                fish.speedX +=moveX;
                fish.speedY +=moveY;

                fish.speedX *=0.9;
                fish.speedY *=0.9;

                double speed = Math.sqrt(fish.speedX*fish.speedX + fish.speedY*fish.speedY);
                if (speed> playerSpeed) {
                    fish.speedX = (fish.speedX / speed) * playerSpeed;
                    fish.speedY = (fish.speedY / speed) * playerSpeed;
                }
                fish.updatePosition();
            }
            else {
                fish.bobbingMotion();
            }
        }
    }

    public static void wildFishMovement() {
        for(int i = wildFish.size()-1; i>0; i--) {
            wildFish.get(i).bobbingMotion();
        }
    }
    
    public static void piranhaMovement() {
        Fish leader = school.get(0);
        piranhaSpawnTimer++;
        if (piranhaSpawnTimer >= 120) {
            spawnPiranha();
            piranhaSpawnTimer = 0;
        }
        for (int i = piranhas.size() - 1; i >= 0; i--) {
            Piranha piranha = piranhas.get(i);
            piranha.piranhaAnimation.update();
            piranha.homingTimer++;
            double dx = leader.x - piranha.x;
            double dy = leader.y - piranha.y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            double speed;
            if (!(piranha.homingTimer>240)) {
                piranha.speedX += (dx /distance)*0.75;
                piranha.speedY += (dy/distance)*0.75;
                speed = Math.sqrt(piranha.speedX * piranha.speedX +piranha.speedY * piranha.speedY);
            }
            else {
                speed = Math.sqrt(piranha.speedX * piranha.speedX +piranha.speedY * piranha.speedY);
                piranha.speedX = (piranha.speedX / speed) * 10;
                piranha.speedY = (piranha.speedY / speed) * 10;
            }

            if (speed>PIRANHA_SPEED) {
                piranha.speedX = (piranha.speedX / speed)*PIRANHA_SPEED;
                piranha.speedY = (piranha.speedY / speed)*PIRANHA_SPEED;
            }

            piranha.updateEnemy();
            for (int j = school.size() - 1; j > 0; j--) {
                Fish fish = school.get(j);
                double dx1 = fish.x - piranha.x;
                double dy1 = fish.y - piranha.y;

                if (Math.sqrt(dx1 * dx1 + dy1 * dy1) < 30) {
                    school.remove(j);
                }
            }
            if (piranha.x<camX-200|| piranha.x >camX+WIDTH+200) {
                piranhas.remove(i);
            }
        }
    }
    
    public static void sharkMovement() {
        sharkSpawnTimer++;
        if (sharkSpawnTimer >= 90) {
            spawnShark();
            sharkSpawnTimer = 0;
        }
        for(int i = sharks.size()-1; i>=0; i--) {
            Shark shark = sharks.get(i);
            shark.x+= shark.speedX;
            shark.y+= shark.speedY;
            shark.sharkAnimation.update();

            if (shark.speedX > 0 && shark.x > MAP_WIDTH) {
                sharks.remove(shark);
            }
            if (shark.speedX < 0 && shark.x+shark.width<0) {
                sharks.remove(shark);
            }
            Rectangle sharkRect = new Rectangle((int)(shark.x),(int)(shark.y),shark.width,shark.height);
            for (int j = school.size()-1; j>=1; j--) {
                Fish fish = school.get(j);
                if (shark.isFishInBiteRange(fish)) {
                    school.remove(fish);
                    continue;
                }
                if(fish.stunTimer>0)
                    continue;
                Rectangle fishRect = new Rectangle((int)(fish.x),(int)(fish.y),fish.size,fish.size);
                if (sharkRect.intersects(fishRect) && fish.stunTimer <= 0) {
                    double dx = fish.x - shark.x;
                    double dy = fish.y - shark.y;

                    double dist = Math.sqrt(dx * dx + dy * dy);

                    if (dist == 0) {
                        dx = 1;
                        dy = 0;
                        dist = 1;
                    }

                    dx /= dist;
                    dy /= dist;

                    Rectangle intersection = sharkRect.intersection(fishRect);

                    if (!intersection.isEmpty()) {

                        if (intersection.width < intersection.height) {
                            if (fish.x < shark.x) {
                                fish.x -= intersection.width;
                            } else {
                                fish.x += intersection.width;
                            }
                        } else {
                            if (fish.y < shark.y) {
                                fish.y -= intersection.height;
                            } else {
                                fish.y += intersection.height;
                            }
                        }
                    }
                    double knockbackStrength = 30;

                    fish.knockbackX = dx * knockbackStrength;
                    fish.knockbackY = dy * knockbackStrength;

                    fish.stunTimer = 10;
                }
                shark.updateShark();
            }
        }
    }
    
    public static void addWildFish() {
        Fish leader = school.get(0);
        for (int i = wildFish.size() - 1; i >= 0; i--) {
            Fish fish = wildFish.get(i);
            double dx = leader.x - fish.x;
            double dy = leader.y - fish.y;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance < 100) {
                school.add(fish);
                wildFish.remove(i);
            }
        }
    }

    public void mouseDragged(MouseEvent e) {
        if (state == GAME) {
            mouseX = e.getX() + camX;
            mouseY = e.getY() + camY;
            isHeld = true;
        }
    }

    public void mouseMoved(MouseEvent e) {}

    public void mouseClicked(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {
        if (state == MENU) {
            clickX = e.getX();
            clickY = e.getY();
            clicked = true;
        }
        if (state == GAME) {
            mouseX = e.getX() + camX;
            mouseY = e.getY() + camY;
            isHeld = true;
        }
    }

    public void mouseReleased(MouseEvent e) {
        isHeld = false;
    }

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    public void keyPressed(KeyEvent e) {}

    public void keyReleased(KeyEvent e) {}

    public void keyTyped(KeyEvent e) {}
}