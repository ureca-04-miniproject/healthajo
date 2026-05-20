package healthajo.page;

import healthajo.component.theme.AppTheme;
import java.awt.*;
import javax.swing.*;

/**
 * 모든 최상위 창의 템플릿. JFrame 설정을 중앙화.
 *
 * 구현 클래스는 frameTitle()과 buildContent()만 정의하면 된다.
 *
 * class SignInPage extends BaseFrame {
 *     protected String     frameTitle()   { return "건강하조"; }
 *     protected JComponent buildContent() { return buildCard(); }
 * }
 */
public abstract class BaseFrame extends JFrame {

    /** 창 타이틀 */
    protected abstract String frameTitle();

    /** 창 안에 배치될 최상위 컴포넌트 */
    protected abstract JComponent buildContent();

    /** 창 복원(un-maximize) 시 기본 크기. 필요 시 오버라이드. */
    protected Dimension defaultSize() { return new Dimension(1280, 800); }

    protected BaseFrame() {
        super();
        setTitle(frameTitle());
        Dimension d = defaultSize();
        setSize(d.width, d.height);
        setExtendedState(MAXIMIZED_BOTH);
        setResizable(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel bg = new JPanel(new GridBagLayout());
        bg.setBackground(AppTheme.BG);
        setContentPane(bg);
        bg.add(buildContent());

        setLocationRelativeTo(null);
        setVisible(true);
    }
}
