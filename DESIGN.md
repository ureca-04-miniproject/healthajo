# DESIGN.md — 헬스장 예약 시스템 Swing UI 가이드

> `1. 디자인 철학`을 제외한 나머지 디자인 관련은 참조만 할 것 따르지 않아도 됨.

---

## 1. 디자인 철학

- **Compact** — 여백을 최소화하고 정보 밀도를 높인다. 스크롤 없이 한 화면에서 작업이 완결되는 것을 목표로 한다.
- **Flat & Modern** — 그라데이션, 입체 테두리 없음. 단색 + 미세한 선으로만 구성한다.
- **Component-first** — shadcn 방식으로 컴포넌트를 미리 정의하고 조립한다. 직접 색상·폰트를 지정하지 않고 토큰을 참조한다.
- **Template Method** — 같은 레이아웃에서 내용만 바뀌는 화면은 추상 베이스 패널을 상속해서 구성한다.

---

## 2. 색상 토큰 — `AppTheme.java`

```java
public class AppTheme {

    // ── Background ──────────────────────────────
    public static final Color BG_BASE       = new Color(0xF9F9F9); // 전체 배경
    public static final Color BG_SURFACE    = new Color(0xFFFFFF); // 카드, 패널 배경
    public static final Color BG_SUBTLE     = new Color(0xF1F1F1); // 테이블 행 홀수, 입력 비활성
    public static final Color BG_OVERLAY    = new Color(0xE8E8E8); // hover 상태

    // ── Border ──────────────────────────────────
    public static final Color BORDER        = new Color(0xE2E2E2); // 기본 선
    public static final Color BORDER_FOCUS  = new Color(0x6366F1); // 포커스 링

    // ── Text ────────────────────────────────────
    public static final Color TEXT_PRIMARY   = new Color(0x111111); // 주요 텍스트
    public static final Color TEXT_SECONDARY = new Color(0x6B6B6B); // 보조 텍스트
    public static final Color TEXT_DISABLED  = new Color(0xBBBBBB); // 비활성
    public static final Color TEXT_ON_ACCENT = new Color(0xFFFFFF); // 강조 배경 위 텍스트

    // ── Accent (Indigo) ──────────────────────────
    public static final Color ACCENT         = new Color(0x6366F1); // primary 버튼, 선택
    public static final Color ACCENT_HOVER   = new Color(0x4F46E5);
    public static final Color ACCENT_SUBTLE  = new Color(0xEEF2FF); // 선택된 행, 배지 배경

    // ── Semantic ────────────────────────────────
    public static final Color SUCCESS        = new Color(0x22C55E);
    public static final Color SUCCESS_BG     = new Color(0xF0FDF4);
    public static final Color WARNING        = new Color(0xF59E0B);
    public static final Color WARNING_BG     = new Color(0xFFFBEB);
    public static final Color DANGER         = new Color(0xEF4444);
    public static final Color DANGER_BG      = new Color(0xFEF2F2);
    public static final Color DANGER_HOVER   = new Color(0xDC2626);
    public static final Color INFO           = new Color(0x3B82F6);
    public static final Color INFO_BG        = new Color(0xEFF6FF);

    // ── Status Badge ────────────────────────────
    // 예약 상태
    public static final Color STATUS_CONFIRMED_FG   = new Color(0x3B82F6);
    public static final Color STATUS_CONFIRMED_BG   = new Color(0xEFF6FF);
    public static final Color STATUS_ISSUED_FG      = new Color(0x22C55E);
    public static final Color STATUS_ISSUED_BG      = new Color(0xF0FDF4);
    public static final Color STATUS_CANCELLED_FG   = new Color(0x6B6B6B);
    public static final Color STATUS_CANCELLED_BG   = new Color(0xF1F1F1);
    // 출석 상태
    public static final Color STATUS_ATTENDED_FG    = new Color(0x22C55E);
    public static final Color STATUS_ATTENDED_BG    = new Color(0xF0FDF4);
    public static final Color STATUS_ABSENT_FG      = new Color(0xEF4444);
    public static final Color STATUS_ABSENT_BG      = new Color(0xFEF2F2);
    public static final Color STATUS_PENDING_FG     = new Color(0xF59E0B);
    public static final Color STATUS_PENDING_BG     = new Color(0xFFFBEB);
    // 회원권 상태
    public static final Color STATUS_ACTIVE_FG      = new Color(0x22C55E);
    public static final Color STATUS_ACTIVE_BG      = new Color(0xF0FDF4);
    public static final Color STATUS_EXPIRED_FG     = new Color(0x6B6B6B);
    public static final Color STATUS_EXPIRED_BG     = new Color(0xF1F1F1);
}
```

---

## 3. 타이포그래피 토큰 — `AppFont.java`

```java
public class AppFont {

    private static final String FAMILY = "Inter";   // 없으면 "Segoe UI" → "Apple SD Gothic Neo" → "맑은 고딕" 순 fallback

    public static Font H1()       { return load(20, Font.BOLD);   }  // 페이지 타이틀
    public static Font H2()       { return load(16, Font.BOLD);   }  // 섹션 헤더
    public static Font H3()       { return load(14, Font.BOLD);   }  // 카드 타이틀
    public static Font BODY()     { return load(13, Font.PLAIN);  }  // 기본 텍스트
    public static Font BODY_SM()  { return load(12, Font.PLAIN);  }  // 테이블 셀, 보조 텍스트
    public static Font LABEL()    { return load(12, Font.BOLD);   }  // 폼 레이블
    public static Font CAPTION()  { return load(11, Font.PLAIN);  }  // 툴팁, 힌트

    private static Font load(int size, int style) {
        // 시스템 폰트 fallback 체인
        String[] candidates = { FAMILY, "Segoe UI", "Apple SD Gothic Neo", "맑은 고딕", "SansSerif" };
        for (String name : candidates) {
            Font f = new Font(name, style, size);
            if (!f.getFamily().equals("Dialog")) return f;
        }
        return new Font("SansSerif", style, size);
    }
}
```

---

## 4. 간격 / 반경 토큰 — `AppSpacing.java`

```java
public class AppSpacing {
    public static final int XS  = 4;
    public static final int SM  = 8;
    public static final int MD  = 12;
    public static final int LG  = 16;
    public static final int XL  = 24;

    public static final int RADIUS_SM  = 4;   // 배지, 소형 버튼
    public static final int RADIUS_MD  = 8;   // 버튼, 입력 필드
    public static final int RADIUS_LG  = 12;  // 카드, 패널
    public static final int RADIUS_XL  = 16;  // 모달

    public static final Insets CELL    = new Insets(6, 12, 6, 12);   // 테이블 셀
    public static final Insets BUTTON  = new Insets(8, 16, 8, 16);   // 버튼
    public static final Insets INPUT   = new Insets(8, 12, 8, 12);   // 입력 필드
    public static final Insets CARD    = new Insets(16, 16, 16, 16); // 카드 패딩
    public static final Insets SECTION = new Insets(24, 24, 24, 24); // 섹션 패딩
}
```

---

## 5. 컴포넌트 카탈로그

### 5-1. AppButton

```java
/**
 * variant: PRIMARY | SECONDARY | DANGER | GHOST
 * size:    SM | MD | LG
 *
 * 사용 예:
 *   AppButton.primary("저장")
 *   AppButton.secondary("취소")
 *   AppButton.danger("삭제")
 *   AppButton.ghost("닫기")
 */
public class AppButton extends JButton {

    public enum Variant { PRIMARY, SECONDARY, DANGER, GHOST }
    public enum Size    { SM, MD, LG }

    private final Variant variant;
    private boolean hovered = false;

    private AppButton(String text, Variant variant, Size size) {
        super(text);
        this.variant = variant;
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        switch (size) {
            case SM -> { setFont(AppFont.BODY_SM()); setMargin(new Insets(5, 10, 5, 10)); }
            case MD -> { setFont(AppFont.BODY());    setMargin(AppSpacing.BUTTON); }
            case LG -> { setFont(AppFont.H3());      setMargin(new Insets(10, 20, 10, 20)); }
        }

        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
            public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bg = switch (variant) {
            case PRIMARY   -> hovered ? AppTheme.ACCENT_HOVER : AppTheme.ACCENT;
            case SECONDARY -> hovered ? AppTheme.BG_OVERLAY   : AppTheme.BG_SURFACE;
            case DANGER    -> hovered ? AppTheme.DANGER_HOVER  : AppTheme.DANGER;
            case GHOST     -> hovered ? AppTheme.BG_OVERLAY    : new Color(0, 0, 0, 0);
        };

        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppSpacing.RADIUS_MD * 2, AppSpacing.RADIUS_MD * 2);

        if (variant == Variant.SECONDARY) {
            g2.setColor(AppTheme.BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, AppSpacing.RADIUS_MD * 2, AppSpacing.RADIUS_MD * 2);
        }

        g2.dispose();
        super.paintComponent(g);

        setForeground(switch (variant) {
            case PRIMARY, DANGER -> AppTheme.TEXT_ON_ACCENT;
            case SECONDARY, GHOST -> AppTheme.TEXT_PRIMARY;
        });
    }

    // ── 팩토리 메서드 ──────────────────────────
    public static AppButton primary(String text)   { return new AppButton(text, Variant.PRIMARY,   Size.MD); }
    public static AppButton secondary(String text) { return new AppButton(text, Variant.SECONDARY, Size.MD); }
    public static AppButton danger(String text)    { return new AppButton(text, Variant.DANGER,    Size.MD); }
    public static AppButton ghost(String text)     { return new AppButton(text, Variant.GHOST,     Size.MD); }
    public static AppButton primary(String text, Size size)   { return new AppButton(text, Variant.PRIMARY,   size); }
    public static AppButton secondary(String text, Size size) { return new AppButton(text, Variant.SECONDARY, size); }
}
```

---

### 5-2. AppTextField

```java
/**
 * 사용 예:
 *   AppTextField field = new AppTextField("이름을 입력하세요");
 *   AppTextField field = new AppTextField("검색", true); // 검색 아이콘 포함
 */
public class AppTextField extends JTextField {

    private final String placeholder;
    private boolean focused = false;

    public AppTextField(String placeholder) {
        this.placeholder = placeholder;
        setFont(AppFont.BODY());
        setForeground(AppTheme.TEXT_PRIMARY);
        setBackground(AppTheme.BG_SURFACE);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(
            AppSpacing.INPUT.top, AppSpacing.INPUT.left,
            AppSpacing.INPUT.bottom, AppSpacing.INPUT.right
        ));
        setPreferredSize(new Dimension(200, 36));

        addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { focused = true;  repaint(); }
            public void focusLost(FocusEvent e)   { focused = false; repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 배경
        g2.setColor(isEnabled() ? AppTheme.BG_SURFACE : AppTheme.BG_SUBTLE);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppSpacing.RADIUS_MD * 2, AppSpacing.RADIUS_MD * 2);

        // 테두리
        g2.setColor(focused ? AppTheme.BORDER_FOCUS : AppTheme.BORDER);
        g2.setStroke(new BasicStroke(focused ? 1.5f : 1f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, AppSpacing.RADIUS_MD * 2, AppSpacing.RADIUS_MD * 2);

        g2.dispose();
        super.paintComponent(g);

        // Placeholder
        if (getText().isEmpty() && !isFocusOwner()) {
            Graphics2D ph = (Graphics2D) g.create();
            ph.setFont(AppFont.BODY());
            ph.setColor(AppTheme.TEXT_DISABLED);
            FontMetrics fm = ph.getFontMetrics();
            ph.drawString(placeholder, AppSpacing.INPUT.left, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            ph.dispose();
        }
    }
}
```

---

### 5-3. AppTable

```java
/**
 * 사용 예:
 *   String[] cols = {"이름", "전화번호", "상태"};
 *   AppTable table = new AppTable(cols);
 *   table.addRow(new Object[]{"홍길동", "010-1234-5678", "ACTIVE"});
 *
 * 체크박스 다중 선택:
 *   AppTable table = new AppTable(cols, true); // 첫 컬럼 체크박스
 */
public class AppTable extends JTable {

    public AppTable(String[] columns) {
        this(columns, false);
    }

    public AppTable(String[] columns, boolean checkable) {
        super(buildModel(columns, checkable));
        setup();
    }

    private void setup() {
        setFont(AppFont.BODY_SM());
        setForeground(AppTheme.TEXT_PRIMARY);
        setBackground(AppTheme.BG_SURFACE);
        setGridColor(AppTheme.BORDER);
        setShowVerticalLines(false);
        setRowHeight(40);
        setIntercellSpacing(new Dimension(0, 0));
        setSelectionBackground(AppTheme.ACCENT_SUBTLE);
        setSelectionForeground(AppTheme.TEXT_PRIMARY);
        setFocusable(false);

        // 헤더 스타일
        JTableHeader header = getTableHeader();
        header.setFont(AppFont.LABEL());
        header.setBackground(AppTheme.BG_SUBTLE);
        header.setForeground(AppTheme.TEXT_SECONDARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER));
        header.setReorderingAllowed(false);

        // 홀수 행 배경 구분
        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) c.setBackground(row % 2 == 0 ? AppTheme.BG_SURFACE : AppTheme.BG_SUBTLE);
                setBorder(BorderFactory.createEmptyBorder(0, AppSpacing.LG, 0, AppSpacing.LG));
                return c;
            }
        });
    }

    private static DefaultTableModel buildModel(String[] columns, boolean checkable) {
        String[] cols = checkable
            ? Stream.concat(Stream.of(""), Arrays.stream(columns)).toArray(String[]::new)
            : columns;
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return checkable && c == 0; }
            @Override public Class<?> getColumnClass(int c)       { return checkable && c == 0 ? Boolean.class : String.class; }
        };
    }

    public void addRow(Object[] rowData) {
        ((DefaultTableModel) getModel()).addRow(rowData);
    }

    public void clearRows() {
        ((DefaultTableModel) getModel()).setRowCount(0);
    }

    /** 체크된 행 인덱스 목록 반환 (checkable 모드) */
    public List<Integer> getCheckedRows() {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < getRowCount(); i++) {
            if (Boolean.TRUE.equals(getValueAt(i, 0))) result.add(i);
        }
        return result;
    }
}
```

---

### 5-4. StatusBadge

```java
/**
 * 사용 예:
 *   StatusBadge.reservation("CONFIRMED")
 *   StatusBadge.attendance("ATTENDED")
 *   StatusBadge.membership("ACTIVE")
 */
public class StatusBadge extends JLabel {

    private final Color bg;
    private final Color fg;

    private StatusBadge(String text, Color fg, Color bg) {
        super(text);
        this.fg = fg;
        this.bg = bg;
        setFont(AppFont.CAPTION());
        setForeground(fg);
        setOpaque(false);
        setHorizontalAlignment(CENTER);
        setPreferredSize(new Dimension(getPreferredSize().width + 16, 22));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppSpacing.RADIUS_SM * 2, AppSpacing.RADIUS_SM * 2);
        g2.dispose();
        super.paintComponent(g);
    }

    // ── 팩토리 ──────────────────────────────────
    public static StatusBadge reservation(String status) {
        return switch (status) {
            case "CONFIRMED"         -> new StatusBadge("예약 확정",    AppTheme.STATUS_CONFIRMED_FG, AppTheme.STATUS_CONFIRMED_BG);
            case "MEMBERSHIP_ISSUED" -> new StatusBadge("회원권 발급",  AppTheme.STATUS_ISSUED_FG,    AppTheme.STATUS_ISSUED_BG);
            case "CANCELLED"         -> new StatusBadge("취소됨",       AppTheme.STATUS_CANCELLED_FG, AppTheme.STATUS_CANCELLED_BG);
            default                  -> new StatusBadge(status,         AppTheme.TEXT_SECONDARY,      AppTheme.BG_SUBTLE);
        };
    }

    public static StatusBadge attendance(String status) {
        return switch (status) {
            case "ATTENDED" -> new StatusBadge("출석",    AppTheme.STATUS_ATTENDED_FG, AppTheme.STATUS_ATTENDED_BG);
            case "ABSENT"   -> new StatusBadge("결석",    AppTheme.STATUS_ABSENT_FG,   AppTheme.STATUS_ABSENT_BG);
            case "PENDING"  -> new StatusBadge("미처리",  AppTheme.STATUS_PENDING_FG,  AppTheme.STATUS_PENDING_BG);
            default         -> new StatusBadge(status,   AppTheme.TEXT_SECONDARY,     AppTheme.BG_SUBTLE);
        };
    }

    public static StatusBadge membership(String status) {
        return switch (status) {
            case "ACTIVE"   -> new StatusBadge("활성",  AppTheme.STATUS_ACTIVE_FG,   AppTheme.STATUS_ACTIVE_BG);
            case "EXPIRED"  -> new StatusBadge("만료",  AppTheme.STATUS_EXPIRED_FG,  AppTheme.STATUS_EXPIRED_BG);
            default         -> new StatusBadge(status, AppTheme.TEXT_SECONDARY,     AppTheme.BG_SUBTLE);
        };
    }
}
```

---

### 5-5. AppCard

```java
/**
 * 그림자 없는 플랫 카드. 테두리 + 둥근 모서리.
 * 사용 예:
 *   AppCard card = new AppCard();
 *   card.add(somePanel);
 */
public class AppCard extends JPanel {

    public AppCard() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(
            AppSpacing.CARD.top, AppSpacing.CARD.left,
            AppSpacing.CARD.bottom, AppSpacing.CARD.right
        ));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(AppTheme.BG_SURFACE);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppSpacing.RADIUS_LG * 2, AppSpacing.RADIUS_LG * 2);
        g2.setColor(AppTheme.BORDER);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, AppSpacing.RADIUS_LG * 2, AppSpacing.RADIUS_LG * 2);
        g2.dispose();
    }
}
```

---

### 5-6. AppDialog (확인 다이얼로그)

```java
/**
 * 사용 예:
 *   boolean ok = AppDialog.confirm(parent, "삭제하시겠습니까?", "선택한 3명의 사용자가 삭제됩니다.");
 *   AppDialog.alert(parent, "오류", "중복된 전화번호입니다.", AppDialog.Type.DANGER);
 */
public class AppDialog extends JDialog {

    public enum Type { INFO, SUCCESS, WARNING, DANGER }

    private boolean confirmed = false;

    private AppDialog(JFrame parent, String title, String message, Type type, boolean hasCancel) {
        super(parent, title, true);
        setLayout(new BorderLayout());
        getContentPane().setBackground(AppTheme.BG_SURFACE);
        setUndecorated(false);
        setSize(400, 180);
        setLocationRelativeTo(parent);
        setResizable(false);

        // 아이콘 + 메시지 영역
        JPanel body = new JPanel(new BorderLayout(AppSpacing.MD, 0));
        body.setBorder(BorderFactory.createEmptyBorder(AppSpacing.XL, AppSpacing.XL, AppSpacing.MD, AppSpacing.XL));
        body.setBackground(AppTheme.BG_SURFACE);

        JLabel icon = new JLabel(switch (type) {
            case SUCCESS -> "✓";
            case WARNING -> "⚠";
            case DANGER  -> "✕";
            case INFO    -> "ℹ";
        });
        icon.setFont(new Font("SansSerif", Font.BOLD, 20));
        icon.setForeground(switch (type) {
            case SUCCESS -> AppTheme.SUCCESS;
            case WARNING -> AppTheme.WARNING;
            case DANGER  -> AppTheme.DANGER;
            case INFO    -> AppTheme.INFO;
        });
        icon.setVerticalAlignment(JLabel.TOP);

        JLabel msg = new JLabel("<html><body style='width:280px'>" + message + "</body></html>");
        msg.setFont(AppFont.BODY());
        msg.setForeground(AppTheme.TEXT_PRIMARY);

        body.add(icon, BorderLayout.WEST);
        body.add(msg,  BorderLayout.CENTER);

        // 버튼 영역
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppSpacing.SM, AppSpacing.MD));
        footer.setBackground(AppTheme.BG_SURFACE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.BORDER));

        if (hasCancel) {
            AppButton cancel = AppButton.secondary("취소");
            cancel.addActionListener(e -> dispose());
            footer.add(cancel);
        }

        AppButton confirm = switch (type) {
            case DANGER  -> AppButton.danger("확인");
            default      -> AppButton.primary("확인");
        };
        confirm.addActionListener(e -> { confirmed = true; dispose(); });
        footer.add(confirm);

        add(body,   BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    public static boolean confirm(JFrame parent, String title, String message) {
        AppDialog d = new AppDialog(parent, title, message, Type.WARNING, true);
        d.setVisible(true);
        return d.confirmed;
    }

    public static boolean confirmDanger(JFrame parent, String title, String message) {
        AppDialog d = new AppDialog(parent, title, message, Type.DANGER, true);
        d.setVisible(true);
        return d.confirmed;
    }

    public static void alert(JFrame parent, String title, String message, Type type) {
        AppDialog d = new AppDialog(parent, title, message, type, false);
        d.setVisible(true);
    }
}
```

---

### 5-7. AppToast

```java
/**
 * 화면 우하단에 2초간 표시 후 자동 소멸.
 * 사용 예:
 *   AppToast.success(frame, "저장되었습니다.");
 *   AppToast.error(frame, "오류가 발생했습니다.");
 */
public class AppToast {

    public static void success(JFrame parent, String message) { show(parent, message, AppTheme.SUCCESS_BG, AppTheme.SUCCESS); }
    public static void error(JFrame parent, String message)   { show(parent, message, AppTheme.DANGER_BG,  AppTheme.DANGER);  }
    public static void info(JFrame parent, String message)    { show(parent, message, AppTheme.INFO_BG,    AppTheme.INFO);    }

    private static void show(JFrame parent, String message, Color bg, Color accent) {
        JWindow toast = new JWindow(parent);
        toast.setBackground(new Color(0, 0, 0, 0));

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, AppSpacing.MD, AppSpacing.SM)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppSpacing.RADIUS_LG * 2, AppSpacing.RADIUS_LG * 2);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, AppSpacing.RADIUS_LG * 2, AppSpacing.RADIUS_LG * 2);
                g2.dispose();
            }
        };
        panel.setOpaque(false);

        JLabel label = new JLabel(message);
        label.setFont(AppFont.BODY());
        label.setForeground(accent);
        panel.add(label);
        panel.setPreferredSize(new Dimension(280, 44));

        toast.add(panel);
        toast.pack();

        // 우하단 위치 계산
        Rectangle screen = parent.getBounds();
        toast.setLocation(screen.x + screen.width - 300, screen.y + screen.height - 80);
        toast.setVisible(true);

        new Timer(2000, e -> toast.dispose()).start();
    }
}
```

---

### 5-8. AppProgressBar (잔여 횟수 시각화)

```java
/**
 * 사용 예:
 *   AppProgressBar bar = new AppProgressBar(10, 7); // 총 10회 중 7회 잔여
 */
public class AppProgressBar extends JPanel {

    private final int total;
    private int remaining;

    public AppProgressBar(int total, int remaining) {
        this.total     = total;
        this.remaining = remaining;
        setPreferredSize(new Dimension(160, 8));
        setOpaque(false);
    }

    public void setRemaining(int remaining) { this.remaining = remaining; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 트랙
        g2.setColor(AppTheme.BG_OVERLAY);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());

        // 채움
        if (total > 0) {
            float ratio = (float) remaining / total;
            Color fill = ratio > 0.5f ? AppTheme.SUCCESS
                       : ratio > 0.2f ? AppTheme.WARNING
                       :                AppTheme.DANGER;
            int w = (int) (getWidth() * ratio);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w, getHeight(), getHeight(), getHeight());
        }

        g2.dispose();
    }
}
```

---

### 5-9. AppSidebar

```java
/**
 * 좌측 네비게이션 사이드바.
 * 메뉴 항목 클릭 시 CardLayout 패널 전환.
 *
 * 사용 예:
 *   AppSidebar sidebar = new AppSidebar("관리자");
 *   sidebar.addMenu("사용자 관리", () -> cardLayout.show(cards, "users"));
 */
public class AppSidebar extends JPanel {

    private final List<SidebarItem> items = new ArrayList<>();
    private SidebarItem activeItem;

    public AppSidebar(String role) {
        setPreferredSize(new Dimension(200, 0));
        setBackground(AppTheme.BG_SURFACE);
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, AppTheme.BORDER));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // 로고/타이틀 영역
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, AppSpacing.LG, AppSpacing.LG));
        header.setBackground(AppTheme.BG_SURFACE);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        JLabel logo = new JLabel("🏋  FIT");
        logo.setFont(AppFont.H2());
        logo.setForeground(AppTheme.ACCENT);
        JLabel roleLabel = new JLabel(role);
        roleLabel.setFont(AppFont.CAPTION());
        roleLabel.setForeground(AppTheme.TEXT_SECONDARY);
        header.add(logo);
        add(header);
        add(Box.createVerticalStrut(AppSpacing.SM));
    }

    public void addMenu(String label, Runnable action) {
        SidebarItem item = new SidebarItem(label, action);
        items.add(item);
        add(item);
    }

    public void setActive(String label) {
        items.forEach(i -> i.setActive(i.label.equals(label)));
    }

    private class SidebarItem extends JPanel {
        private final String label;
        private boolean active = false;
        private boolean hovered = false;

        SidebarItem(String label, Runnable action) {
            this.label = label;
            setLayout(new FlowLayout(FlowLayout.LEFT, AppSpacing.LG, AppSpacing.SM));
            setOpaque(false);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel lbl = new JLabel(label);
            lbl.setFont(AppFont.BODY());
            add(lbl);

            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    items.forEach(i -> i.setActive(false));
                    setActive(true);
                    action.run();
                }
                public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            });
        }

        void setActive(boolean v) { active = v; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            if (active) {
                g2.setColor(AppTheme.ACCENT_SUBTLE);
                g2.fillRoundRect(AppSpacing.SM, 0, getWidth() - AppSpacing.MD, getHeight(),
                    AppSpacing.RADIUS_MD * 2, AppSpacing.RADIUS_MD * 2);
            } else if (hovered) {
                g2.setColor(AppTheme.BG_SUBTLE);
                g2.fillRoundRect(AppSpacing.SM, 0, getWidth() - AppSpacing.MD, getHeight(),
                    AppSpacing.RADIUS_MD * 2, AppSpacing.RADIUS_MD * 2);
            }
            g2.dispose();
            getComponent(0).setForeground(active ? AppTheme.ACCENT : AppTheme.TEXT_PRIMARY);
            ((JLabel) getComponent(0)).setFont(active ? AppFont.LABEL() : AppFont.BODY());
            super.paintComponent(g);
        }
    }
}
```

---

### 5-10. AppFormField (레이블 + 입력 묶음)

```java
/**
 * 폼에서 레이블과 입력 필드를 수직으로 묶는 컴포넌트.
 * 사용 예:
 *   AppFormField nameField = new AppFormField("이름", new AppTextField("홍길동"), true);
 *   AppFormField select    = new AppFormField("종목", new JComboBox<>(categories), false);
 */
public class AppFormField extends JPanel {

    public AppFormField(String label, JComponent input, boolean required) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        JPanel labelRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        labelRow.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(AppFont.LABEL());
        lbl.setForeground(AppTheme.TEXT_SECONDARY);
        labelRow.add(lbl);

        if (required) {
            JLabel star = new JLabel(" *");
            star.setFont(AppFont.LABEL());
            star.setForeground(AppTheme.DANGER);
            labelRow.add(star);
        }

        // AppTextField 외 컴포넌트 공통 스타일
        if (!(input instanceof AppTextField)) {
            input.setFont(AppFont.BODY());
            input.setPreferredSize(new Dimension(input.getPreferredSize().width, 36));
        }

        add(labelRow);
        add(Box.createVerticalStrut(AppSpacing.XS));
        add(input);
        input.setAlignmentX(LEFT_ALIGNMENT);
        labelRow.setAlignmentX(LEFT_ALIGNMENT);
    }

    /** 에러 메시지 표시 */
    public void showError(String message) {
        if (getComponentCount() < 4) {
            JLabel err = new JLabel(message);
            err.setFont(AppFont.CAPTION());
            err.setForeground(AppTheme.DANGER);
            err.setAlignmentX(LEFT_ALIGNMENT);
            add(Box.createVerticalStrut(2));
            add(err);
        } else {
            ((JLabel) getComponent(4)).setText(message);
        }
        revalidate();
    }

    public void clearError() {
        if (getComponentCount() >= 5) {
            remove(3); remove(3);
            revalidate(); repaint();
        }
    }
}
```

---

## 6. 레이아웃 템플릿 — Template Method 패턴

### 6-1. BaseListPanel (목록 + 검색 + 버튼 바)

```java
/**
 * 목록 화면의 공통 구조를 정의한다.
 * 하위 클래스에서 abstract 메서드만 구현하면 완성된 목록 화면이 만들어진다.
 *
 * 상속 예:
 *   public class UserListPanel extends BaseListPanel {
 *       protected String pageTitle()           { return "사용자 관리"; }
 *       protected String[] tableColumns()      { return new String[]{"이름","전화번호","이메일","등록일"}; }
 *       protected boolean hasCheckbox()        { return true; }
 *       protected List<AppButton> toolbarButtons() { return List.of(AppButton.primary("사용자 추가"), AppButton.danger("삭제")); }
 *       protected void loadData()              { /* DB 조회 후 table.addRow(...) * / }
 *       protected void onRowClick(int row)     { /* 상세 모달 오픈 * / }
 *       protected String searchPlaceholder()   { return "이름 또는 전화번호 검색"; }
 *   }
 */
public abstract class BaseListPanel extends JPanel {

    protected AppTable table;
    protected AppTextField searchField;
    private final JPanel toolbarPanel;

    public BaseListPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(AppTheme.BG_BASE);

        // ── 상단 헤더 ────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppTheme.BG_BASE);
        header.setBorder(BorderFactory.createEmptyBorder(
            AppSpacing.XL, AppSpacing.XL, AppSpacing.MD, AppSpacing.XL));

        JLabel title = new JLabel(pageTitle());
        title.setFont(AppFont.H1());
        title.setForeground(AppTheme.TEXT_PRIMARY);

        // 검색 + 버튼 우측 정렬
        toolbarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppSpacing.SM, 0));
        toolbarPanel.setBackground(AppTheme.BG_BASE);

        searchField = new AppTextField(searchPlaceholder());
        searchField.setPreferredSize(new Dimension(220, 36));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { filterTable(searchField.getText()); }
            public void removeUpdate(DocumentEvent e)  { filterTable(searchField.getText()); }
            public void changedUpdate(DocumentEvent e) { filterTable(searchField.getText()); }
        });
        toolbarPanel.add(searchField);

        toolbarButtons().forEach(toolbarPanel::add);

        header.add(title,        BorderLayout.WEST);
        header.add(toolbarPanel, BorderLayout.EAST);

        // ── 테이블 ───────────────────────────────
        table = new AppTable(tableColumns(), hasCheckbox());
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0) onRowClick(row);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.BORDER));
        scroll.getViewport().setBackground(AppTheme.BG_SURFACE);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        loadData();
    }

    // ── 하위 클래스에서 구현 ─────────────────────
    protected abstract String        pageTitle();
    protected abstract String[]      tableColumns();
    protected abstract boolean       hasCheckbox();
    protected abstract List<AppButton> toolbarButtons();
    protected abstract void          loadData();
    protected abstract void          onRowClick(int row);
    protected abstract String        searchPlaceholder();
    protected abstract void          filterTable(String keyword);
}
```

---

### 6-2. BaseFormDialog (등록 / 수정 모달)

```java
/**
 * 등록/수정 모달의 공통 구조.
 *
 * 상속 예:
 *   public class UserFormDialog extends BaseFormDialog {
 *       protected String dialogTitle()          { return "사용자 등록"; }
 *       protected JPanel buildForm()            { /* AppFormField 조합 * / }
 *       protected boolean validate()            { /* 유효성 검사 * / }
 *       protected void onConfirm()              { /* DB insert/update * / }
 *   }
 */
public abstract class BaseFormDialog extends JDialog {

    protected boolean saved = false;

    public BaseFormDialog(JFrame parent) {
        super(parent, "", true);
        setTitle(dialogTitle());
        setLayout(new BorderLayout());
        getContentPane().setBackground(AppTheme.BG_SURFACE);
        setResizable(false);

        // ── 폼 영역 ──────────────────────────────
        JPanel formWrapper = new JPanel();
        formWrapper.setLayout(new BoxLayout(formWrapper, BoxLayout.Y_AXIS));
        formWrapper.setBackground(AppTheme.BG_SURFACE);
        formWrapper.setBorder(BorderFactory.createEmptyBorder(
            AppSpacing.XL, AppSpacing.XL, AppSpacing.MD, AppSpacing.XL));
        formWrapper.add(buildForm());

        // ── 버튼 영역 ────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppSpacing.SM, AppSpacing.MD));
        footer.setBackground(AppTheme.BG_SURFACE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.BORDER));

        AppButton cancel  = AppButton.secondary("취소");
        AppButton confirm = AppButton.primary(confirmLabel());

        cancel.addActionListener(e -> dispose());
        confirm.addActionListener(e -> {
            if (validate()) { onConfirm(); saved = true; dispose(); }
        });

        footer.add(cancel);
        footer.add(confirm);

        add(formWrapper, BorderLayout.CENTER);
        add(footer,      BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(440, 0));
        setLocationRelativeTo(parent);
    }

    protected abstract String  dialogTitle();
    protected abstract JPanel  buildForm();
    protected abstract boolean validate();
    protected abstract void    onConfirm();
    protected String           confirmLabel() { return "저장"; }

    public boolean isSaved() { return saved; }
}
```

---

### 6-3. BaseDetailPanel (탭 기반 상세 패널)

```java
/**
 * 사용자/프로그램 상세처럼 탭으로 구성된 상세 패널.
 *
 * 상속 예:
 *   public class UserDetailPanel extends BaseDetailPanel {
 *       protected String panelTitle()      { return "홍길동"; }
 *       protected String panelSubtitle()   { return "010-1234-5678"; }
 *       protected List<TabDef> tabs()      {
 *           return List.of(
 *               new TabDef("기본 정보", buildInfoTab()),
 *               new TabDef("회원권",   buildMembershipTab()),
 *               new TabDef("예약 이력", buildReservationTab())
 *           );
 *       }
 *   }
 */
public abstract class BaseDetailPanel extends JPanel {

    public record TabDef(String label, JPanel content) {}

    public BaseDetailPanel() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_BASE);

        // ── 상단 타이틀 ──────────────────────────
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(AppTheme.BG_BASE);
        header.setBorder(BorderFactory.createEmptyBorder(
            AppSpacing.XL, AppSpacing.XL, AppSpacing.MD, AppSpacing.XL));

        JLabel title = new JLabel(panelTitle());
        title.setFont(AppFont.H1());
        title.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel(panelSubtitle());
        subtitle.setFont(AppFont.BODY());
        subtitle.setForeground(AppTheme.TEXT_SECONDARY);

        title.setAlignmentX(LEFT_ALIGNMENT);
        subtitle.setAlignmentX(LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(AppSpacing.XS));
        header.add(subtitle);

        // ── 탭 ──────────────────────────────────
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(AppFont.BODY());
        tabs.setBackground(AppTheme.BG_BASE);
        tabs.setBorder(BorderFactory.createEmptyBorder(0, AppSpacing.LG, AppSpacing.LG, AppSpacing.LG));

        tabs().forEach(t -> tabs.addTab(t.label(), t.content()));

        add(header, BorderLayout.NORTH);
        add(tabs,   BorderLayout.CENTER);
    }

    protected abstract String        panelTitle();
    protected abstract String        panelSubtitle();
    protected abstract List<TabDef>  tabs();
}
```

---

## 7. 화면별 컴포넌트 사용 매핑

| 화면 | 베이스 클래스 | 사용 컴포넌트 |
|---|---|---|
| 로그인 | - | `AppTextField`, `AppButton.primary` |
| 사용자 목록 | `BaseListPanel` | `AppTable(checkbox=true)`, `StatusBadge`, `AppButton` |
| 사용자 등록/수정 | `BaseFormDialog` | `AppFormField`, `AppTextField` |
| 사용자 상세 | `BaseDetailPanel` | `AppProgressBar`, `StatusBadge`, `AppTable` |
| 프로그램 목록 | `BaseListPanel` | `AppTable`, `StatusBadge` |
| 프로그램 등록 | `BaseFormDialog` | `AppFormField`, `JComboBox`, `AppTextField` |
| 프로그램 상세 | `BaseDetailPanel` | `AppTable`, `AppButton` |
| 세션 목록 | `BaseListPanel` | `AppTable`, `StatusBadge`, `AppButton` |
| 출석 관리 | `BaseListPanel` | `AppTable(checkbox=true)`, `StatusBadge.attendance`, `AppButton` |
| 예약 관리 | `BaseListPanel` | `AppTable`, `StatusBadge.reservation`, `AppButton.danger` |
| 회원권 관리 | `BaseDetailPanel` | `AppProgressBar`, `StatusBadge.membership`, `AppTable` |
| 확인 다이얼로그 | - | `AppDialog.confirm`, `AppDialog.confirmDanger` |
| 성공/실패 알림 | - | `AppToast.success`, `AppToast.error` |

---

## 8. 전체 앱 진입점 구조

```java
public class MainApp {

    public static void main(String[] args) {
        // 전역 폰트 설정
        UIManager.put("defaultFont", AppFont.BODY());

        // 스크롤바 슬림하게
        UIManager.put("ScrollBar.width", 6);
        UIManager.put("ScrollBar.thumbColor", AppTheme.BORDER);

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("헬스장 예약 시스템");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 760);
            frame.setMinimumSize(new Dimension(900, 600));
            frame.setLocationRelativeTo(null);
            frame.getContentPane().setBackground(AppTheme.BG_BASE);

            // CardLayout으로 로그인 ↔ 메인 전환
            CardLayout root = new CardLayout();
            JPanel rootPanel = new JPanel(root);
            rootPanel.add(new LoginPanel(frame, root, rootPanel), "login");
            rootPanel.add(new AdminMainPanel(frame),              "admin");
            rootPanel.add(new UserMainPanel(frame),               "user");

            frame.add(rootPanel);
            frame.setVisible(true);
        });
    }
}

// ── 관리자 메인 (사이드바 + CardLayout) ──────────────
public class AdminMainPanel extends JPanel {

    public AdminMainPanel(JFrame frame) {
        setLayout(new BorderLayout());

        CardLayout cards  = new CardLayout();
        JPanel     content = new JPanel(cards);

        content.add(new UserListPanel(frame),       "users");
        content.add(new ProgramListPanel(frame),    "programs");
        content.add(new SessionListPanel(frame),    "sessions");
        content.add(new AttendancePanel(frame),     "attendance");
        content.add(new ReservationPanel(frame),    "reservations");
        content.add(new MembershipAdminPanel(frame),"memberships");

        AppSidebar sidebar = new AppSidebar("관리자");
        sidebar.addMenu("사용자 관리",  () -> cards.show(content, "users"));
        sidebar.addMenu("프로그램 관리",() -> cards.show(content, "programs"));
        sidebar.addMenu("세션 관리",    () -> cards.show(content, "sessions"));
        sidebar.addMenu("출석 관리",    () -> cards.show(content, "attendance"));
        sidebar.addMenu("예약 관리",    () -> cards.show(content, "reservations"));
        sidebar.addMenu("회원권 관리",  () -> cards.show(content, "memberships"));

        add(sidebar, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);

        cards.show(content, "users");
        sidebar.setActive("사용자 관리");
    }
}
```

---

## 9. 패키지 구조

```
src/
├── main/
│   └── MainApp.java
├── theme/
│   ├── AppTheme.java       # 색상 토큰
│   ├── AppFont.java        # 폰트 토큰
│   └── AppSpacing.java     # 간격·반경 토큰
├── component/              # 재사용 컴포넌트 (shadcn 역할)
│   ├── AppButton.java
│   ├── AppTextField.java
│   ├── AppTable.java
│   ├── StatusBadge.java
│   ├── AppCard.java
│   ├── AppDialog.java
│   ├── AppToast.java
│   ├── AppProgressBar.java
│   ├── AppSidebar.java
│   ├── AppFormField.java
│   └── AppComboBox.java
├── template/               # Template Method 베이스 클래스
│   ├── BaseListPanel.java
│   ├── BaseFormDialog.java
│   └── BaseDetailPanel.java
# 아래부터 관리자 화면
├── AdminMainPanel.java
├── user/view/admin
│   ├── UserListPanel.java      extends BaseListPanel
│   ├── UserFormDialog.java     extends BaseFormDialog
│   └── UserDetailPanel.java    extends BaseDetailPanel
├── program/view/admin
│   ├── ProgramListPanel.java   extends BaseListPanel
│   ├── ProgramFormDialog.java  extends BaseFormDialog
│   └── ProgramDetailPanel.java extends BaseDetailPanel
├── session/view/admin
│   └── SessionListPanel.java   extends BaseListPanel
├── attendance/view/admin
│   └── AttendancePanel.java    extends BaseListPanel
├── reservation/view/admin
│   │   └── ReservationPanel.java   extends BaseListPanel
│   └── membership/view
│       └── MembershipAdminPanel.java extends BaseDetailPanel
# 아래부터 사용자 화면
├── user/view/user
│   ├── UserMainPanel.java
├── user/view/program
│   ├── ProgramBrowsePanel.java
...
```

# 주의할 점

위와 같은 구조로 작성하되, DB 접근 기술이 필요한 곳들 ex) 사용자 정보 조회, 예약 정보 조회, 예약 정보 생성 과 같은 곳들은 `// TODO` 와 어떤 데이터가 필요한지 및 어떤 기능이 필요한지 설명을 상세하게 서술한다.  
미 구현으로 인해 비어있는 곳은 주석으로 MOCK 데이터 부분을 명확하게 표기 한 뒤 MOCK 으로 일단은 동작 되도록 작성한다.
