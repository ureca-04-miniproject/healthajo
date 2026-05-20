package healthajo.program.view.admin;

import healthajo.component.HButton;
import healthajo.component.HComboBox;
import healthajo.component.HDatePicker;
import healthajo.component.HDialog;
import healthajo.component.HLabel;
import healthajo.component.HTextField;
import healthajo.component.theme.AppTheme;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicSpinnerUI;

/**
 * 프로그램 등록 다이얼로그 (2단계).
 *
 * Step 1: 기본 정보 (프로그램명, 종목, 설명, 예약 가능 기간, 취소 마감일)
 * Step 2: 스케줄 설정 (운영 기간, 기본 정원, 요일별 시각)
 */
public class ProgramFormDialog extends JDialog {

    private boolean  saved = false;
    private String[] values;

    // Step 1 필드
    private HTextField        nameField;
    private HComboBox<String> categoryBox;
    private HTextField        descField;
    private HDatePicker       resStartPicker;
    private HDatePicker       resEndPicker;
    private HDatePicker       cancelDeadlinePicker;

    // Step 2 필드
    private HDatePicker opStartPicker;
    private HDatePicker opEndPicker;
    private HTextField  capacityField;
    private JPanel      weekdayPanel;
    private HButton     addDayBtn;

    // 내비게이션
    private CardLayout stepLayout;
    private JPanel     stepPanel;
    private HButton    nextBtn;
    private HButton    prevBtn;
    private int        currentStep = 0;

    // 스텝 인디케이터 라벨 (동적 갱신)
    private JLabel step1Lbl;
    private JLabel step2Lbl;

    public ProgramFormDialog(JFrame parent) {
        super(parent, "프로그램 등록", true);
        setLayout(new BorderLayout());
        setResizable(false);
        setSize(500, 580);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(AppTheme.SURFACE);

        stepLayout = new CardLayout();
        stepPanel  = new JPanel(stepLayout);
        stepPanel.setBackground(AppTheme.SURFACE);
        stepPanel.add(buildStep1(), "step1");
        stepPanel.add(buildStep2(), "step2");

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SP_2, AppTheme.SP_3));
        footer.setBackground(AppTheme.SURFACE);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, AppTheme.BORDER));

        HButton cancelBtn = HButton.ghost("취소", HButton.Size.SM);
        prevBtn = HButton.secondary("이전", HButton.Size.SM);
        nextBtn = HButton.primary("다음", HButton.Size.SM);
        prevBtn.setVisible(false);

        cancelBtn.addActionListener(e -> dispose());
        prevBtn.addActionListener(e -> goStep(0));
        nextBtn.addActionListener(e -> {
            if (currentStep == 0) goStep(1);
            else                  onConfirm();
        });
        footer.add(cancelBtn);
        footer.add(prevBtn);
        footer.add(nextBtn);

        add(buildStepBar(), BorderLayout.NORTH);
        add(stepPanel,      BorderLayout.CENTER);
        add(footer,         BorderLayout.SOUTH);
    }

    // ── 스텝 인디케이터 ──────────────────────────────────────────────────────────

    private JPanel buildStepBar() {
        step1Lbl = new JLabel("① 기본 정보");
        step2Lbl = new JLabel("② 스케줄 설정");

        JLabel arrow = new JLabel("→");
        arrow.setFont(AppTheme.SMALL);
        arrow.setForeground(AppTheme.TEXT_MUTED);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, AppTheme.SP_4, AppTheme.SP_3));
        bar.setBackground(AppTheme.SURFACE);
        bar.setBorder(new MatteBorder(0, 0, 1, 0, AppTheme.BORDER));
        bar.add(step1Lbl);
        bar.add(arrow);
        bar.add(step2Lbl);

        updateStepBarStyle(0);
        return bar;
    }

    private void updateStepBarStyle(int active) {
        if (step1Lbl == null || step2Lbl == null) return;
        step1Lbl.setFont(active == 0 ? AppTheme.LABEL : AppTheme.SMALL);
        step1Lbl.setForeground(active == 0 ? AppTheme.PRIMARY : AppTheme.TEXT_MUTED);
        step2Lbl.setFont(active == 1 ? AppTheme.LABEL : AppTheme.SMALL);
        step2Lbl.setForeground(active == 1 ? AppTheme.PRIMARY : AppTheme.TEXT_MUTED);
    }

    // ── Step 1: 기본 정보 ────────────────────────────────────────────────────────

    private JPanel buildStep1() {
        // ScrollablePanel: 뷰포트 너비를 추적해 콘텐츠 폭 오버플로우 방지
        ScrollablePanel p = new ScrollablePanel();
        p.setBackground(AppTheme.SURFACE);
        p.setBorder(new EmptyBorder(AppTheme.SP_4, AppTheme.SP_6, AppTheme.SP_4, AppTheme.SP_6));

        nameField            = new HTextField("예: 스피닝 A반");
        categoryBox          = new HComboBox<>(new String[]{"SPINNING", "YOGA", "PILATES", "GOLF"});
        descField            = new HTextField("프로그램 설명");
        resStartPicker       = new HDatePicker("시작일");
        resEndPicker         = new HDatePicker("종료일");
        cancelDeadlinePicker = new HDatePicker("취소 마감일");

        Dimension full = new Dimension(Short.MAX_VALUE, 44);
        nameField.setMaximumSize(full);
        categoryBox.setMaximumSize(full);
        descField.setMaximumSize(full);
        cancelDeadlinePicker.setMaximumSize(full);

        addFormRow(p, "프로그램명 *", nameField);
        p.add(Box.createVerticalStrut(AppTheme.SP_3));
        addFormRow(p, "종목 *",       categoryBox);
        p.add(Box.createVerticalStrut(AppTheme.SP_3));
        addFormRow(p, "설명",          descField);
        p.add(Box.createVerticalStrut(AppTheme.SP_3));

        // 예약 가능 기간
        JLabel periodLbl = formLabel("예약 가능 기간 *");
        p.add(periodLbl);
        p.add(Box.createVerticalStrut(AppTheme.SP_1));

        JPanel periodRow = new JPanel(new GridLayout(1, 3, AppTheme.SP_2, 0));
        periodRow.setOpaque(false);
        periodRow.setAlignmentX(LEFT_ALIGNMENT);
        periodRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 44));
        resStartPicker.setMaximumSize(full);
        resEndPicker.setMaximumSize(full);
        JLabel sep = HLabel.muted("~");
        sep.setHorizontalAlignment(SwingConstants.CENTER);
        sep.setMaximumSize(new Dimension(Short.MAX_VALUE, 44));
        periodRow.add(resStartPicker);
        periodRow.add(sep);
        periodRow.add(resEndPicker);
        p.add(periodRow);

        p.add(Box.createVerticalStrut(AppTheme.SP_3));
        addFormRow(p, "취소 마감일 *", cancelDeadlinePicker);

        return scrollWrap(p);
    }

    // ── Step 2: 스케줄 설정 ──────────────────────────────────────────────────────

    private JPanel buildStep2() {
        ScrollablePanel p = new ScrollablePanel();
        p.setBackground(AppTheme.SURFACE);
        p.setBorder(new EmptyBorder(AppTheme.SP_4, AppTheme.SP_6, AppTheme.SP_4, AppTheme.SP_6));

        opStartPicker = new HDatePicker("시작일");
        opEndPicker   = new HDatePicker("종료일");
        capacityField = new HTextField("20");
        capacityField.setMaximumSize(new Dimension(Short.MAX_VALUE, 44));

        // 운영 기간
        p.add(formLabel("운영 기간 *"));
        p.add(Box.createVerticalStrut(AppTheme.SP_1));

        Dimension full = new Dimension(Short.MAX_VALUE, 44);
        JPanel opRow = new JPanel(new GridLayout(1, 3, AppTheme.SP_2, 0));
        opRow.setOpaque(false);
        opRow.setAlignmentX(LEFT_ALIGNMENT);
        opRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 44));
        opStartPicker.setMaximumSize(full);
        opEndPicker.setMaximumSize(full);
        JLabel sep = HLabel.muted("~");
        sep.setHorizontalAlignment(SwingConstants.CENTER);
        sep.setMaximumSize(new Dimension(Short.MAX_VALUE, 44));
        opRow.add(opStartPicker);
        opRow.add(sep);
        opRow.add(opEndPicker);
        p.add(opRow);

        p.add(Box.createVerticalStrut(AppTheme.SP_3));
        addFormRow(p, "기본 정원 *", capacityField);
        p.add(Box.createVerticalStrut(AppTheme.SP_4));

        // 요일별 시각 헤더 (라벨 + 추가 버튼 한 줄)
        addDayBtn = HButton.ghost("+ 요일 추가", HButton.Size.SM);
        addDayBtn.addActionListener(e -> {
            if (weekdayPanel.getComponentCount() < 7) {
                weekdayPanel.add(buildWeekdayRow());
                weekdayPanel.revalidate();
                weekdayPanel.repaint();
                if (weekdayPanel.getComponentCount() >= 7) addDayBtn.setEnabled(false);
            }
        });

        JPanel wdHeader = new JPanel(new BorderLayout(AppTheme.SP_2, 0));
        wdHeader.setOpaque(false);
        wdHeader.setAlignmentX(LEFT_ALIGNMENT);
        wdHeader.setMaximumSize(new Dimension(Short.MAX_VALUE, 32));
        wdHeader.setBorder(new EmptyBorder(AppTheme.SP_1, 0, 0, 0));
        wdHeader.add(HLabel.label("요일별 시각 *"), BorderLayout.WEST);
        wdHeader.add(addDayBtn, BorderLayout.EAST);
        p.add(wdHeader);
        p.add(Box.createVerticalStrut(AppTheme.SP_2));

        weekdayPanel = new JPanel();
        weekdayPanel.setOpaque(false);
        weekdayPanel.setLayout(new BoxLayout(weekdayPanel, BoxLayout.Y_AXIS));
        weekdayPanel.setAlignmentX(LEFT_ALIGNMENT);
        weekdayPanel.add(buildWeekdayRow());
        p.add(weekdayPanel);

        return scrollWrap(p);
    }

    // ── 요일 행 ──────────────────────────────────────────────────────────────────

    private JPanel buildWeekdayRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, AppTheme.SP_3, AppTheme.SP_2));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Short.MAX_VALUE, 50));

        HComboBox<String> dayBox = new HComboBox<>(new String[]{"월", "화", "수", "목", "금", "토", "일"});
        dayBox.setPreferredSize(new Dimension(80, 36));

        JLabel startLbl = new JLabel("시작");
        JLabel endLbl   = new JLabel("종료");
        startLbl.setFont(AppTheme.CAPTION);
        startLbl.setForeground(AppTheme.TEXT_MUTED);
        endLbl.setFont(AppTheme.CAPTION);
        endLbl.setForeground(AppTheme.TEXT_MUTED);

        JPanel startTime = buildTimePicker(7, 0);
        JPanel endTime   = buildTimePicker(8, 0);

        HButton removeBtn = HButton.ghost("✕", HButton.Size.SM);
        removeBtn.setPreferredSize(new Dimension(32, 36));
        removeBtn.addActionListener(e -> {
            weekdayPanel.remove(row);
            weekdayPanel.revalidate();
            weekdayPanel.repaint();
            if (addDayBtn != null) addDayBtn.setEnabled(true);
        });

        row.add(dayBox);
        row.add(startLbl);
        row.add(startTime);
        row.add(endLbl);
        row.add(endTime);
        row.add(removeBtn);
        return row;
    }

    // ── 시간 선택기 ───────────────────────────────────────────────────────────────

    private JPanel buildTimePicker(int defaultH, int defaultM) {
        JSpinner hourSpin = new JSpinner(new SpinnerNumberModel(defaultH, 0, 23, 1));
        JSpinner minSpin  = new JSpinner(new SpinnerNumberModel(defaultM, 0, 55, 5));
        styleSpinner(hourSpin, 56);
        styleSpinner(minSpin,  56);

        JLabel colon = new JLabel(":");
        colon.setFont(AppTheme.font(Font.BOLD, 14));
        colon.setForeground(AppTheme.TEXT_SECONDARY);

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 0));
        p.setOpaque(false);
        p.add(hourSpin);
        p.add(colon);
        p.add(minSpin);
        return p;
    }

    private static void styleSpinner(JSpinner spinner, int totalWidth) {
        // 커스텀 UI: 플랫 화살표 버튼
        spinner.setUI(new BasicSpinnerUI() {
            @Override protected Component createNextButton() {
                JButton b = makeSpinBtn("▲");
                installNextButtonListeners(b);
                return b;
            }
            @Override protected Component createPreviousButton() {
                JButton b = makeSpinBtn("▼");
                installPreviousButtonListeners(b);
                return b;
            }
        });

        // 에디터 스타일 (setUI 후 적용)
        if (spinner.getEditor() instanceof JSpinner.NumberEditor ne) {
            ne.getFormat().applyPattern("00");
            JFormattedTextField tf = ne.getTextField();
            tf.setFont(AppTheme.BODY_SM);
            tf.setForeground(AppTheme.TEXT);
            tf.setBackground(AppTheme.INPUT_BG);
            tf.setHorizontalAlignment(JTextField.CENTER);
            tf.setBorder(new EmptyBorder(0, 4, 0, 4));
        }

        spinner.setPreferredSize(new Dimension(totalWidth, 36));
        spinner.setBackground(AppTheme.INPUT_BG);
        spinner.setBorder(BorderFactory.createLineBorder(AppTheme.INPUT_BORDER));
    }

    /** 스피너 증감 버튼 — 앱 테마에 맞는 플랫 스타일 */
    private static JButton makeSpinBtn(String sym) {
        JButton b = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()  ? AppTheme.PRIMARY_TINT
                           : getModel().isRollover() ? AppTheme.SURFACE_RAISED
                           :                          AppTheme.INPUT_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setFont(AppTheme.font(Font.PLAIN, 8));
                g2.setColor(getModel().isRollover() ? AppTheme.PRIMARY : AppTheme.TEXT_MUTED);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(sym,
                    (getWidth()  - fm.stringWidth(sym)) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
            @Override
            protected void paintBorder(Graphics g) {
                // 텍스트 영역과의 구분선 (좌측)
                g.setColor(AppTheme.INPUT_BORDER);
                g.drawLine(0, 0, 0, getHeight());
            }
            @Override public Dimension getPreferredSize() { return new Dimension(18, 0); }
            @Override public Dimension getMinimumSize()   { return getPreferredSize(); }
        };
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setRolloverEnabled(true);
        b.setBorderPainted(true);
        return b;
    }

    // ── 내비게이션 ───────────────────────────────────────────────────────────────

    private void goStep(int step) {
        if (step == 0) {
            // 이전 으로 돌아갈 때는 검증 없이 즉시 이동
            currentStep = 0;
            updateStepBarStyle(0);
            stepLayout.show(stepPanel, "step1");
            prevBtn.setVisible(false);
            nextBtn.setText("다음");
        } else {
            // 검증 먼저, 통과하면 상태 변경
            if (!validateStep1()) return;
            currentStep = 1;
            updateStepBarStyle(1);
            stepLayout.show(stepPanel, "step2");
            prevBtn.setVisible(true);
            nextBtn.setText("생성");
        }
    }

    private boolean validateStep1() {
        if (nameField.getText().trim().isEmpty()) {
            HDialog.error((JFrame) getOwner(), "프로그램명을 입력하세요.");
            return false;
        }
        if (resStartPicker.getText().isEmpty() || resEndPicker.getText().isEmpty()) {
            HDialog.error((JFrame) getOwner(), "예약 가능 기간을 입력하세요.");
            return false;
        }
        return true;
    }

    private void onConfirm() {
        if (capacityField.getText().trim().isEmpty()) {
            HDialog.error((JFrame) getOwner(), "기본 정원을 입력하세요.");
            return;
        }
        // TODO: INSERT INTO programs / program_schedules / schedule_weekdays / sessions
        values = new String[]{
            nameField.getText().trim(),
            (String) categoryBox.getSelectedItem(),
            resStartPicker.getText(),
            resEndPicker.getText(),
            capacityField.getText().trim()
        };
        saved = true;
        dispose();
    }

    public boolean  isSaved()   { return saved; }
    public String[] getValues() { return values; }

    // ── 공통 헬퍼 ────────────────────────────────────────────────────────────────

    private static JLabel formLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppTheme.LABEL);
        lbl.setForeground(AppTheme.TEXT);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(AppTheme.SP_1, 0, 0, 0));
        return lbl;
    }

    private static void addFormRow(JPanel p, String label, JComponent field) {
        p.add(formLabel(label));
        p.add(Box.createVerticalStrut(AppTheme.SP_1));
        field.setAlignmentX(LEFT_ALIGNMENT);
        p.add(field);
    }

    /**
     * getScrollableTracksViewportWidth = true 덕분에
     * JScrollPane이 항상 패널 폭을 뷰포트 폭에 맞춰 제한한다.
     * → 콘텐츠가 모달 너비를 벗어나는 오버플로우 방지.
     */
    private static final class ScrollablePanel extends JPanel implements Scrollable {
        ScrollablePanel() { setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d)  { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 64; }
        @Override public boolean getScrollableTracksViewportWidth()  { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    private static JPanel scrollWrap(JPanel content) {
        JScrollPane sp = new JScrollPane(content);
        sp.setBorder(null);
        sp.setViewportBorder(new EmptyBorder(AppTheme.SP_2, 0, 0, 0));
        sp.getViewport().setBackground(AppTheme.SURFACE);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        JPanel w = new JPanel(new BorderLayout());
        w.setBackground(AppTheme.SURFACE);
        w.add(sp);
        return w;
    }
}
