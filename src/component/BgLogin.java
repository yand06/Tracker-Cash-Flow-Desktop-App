package component;

import java.awt.*;
import javax.swing.*;

public class BgLogin extends JPanel {

    private final Image image;

    public BgLogin() {
        image = new ImageIcon(getClass().getResource("/component/bgLogin.jpeg")).getImage();
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
        g2d.drawImage(image, 0, 0, getWidth(), getHeight(), this);

        g2d.dispose();
    }
}
