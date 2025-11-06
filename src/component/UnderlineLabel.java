package component;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JLabel;

/**
 * JLabel dengan garis bawah otomatis dan ketebalan yang dapat diatur.
 *
 * @author YAND
 */
public class UnderlineLabel extends JLabel {

    private Color underlineColor = Color.BLACK;
    private float underlineThickness = 2.0f;  // Ketebalan garis bawah default

    public UnderlineLabel() {
        super();
        setOpaque(false);
        setForeground(Color.BLACK);
    }

    public UnderlineLabel(String text) {
        super(text);
        setOpaque(false);
    }

    // Mengatur warna garis bawah
    public void setUnderlineColor(Color color) {
        this.underlineColor = color;
        repaint();
    }

    public Color getUnderlineColor() {
        return underlineColor;
    }

    // Mengatur ketebalan garis bawah
    public void setUnderlineThickness(float thickness) {
        this.underlineThickness = thickness;
        repaint();
    }

    public float getUnderlineThickness() {
        return underlineThickness;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(underlineColor);

        // Mengatur ketebalan garis bawah
        g2d.setStroke(new BasicStroke(underlineThickness));

        int y = getHeight() - 2;
        g2d.drawLine(0, y, getWidth(), y);
    }
}
