package healthajo.component;

import healthajo.component.theme.AppTheme;
import javax.swing.*;

/**
 * 라벨 + 입력 필드 쌍. 정렬 문제를 내부에서 처리.
 *
 * HFormGroup group = new HFormGroup("이름", new HTextField("이름 입력"));
 * // 또는
 * HFormGroup group = new HFormGroup("역할", new HComboBox<>(roles));
 */
public class HFormGroup extends JPanel {

    private final HLabel labelComp;

    public HFormGroup(String labelText, JComponent field) {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(LEFT_ALIGNMENT);

        labelComp = HLabel.label(labelText);
        labelComp.setAlignmentX(LEFT_ALIGNMENT);

        field.setAlignmentX(LEFT_ALIGNMENT);

        add(labelComp);
        add(Box.createVerticalStrut(AppTheme.SP_2));
        add(field);
    }

    public void setLabel(String text) {
        labelComp.setText(text);
    }
}
