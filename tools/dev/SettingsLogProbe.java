package com.darwish.nppsim;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.WindowEvent;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

/**
 * 复核：① 设置里各物理量的单位是否真能独立切换、是否持久化；② 事件日志窗口能不能正常关闭/再打开。
 *
 * <p>用法：java ... com.darwish.nppsim.SettingsLogProbe</p>
 */
public class SettingsLogProbe {

    public static void main(String[] a) throws Exception {
        Localization.init();
        UiStyle.applyLaf();
        Fonts.init();
        Loader.tables = new SteamTables();
        Loader.soundProvider = new SoundProvider();
        new NPPSim();
        Thread.sleep(600);

        System.out.println("=========== ① 设置 → 单位 ===========");
        SettingsDialog d1 = new SettingsDialog(null);
        JTabbedPane tabs = findTabs(d1.getContentPane());
        for (int i = 0; i < tabs.getTabCount(); i++) {
            System.out.println("  页 " + i + ": " + tabs.getTitleAt(i));
        }
        int unitsIdx = 3;
        Container unitsPage = (Container) tabs.getComponentAt(unitsIdx);
        java.util.List<JComboBox> combos = new java.util.ArrayList<>();
        collectCombos(unitsPage, combos);
        System.out.println("  单位页下拉框数 = " + combos.size());
        for (JComboBox cb : combos) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cb.getItemCount(); i++) {
                sb.append(cb.getItemAt(i)).append(i == cb.getItemCount() - 1 ? "" : "/");
            }
            System.out.println("    [" + sb + "] 选中=" + cb.getSelectedItem());
        }
        System.out.println("  当前各量：压力=" + Units.unitOf(Units.Quantity.PRESSURE)
                + " 温度=" + Units.unitOf(Units.Quantity.TEMPERATURE)
                + " 流量=" + Units.unitOf(Units.Quantity.FLOW)
                + " 功率=" + Units.unitOf(Units.Quantity.POWER)
                + " 长度=" + Units.unitOf(Units.Quantity.LENGTH));

        // 把"压力"改成 psi，其它不动
        if (combos.size() >= 1) {
            combos.get(0).setSelectedItem("psi");
        }
        System.out.println("  改压力 → psi 之后：压力=" + Units.unitOf(Units.Quantity.PRESSURE)
                + " 温度=" + Units.unitOf(Units.Quantity.TEMPERATURE)
                + " 流量=" + Units.unitOf(Units.Quantity.FLOW));
        // 再把"温度"改成 K
        if (combos.size() >= 2) {
            combos.get(1).setSelectedItem("K");
        }
        System.out.println("  再改温度 → K：压力=" + Units.unitOf(Units.Quantity.PRESSURE)
                + " 温度=" + Units.unitOf(Units.Quantity.TEMPERATURE));
        System.out.println("  换算抽查：1 MPa = " + String.format("%.1f", Units.pressure(1.0))
                + " " + Units.pressureUnit() + "；100 °C = " + String.format("%.1f", Units.temperature(100))
                + " " + Units.temperatureUnit() + "；100 kg/s = " + String.format("%.1f", Units.flow(100))
                + " " + Units.flowUnit());
        d1.dispose();

        // 重新打开设置，看是否记住
        SettingsDialog d2 = new SettingsDialog(null);
        java.util.List<JComboBox> combos2 = new java.util.ArrayList<>();
        collectCombos((Container) findTabs(d2.getContentPane()).getComponentAt(unitsIdx), combos2);
        System.out.print("  重新打开后的选中：");
        for (JComboBox cb : combos2) {
            System.out.print(cb.getSelectedItem() + " ");
        }
        System.out.println();
        d2.dispose();

        // 还原成公制，别污染用户配置
        Units.setUnit(Units.Quantity.PRESSURE, 0);
        Units.setUnit(Units.Quantity.TEMPERATURE, 0);
        Units.setUnit(Units.Quantity.FLOW, 0);
        Units.setUnit(Units.Quantity.POWER, 0);
        Units.setUnit(Units.Quantity.LENGTH, 0);

        System.out.println("=========== ② 事件日志窗口 ===========");
        LogWindow log = new LogWindow();
        log.setVisible(true);
        Thread.sleep(400);
        System.out.println("  打开后：displayable=" + log.isDisplayable() + " visible=" + log.isVisible()
                + " focusableWindow=" + log.isFocusableWindow());
        // 模拟点窗口右上角 ×
        SwingUtilities.invokeAndWait(() ->
                log.dispatchEvent(new WindowEvent(log, WindowEvent.WINDOW_CLOSING)));
        Thread.sleep(400);
        System.out.println("  点 × 之后：displayable=" + log.isDisplayable() + " visible=" + log.isVisible());
        // 再次打开（模拟设置里点“打开事件日志” → createOrContinue 复用同一实例）
        try {
            SwingUtilities.invokeAndWait(() -> log.setVisibility(true));
            Thread.sleep(400);
            System.out.println("  再次打开：displayable=" + log.isDisplayable() + " visible=" + log.isVisible());
        } catch (Exception e) {
            System.out.println("  再次打开失败：" + e);
        }
        // 刷新循环对已关闭窗口调 update()
        try {
            log.update();
            System.out.println("  对已关闭/再打开的窗口调用 update()：正常");
        } catch (Exception e) {
            System.out.println("  update() 抛异常：" + e);
        }
        System.out.println("PROBE_DONE");
        System.exit(0);
    }

    static void collectCombos(Container c, java.util.List<JComboBox> out) {
        for (Component x : c.getComponents()) {
            if (x instanceof JComboBox) {
                out.add((JComboBox) x);
            }
            if (x instanceof Container) {
                collectCombos((Container) x, out);
            }
        }
    }

    static JTabbedPane findTabs(Container c) {
        for (Component x : c.getComponents()) {
            if (x instanceof JTabbedPane) {
                return (JTabbedPane) x;
            }
            if (x instanceof Container) {
                JTabbedPane t = findTabs((Container) x);
                if (t != null) {
                    return t;
                }
            }
        }
        return null;
    }

    static JFrame frame(Container c) {
        return null;
    }
}
