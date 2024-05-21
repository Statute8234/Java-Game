import javax.swing.*;

public class App {
    public static void main(String[] args) {
        int WIDTH = 600;
        int HEIGHT = 600;
        JFrame window = new  JFrame("APP");
        window.setSize(600,600);
        // --- game
        Game game = new Game(WIDTH, HEIGHT);
        window.add(game);
        window.pack();
        // -- loop --
        window.setVisible(true);
        window.setLocationRelativeTo(null);
        window.setResizable(false);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}