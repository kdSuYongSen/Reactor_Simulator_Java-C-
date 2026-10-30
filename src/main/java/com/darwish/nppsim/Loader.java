package com.darwish.nppsim;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Image;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import javax.imageio.ImageIO;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class Loader extends javax.swing.JFrame {
    /**
     * 本项目的 GitHub 主页。
     * 主菜单的 GitHub 按钮、按钮悬停说明、以及文档里的项目地址都指向这里。
     */
    static final String REPO_URL = "https://github.com/kdSuYongSen/Reactor_Simulator_JavaCpp";
    /** GitHub 按钮的说明文字（悬停提示里跟在地址后面的仓库标识，不随语言变化） */
    static final String REPO_PAGE = "kdSuYongSen/Reactor_Simulator_JavaCpp";

    static Color BACKGROUND = new Color(180, 140, 40);
    protected static Loader loader;
    protected static NPPSim simulation;
    protected static SteamTables tables;
    protected static SoundProvider soundProvider;
    private boolean loading = false;
    /** 初始化（蒸汽表/音频）失败：Start/Load 保持禁用，主菜单显示失败原因。 */
    private boolean failed = false;
    /** 失败原因（切换语言后重新渲染用） */
    private String failureMessage = null;
    /**
     * 主菜单图片的【强引用】。
     * JXImagePanel 内部用 SoftReference 保存图片，而主反应堆窗口 (NPPSim/UI) 创建时会
     * 申请大块内存触发 GC，软引用会被回收 → 表现为"打开主反应堆窗口后主菜单图片消失"。
     * 这里自己长期持有，并在 LookAndFeel 变化后重新贴图。
     */
    private static java.awt.image.BufferedImage splashImage;
    
    /**
     * Creates new form Loader
     */
    public Loader() {
        try {
            // 恢复原有的 NetBeans DarkMetal 金属风格外观（由反编译源码移植，见 library-java/org/netbeans/swing/laf/dark）
            UiStyle.applyLaf();
        } catch (Exception e) {
            e.printStackTrace();
        }
        initComponents();
        applyLocalization();
        installSplashImage();
        try {
            String uriString = getClass().getProtectionDomain().getCodeSource().getLocation().toURI().toString();
            // 原代码按固定 jar 名 "RBMK-1500-Simulator.jar" 定位目录；合并后 jar 名变化，改为按最后一个路径分隔符定位 jar 所在目录
            String baseDir = uriString.substring(0, uriString.lastIndexOf('/') + 1);
            PrintStream logStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File(new URI(baseDir + "/log.txt")))), true);
            System.setErr(logStream);
            System.setOut(logStream);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                PrintStream logStream = new PrintStream(new BufferedOutputStream(new FileOutputStream("log.txt")), true);
                System.setErr(logStream);
                System.setOut(logStream);
            } catch (Exception ex) {
                e.printStackTrace();
                new ErrorWindow("Warning: Failed to redirect stdout", ExceptionUtils.getStackTrace(ex), false).setVisible(true);
            }
        }
    }

    /**
     * 加载并长期持有主菜单图片（修复"打开主反应堆窗口后图片消失"）：
     * 1) 把 getScaledInstance 的懒缩放图物化成 BufferedImage，避免像素在 GC 后不可用；
     * 2) 用静态字段强引用，避免 JXImagePanel 内部 SoftReference 被回收；
     * 3) 监听 LookAndFeel 变化（UI 构造时会再次 setLookAndFeel），变化后重新贴图。
     */
    private void installSplashImage() {
        try {
            Image src = ImageIO.read(getClass().getResource("/res/ignalina.jpg"));
            if (src == null) {
                return;
            }
            Image scaled = src.getScaledInstance(507, 318, Image.SCALE_SMOOTH);
            java.awt.image.BufferedImage buf =
                    new java.awt.image.BufferedImage(507, 318, java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g2 = buf.createGraphics();
            g2.drawImage(scaled, 0, 0, null);
            g2.dispose();
            splashImage = buf;
            jXImagePanel1.setImage(splashImage);
            jXImagePanel1.setPreferredSize(jXImagePanel1.getPreferredSize());
            javax.swing.UIManager.addPropertyChangeListener(evt -> {
                if ("lookAndFeel".equals(evt.getPropertyName())) {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        if (splashImage != null) {
                            jXImagePanel1.setImage(splashImage);
                            jXImagePanel1.repaint();
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 按当前语言刷新主菜单上的文案（切换语言后立即调用） */
    void applyLocalization() {
        setTitle(Localization.t("RBMK-1500 Simulator"));
        jLabel1.setText(Localization.t("app.welcome"));
        jLabel2.setText(Localization.t("app.version"));
        jButton1.setText(Localization.t("loader.start"));
        jButton2.setText(Localization.t("loader.loadSavedIC"));
        jButton5.setText(Localization.t("loader.loadStandardIC"));
        jLabel3.setText(failed ? failureText() : Localization.t("loader.loading"));
        if (jButton6 != null) {
            jButton6.setText(Localization.t("loader.settings"));
        }
        // GitHub 按钮：抬头显示本仓库地址与说明（说明用仓库标识，不随语言变化）
        if (jButton3 != null) {
            jButton3.setText(Localization.t("GitHub"));
            jButton3.setToolTipText(REPO_URL + "  —  " + REPO_PAGE);
        }
    }

    public static void main(String args[]) {
        Localization.init();          // 先加载已保存的界面语言
        Fonts.init();                 // 按语言选定能显示该语言文字的字体（韩文尤其需要）
        UiZoom.init();                // Ctrl+滚轮 缩放（开关在 设置→游戏）
        loader = new Loader();
        new Thread(() -> {
            try {
                tables = new SteamTables();
                soundProvider = new SoundProvider();
                // 应用设置对话框里保存的音量/静音
                soundProvider.setVolume(SoundProvider.getDefaultVolume());
                soundProvider.setAlarmVolume(SoundProvider.getDefaultAlarmVolume());
                soundProvider.setAmbientVolume(SoundProvider.getDefaultAmbientVolume());
                soundProvider.setMuted(SoundProvider.isDefaultMuted());
                // setLoading 会改 Swing 组件，必须回到 EDT（后台线程只做加载）
                javax.swing.SwingUtilities.invokeLater(() -> loader.setLoading(false));
            } catch (Throwable t) {
                // 任何初始化失败（蒸汽表缺失/损坏、音频设备不可用等）都不能让线程静默死掉：
                // 否则 Start 永远灰着、界面停在 Loading...，用户无任何提示。
                t.printStackTrace();
                String detail = ExceptionUtils.getStackTrace(t);
                String message = t.getMessage();
                if (message == null || message.isEmpty()) {
                    message = t.getClass().getSimpleName();
                }
                final String shortMessage = message;
                javax.swing.SwingUtilities.invokeLater(() -> {
                    loader.setFailed(shortMessage);
                    new ErrorWindow(Localization.t("loader.initFailed"), detail, true).setVisible(true);
                });
            }
        }).start();
        loader.pack();
        // 【主菜单窗口】禁止最大化（仍可自由缩放）：拦截最大化状态并立即还原
        loader.setResizable(true);
        loader.setMinimumSize(loader.getPreferredSize());
        loader.addWindowStateListener(evt -> {
            if ((evt.getNewState() & java.awt.Frame.MAXIMIZED_BOTH) != 0) {
                loader.setExtendedState(java.awt.Frame.NORMAL);
            }
        });
        loader.setLocationRelativeTo(null);
        loader.setVisible(true);
        // 【预加载】加载阶段（"Loading..." 显示期间）在后台分批预建堆芯图 2500 个格子组件，
        // 点击 Start 后主窗口构建时直接取用，避免开局长达约 1 秒的无响应。
        com.darwish.nppsim.MTK.requestPreload();
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
        if (!failed) {
            jLabel3.setText(Localization.t("loader.loading"));
            jLabel3.setForeground(java.awt.Color.BLACK);
            jLabel3.setVisible(loading);
        }
        jButton1.setEnabled(!loading && !failed);
        jButton2.setEnabled(!loading && !failed);
        //jButton5.setEnabled(!loading); //TODO
    }

    /**
     * 后台初始化失败：进入失败态。
     * Start / Load Saved IC / Load Standard IC 全部禁用，主菜单标签显示原因；
     * 错误详情由调用方用 ErrorWindow 展示。失败态只能重启程序恢复。
     */
    void setFailed(String message) {
        failed = true;
        loading = false;
        failureMessage = message;
        jLabel3.setText(failureText());
        jLabel3.setForeground(java.awt.Color.RED);
        jLabel3.setVisible(true);
        jButton1.setEnabled(false);
        jButton2.setEnabled(false);
        jButton5.setEnabled(false);
        pack();
    }

    /** 失败态文案（截断过长的异常消息，避免把窗口撑爆；详情在 ErrorWindow 里）。 */
    private String failureText() {
        String suffix = (failureMessage == null || failureMessage.isEmpty())
                ? "" : ": " + failureMessage;
        if (suffix.length() > 120) {
            suffix = suffix.substring(0, 120) + "...";
        }
        return Localization.t("loader.initFailed") + suffix;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jXImagePanel1 = new org.jdesktop.swingx.JXImagePanel();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(157, 109, 34));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        jPanel3.setBackground(BACKGROUND);
        jPanel3.setMinimumSize(new java.awt.Dimension(700, 500));
        jPanel3.setPreferredSize(new java.awt.Dimension(700, 500));

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setText(Localization.t("Welcome to RBMK-1500 Simulator!"));

        jButton1.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jButton1.setText(Localization.t("Start"));
        jButton1.setEnabled(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jButton2.setText(Localization.t("Load Saved IC"));
        jButton2.setEnabled(false);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jXImagePanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jXImagePanel1.setPreferredSize(new java.awt.Dimension(507, 318));

        javax.swing.GroupLayout jXImagePanel1Layout = new javax.swing.GroupLayout(jXImagePanel1);
        jXImagePanel1.setLayout(jXImagePanel1Layout);
        jXImagePanel1Layout.setHorizontalGroup(
            jXImagePanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 503, Short.MAX_VALUE)
        );
        jXImagePanel1Layout.setVerticalGroup(
            jXImagePanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 314, Short.MAX_VALUE)
        );

        jButton3.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jButton3.setText(Localization.t("GitHub"));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jButton4.setText(Localization.t("Discord"));
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Dialog", 2, 12)); // NOI18N
        jLabel2.setText(Localization.t("Version 1.00Alpha"));

        jButton5.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jButton5.setText(Localization.t("Load Standard IC"));
        jButton5.setEnabled(false);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        // 【新增】设置按钮：与 Start / Load Saved IC 等并列
        jButton6 = new javax.swing.JButton();
        jButton6.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jButton6.setText(Localization.t("Settings"));
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Dialog", 2, 14)); // NOI18N
        jLabel3.setText(Localization.t("Loading..."));

        jLabel4.setFont(new java.awt.Font("Dialog", 2, 14)); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jLabel1)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2))
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addGap(21, 21, 21)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jXImagePanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jXImagePanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(57, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (loading) {
            return;
        }
        loading = true;
        // 先显示 "Loading..." 并禁用按钮；用 invokeLater 让这些界面变化先绘制出来，
        // 再开始构建仿真（堆芯图组件已预加载，剩余构建时间仍需等待，但加载提示可见）。
        setLoading(true);
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                simulation = new NPPSim();
                Loader.loader.setVisible(false);
            } catch (Throwable t) {
                loading = false;
                setLoading(false);
                t.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(this,
                        Localization.t("app.error.createFailed") + "\n" + t
                                + Localization.t("app.error.details"),
                        Localization.t("app.error.startTitle"), javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });
    }//GEN-LAST:event_jButton1ActionPerformed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        simulation = null;
    }//GEN-LAST:event_formWindowActivated

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if (loading) {
            return;
        }
        loading = true;
        new LoadDialog().setVisible(true);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                // 本项目主页（原 Ali Darwish 上游仓库只在 LICENSE/NOTICE 里保留署名）
                Desktop.getDesktop().browse(new URI(REPO_URL));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
            Desktop.getDesktop().browse(new URI("https://discord.gg/DDKX5qVW8W"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        new SettingsDialog(Loader.this).setVisible(true);
    }//GEN-LAST:event_jButton6ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel3;
    private org.jdesktop.swingx.JXImagePanel jXImagePanel1;
    // End of variables declaration//GEN-END:variables
}
