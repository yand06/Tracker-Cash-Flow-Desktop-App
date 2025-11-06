package component;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

public class CustomCellRenderer extends DefaultTableCellRenderer {

    private static final Color EVEN_ROW_COLOR = Color.WHITE;
    private static final Color ODD_ROW_COLOR = new Color(178, 203, 255);
    private static final Color SELECTED_COLOR = new Color(215, 215, 215);
    private static final Color TEXT_COLOR = new Color(39, 49, 58);
    private static final Color GRID_COLOR = new Color(199, 199, 199);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        setOpaque(true);
        setHorizontalAlignment(JLabel.CENTER);
        setFont(new Font("Sans Serif", Font.PLAIN, 14));
        setBorder(new EmptyBorder(5, 10, 5, 10));
        setForeground(TEXT_COLOR);

        if (isSelected) {
            setBackground(SELECTED_COLOR);
        } else {
            setBackground(row % 2 == 0 ? EVEN_ROW_COLOR : ODD_ROW_COLOR);
        }

        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, GRID_COLOR));

        return this;
    }
}
