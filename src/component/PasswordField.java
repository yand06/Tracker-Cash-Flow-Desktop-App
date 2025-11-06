package component;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import javax.swing.*;

public class PasswordField extends JPasswordField {

    private boolean showPassword = false;
    private Rectangle iconBounds;
    private ImageIcon eyeIcon;
    private ImageIcon eyeSlashIcon;

    public PasswordField() {
        setOpaque(false);
        setBorder(null);
        setForeground(Color.BLACK);
        setFont(new Font("Sans Serif", Font.PLAIN, 14));
        setCaretColor(Color.BLACK);
        setEchoChar('•');

        // Load ikon
        eyeIcon = loadIcon("/icons/eye.png");
        eyeSlashIcon = loadIcon("/icons/eye-slash.png");

        // Mouse click untuk toggle show/hide password
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (iconBounds != null && iconBounds.contains(e.getPoint())) {
                    showPassword = !showPassword;
                    setEchoChar(showPassword ? (char) 0 : '•');
                    repaint();
                }
            }
        });
    }

    private ImageIcon loadIcon(String path) {
        URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            ImageIcon original = new ImageIcon(imgURL);
            Image scaled = original.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } else {
            System.err.println("Icon tidak ditemukan: " + path);
            return null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Gambar garis bawah
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(Color.BLACK);
        g2.fillRect(0, getHeight() - 2, getWidth(), 2);

        // Gambar ikon di kanan
        if (eyeIcon != null && eyeSlashIcon != null) {
            int iconSize = 18;
            int padding = 6;
            int x = getWidth() - iconSize - padding;
            int y = (getHeight() - iconSize) / 2;

            iconBounds = new Rectangle(x, y, iconSize, iconSize);
            ImageIcon iconToDraw = showPassword ? eyeSlashIcon : eyeIcon;
            g2.drawImage(iconToDraw.getImage(), x, y, iconSize, iconSize, null);
        }

        g2.dispose();
    }
}
