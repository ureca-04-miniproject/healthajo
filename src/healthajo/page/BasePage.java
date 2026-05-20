package healthajo.page;

import healthajo.component.HLabel;
import healthajo.component.theme.AppTheme;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * 모든 내부 페이지의 템플릿. 상단 헤더(타이틀 + 툴바)와 콘텐츠 영역을 제공.
 *
 * class UserPage extends BasePage {
 *     protected String pageTitle() { return "회원 관리"; }
 *
 *     protected JComponent buildContent() {
 *         JPanel p = contentPanel();
 *         p.add(userTable.inScrollPane());
 *         return p;
 *     }
 *
 *     protected JComponent buildToolbar() {
 *         JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
 *         bar.setOpaque(false);
 *         bar.add(HButton.primary("회원 추가", HButton.Size.SM));
 *         return bar;
 *     }
 * }
 */
public abstract class BasePage extends JPanel {

    protected abstract String     pageTitle();
    protected abstract JComponent buildContent();

    /** 우측 상단 툴바 영역. 필요 시 오버라이드. */
    protected JComponent buildToolbar() { return null; }

    protected BasePage() {
        setLayout(new BorderLayout(0, 0));
        setBackground(AppTheme.BG);
        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(AppTheme.BORDER);
                g.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
        };
        header.setBackground(AppTheme.SURFACE);
        header.setBorder(new EmptyBorder(AppTheme.SP_4, AppTheme.SP_6, AppTheme.SP_4, AppTheme.SP_6));

        header.add(HLabel.h3(pageTitle()), BorderLayout.WEST);

        JComponent toolbar = buildToolbar();
        if (toolbar != null) {
            toolbar.setOpaque(false);
            header.add(toolbar, BorderLayout.EAST);
        }
        return header;
    }

    /** 콘텐츠 영역에 쓸 기본 패딩 패널. buildContent()에서 사용. */
    protected JPanel contentPanel() {
        JPanel p = new JPanel();
        p.setBackground(AppTheme.BG);
        p.setBorder(new EmptyBorder(AppTheme.SP_6, AppTheme.SP_6, AppTheme.SP_6, AppTheme.SP_6));
        return p;
    }

    /** 스크롤 가능한 콘텐츠 래퍼 */
    protected JScrollPane scrollContent(JComponent content) {
        JScrollPane sp = new JScrollPane(content);
        sp.setBorder(null);
        sp.getViewport().setBackground(AppTheme.BG);
        sp.setBackground(AppTheme.BG);
        return sp;
    }
}
