package component;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JTextField;

/**
 *
 * @author YAND_LAAWE
 */
public class TextField extends JTextField {

    public TextField() {
        setOpaque(false);
        setBorder(null);
        setForeground(Color.BLACK);
        setFont(new Font("Sans Serif", Font.PLAIN, 14));
        setCaretColor(Color.BLACK);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.BLACK);
        g2.fillRect(0, getHeight() - 2, getWidth(), 2);
        g2.dispose();
    }
}
