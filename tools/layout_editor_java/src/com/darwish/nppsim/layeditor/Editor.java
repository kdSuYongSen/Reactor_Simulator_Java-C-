package com.darwish.nppsim.layeditor;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * RBMK-1500 外部布局编辑器（独立 jar）。
 *
 * <p>工作方式：模拟器运行时会把每个窗口的方块和**递归**子组件导出成
 * {@code ui_layout_pages.json}；本工具读它，改完写 {@code <pageId>.layout.json}
 * （主窗口写 {@code ui_layout.json}），模拟器每秒轮询、约 1 秒内界面就跟上。</p>
 *
 * <p>与旧 python 工具的区别：树能展开到任意层级，所以面板里再套一层的按键也调得到；
 * 画布显示的是"选中节点所在父容器及其所有兄弟"，拖谁改谁，兄弟不会跟着跑。</p>
 */
public final class Editor extends JFrame {

    private final Manifest manifest;
    private final File dir;

    private final JComboBox<Manifest.Page> pageCombo = new JComboBox<>();
    private final JTree tree = new JTree(new DefaultTreeModel(new DefaultMutableTreeNode("root")));
    private final Canvas canvas = new Canvas();
    private final JSpinner spX = spin(), spY = spin(), spW = spin(), spH = spin();
    private final JCheckBox cbVisible = new JCheckBox(L.t("lbl.visible"));
    private final JLabel status = new JLabel(" ");
    private final JLabel pathLabel = new JLabel(" ");
    private final JLabel hint = new JLabel(L.t("hint.canvas"));

    /** 左侧树 / 右侧画布的分隔条（显示后要显式给位置，否则只按树的极小首选宽度分） */
    private JSplitPane split;
    private Manifest.Page page;
    private Manifest.Node selected;
    private String lastSaved = "";
    /**
     * 正在把数值写进输入框（syncSpinners）。
     * 写值会触发 Spinner 的 ChangeListener，如果不去掉，切换选中组件时
     * 新旧数值会互相串台——表现为“点一下组件它就消失 / 随机变大变小”。
     */
    private boolean syncing = false;
    /**
     * Manifest.Node → 树里的 DefaultMutableTreeNode。
     * 之前 pathOfNode() 直接把 Manifest.Node 塞进 TreePath，JTree 解析路径时
     * 抛 ClassCastException（"点一下组件就出问题"的根因之一）。
     */
    private final java.util.IdentityHashMap<Manifest.Node, DefaultMutableTreeNode> treeNodes =
            new java.util.IdentityHashMap<>();

    public Editor(Manifest manifest) {
        super(L.t("app.title"));
        this.manifest = manifest;
        this.dir = manifest == null ? new File(".") : manifest.dir;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        buildUi();
        loadPages();
        // 显示后给分隔条一个明确位置（否则树只占自己那点首选宽度，名字全被截断）
        SwingUtilities.invokeLater(() -> split.setDividerLocation(0.28));
    }

    private static JSpinner spin() {
        JSpinner s = new JSpinner(new SpinnerNumberModel(0, -8000, 8000, 1));
        s.setPreferredSize(new Dimension(72, 24));
        return s;
    }

    // ------------------------------------------------------------------
    private void buildUi() {
        // ---- 顶部：选窗口 + 重载 ----
        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.setBorder(BorderFactory.createEmptyBorder(8, 10, 6, 10));
        JPanel left = new JPanel(new BorderLayout(6, 0));
        left.add(new JLabel(L.t("lbl.window")), BorderLayout.WEST);
        pageCombo.addActionListener(e -> selectPage((Manifest.Page) pageCombo.getSelectedItem()));
        left.add(pageCombo, BorderLayout.CENTER);
        top.add(left, BorderLayout.CENTER);

        JButton reload = new JButton(L.t("btn.reload"));
        reload.addActionListener(e -> reloadManifest());
        JButton help = new JButton(L.t("btn.help"));
        help.setToolTipText(L.t("btn.help.tip"));
        help.addActionListener(e -> showHelp());
        JComboBox<L.Lang> langCombo = new JComboBox<>(L.Lang.values());
        langCombo.setSelectedItem(L.lang());
        langCombo.setToolTipText(L.t("lbl.language"));
        langCombo.addActionListener(e -> {
            L.Lang l = (L.Lang) langCombo.getSelectedItem();
            if (l != null && l != L.lang()) {
                L.setLang(l);
                rebuildWithSameManifest();
            }
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.add(new JLabel(L.t("lbl.language")));
        right.add(langCombo);
        right.add(help);
        right.add(reload);
        top.add(right, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // ---- 左：组件树 ----
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setRowHeight(20);
        tree.addTreeSelectionListener(this::onTreeSelected);
        JScrollPane treeScroll = new JScrollPane(tree);
        treeScroll.setBorder(BorderFactory.createTitledBorder(L.t("tree.title")));
        treeScroll.setPreferredSize(new Dimension(360, 500));

        // ---- 右：画布 ----
        JPanel canvasWrap = new JPanel(new BorderLayout());
        canvasWrap.setBorder(BorderFactory.createTitledBorder(L.t("canvas.title")));
        canvasWrap.add(canvas, BorderLayout.CENTER);
        JLabel canvasHint = new JLabel(hintText());
        canvasHint.setBorder(BorderFactory.createEmptyBorder(2, 6, 0, 6));
        canvasWrap.add(canvasHint, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout());
        center.add(canvasWrap, BorderLayout.CENTER);
        center.add(buildPropBar(), BorderLayout.SOUTH);

        split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, center);
        split.setResizeWeight(0.3);
        split.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        add(split, BorderLayout.CENTER);

        // 画布上的方向键微调（±1 / Shift ±10）
        canvas.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(java.awt.event.KeyEvent e) {
                int dx = 0;
                int dy = 0;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:  dx = -1; break;
                    case KeyEvent.VK_RIGHT: dx = 1; break;
                    case KeyEvent.VK_UP:    dy = -1; break;
                    case KeyEvent.VK_DOWN:  dy = 1; break;
                    default: return;
                }
                nudge(dx, dy, e.isShiftDown() ? 10 : 1);
                e.consume();
            }
        });

        // ---- 底部：状态 ----
        JPanel bottom = new JPanel(new BorderLayout(8, 0));
        bottom.setBorder(BorderFactory.createEmptyBorder(6, 10, 8, 10));
        bottom.add(status, BorderLayout.WEST);
        JLabel man = new JLabel(manifest == null ? L.t("no.manifest")
                : manifest.manifestFile.getAbsolutePath() + "   " + manifest.generatedAt);
        man.setFont(man.getFont().deriveFont(11f));
        bottom.add(man, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        // 快捷键
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "save");
        getRootPane().getActionMap().put("save", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                doSave();
            }
        });
        // Ctrl+R：重新读取模拟器导出的清单
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK), "reload");
        getRootPane().getActionMap().put("reload", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                reloadManifest();
            }
        });
        // F1：打开“提示”对话框
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "help");
        getRootPane().getActionMap().put("help", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                showHelp();
            }
        });
    }

    private String hintText() {
        return L.t("hint.canvas");
    }

    // ==================================================================
    // 提示：详细的设置 / 操作说明
    // ==================================================================
    private void showHelpButtonAction() {
        showHelp();
    }

    /** 打开“操作说明”对话框（右上角“提示”按钮 / F1 / 启动参数 --help 都走这里） */
    void showHelp() {
        JDialog dlg = new JDialog(this, L.t("help.title"), true);
        dlg.setLayout(new BorderLayout(0, 0));
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab(L.t("help.tab1"), helpPane(helpQuick()));
        tabs.addTab(L.t("help.tab2"), helpPane(helpCanvas()));
        tabs.addTab(L.t("help.tab3"), helpPane(helpSave()));
        tabs.addTab(L.t("help.tab4"), helpPane(helpKeys()));
        tabs.addTab(L.t("help.tab5"), helpPane(helpFaq()));
        dlg.add(tabs, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton ok = new JButton(L.t("btn.ok"));
        ok.addActionListener(e -> dlg.dispose());
        south.add(ok);
        dlg.add(south, BorderLayout.SOUTH);

        dlg.setSize(780, 580);
        dlg.setMinimumSize(new Dimension(560, 400));
        dlg.setLocationRelativeTo(this);
        dlg.getRootPane().setDefaultButton(ok);
        dlg.getRootPane().registerKeyboardAction(e -> dlg.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        dlg.setVisible(true);
    }

    /** 一页说明：只读、自动换行的文本框 */
    private static JScrollPane helpPane(String text) {
        JTextArea ta = new JTextArea(text);
        ta.setEditable(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 13));
        ta.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        ta.setCaretPosition(0);
        JScrollPane sp = new JScrollPane(ta);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(18);
        return sp;
    }

    private String helpQuick() {
        return String.format(L.t("help.b1"), dir.getAbsolutePath());
    }

    private String helpCanvas() {
        return L.t("help.b2");
    }

    private String helpSave() {
        return String.format(L.t("help.b3"), dir.getAbsolutePath());
    }

    private String helpKeys() {
        return L.t("help.b4");
    }

    private String helpFaq() {
        return String.format(L.t("help.b5"),
                new File(dir, "ui_layout_pages.json").getAbsolutePath());
    }

    private JPanel buildPropBar() {
        JPanel bar = new JPanel(new GridBagLayout());
        bar.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(2, 4, 2, 4);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridy = 0; gc.gridx = 0; gc.gridwidth = 6;
        bar.add(pathLabel, gc);

        gc.gridy = 1; gc.gridwidth = 1;
        gc.gridx = 0; bar.add(new JLabel("X"), gc);
        gc.gridx = 1; bar.add(spX, gc);
        gc.gridx = 2; bar.add(new JLabel("Y"), gc);
        gc.gridx = 3; bar.add(spY, gc);
        gc.gridx = 4; bar.add(new JLabel("W"), gc);
        gc.gridx = 5; bar.add(spW, gc);
        gc.gridx = 6; bar.add(new JLabel("H"), gc);
        gc.gridx = 7; bar.add(spH, gc);
        gc.gridx = 8; bar.add(cbVisible, gc);

        spX.addChangeListener(e -> applySpinners());
        spY.addChangeListener(e -> applySpinners());
        spW.addChangeListener(e -> applySpinners());
        spH.addChangeListener(e -> applySpinners());
        cbVisible.addActionListener(e -> applySpinners());

        gc.gridx = 9;
        bar.add(Box.createHorizontalStrut(12), gc);
        gc.gridx = 10;
        JButton save = new JButton(L.t("btn.save"));
        save.addActionListener(e -> doSave());
        bar.add(save, gc);
        gc.gridx = 11;
        JButton resetNode = new JButton(L.t("btn.resetNode"));
        resetNode.addActionListener(e -> doResetNode());
        bar.add(resetNode, gc);
        gc.gridx = 12;
        JButton resetPage = new JButton(L.t("btn.resetPage"));
        resetPage.addActionListener(e -> doResetPage());
        bar.add(resetPage, gc);
        gc.gridx = 13;
        JButton del = new JButton(L.t("btn.delete"));
        del.addActionListener(e -> doDeleteFile());
        bar.add(del, gc);
        return bar;
    }

    // ------------------------------------------------------------------
    private void loadPages() {
        pageCombo.removeAllItems();
        if (manifest == null || manifest.pages.isEmpty()) {
            status.setText(L.t("status.noManifest"));
            return;
        }
        for (Manifest.Page p : manifest.pages) {
            pageCombo.addItem(p);
        }
        pageCombo.setSelectedIndex(0);
    }

    /** 换语言：界面文字整体重建（用同一份清单，不重新读盘） */
    private void rebuildWithSameManifest() {
        Editor fresh = new Editor(manifest);
        fresh.setLocation(getLocation());
        fresh.setSize(getSize());
        fresh.setVisible(true);
        dispose();
    }

    private void reloadManifest() {
        try {
            Manifest m = Manifest.load(dir);
            if (m == null || m.pages.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        String.format(L.t("dlg.manifestFail"),
                                new File(dir, "ui_layout_pages.json").getAbsolutePath()),
                        L.t("dlg.noManifest"), JOptionPane.WARNING_MESSAGE);
                return;
            }
            Editor fresh = new Editor(m);
            fresh.setLocation(getLocation());
            fresh.setVisible(true);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, L.t("dlg.readFail") + ex, L.t("dlg.error"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void selectPage(Manifest.Page p) {
        if (p == null) {
            return;
        }
        page = p;
        selected = null;
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        treeNodes.clear();
        for (Manifest.Node b : p.blocks) {
            root.add(treeOf(b));
        }
        tree.setModel(new DefaultTreeModel(root));
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
        canvas.setContext(p.w, p.h, p.blocks, null);
        syncSpinners(null);
        updateStatus();
    }

    private DefaultMutableTreeNode treeOf(Manifest.Node n) {
        DefaultMutableTreeNode t = new DefaultMutableTreeNode(new TreeItem(n));
        treeNodes.put(n, t);
        for (Manifest.Node c : n.children) {
            t.add(treeOf(c));
        }
        return t;
    }

    /** 树节点包装：toString 决定显示文字（标签为空时用 "#索引 类名"） */
    static final class TreeItem {
        final Manifest.Node node;

        TreeItem(Manifest.Node node) {
            this.node = node;
        }

        @Override public String toString() {
            return node.treeLabel();
        }
    }

    private void onTreeSelected(TreeSelectionEvent e) {
        Object o = tree.getLastSelectedPathComponent();
        if (!(o instanceof DefaultMutableTreeNode)) {
            return;
        }
        Object u = ((DefaultMutableTreeNode) o).getUserObject();
        if (!(u instanceof TreeItem)) {
            return;
        }
        select(((TreeItem) u).node);
    }

    /** 选中一个节点：画布换成"它的父容器 + 兄弟"，并把数值填到表单 */
    void select(Manifest.Node n) {
        selected = n;
        if (n == null || page == null) {
            canvas.setContext(page == null ? 100 : page.w, page == null ? 100 : page.h,
                    page == null ? java.util.Collections.emptyList() : page.blocks, null);
            syncSpinners(null);
            return;
        }
        if (n.path.isEmpty()) {
            canvas.setContext(page.w, page.h, page.blocks, n);
        } else {
            Manifest.Node parent = parentOf(n);
            if (parent == null) {
                parent = page.block(n.blockId);
            }
            canvas.setContext(parent.w, parent.h, parent.children, n);
        }
        syncSpinners(n);

        // 树里的选中项跟画布保持一致（点画布时同步树）
        TreePath tp = pathOfNode(n);
        if (tp != null && !tp.equals(tree.getSelectionPath())) {
            tree.setSelectionPath(tp);
            tree.scrollPathToVisible(tp);
        }
    }

    private Manifest.Node parentOf(Manifest.Node target) {
        Manifest.Node block = page.block(target.blockId);
        return block == null ? null : parentOf(block, target);
    }

    private Manifest.Node parentOf(Manifest.Node cur, Manifest.Node target) {
        for (Manifest.Node c : cur.children) {
            if (c == target) {
                return cur;
            }
            Manifest.Node r = parentOf(c, target);
            if (r != null) {
                return r;
            }
        }
        return null;
    }

    private TreePath pathOfNode(Manifest.Node target) {
        if (page == null) {
            return null;
        }
        Object root = tree.getModel().getRoot();
        for (Manifest.Node b : page.blocks) {
            if (b == target) {
                DefaultMutableTreeNode t = treeNodes.get(b);
                return t == null ? null : new TreePath(new Object[]{root, t});
            }
        }
        // 逐层构造路径（每一段都要换成树里的 DefaultMutableTreeNode）
        java.util.List<Manifest.Node> chain = new java.util.ArrayList<>();
        if (chainOf(page.block(target.blockId), target, chain)) {
            java.util.List<Object> objs = new java.util.ArrayList<>();
            objs.add(root);
            for (Manifest.Node n : chain) {
                DefaultMutableTreeNode t = treeNodes.get(n);
                if (t == null) {
                    return null;
                }
                objs.add(t);
            }
            return new TreePath(objs.toArray());
        }
        return null;
    }

    private boolean chainOf(Manifest.Node cur, Manifest.Node target, List<Manifest.Node> out) {
        if (cur == target) {
            out.add(cur);
            return true;
        }
        for (Manifest.Node c : cur.children) {
            if (chainOf(c, target, out)) {
                out.add(0, cur);
                return true;
            }
        }
        return false;
    }

    private void syncSpinners(Manifest.Node n) {
        boolean on = n != null;
        // 关键：写值期间禁止回写，否则切换选中项会把上一个组件的宽高带过来
        syncing = true;
        try {
            spX.setEnabled(on);
            spY.setEnabled(on);
            spW.setEnabled(on);
            spH.setEnabled(on);
            cbVisible.setEnabled(on);
            if (n == null) {
                pathLabel.setText(L.t("not.selected"));
                return;
            }
            spX.setValue(n.x);
            spY.setValue(n.y);
            spW.setValue(n.w);
            spH.setValue(n.h);
            cbVisible.setSelected(n.visible);
        } finally {
            syncing = false;
        }
        String where = n.path.isEmpty() ? (L.t("path.block") + n.blockId)
                : (L.t("path.block") + n.blockId + " / " + n.path);
        pathLabel.setText(String.format("%s    %s%s", n.display(), where,
                n.dirty ? L.t("path.dirty2") : ""));
    }

    private void applySpinners() {
        if (syncing || selected == null) {
            return;                     // 程序写值引起的回调，忽略
        }
        int x = (Integer) spX.getValue();
        int y = (Integer) spY.getValue();
        int w = Math.max(1, (Integer) spW.getValue());
        int h = Math.max(1, (Integer) spH.getValue());
        boolean vis = cbVisible.isSelected();
        Manifest.Node n = selected;
        if (n.x == x && n.y == y && n.w == w && n.h == h && n.visible == vis) {
            return;
        }
        n.x = x;
        n.y = y;
        n.w = w;
        n.h = h;
        n.visible = vis;
        n.dirty = true;
        canvas.repaint();
        updateStatus();
        pathLabel.setText(String.format("%s    %s%s", n.display(),
                n.path.isEmpty() ? (L.t("path.block") + n.blockId)
                        : (L.t("path.block") + n.blockId + " / " + n.path),
                L.t("path.dirty2")));
    }

    /** 键盘微调选中组件的位置（step 像素） */
    private void nudge(int dx, int dy, int step) {
        if (selected == null) {
            return;
        }
        selected.x = Math.max(0, selected.x + dx * step);
        selected.y = Math.max(0, selected.y + dy * step);
        selected.dirty = true;
        syncSpinners(selected);
        canvas.repaint();
        updateStatus();
    }

    // ------------------------------------------------------------------
    void doSave() {
        if (page == null) {
            return;
        }
        try {
            File out = LayoutWriter.save(page, dir);
            lastSaved = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
            status.setText(String.format(L.t("status.saved"), out.getName(), lastSaved));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, L.t("dlg.saveFail") + ex, L.t("dlg.error"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doResetNode() {
        if (selected == null) {
            return;
        }
        selected.reset();
        syncSpinners(selected);
        canvas.repaint();
        updateStatus();
    }

    private void doResetPage() {
        if (page == null) {
            return;
        }
        for (Manifest.Node b : page.blocks) {
            b.reset();
        }
        syncSpinners(selected);
        canvas.repaint();
        updateStatus();
    }

    private void doDeleteFile() {
        if (page == null) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                String.format(L.t("dlg.deleteAsk"), LayoutWriter.targetFile(page, dir).getName()),
                L.t("dlg.deleteTitle"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        boolean ok = LayoutWriter.remove(page, dir);
        status.setText(ok ? L.t("status.deleted") : L.t("status.deleteFail"));
    }

    private void updateStatus() {
        if (page == null) {
            return;
        }
        int n = LayoutWriter.dirtyPaths(page).size();
        File f = LayoutWriter.existing(page, dir);
        status.setText(String.format(L.t("status.summary"),
                page.title.isEmpty() ? page.id : page.title, page.blocks.size(), n,
                f == null ? L.t("status.noFile")
                        : String.format(L.t("status.hasFile"), f.getName())));
    }

    // ==================================================================
    /** 画布：显示当前父容器 + 所有兄弟，可拖动/缩放选中的那个 */
    final class Canvas extends JComponent {

        private int parentW = 100, parentH = 100;
        private List<Manifest.Node> siblings = java.util.Collections.emptyList();
        private Manifest.Node sel;

        private double scale = 1;
        private int ox, oy;

        private boolean dragging, resizing;
        private int dragDX, dragDY;
        private int startX, startY, startW, startH;

        Canvas() {
            setBackground(new Color(0x22, 0x26, 0x2E));
            setOpaque(true);
            setFocusable(true);
            MouseAdapter h = new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { onPress(e); }
                @Override public void mouseDragged(MouseEvent e) { onDrag(e); }
                @Override public void mouseReleased(MouseEvent e) { onRelease(); }
                @Override public void mouseMoved(MouseEvent e) { updateCursor(e); }
            };
            addMouseListener(h);
            addMouseMotionListener(h);

            // 方向键微调
            bind(KeyEvent.VK_LEFT, -1, 0);
            bind(KeyEvent.VK_RIGHT, 1, 0);
            bind(KeyEvent.VK_UP, 0, -1);
            bind(KeyEvent.VK_DOWN, 0, 1);
        }

        private void bind(int key, int dx, int dy) {
            getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(key, 0), "n" + key);
            getInputMap(WHEN_FOCUSED).put(
                    KeyStroke.getKeyStroke(key, InputEvent.SHIFT_DOWN_MASK), "s" + key);
            getActionMap().put("n" + key, new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) { nudge(dx, dy, 1); }
            });
            getActionMap().put("s" + key, new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) { nudge(dx, dy, 10); }
            });
        }

        private void nudge(int dx, int dy, int step) {
            if (sel == null) {
                return;
            }
            sel.x += dx * step;
            sel.y += dy * step;
            sel.dirty = true;
            syncSpinners(sel);
            repaint();
            updateStatus();
        }

        void setContext(int w, int h, List<Manifest.Node> siblings, Manifest.Node sel) {
            this.parentW = Math.max(1, w);
            this.parentH = Math.max(1, h);
            this.siblings = siblings == null ? java.util.Collections.emptyList() : siblings;
            this.sel = sel;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0;
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int pad = 14;
            scale = Math.min((getWidth() - pad * 2.0) / parentW, (getHeight() - pad * 2.0) / parentH);
            if (!(scale > 0)) {
                scale = 1;
            }
            ox = (int) Math.round((getWidth() - parentW * scale) / 2);
            oy = (int) Math.round((getHeight() - parentH * scale) / 2);

            // 父容器底
            g.setColor(new Color(0x2C, 0x31, 0x3B));
            g.fillRect(ox, oy, (int) Math.round(parentW * scale), (int) Math.round(parentH * scale));
            g.setColor(new Color(0x55, 0x5C, 0x6A));
            g.drawRect(ox, oy, (int) Math.round(parentW * scale), (int) Math.round(parentH * scale));

            g.setFont(new Font("Dialog", Font.PLAIN, 11));
            for (Manifest.Node n : siblings) {
                Rectangle r = toScreen(n);
                boolean isSel = n == sel;
                g.setColor(n.visible
                        ? (isSel ? new Color(0x3A, 0x5A, 0x8C) : new Color(0x39, 0x40, 0x4C))
                        : new Color(0x4A, 0x2A, 0x2A));
                g.fillRect(r.x, r.y, r.width, r.height);
                g.setColor(isSel ? new Color(0xFF, 0xD8, 0x4A) : new Color(0x6E, 0x76, 0x86));
                g.setStroke(new BasicStroke(isSel ? 2f : 1f));
                g.drawRect(r.x, r.y, r.width, r.height);
                g.setStroke(new BasicStroke(1f));

                String label = n.label.isEmpty() ? n.cls : n.label;
                if (label.length() > 16) {
                    label = label.substring(0, 16) + "…";
                }
                g.setColor(isSel ? Color.WHITE : new Color(0xC8, 0xCE, 0xD8));
                if (r.width > 30 && r.height > 12) {
                    g.drawString(label, r.x + 4, r.y + Math.min(r.height - 3, 13));
                }
                if (isSel) {
                    // 右下角缩放把手
                    g.setColor(new Color(0xFF, 0xD8, 0x4A));
                    g.fillRect(r.x + r.width - 6, r.y + r.height - 6, 6, 6);
                    // 坐标提示
                    g.setColor(new Color(0xFF, 0xE9, 0x8A));
                    g.drawString(n.x + "," + n.y + "  " + n.w + "×" + n.h,
                            r.x + 4, r.y + Math.min(r.height - 3, 26));
                }
            }
            if (siblings.isEmpty()) {
                g.setColor(new Color(0x88, 0x90, 0x9C));
                g.drawString(L.t("canvas.noChild"), ox + 8, oy + 18);
            }
        }

        private Rectangle toScreen(Manifest.Node n) {
            return new Rectangle(
                    ox + (int) Math.round(n.x * scale),
                    oy + (int) Math.round(n.y * scale),
                    Math.max(2, (int) Math.round(n.w * scale)),
                    Math.max(2, (int) Math.round(n.h * scale)));
        }

        private Manifest.Node hit(int mx, int my) {
            for (int i = siblings.size() - 1; i >= 0; i--) {
                if (toScreen(siblings.get(i)).contains(mx, my)) {
                    return siblings.get(i);
                }
            }
            return null;
        }

        private boolean nearHandle(Manifest.Node n, int mx, int my) {
            Rectangle r = toScreen(n);
            return mx >= r.x + r.width - 8 && mx <= r.x + r.width + 4
                    && my >= r.y + r.height - 8 && my <= r.y + r.height + 4;
        }

        private void onPress(MouseEvent e) {
            requestFocusInWindow();
            Manifest.Node n = hit(e.getX(), e.getY());
            if (n == null) {
                return;
            }
            if (n != sel) {
                select(n);
            }
            dragging = true;
            resizing = nearHandle(n, e.getX(), e.getY());
            Point p = toModel(e.getX(), e.getY());
            dragDX = p.x - n.x;
            dragDY = p.y - n.y;
            startX = n.x;
            startY = n.y;
            startW = n.w;
            startH = n.h;
        }

        private void onDrag(MouseEvent e) {
            if (!dragging || sel == null) {
                return;
            }
            Point p = toModel(e.getX(), e.getY());
            if (resizing) {
                sel.w = Math.max(4, p.x - sel.x);
                sel.h = Math.max(4, p.y - sel.y);
            } else {
                sel.x = p.x - dragDX;
                sel.y = p.y - dragDY;
            }
            sel.dirty = true;
            syncSpinners(sel);
            repaint();
        }

        private void onRelease() {
            if (dragging && sel != null) {
                updateStatus();
            }
            dragging = false;
            resizing = false;
        }

        private void updateCursor(MouseEvent e) {
            Manifest.Node n = hit(e.getX(), e.getY());
            if (n != null && nearHandle(n, e.getX(), e.getY())) {
                setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
            } else if (n != null) {
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            } else {
                setCursor(Cursor.getDefaultCursor());
            }
        }

        private Point toModel(int mx, int my) {
            return new Point((int) Math.round((mx - ox) / scale), (int) Math.round((my - oy) / scale));
        }
    }
}
