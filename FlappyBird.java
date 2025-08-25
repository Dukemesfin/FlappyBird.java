import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {

    // Bird
    Point p;
    int size;
    double velY;
    double gravity;

    // Score
    int score = 0;

    // Pipe class
    class Pipe {
        int x, gapY, width, gapHeight;
        boolean passed = false; // track if bird already scored

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

        public void draw(Graphics g, int panelHeight) {
            g.setColor(Color.GREEN);
            // Top pipe
            g.fillRect(x, 0, width, gapY);
            // Bottom pipe
            g.fillRect(x, gapY + gapHeight, width, panelHeight - (gapY + gapHeight));
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
        size = 30;
        velY = 0;
        gravity = 0.6;

        pipes = new ArrayList<>();

        timer = new javax.swing.Timer(16, this); // ~60 FPS
        timer.start();

        setFocusable(true);
        addKeyListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Background
        g.setColor(Color.CYAN);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Pipes
        for (Pipe pipe : pipes) {
            pipe.draw(g, getHeight());
        }

        // Bird
        g.setColor(Color.YELLOW);
        g.fillOval(p.x, p.y, size, size);

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
                    toRemove.add(pipe); // remove off screen
                }

                if (pipe.collision(birdRect)) {
                    gameOver = true;
                }

                // Scoring: bird passes pipe
                if (!pipe.passed && pipe.x + pipe.width < p.x) {
                    score++;
                    pipe.passed = true;
                }
            }

            pipes.removeAll(toRemove);
        }
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (gameOver) {
                // Restart
                p = new Point(100, 300);
                velY = 0;
                pipes.clear();
                score = 0;
                gameOver = false;
            } else {
                velY = -10; // Jump
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame window = new JFrame("FlappyBird");
        FlappyBird game = new FlappyBird();

        window.setSize(400, 700);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);
        window.add(game);
        window.setVisible(true);
    }
}
