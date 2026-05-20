package healthajo.component;

import healthajo.component.theme.AppTheme;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * 플랫 카드 컨테이너. 흰 배경 + 테두리만으로 구성 (그림자 없음).
 *
 * HCard card = new HCard();
 * card.add(someComponent);
 *
 * HCard card = HCard.titled("회원 목록", contentPanel);
 */
public class HCard extends JPanel {

    public HCard() {
        this(AppTheme.SP_6);
    }

    public HCard(int padding) {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(padding, padding, padding, padding));
    }

    public static HCard titled(String title, JComponent content) {
        HCard card = new HCard();
        JLabel lbl = HLabel.h3(title);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        content.setAlignmentX(LEFT_ALIGNMENT);
        card.add(lbl);
        card.add(Box.createVerticalStrut(AppTheme.SP_4));
        card.add(content);
        return card;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        g2.setColor(AppTheme.SURFACE);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, AppTheme.R_LG, AppTheme.R_LG));
        g2.setColor(AppTheme.BORDER);
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, AppTheme.R_LG, AppTheme.R_LG));
        g2.dispose();
    }
}
