package com.darwish.nppsim;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.sound.sampled.Clip;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * 设置对话框：游戏 / 音频 / 视频 / 语言 / 模组 / 原版设置 六个分页。
 *  - 游戏：Ctrl+滚轮缩放开关 + 重置
 *  - 音频：主音量 / 警报音量 / 环境音量（三通道，大滑块）+ 静音 + 声音选择试听
 *  - 视频：UI 风格（经典深色 / FlatLaf 深色 / FlatLaf 浅色）+ 窗口模式（窗口化 / 最大化）
 *  - 语言：七种语言
 *  - 模组：添加 JAR 文件 + 已添加列表（持久化）
 *  - 原版设置：页面刷新率、Log 操作（打开日志窗口/清空事件日志）、MTK（符号/阈值）
 *    —— 由游戏内菜单栏移入；「暂停」选项保留在原菜单未动。
 *  底部「应用」「确定」：应用 = 保存；确定 = 保存并关闭。
 */
public class SettingsDialog extends JDialog {

    /** 窗口模式 */
    public static final int MODE_WINDOWED = 0;
    public static final int MODE_MAXIMIZED = 1;

    private static final Preferences PREFS = Preferences.userRoot().node("com/darwish/nppsim");
    private static final String KEY_WINDOW_MODE = "video.windowMode";
    private static final String KEY_MODS = "mods.jars";
    private static final String KEY_UI_UPDATE_RATE = "game.uiUpdateRate";

    /** 试听专用 owner：避免试听结束时把正在响的真实警笛一起掐掉 */
    private static final Object TEST_OWNER = new Object();

    private final JTabbedPane tabs = new JTabbedPane();

    // 音频分页控件
    private JSlider masterSlider, alarmSlider, ambientSlider;
    private JLabel masterValue, alarmValue, ambientValue;
    private JCheckBox muteBox;
    private JComboBox<String> soundCombo;
    private JButton testButton;
    private JPanel audioPanel;

    // 语言分页控件
    private JPanel languagePanel;
    private JLabel languageHint;

    // 游戏分页控件
    private JPanel gamePanel;
    private JCheckBox zoomBox;
    private JLabel zoomHint;
    private JLabel zoomLevel;
    private JButton resetZoomButton;

    // 视频分页控件
    private JPanel videoPanel;
    private JRadioButton styleClassic, styleFlatDark, styleFlatLight;
    private JRadioButton windowedRadio, maximizedRadio;
    private JLabel videoHint;

    // 模组分页控件
    private JPanel modsPanel;
    private JList<String> modsList;
    private javax.swing.DefaultListModel<String> modsModel;
    private JButton modsAddButton;
    private JButton modsRemoveButton;
    private JLabel modsHint;

    // 原版设置分页控件
    private JPanel classicPanel;
    private JComboBox<String> refreshCombo;
    private JButton openLogButton;
    private JButton clearLogButton;
    private JRadioButton signPlus, signMinus;
    private JComboBox<String> thresholdCombo;
    private JPanel unitsPanel;
    private JLabel unitsHint;
    private JLabel classicHint;

    private final JButton applyButton = new JButton();
    private final JButton okButton = new JButton();

    /* 页签顺序必须与构造里 addTab 的顺序**严格一致**：
     * Game / Audio / Video / Units / Language / Mods / Classic。
     * 旧代码漏掉 Units 的索引，导致 Language 页挂着"单位"的标题、Mods 挂着"语言"、
     * Classic 挂着"模组"，最后一个页签还是空标题 —— 这就是"选项和内容不匹配"。 */
    private static final int TAB_GAME = 0;
    private static final int TAB_AUDIO = 1;
    private static final int TAB_VIDEO = 2;
    private static final int TAB_UNITS = 3;
    private static final int TAB_LANGUAGE = 4;
    private static final int TAB_MODS = 5;
    private static final int TAB_CLASSIC = 6;

    /** 页面刷新率可选项（毫秒） */
    private static final long[] REFRESH_VALUES = {50, 100, 150, 200, 500, 1000};
    private static final String[] REFRESH_KEYS = {"50ms", "100ms", "150ms", "200ms", "500ms", "1000ms"};
    /** MTK 阈值可选项（比例） */
    private static final float[] THRESHOLD_VALUES = {0.005f, 0.01f, 0.025f, 0.05f, 0.1f, 0.15f, 0.2f, 0.25f};
    private static final String[] THRESHOLD_KEYS = {"0.5%", "1%", "2.5%", "5%", "10%", "15%", "20%", "25%"};

    public SettingsDialog(Frame owner) {
        super(owner, true);

        tabs.addTab("", buildGameTab());
        tabs.addTab("", buildAudioTab());
        tabs.addTab("", buildVideoTab());
        tabs.addTab("", buildUnitsTab());
        tabs.addTab("", buildLanguageTab());
        tabs.addTab("", buildModsTab());
        tabs.addTab("", buildClassicTab());

        applyButton.addActionListener((ActionEvent e) -> applyAll());
        okButton.addActionListener((ActionEvent e) -> {
            applyAll();
            dispose();
        });
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);
        buttons.add(applyButton);
        buttons.add(okButton);

        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setBorder(BorderFactory.createEmptyBorder(10, 12, 8, 12));
        content.setBackground(UI.BACKGROUND);
        content.add(tabs, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        getContentPane().setBackground(UI.BACKGROUND);
        add(content, BorderLayout.CENTER);

        applyTexts();
        setPreferredSize(new Dimension(520, 500));
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    /** 应用全部设置：持久化 + 立即刷新（音量/风格等控件本身即改即生效，这里兜底保存） */
    private void applyAll() {
        SoundProvider.saveVolumes();
        try {
            PREFS.flush();
        } catch (Exception ignored) {
        }
        for (Window w : Window.getWindows()) {
            if (w instanceof Frame && w.isVisible()) {
                SettingsDialog.applyWindowMode((Frame) w);
            }
        }
    }

    /** 按当前语言刷新标题/分页名/按钮（语言切换后立即调用） */
    private final void applyTexts() {
        setTitle(Localization.t("settings.title"));
        tabs.setTitleAt(TAB_GAME, Localization.t("settings.tab.game"));
        tabs.setTitleAt(TAB_AUDIO, Localization.t("settings.tab.audio"));
        tabs.setTitleAt(TAB_VIDEO, Localization.t("settings.tab.video"));
        tabs.setTitleAt(TAB_UNITS, Localization.t("settings.units.tab"));
        tabs.setTitleAt(TAB_LANGUAGE, Localization.t("settings.tab.language"));
        tabs.setTitleAt(TAB_MODS, Localization.t("settings.tab.mods"));
        tabs.setTitleAt(TAB_CLASSIC, Localization.t("settings.tab.classic"));
        applyButton.setText(Localization.t("settings.apply"));
        okButton.setText(Localization.t("settings.ok"));
        if (audioPanel != null) {
            audioPanel.setBorder(BorderFactory.createTitledBorder(Localization.t("settings.audio.group")));
            muteBox.setText(Localization.t("settings.audio.mute"));
            testButton.setText(Localization.t("settings.audio.test"));
            refreshSoundComboItems();
        }
        if (languagePanel != null) {
            languagePanel.setBorder(BorderFactory.createTitledBorder(Localization.t("settings.language.group")));
            languageHint.setText(Localization.t("settings.language.hint"));
        }
        if (gamePanel != null) {
            gamePanel.setBorder(BorderFactory.createTitledBorder(Localization.t("settings.game.group")));
            zoomBox.setText(Localization.t("settings.game.zoom"));
            zoomHint.setText(Localization.t("settings.game.zoomHint"));
            resetZoomButton.setText(Localization.t("settings.game.zoomReset"));
            updateZoomLevelText();
        }
        if (videoPanel != null) {
            videoPanel.setBorder(BorderFactory.createTitledBorder(Localization.t("settings.video.group")));
            styleClassic.setText(Localization.t("settings.video.styleClassic"));
            styleFlatDark.setText(Localization.t("settings.video.styleFlatDark"));
            styleFlatLight.setText(Localization.t("settings.video.styleFlatLight"));
            windowedRadio.setText(Localization.t("settings.video.window"));
            maximizedRadio.setText(Localization.t("settings.video.maximized"));
            videoHint.setText(Localization.t("settings.video.hint"));
        }
        if (modsPanel != null) {
            modsPanel.setBorder(BorderFactory.createTitledBorder(Localization.t("settings.mods.group")));
            modsAddButton.setText(Localization.t("settings.mods.add"));
            modsRemoveButton.setText(Localization.t("settings.mods.remove"));
            modsHint.setText(Localization.t("settings.mods.hint"));
        }
        if (classicPanel != null) {
            classicPanel.setBorder(BorderFactory.createTitledBorder(Localization.t("settings.classic.group")));
            openLogButton.setText(Localization.t("settings.classic.openLog"));
            clearLogButton.setText(Localization.t("settings.classic.clearLog"));
            classicHint.setText(Localization.t("settings.classic.hint"));
        }
        // 语言切换后重刷下拉框的本地化条目（保持选中项）
        if (refreshCombo != null) {
            int sel = refreshCombo.getSelectedIndex();
            refreshCombo.removeAllItems();
            for (String k : REFRESH_KEYS) {
                refreshCombo.addItem(Localization.t(k));
            }
            if (sel >= 0 && sel < REFRESH_KEYS.length) {
                refreshCombo.setSelectedIndex(sel);
            }
        }
        if (thresholdCombo != null) {
            int sel = thresholdCombo.getSelectedIndex();
            thresholdCombo.removeAllItems();
            for (String k : THRESHOLD_KEYS) {
                thresholdCombo.addItem(Localization.t(k));
            }
            if (sel >= 0 && sel < THRESHOLD_KEYS.length) {
                thresholdCombo.setSelectedIndex(sel);
            }
        }
    }

    /** 试听下拉框的条目（语言切换后重刷，保持选中） */
    private void refreshSoundComboItems() {
        int sel = soundCombo.getSelectedIndex();
        soundCombo.removeAllItems();
        soundCombo.addItem(Localization.t("settings.audio.itemAlarm"));
        soundCombo.addItem(Localization.t("settings.audio.itemTrip"));
        soundCombo.setSelectedIndex(Math.max(0, Math.min(1, sel)));
    }

    // ---------------- 游戏 ----------------
    private JPanel buildGameTab() {
        gamePanel = new JPanel(new GridBagLayout());
        gamePanel.setBorder(BorderFactory.createTitledBorder(Localization.t("settings.game.group")));
        gamePanel.setBackground(UI.BACKGROUND);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;

        zoomBox = new JCheckBox(Localization.t("settings.game.zoom"), UiZoom.isEnabled());
        zoomBox.setBackground(UI.BACKGROUND);
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        gamePanel.add(zoomBox, gc);

        zoomHint = new JLabel(Localization.t("settings.game.zoomHint"));
        gc.gridy = 1;
        gamePanel.add(zoomHint, gc);

        resetZoomButton = new JButton(Localization.t("settings.game.zoomReset"));
        resetZoomButton.addActionListener((ActionEvent e) -> UiZoom.reset());
        gc.gridy = 2; gc.gridwidth = 1;
        gamePanel.add(resetZoomButton, gc);

        zoomLevel = new JLabel();
        gc.gridx = 1;
        gamePanel.add(zoomLevel, gc);
        updateZoomLevelText();

        zoomBox.addActionListener((ActionEvent e) -> UiZoom.setEnabled(zoomBox.isSelected()));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(UI.BACKGROUND);
        wrap.add(gamePanel, BorderLayout.NORTH);
        return wrap;
    }

    private void updateZoomLevelText() {
        if (zoomLevel != null) {
            zoomLevel.setText(String.format("%.0f%%", UiZoom.getScale() * 100));
        }
    }

    // ---------------- 音频：三通道音量 + 选择试听 ----------------
    private JPanel buildAudioTab() {
        audioPanel = new JPanel(new GridBagLayout());
        audioPanel.setBorder(BorderFactory.createTitledBorder(Localization.t("settings.audio.group")));
        audioPanel.setBackground(UI.BACKGROUND);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;

        // 三条音量滑块（加大尺寸：300×46）
        String[] labels = {Localization.t("settings.audio.master"),
                           Localization.t("settings.audio.alarm"),
                           Localization.t("settings.audio.ambient")};
        masterSlider = makeVolumeSlider(SoundProvider.getDefaultVolume());
        alarmSlider = makeVolumeSlider(SoundProvider.getDefaultAlarmVolume());
        ambientSlider = makeVolumeSlider(SoundProvider.getDefaultAmbientVolume());
        JSlider[] sliders = {masterSlider, alarmSlider, ambientSlider};
        for (int i = 0; i < 3; i++) {
            JLabel l = new JLabel(labels[i]);
            l.setFont(Fonts.text("Dialog", java.awt.Font.PLAIN, 13));
            gc.gridx = 0; gc.gridy = i; gc.gridwidth = 1;
            gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
            audioPanel.add(l, gc);
            gc.gridx = 1;
            gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1.0;   // 滑块撑满可用宽度
            audioPanel.add(sliders[i], gc);
        }
        masterValue = new JLabel(masterSlider.getValue() + "%");
        alarmValue = new JLabel(alarmSlider.getValue() + "%");
        ambientValue = new JLabel(ambientSlider.getValue() + "%");
        JLabel[] values = {masterValue, alarmValue, ambientValue};
        for (int i = 0; i < 3; i++) {
            values[i].setPreferredSize(new Dimension(48, 20));
            gc.gridx = 2; gc.gridy = i;
            gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
            audioPanel.add(values[i], gc);
        }

        masterSlider.addChangeListener((ChangeEvent e) -> {
            int v = masterSlider.getValue();
            masterValue.setText(v + "%");
            SoundProvider.setDefaultVolume(v / 100f);
            if (Loader.soundProvider != null) {
                Loader.soundProvider.setVolume(v / 100f);
            }
        });
        alarmSlider.addChangeListener((ChangeEvent e) -> {
            int v = alarmSlider.getValue();
            alarmValue.setText(v + "%");
            SoundProvider.setDefaultAlarmVolume(v / 100f);
            if (Loader.soundProvider != null) {
                Loader.soundProvider.setAlarmVolume(v / 100f);
            }
        });
        ambientSlider.addChangeListener((ChangeEvent e) -> {
            int v = ambientSlider.getValue();
            ambientValue.setText(v + "%");
            SoundProvider.setDefaultAmbientVolume(v / 100f);
            if (Loader.soundProvider != null) {
                Loader.soundProvider.setAmbientVolume(v / 100f);
            }
        });

        // 静音 + 声音选择 + 试听
        muteBox = new JCheckBox(Localization.t("settings.audio.mute"), SoundProvider.isDefaultMuted());
        muteBox.setBackground(UI.BACKGROUND);
        gc.gridx = 0; gc.gridy = 3; gc.gridwidth = 1;
        audioPanel.add(muteBox, gc);

        gc.gridx = 1;
        audioPanel.add(new JLabel(Localization.t("settings.audio.sound")), gc);

        soundCombo = new JComboBox<>();
        refreshSoundComboItems();
        soundCombo.setPreferredSize(new Dimension(150, 28));
        gc.gridx = 2;
        audioPanel.add(soundCombo, gc);

        testButton = new JButton(Localization.t("settings.audio.test"));
        testButton.addActionListener((ActionEvent e) -> {
            Clip clip = null;
            if (Loader.soundProvider != null) {
                clip = soundCombo.getSelectedIndex() == 1
                        ? Loader.soundProvider.ALARM_2
                        : Loader.soundProvider.ALARM_1;
            }
            playTestAlarm(clip);   // 初始化失败时 soundProvider 为 null，这里安静地不播放
        });
        gc.gridx = 0; gc.gridy = 4; gc.gridwidth = 3;
        audioPanel.add(testButton, gc);

        muteBox.addActionListener((ActionEvent e) -> {
            boolean m = muteBox.isSelected();
            SoundProvider.setDefaultMuted(m);
            if (Loader.soundProvider != null) {
                Loader.soundProvider.setMuted(m);
            }
        });

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(UI.BACKGROUND);
        wrap.add(audioPanel, BorderLayout.NORTH);
        return wrap;
    }

    /** 统一规格的音量滑块（加大：300×46） */
    private JSlider makeVolumeSlider(float init) {
        JSlider s = new JSlider(SwingConstants.HORIZONTAL, 0, 100, Math.round(init * 100));
        s.setBackground(UI.BACKGROUND);
        s.setPreferredSize(new Dimension(300, 46));
        s.setMajorTickSpacing(25);
        s.setPaintTicks(true);
        return s;
    }

    // ---------------- 视频：UI 风格 + 窗口模式 ----------------
    private JPanel buildVideoTab() {
        videoPanel = new JPanel(new GridBagLayout());
        videoPanel.setBorder(BorderFactory.createTitledBorder(Localization.t("settings.video.group")));
        videoPanel.setBackground(UI.BACKGROUND);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 6, 5, 6);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0; gc.gridy = 0;
        videoPanel.add(new JLabel(Localization.t("settings.video.style")), gc);

        ButtonGroup styleGroup = new ButtonGroup();
        styleClassic = new JRadioButton(Localization.t("settings.video.styleClassic"));
        styleFlatDark = new JRadioButton(Localization.t("settings.video.styleFlatDark"));
        styleFlatLight = new JRadioButton(Localization.t("settings.video.styleFlatLight"));
        for (JRadioButton rb : new JRadioButton[]{styleClassic, styleFlatDark, styleFlatLight}) {
            rb.setBackground(UI.BACKGROUND);
            styleGroup.add(rb);
        }
        int style = UiStyle.get();
        styleClassic.setSelected(style == UiStyle.CLASSIC);
        styleFlatDark.setSelected(style == UiStyle.FLAT_DARK);
        styleFlatLight.setSelected(style == UiStyle.FLAT_LIGHT);
        gc.gridx = 1; gc.gridwidth = 2;
        videoPanel.add(styleClassic, gc);
        gc.gridy = 1;
        videoPanel.add(styleFlatDark, gc);
        gc.gridy = 2;
        videoPanel.add(styleFlatLight, gc);
        styleClassic.addActionListener((ActionEvent e) -> UiStyle.set(UiStyle.CLASSIC));
        styleFlatDark.addActionListener((ActionEvent e) -> UiStyle.set(UiStyle.FLAT_DARK));
        styleFlatLight.addActionListener((ActionEvent e) -> UiStyle.set(UiStyle.FLAT_LIGHT));

        gc.gridx = 0; gc.gridy = 3; gc.gridwidth = 1;
        videoPanel.add(new JLabel(Localization.t("settings.video.mode")), gc);

        ButtonGroup modeGroup = new ButtonGroup();
        windowedRadio = new JRadioButton(Localization.t("settings.video.window"));
        maximizedRadio = new JRadioButton(Localization.t("settings.video.maximized"));
        for (JRadioButton rb : new JRadioButton[]{windowedRadio, maximizedRadio}) {
            rb.setBackground(UI.BACKGROUND);
            modeGroup.add(rb);
        }
        boolean maximized = getWindowMode() == MODE_MAXIMIZED;
        maximizedRadio.setSelected(maximized);
        windowedRadio.setSelected(!maximized);
        gc.gridx = 1; gc.gridwidth = 2;
        videoPanel.add(windowedRadio, gc);
        gc.gridy = 4;
        videoPanel.add(maximizedRadio, gc);
        windowedRadio.addActionListener((ActionEvent e) -> setWindowMode(MODE_WINDOWED));
        maximizedRadio.addActionListener((ActionEvent e) -> setWindowMode(MODE_MAXIMIZED));

        videoHint = new JLabel(Localization.t("settings.video.hint"));
        gc.gridx = 0; gc.gridy = 5; gc.gridwidth = 3;
        videoPanel.add(videoHint, gc);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(UI.BACKGROUND);
        wrap.add(videoPanel, BorderLayout.NORTH);
        return wrap;
    }

    /** 当前窗口模式（0=窗口化，1=最大化） */
    public static int getWindowMode() {
        return PREFS.getInt(KEY_WINDOW_MODE, MODE_WINDOWED);
    }

    /** 设置窗口模式：持久化 + 立即应用到所有已打开窗口 */
    public static void setWindowMode(int mode) {
        int m = (mode == MODE_MAXIMIZED) ? MODE_MAXIMIZED : MODE_WINDOWED;
        PREFS.putInt(KEY_WINDOW_MODE, m);
        try {
            PREFS.flush();
        } catch (Exception ignored) {
        }
        for (Window w : Window.getWindows()) {
            if (w instanceof Frame && w.isVisible()) {
                applyWindowMode((Frame) w);
            }
        }
    }

    /** 把窗口模式套用到单个窗口（新开窗口由 UI.createOrContinue 调用） */
    public static void applyWindowMode(Frame frame) {
        if (frame == null) {
            return;
        }
        if (getWindowMode() == MODE_MAXIMIZED) {
            frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        } else if ((frame.getExtendedState() & Frame.MAXIMIZED_BOTH) != 0) {
            frame.setExtendedState(Frame.NORMAL);
        }
    }

    // ---------------- 单位 ----------------
    /** 每个物理量一行，各自一个下拉框；改哪个只影响哪个 */
    private final java.util.Map<Units.Quantity, JComboBox<String>> unitCombos = new java.util.LinkedHashMap<>();

    private JPanel buildUnitsTab() {
        unitsPanel = new JPanel();
        unitsPanel.setBorder(BorderFactory.createTitledBorder(Localization.t("settings.units.group")));
        unitsPanel.setBackground(UI.BACKGROUND);
        unitsPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gc = new java.awt.GridBagConstraints();
        gc.insets = new java.awt.Insets(6, 8, 6, 8);
        gc.anchor = java.awt.GridBagConstraints.WEST;

        int row = 0;
        for (Units.Quantity q : Units.Quantity.values()) {
            JLabel name = new JLabel(Localization.t("settings.units.q." + q.name().toLowerCase()));
            name.setBackground(UI.BACKGROUND);
            gc.gridx = 0; gc.gridy = row; gc.gridwidth = 1; gc.weightx = 0;
            unitsPanel.add(name, gc);

            JComboBox<String> combo = new JComboBox<>(Units.unitsOf(q));
            combo.setSelectedIndex(Units.indexOf(q));
            combo.setPrototypeDisplayValue("lb/h");
            final Units.Quantity quantity = q;
            combo.addActionListener((ActionEvent e) -> {
                Units.setUnit(quantity, combo.getSelectedIndex());   // 只改这一项
            });
            unitCombos.put(q, combo);
            gc.gridx = 1; gc.gridy = row; gc.weightx = 1;
            unitsPanel.add(combo, gc);
            row++;
        }

        unitsHint = new JLabel(Localization.t("settings.units.hint"));
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2; gc.weightx = 1;
        unitsPanel.add(unitsHint, gc);

        // 恢复公制：一键把五个物理量都切回 SI（原来混着 bar/°F/t/h/GW 时最难找回来）
        row++;
        JButton resetMetric = new JButton(Localization.t("settings.units.resetMetric"));
        resetMetric.addActionListener((ActionEvent e) -> {
            Units.resetToMetric();
            for (java.util.Map.Entry<Units.Quantity, JComboBox<String>> en : unitCombos.entrySet()) {
                en.getValue().setSelectedIndex(Units.indexOf(en.getKey()));
            }
        });
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2; gc.weightx = 1;
        unitsPanel.add(resetMetric, gc);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(UI.BACKGROUND);
        wrap.add(unitsPanel, BorderLayout.NORTH);
        return wrap;
    }

    // ---------------- 语言 ----------------
    private JPanel buildLanguageTab() {
        languagePanel = new JPanel();
        languagePanel.setBorder(BorderFactory.createTitledBorder(Localization.t("settings.language.group")));
        languagePanel.setBackground(UI.BACKGROUND);
        languagePanel.setLayout(new BoxLayout(languagePanel, BoxLayout.Y_AXIS));

        ButtonGroup group = new ButtonGroup();
        for (Language lang : Language.values()) {
            JRadioButton rb = new JRadioButton(lang.nativeName);
            rb.setBackground(UI.BACKGROUND);
            rb.setSelected(lang == Localization.getLanguage());
            rb.addActionListener((ActionEvent e) -> onLanguageSelected(lang));
            group.add(rb);
            languagePanel.add(rb);
        }
        languagePanel.add(Box.createVerticalStrut(6));
        languageHint = new JLabel(Localization.t("settings.language.hint"));
        languagePanel.add(languageHint);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(UI.BACKGROUND);
        wrap.add(languagePanel, BorderLayout.NORTH);
        return wrap;
    }

    private void onLanguageSelected(Language lang) {
        Localization.setLanguage(lang);
        Fonts.init();                                   // 重新选定字体（含已存在窗口）
        /* 仪表（steelseries）在绘制时才从 TextFonts 取字体，改完字族必须重画一遍，
         * 否则已打开的窗口会继续用旧字族 → 中日韩文字显示成方框。 */
        for (java.awt.Window w : java.awt.Window.getWindows()) {
            if (w.isDisplayable()) {
                w.repaint();
            }
        }
        if (getOwner() instanceof Loader) {
            ((Loader) getOwner()).applyLocalization();  // 主菜单立即切换
        }
        javax.swing.SwingUtilities.updateComponentTreeUI(this);   // 本对话框按新语言重刷
        applyTexts();
    }

    // ---------------- 模组 ----------------
    private JPanel buildModsTab() {
        modsPanel = new JPanel(new GridBagLayout());
        modsPanel.setBorder(BorderFactory.createTitledBorder(Localization.t("settings.mods.group")));
        modsPanel.setBackground(UI.BACKGROUND);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 5, 5, 5);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        modsModel = new javax.swing.DefaultListModel<>();
        for (String path : loadModPaths()) {
            modsModel.addElement(path);
        }
        modsList = new JList<>(modsModel);
        modsList.setBackground(UI.BACKGROUND);
        JScrollPane scroll = new JScrollPane(modsList);
        scroll.setPreferredSize(new Dimension(400, 110));
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        modsPanel.add(scroll, gc);

        modsAddButton = new JButton(Localization.t("settings.mods.add"));
        modsAddButton.addActionListener((ActionEvent e) -> addModJars());
        gc.gridx = 0; gc.gridy = 1; gc.gridwidth = 1;
        modsPanel.add(modsAddButton, gc);

        modsRemoveButton = new JButton(Localization.t("settings.mods.remove"));
        modsRemoveButton.addActionListener((ActionEvent e) -> removeSelectedMods());
        gc.gridx = 1;
        modsPanel.add(modsRemoveButton, gc);

        modsHint = new JLabel(Localization.t("settings.mods.hint"));
        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 2;
        modsPanel.add(modsHint, gc);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(UI.BACKGROUND);
        wrap.add(modsPanel, BorderLayout.NORTH);
        return wrap;
    }

    /** 已添加的 mod jar 路径列表（持久化） */
    public static List<String> loadModPaths() {
        List<String> out = new ArrayList<>();
        String raw = PREFS.get(KEY_MODS, "");
        for (String p : raw.split("\n")) {
            if (!p.trim().isEmpty()) {
                out.add(p);
            }
        }
        return out;
    }

    private static void saveModPaths(List<String> paths) {
        PREFS.put(KEY_MODS, String.join("\n", paths));
        try {
            PREFS.flush();
        } catch (Exception ignored) {
        }
    }

    private void addModJars() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(Localization.t("settings.mods.add"));
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileNameExtensionFilter("JAR", "jar"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        List<String> paths = loadModPaths();
        for (File f : chooser.getSelectedFiles()) {
            String path = f.getAbsolutePath();
            if (!paths.contains(path)) {
                paths.add(path);
                modsModel.addElement(path);
            }
        }
        saveModPaths(paths);
    }

    private void removeSelectedMods() {
        List<String> selected = modsList.getSelectedValuesList();
        if (selected.isEmpty()) {
            return;
        }
        List<String> paths = loadModPaths();
        paths.removeAll(selected);
        saveModPaths(paths);
        for (String s : selected) {
            modsModel.removeElement(s);
        }
    }

    // ---------------- 原版设置：页面刷新率 / Log 操作 / MTK ----------------
    private JPanel buildClassicTab() {
        classicPanel = new JPanel(new GridBagLayout());
        classicPanel.setBorder(BorderFactory.createTitledBorder(Localization.t("settings.classic.group")));
        classicPanel.setBackground(UI.BACKGROUND);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 6, 5, 6);
        gc.anchor = GridBagConstraints.WEST;

        // ---- 页面刷新率 ----
        gc.gridx = 0; gc.gridy = 0;
        classicPanel.add(new JLabel(Localization.t("UI Update rate")), gc);
        refreshCombo = new JComboBox<>();
        for (String k : REFRESH_KEYS) {
            refreshCombo.addItem(Localization.t(k));
        }
        long saved = getUiUpdateRate();
        int sel = 0;
        for (int i = 0; i < REFRESH_VALUES.length; i++) {
            if (REFRESH_VALUES[i] == saved) {
                sel = i;
                break;
            }
        }
        refreshCombo.setSelectedIndex(sel);
        refreshCombo.setPreferredSize(new Dimension(130, 28));
        gc.gridx = 1;
        classicPanel.add(refreshCombo, gc);
        refreshCombo.addActionListener((ActionEvent e) -> {
            int i = refreshCombo.getSelectedIndex();
            if (i >= 0 && i < REFRESH_VALUES.length) {
                PREFS.putInt(KEY_UI_UPDATE_RATE, (int) REFRESH_VALUES[i]);
                UI.setUiUpdateRate(REFRESH_VALUES[i]);
            }
        });

        // ---- Log 操作 ----
        gc.gridx = 0; gc.gridy = 1; gc.gridwidth = 2;
        classicPanel.add(new JLabel(Localization.t("Log")), gc);
        openLogButton = new JButton(Localization.t("settings.classic.openLog"));
        openLogButton.addActionListener((ActionEvent e) -> {
            // 设置对话框是模态的：先关掉它再开日志窗口，否则日志窗口会被模态框挡住、点了像没反应
            if (NPPSim.ui != null) {
                applyAll();
                dispose();
                UI.createOrContinue(LogWindow.class, false, false);
            }
        });
        // 两个按钮并排（各占一格）；之前 gridwidth 没复位，两个按钮叠在同一格里
        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 1;
        classicPanel.add(openLogButton, gc);
        clearLogButton = new JButton(Localization.t("settings.classic.clearLog"));
        clearLogButton.addActionListener((ActionEvent e) -> {
            if (NPPSim.autoControl != null) {
                NPPSim.autoControl.eventLog.clear();
            }
        });
        gc.gridx = 1; gc.gridwidth = 1;
        classicPanel.add(clearLogButton, gc);

        // ---- MTK：符号 + 阈值 ----
        gc.gridx = 0; gc.gridy = 3; gc.gridwidth = 2;
        classicPanel.add(new JLabel(Localization.t("MTK")), gc);

        gc.gridx = 0; gc.gridy = 4; gc.gridwidth = 1;
        classicPanel.add(new JLabel(Localization.t("Sign")), gc);
        ButtonGroup signGroup = new ButtonGroup();
        signPlus = new JRadioButton("+");
        signMinus = new JRadioButton("-");
        for (JRadioButton rb : new JRadioButton[]{signPlus, signMinus}) {
            rb.setBackground(UI.BACKGROUND);
            signGroup.add(rb);
        }
        signPlus.setSelected(true);
        gc.gridx = 1;
        classicPanel.add(signPlus, gc);
        gc.gridx = 2;
        classicPanel.add(signMinus, gc);
        signPlus.addActionListener((ActionEvent e) -> {
            if (NPPSim.ui != null && NPPSim.ui.getMTK1() != null) {
                NPPSim.ui.getMTK1().setSignPositive(true);
            }
        });
        signMinus.addActionListener((ActionEvent e) -> {
            if (NPPSim.ui != null && NPPSim.ui.getMTK1() != null) {
                NPPSim.ui.getMTK1().setSignPositive(false);
            }
        });

        gc.gridx = 0; gc.gridy = 5;
        classicPanel.add(new JLabel(Localization.t("Threshold")), gc);
        thresholdCombo = new JComboBox<>();
        for (String k : THRESHOLD_KEYS) {
            thresholdCombo.addItem(Localization.t(k));
        }
        thresholdCombo.setPreferredSize(new Dimension(130, 28));
        gc.gridx = 1;
        classicPanel.add(thresholdCombo, gc);
        thresholdCombo.addActionListener((ActionEvent e) -> {
            int i = thresholdCombo.getSelectedIndex();
            if (i >= 0 && i < THRESHOLD_VALUES.length && NPPSim.ui != null && NPPSim.ui.getMTK1() != null) {
                NPPSim.ui.getMTK1().setThresHold(THRESHOLD_VALUES[i]);
            }
        });

        classicHint = new JLabel(Localization.t("settings.classic.hint"));
        gc.gridx = 0; gc.gridy = 6; gc.gridwidth = 3;
        classicPanel.add(classicHint, gc);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(UI.BACKGROUND);
        wrap.add(classicPanel, BorderLayout.NORTH);
        return wrap;
    }

    /** 上次选择的页面刷新率（毫秒）；UI 构造时读取 */
    public static long getUiUpdateRate() {
        return PREFS.getInt(KEY_UI_UPDATE_RATE, 50);
    }

    // ---------------- 控制棒数量限制（选择限值开关） ----------------
    private static final String KEY_ROD_LIMIT = "game.rodSelectionLimitEnabled";

    /** 是否启用"控制棒数量限制"（选中超过限值的棒时禁止整组拔出）；默认开启 */
    public static boolean getRodSelectionLimitEnabled() {
        return PREFS.getBoolean(KEY_ROD_LIMIT, true);
    }

    public static void setRodSelectionLimitEnabled(boolean v) {
        PREFS.putBoolean(KEY_ROD_LIMIT, v);
        try {
            PREFS.flush();
        } catch (Exception ignored) {
        }
    }

    // ---------------- 试听 ----------------
    /** 试听：临时解除静音播放指定警报音（1.3 秒） */
    private void playTestAlarm(Clip clip) {
        SoundProvider sp = Loader.soundProvider;
        if (sp == null || clip == null) {
            return;
        }
        boolean wasMuted = sp.isMuted();
        if (wasMuted) {
            sp.setMuted(false);
        }
        sp.request(clip, TEST_OWNER);
        javax.swing.Timer timer = new javax.swing.Timer(1300, (ActionEvent e) -> {
            sp.release(clip, TEST_OWNER);
            if (wasMuted) {
                sp.setMuted(true);
            }
        });
        timer.setRepeats(false);
        timer.start();
    }
}
