import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameOverPanel extends JPanel {
    public interface GameOverListener {
        void onRetry();
        void onQuit();
    }

    private final GameOverListener listener;

    public GameOverPanel(GameOverListener listener) {
        this.listener = listener;
        setLayout(null);
        setOpaque(false);
        setBounds(0, 0, 1024, 805);

        // Background (semi-transparent overlay)
        setBackground(new Color(0, 0, 0, 150));

        JLabel title = new JLabel("GAME OVER", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 80));
        title.setForeground(Color.RED);
        title.setBounds(200, 200, 600, 100);
        add(title);

        JButton retryButton = new JButton("RETRY");
        retryButton.setBounds(350, 350, 120, 50);
        retryButton.addActionListener(e -> {
            Container parent = getParent();
            if (parent != null) parent.remove(this);
            if (listener != null) listener.onRetry();
        });
        add(retryButton);

        JButton quitButton = new JButton("QUIT");
        quitButton.setBounds(550, 350, 120, 50);
        quitButton.addActionListener(e -> {
            Container parent = getParent();
            if (parent != null) parent.remove(this);
            if (listener != null) listener.onQuit();
        });
        add(quitButton);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Dark transparent overlay
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}
