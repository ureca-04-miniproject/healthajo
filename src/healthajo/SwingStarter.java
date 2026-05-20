package healthajo;

import healthajo.component.theme.AppTheme;
import healthajo.signin.SignInPage;
import javax.swing.UIManager;

public final class SwingStarter {

    public static void startSwing() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        UIManager.put("defaultFont", AppTheme.BODY);
        UIManager.put("ScrollBar.width", 6);
        new SignInPage();
    }
}
