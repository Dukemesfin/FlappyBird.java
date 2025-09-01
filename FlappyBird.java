import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {

    // Bird
    Point p;
    int size;
    double velY;
    double gravity;
    int frame = 0; // animation frame counter

    // Score
    int score = 0;

    // Sprites
    BufferedImage spriteSheet;
    BufferedImage[] birdFrames;
    BufferedImage pipeImg;
    BufferedImage backgroundImg;

    // Pipe class
    class Pipe {
        int x, gapY, width, gapHeight;
        boolean passed = false;

        public Pipe(int startX, int panelHeight) {
            x = startX;
            width = 80;
            gapHeight = 150;

            Random rand = new Random();
            gapY = 100 + rand.nextInt(panelHeight - 300);
        }

        public void move() {
            x -= 4;
        }

        public boolean collision(Rectangle bird) {
            Rectangle topRect = new Rectangle(x, 0, width, gapY);
            Rectangle bottomRect = new Rectangle(x, gapY + gapHeight, width, 700 - (gapY + gapHeight));
            return bird.intersects(topRect) || bird.intersects(bottomRect);
        }
    }

    ArrayList<Pipe> pipes;
    int pipeTimer = 0;

    javax.swing.Timer timer;
    boolean gameOver = false;

    public FlappyBird() {
        p = new Point(100, 300);
        size = 40; // scaled bird
        velY = 0;
        gravity = 0.6;

        pipes = new ArrayList<>();

        // Load sprites safely
        try {
            spriteSheet = ImageIO.read(new File("sprites.png"));
            System.out.println("✅ Sprite sheet loaded!");

            // Bird frames (orange bird)
            birdFrames = new BufferedImage[3];
            birdFrames[0] = spriteSheet.getSubimage(3, 491, 17, 12);   // wing up
            birdFrames[1] = spriteSheet.getSubimage(31, 491, 17, 12);  // wing middle
            birdFrames[2] = spriteSheet.getSubimage(59, 491, 17, 12);  // wing down

            // Pipe
            pipeImg = spriteSheet.getSubimage(0, 323, 26, 160);

            // Background
            backgroundImg = spriteSheet.getSubimage(0, 0, 144, 256);

        } catch (IOException e) {
            System.out.println("⚠️ ERROR: Could not load sprites.png. Using fallback shapes.");
            e.printStackTrace();
        }

        timer = new javax.swing.Timer(16, this); // ~60 FPS
        timer.start();

        setFocusable(true);
        addKeyListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Background
        if (backgroundImg != null) {
            g.drawImage(backgroundImg, 0, 0, getWidth(), getHeight(), null);
        } else {
            g.setColor(Color.CYAN);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
Graphics2D g2d = (Graphics2D) g; // cast once at the start

for (Pipe pipe : pipes) {
    if (pipeImg != null) {
        // --- Top pipe (flipped vertically) ---
        int topHeight = pipe.gapY; // height of top pipe
        g2d.translate(pipe.x + pipe.width / 2, topHeight / 2); // move origin to center of top pipe
        g2d.rotate(Math.PI); // rotate 180 degrees
        g2d.drawImage(pipeImg, -pipe.width / 2, -topHeight / 2, pipe.width, topHeight, null);
        g2d.rotate(-Math.PI); // reset rotation
        g2d.translate(-(pipe.x + pipe.width / 2), -(topHeight / 2)); // reset origin

        // --- Bottom pipe (normal) ---
        int bottomY = pipe.gapY + pipe.gapHeight;
        int bottomHeight = getHeight() - bottomY;
        g2d.drawImage(pipeImg, pipe.x, bottomY, pipe.width, bottomHeight, null);
    } else {
        // fallback rectangles
        g2d.setColor(Color.GREEN);
        g2d.fillRect(pipe.x, 0, pipe.width, pipe.gapY);
        g2d.fillRect(pipe.x, pipe.gapY + pipe.gapHeight, pipe.width, getHeight() - (pipe.gapY + pipe.gapHeight));
    }
}





        // Bird
        if (birdFrames != null) {
            BufferedImage birdImg = birdFrames[(frame / 9) % 3];
            g.drawImage(birdImg, p.x, p.y, size, size, null);
        } else {
            g.setColor(Color.YELLOW);
            g.fillOval(p.x, p.y, size, size);
        }

        // Score
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.drawString("Score: " + score, 20, 50);

        // Game Over text
        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("Game Over!", 100, getHeight() / 2);
            g.setFont(new Font("Arial", Font.PLAIN, 25));
            g.drawString("Press SPACE to restart", 70, getHeight() / 2 + 50);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            // Bird physics
            velY += gravity;
            p.y += velY;

            if (p.y + size > getHeight()) {
                p.y = getHeight() - size;
                velY = 0;
                gameOver = true;
            }
            if (p.y < 0) {
                p.y = 0;
                velY = 0;
            }

            // Spawn pipes
            pipeTimer++;
            if (pipeTimer > 100) {
                pipes.add(new Pipe(getWidth(), getHeight()));
                pipeTimer = 0;
            }

            // Move pipes & check collisions
            ArrayList<Pipe> toRemove = new ArrayList<>();
            Rectangle birdRect = new Rectangle(p.x, p.y, size, size);

            for (Pipe pipe : pipes) {
                pipe.move();

                if (pipe.x + pipe.width < 0) {
                    toRemove.add(pipe);
                }

                if (pipe.collision(birdRect)) {
                    gameOver = true;
                }

                if (!pipe.passed && pipe.x + pipe.width < p.x) {
                    score++;
                    pipe.passed = true;
                }
            }

            pipes.removeAll(toRemove);

            // Animate bird
            frame++;
        }
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (gameOver) {
                p = new Point(100, 300);
                velY = 0;
                pipes.clear();
                score = 0;
                gameOver = false;
                frame = 0;
            } else {
                velY = -10; // Jump
            }
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame window = new JFrame("FlappyBird");
        FlappyBird game = new FlappyBird();

        window.setSize(400, 800);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);
        window.add(game);
        window.setVisible(true);
    }
}
