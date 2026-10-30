package com.darwish.nppsim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * 蒸汽环路 / 全厂能量平衡监控窗口（2026-10-17 新增）。
 *
 * 环路（与 NPPSim 的实际接线一致）：
 *   给水 → 换热器（管侧蒸发）→ 汽水分离器 → 蒸汽母管 → 除氧器蒸汽入口
 *        → 除氧器 → 除氧器蒸汽出口联箱 → 汽轮机主汽门 / 旁路膨胀阀
 *        → 凝汽器 → 凝结水泵 → 凝结水联箱 → PCS 凝结水过滤（热阱）
 *        → 除氧器水入口 → 除氧器水出口 → 给水
 *
 * 三栏读数：
 *   蒸汽发生：换热器抽汽/传热/产汽、汽水分离器压力/产汽/疏水/水位、蒸汽母管压力
 *   除氧器：4 台各自进汽/透传/水温/水位 + 出口联箱压力/进汽/出汽
 *   汽轮机与热阱：主汽门开度/流量、两台机功率/转速/真空、PCS 过滤通过量、
 *                以及全厂能量平衡（堆芯热功率 → 总电功率 → 净效率）
 */
public class SteamLoopUI extends javax.swing.JFrame implements UIUpdateable {

    /* 配色全部取自 {@link UiTheme}（= 原版 jar 的色板）：
     *   面板背板 = LAF 默认 rgb(51,51,55)，文字 rgb(222,222,222)，边框 rgb(222,222,222)。
     * 以前这里写死 52,52,52 / 235,235,235 —— 数值几乎一样，但切换"设置→视频"的
     * FlatLaf 浅色后就成了浅底浅字，什么都看不见。改成动态取色后跟随 LAF。 */
    private static Color plateColor() {
        return UiTheme.plate();
    }

    private static Color textColor() {
        return UiTheme.plateText();
    }

    private static Color dimColor() {
        return UiTheme.dimText();
    }

    private static Color accentColor() {
        return UiTheme.accent();
    }

    /** 平衡/效率这类"好值"用绿色（浅底时压暗，保证对比度） */
    private static Color goodColor() {
        return UiTheme.isDark() ? new Color(150, 235, 150) : new Color(20, 110, 40);
    }

    /** 一行读数：名称 + 数值 + 单位 */
    /** 读数种类：决定单位换算与单位标签（设置 → 单位 切换公制/英制） */
    private enum Kind {
        PRESSURE,      // MPa
        PRESSURE_KPA,  // kPa（真空）
        TEMPERATURE,   // °C
        FLOW,          // kg/s
        POWER,         // MW
        PERCENT,       // %
        RAW            // rpm 等不换算
    }

    private static final class Metric {
        final javax.swing.JLabel value = new javax.swing.JLabel("0.0");
        private final Kind kind;
        private final javax.swing.JLabel unitLabel;
        Metric(javax.swing.JPanel parent, GridBagConstraints gc, int row, String name, Kind kind) {
            this.kind = kind;
            javax.swing.JLabel n = new javax.swing.JLabel(Localization.t(name));
            n.setFont(new Font("Dialog", Font.BOLD, 11));
            n.setForeground(textColor());
            gc.gridx = 0; gc.gridy = row; gc.anchor = GridBagConstraints.WEST;
            parent.add(n, gc);

            value.setFont(new Font("Monospaced", Font.BOLD, 12));
            value.setForeground(accentColor());
            value.setHorizontalAlignment(javax.swing.JLabel.RIGHT);
            value.setPreferredSize(new Dimension(62, 16));
            gc.gridx = 1; gc.anchor = GridBagConstraints.EAST;
            parent.add(value, gc);

            unitLabel = new javax.swing.JLabel(unitLabelText());
            unitLabel.setFont(new Font("Dialog", Font.PLAIN, 10));
            unitLabel.setForeground(dimColor());
            gc.gridx = 2; gc.anchor = GridBagConstraints.WEST;
            parent.add(unitLabel, gc);
        }
        /** 单位标签随单位制切换（每次刷新都刷新，保证设置里改了立即生效） */
        private String unitLabelText() {
            switch (kind) {
                case PRESSURE:     return Units.pressureUnit();
                case PRESSURE_KPA: return Units.isUS() ? "inHg" : "kPa";
                case TEMPERATURE:  return Units.temperatureUnit();
                case FLOW:         return Units.flowUnit();
                case POWER:        return Units.powerUnit();
                case PERCENT:      return "%";
                default:           return "";
            }
        }
        /** 换算成当前单位制的数值 */
        private double convert(double v) {
            switch (kind) {
                case PRESSURE:     return Units.pressure(v);
                case PRESSURE_KPA: return Units.isUS() ? v * 0.2953 : v;
                case TEMPERATURE:  return Units.temperature(v);
                case FLOW:         return Units.flow(v);
                case POWER:        return Units.power(v);
                default:           return v;
            }
        }
        void set(double v) {
            unitLabel.setText(unitLabelText());
            value.setText(format(convert(v)));
        }
        /** 报警态：文字转红（泄压阀开启等"必须一眼看到"的事件） */
        void setAlarm(boolean alarm) {
            value.setForeground(alarm ? new Color(255, 70, 70) : accentColor());
        }

        void setTrend(double v, double warnHi, double warnLo) {
            set(v);
            value.setForeground(v > warnHi || v < warnLo ? Color.ORANGE : accentColor());
        }
    }

    private static String format(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            return "---";
        }
        double a = Math.abs(v);
        if (a >= 10000) {
            return String.format("%.0f", v);
        }
        if (a >= 100) {
            return String.format("%.1f", v);
        }
        return String.format("%.3f", v).replaceAll("0+$", "").replaceAll("\\.$", ".0");
    }

    private final Metric[] hxDraw = new Metric[2], hxHeat = new Metric[2], hxSteam = new Metric[2];
    private final Metric[] sepP = new Metric[2], sepSteam = new Metric[2],
            sepWater = new Metric[2], sepLevel = new Metric[2];
    private Metric mainHeaderP;
    /** 泄压阀状态（开启=红色） */
    private final Metric[] sepRelief = new Metric[2];
    /** 全厂水/汽总量（分开显示，用于观察物质守恒） */
    private Metric plantWater, plantSteam;

    private final Metric[] dIn = new Metric[4], dPass = new Metric[4],
            dTemp = new Metric[4], dLevel = new Metric[4];
    private Metric collP, collIn, collOut;

    private final Metric[] msPos = new Metric[4], msFlow = new Metric[4];
    private final Metric[] tgMW = new Metric[2], tgRpm = new Metric[2], tgVac = new Metric[2];
    private Metric pcsFlow, pcsBlow, coreMW, totalMW, eff;

    public SteamLoopUI() {
        setTitle(Localization.t("Steam Loop"));
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UI.BACKGROUND);
        setLayout(new java.awt.BorderLayout());
        WindowMenu.install(this);
        PageLayout.installAuto(this, "SteamLoopUI");   // 本页布局可由布局编辑器调整

        // 内容用 BorderLayout.NORTH 贴顶，避免 GridBagLayout 把三栏垂直居中留出大片空白
        javax.swing.JPanel content = new javax.swing.JPanel(new GridBagLayout());
        content.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 2, 4, 2);
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.gridx = 0; gc.gridy = 0;
        content.add(buildSteamGeneration(), gc);
        gc.gridx = 1;
        content.add(buildDeaerators(), gc);
        gc.gridx = 2;
        content.add(buildTurbineAndSink(), gc);

        // FlowLayout(LEFT) 包一层：内容自然尺寸、贴左上角，不被 BorderLayout 拉伸居中
        javax.swing.JPanel wrap = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        wrap.setOpaque(false);
        wrap.add(content);
        add(wrap, java.awt.BorderLayout.NORTH);

        // 尺寸直接给足：CJK 回退字体的实际字宽比 JLabel 的 preferredSize 大，
        // 靠 pack() 会算窄（实测被压掉约 15%），这里按实测需要的宽度固定。
        setSize(960, 500);
        setMinimumSize(new Dimension(760, 380));
        setLocationRelativeTo(null);
    }

    private javax.swing.JPanel panel(String title) {
        javax.swing.JPanel p = new javax.swing.JPanel(new GridBagLayout());
        p.setBackground(plateColor());
        p.setOpaque(true);
        javax.swing.border.TitledBorder tb = javax.swing.BorderFactory.createTitledBorder(Localization.t(title));
        tb.setTitleColor(textColor());   // 面板背板是深色，默认黑字看不见
        tb.setTitleFont(new Font("Dialog", Font.BOLD, 12));
        p.setBorder(javax.swing.BorderFactory.createCompoundBorder(tb,
                javax.swing.BorderFactory.createLineBorder(new Color(222, 222, 222), 1, true)));
        return p;
    }

    private javax.swing.JPanel buildSteamGeneration() {
        javax.swing.JPanel p = panel("Steam Generation");
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0, 4, 0, 3);
        int r = 0;
        for (int i = 0; i < 2; i++) {
            String tag = i == 0 ? "A" : "B";
            hxDraw[i] = new Metric(p, gc, r++, Localization.t("HX") + " " + tag + " " + Localization.t("Steam Draw"), Kind.FLOW);
            hxHeat[i] = new Metric(p, gc, r++, Localization.t("HX") + " " + tag + " " + Localization.t("Heat Transfer"), Kind.POWER);
            hxSteam[i] = new Metric(p, gc, r++, Localization.t("HX") + " " + tag + " " + Localization.t("Steam Out"), Kind.FLOW);
        }
        for (int i = 0; i < 2; i++) {
            String tag = i == 0 ? "A" : "B";
            sepP[i] = new Metric(p, gc, r++, Localization.t("Separator") + " " + tag + " " + Localization.t("Pressure"), Kind.PRESSURE);
            sepSteam[i] = new Metric(p, gc, r++, Localization.t("Separator") + " " + tag + " " + Localization.t("Steam Output"), Kind.FLOW);
            sepWater[i] = new Metric(p, gc, r++, Localization.t("Separator") + " " + tag + " " + Localization.t("Output Water"), Kind.FLOW);
            sepLevel[i] = new Metric(p, gc, r++, Localization.t("Separator") + " " + tag + " " + Localization.t("Water Level"), Kind.PERCENT);
            sepRelief[i] = new Metric(p, gc, r++, Localization.t("Separator") + " " + tag + " " + Localization.t("Relief Valve"), Kind.RAW);
        }
        mainHeaderP = new Metric(p, gc, r++, Localization.t("Main Steam Header") + " " + Localization.t("Pressure"), Kind.PRESSURE);
        return p;
    }

    private javax.swing.JPanel buildDeaerators() {
        javax.swing.JPanel p = panel("Dearator");
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0, 4, 0, 3);
        int r = 0;
        for (int i = 0; i < 4; i++) {
            String tag = "#" + (i + 1);
            dIn[i] = new Metric(p, gc, r++, Localization.t("Dearator") + " " + tag + " " + Localization.t("Steam In"), Kind.FLOW);
            dPass[i] = new Metric(p, gc, r++, Localization.t("Dearator") + " " + tag + " " + Localization.t("To Turbine"), Kind.FLOW);
            dTemp[i] = new Metric(p, gc, r++, Localization.t("Dearator") + " " + tag + " " + Localization.t("Water Temp"), Kind.TEMPERATURE);
            dLevel[i] = new Metric(p, gc, r++, Localization.t("Dearator") + " " + tag + " " + Localization.t("Water Level"), Kind.PERCENT);
        }
        collP = new Metric(p, gc, r++, Localization.t("Dearator Steam Header") + " " + Localization.t("Pressure"), Kind.PRESSURE);
        collIn = new Metric(p, gc, r++, Localization.t("Dearator Steam Header") + " " + Localization.t("Inflow"), Kind.FLOW);
        collOut = new Metric(p, gc, r++, Localization.t("Dearator Steam Header") + " " + Localization.t("Outflow"), Kind.FLOW);
        return p;
    }

    private javax.swing.JPanel buildTurbineAndSink() {
        javax.swing.JPanel p = panel("Turbine & Heat Sink");
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0, 4, 0, 3);
        int r = 0;
        for (int i = 0; i < 4; i++) {
            String tag = "#" + (i + 1);
            msPos[i] = new Metric(p, gc, r++, Localization.t("Main Steam Valve") + " " + tag + " " + Localization.t("Position"), Kind.PERCENT);
            msFlow[i] = new Metric(p, gc, r++, Localization.t("Main Steam Valve") + " " + tag + " " + Localization.t("Flow"), Kind.FLOW);
        }
        for (int i = 0; i < 2; i++) {
            String tag = "TG-" + (i + 1);
            tgMW[i] = new Metric(p, gc, r++, tag + " " + Localization.t("Generator Load"), Kind.POWER);
            tgRpm[i] = new Metric(p, gc, r++, tag + " " + Localization.t("Turbine Speed"), Kind.RAW);
            tgVac[i] = new Metric(p, gc, r++, tag + " " + Localization.t("Condenser Vacuum"), Kind.PRESSURE_KPA);
        }
        pcsFlow = new Metric(p, gc, r++, Localization.t("PCS Condensate Filter") + " " + Localization.t("Throughput"), Kind.FLOW);
        pcsBlow = new Metric(p, gc, r++, Localization.t("PCS Condensate Filter") + " " + Localization.t("Blowdown"), Kind.FLOW);
        plantWater = new Metric(p, gc, r++, Localization.t("Plant Water Mass (t)"), Kind.RAW);
        plantSteam = new Metric(p, gc, r++, Localization.t("Plant Steam Mass (kg)"), Kind.RAW);
        coreMW = new Metric(p, gc, r++, Localization.t("Core Thermal Power"), Kind.POWER);
        totalMW = new Metric(p, gc, r++, Localization.t("Total Generator Power"), Kind.POWER);
        eff = new Metric(p, gc, r++, Localization.t("Net Efficiency"), Kind.PERCENT);
        totalMW.value.setForeground(goodColor());
        eff.value.setForeground(goodColor());
        return p;
    }

    @Override
    public void update() {
        if (!isVisible()) {
            return;
        }
        java.awt.EventQueue.invokeLater(() -> {
            try {
                refresh();
            } catch (Exception ex) {
                // 单个窗口刷新异常不得拖垮全局 UI 更新
            }
        });
    }

    private void refresh() {
        java.util.List<HeatExchanger> hxs = NPPSim.heatExchangers;
        for (int i = 0; i < 2; i++) {
            if (hxs != null && hxs.size() > i) {
                HeatExchanger hx = hxs.get(i);
                hxDraw[i].set(hx.primarySteamInKgs);
                hxHeat[i].set(hx.primaryHeatMW);
                hxSteam[i].set(hx.steamOutKgs);
            }
        }
        java.util.List<Separator> seps = NPPSim.separators;
        for (int i = 0; i < 2; i++) {
            if (seps != null && seps.size() > i) {
                Separator s = seps.get(i);
                sepP[i].set(s.pressureMPa);
                sepSteam[i].set(s.outSteamKgs);
                sepWater[i].set(s.outWaterKgs);
                sepLevel[i].set(s.waterLevel);
                sepRelief[i].set(s.reliefValveOpen ? 1 : 0);
                sepRelief[i].setAlarm(s.alarmReliefOpen);
            }
        }
        if (NPPSim.steamPiping != null) {
            mainHeaderP.set(NPPSim.steamPiping.getPressure());
        }
        if (plantWater != null) {
            plantWater.set(MassLedger.waterKg() / 1000.0);
        }
        if (plantSteam != null) {
            plantSteam.set(MassLedger.steamKg());
        }

        java.util.List<Dearator> ds = NPPSim.dearators;
        for (int i = 0; i < 4; i++) {
            if (ds != null && ds.size() > i) {
                Dearator d = ds.get(i);
                dIn[i].set(d.steamInlet.getFlowRate());
                dPass[i].set(d.getPassKgs());
                dTemp[i].set(d.getWaterTemperature());
                dLevel[i].set(d.getWaterLevel());
            }
        }
        SteamCollectorHeader coll = NPPSim.deaeratorSteamHeader;
        if (coll != null) {
            collP.set(coll.getPressure());
            collIn.set(coll.getInflowKgs());
            collOut.set(coll.getOutflowKgs());
        }

        for (int i = 0; i < 4; i++) {
            java.util.ArrayList<SteamValve> group = (i < 2) ? NPPSim.TG1InletValves : NPPSim.TG2InletValves;
            if (group != null && group.size() > (i % 2)) {
                SteamValve v = group.get(i % 2);
                msPos[i].set(v.getPosition() * 100.0);
                msFlow[i].set(v.getFlowRate());
            }
        }
        TG[] tgs = {NPPSim.tg1, NPPSim.tg2};
        double total = 0;
        for (int i = 0; i < 2; i++) {
            if (tgs[i] != null) {
                tgMW[i].set(tgs[i].getGeneratorLoad());
                tgRpm[i].set(tgs[i].getRpm());
                tgVac[i].set(tgs[i].condenser.getPressure() * 1000.0);
                total += tgs[i].getGeneratorLoad();
            }
        }
        CondensateFilter f = NPPSim.pcsCondensateFilter;
        if (f != null) {
            pcsFlow.set(f.getThroughputKgs());
            pcsBlow.set(f.getBlowdownKgs());
        }
        double q = NPPSim.core != null ? NPPSim.core.getThermalPower() : 0.0;
        coreMW.set(q);
        totalMW.set(total);
        eff.set(q > 1.0 ? total / q * 100.0 : 0.0);
    }

    @Override
    public void initializeDialUpdateThread() {
    }

    @Override
    public void setVisibility(boolean visible) {
        setVisible(visible);
    }

    @Override
    public void discard() {
        setVisible(false);
    }

    @Override
    public void acknowledge() {
    }
}
