package org.jfree.chart.editor;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.ui.LCBLayout;

class DefaultNumberAxisEditor extends DefaultValueAxisEditor implements FocusListener {
   private double manualTickUnitValue;
   private JTextField manualTickUnit;

   public DefaultNumberAxisEditor(NumberAxis axis) {
      super(axis);
      this.manualTickUnitValue = axis.getTickUnit().getSize();
      this.validateTickUnit();
   }

   @Override
   protected JPanel createTickUnitPanel() {
      JPanel tickUnitPanel = new JPanel(new LCBLayout(3));
      tickUnitPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
      tickUnitPanel.add(new JPanel());
      JCheckBox autoTickUnitSelectionCheckBox = new JCheckBox(localizationResources.getString("Auto-TickUnit_Selection"), this.isAutoTickUnitSelection());
      autoTickUnitSelectionCheckBox.setActionCommand("AutoTickOnOff");
      autoTickUnitSelectionCheckBox.addActionListener(this);
      this.setAutoTickUnitSelectionCheckBox(autoTickUnitSelectionCheckBox);
      tickUnitPanel.add(this.getAutoTickUnitSelectionCheckBox());
      tickUnitPanel.add(new JPanel());
      tickUnitPanel.add(new JLabel(localizationResources.getString("Manual_TickUnit_value")));
      this.manualTickUnit = new JTextField(Double.toString(this.manualTickUnitValue));
      this.manualTickUnit.setEnabled(!this.isAutoTickUnitSelection());
      this.manualTickUnit.setActionCommand("TickUnitValue");
      this.manualTickUnit.addActionListener(this);
      this.manualTickUnit.addFocusListener(this);
      tickUnitPanel.add(this.manualTickUnit);
      tickUnitPanel.add(new JPanel());
      return tickUnitPanel;
   }

   @Override
   public void actionPerformed(ActionEvent event) {
      String command = event.getActionCommand();
      if (command.equals("TickUnitValue")) {
         this.validateTickUnit();
      } else {
         super.actionPerformed(event);
      }
   }

   @Override
   public void focusLost(FocusEvent event) {
      super.focusLost(event);
      if (event.getSource() == this.manualTickUnit) {
         this.validateTickUnit();
      }
   }

   @Override
   public void toggleAutoTick() {
      super.toggleAutoTick();
      if (this.isAutoTickUnitSelection()) {
         this.manualTickUnit.setText(Double.toString(this.manualTickUnitValue));
         this.manualTickUnit.setEnabled(false);
      } else {
         this.manualTickUnit.setEnabled(true);
      }
   }

   public void validateTickUnit() {
      double newTickUnit;
      try {
         newTickUnit = Double.parseDouble(this.manualTickUnit.getText());
      } catch (NumberFormatException e) {
         newTickUnit = this.manualTickUnitValue;
      }

      if (newTickUnit > 0.0) {
         this.manualTickUnitValue = newTickUnit;
      }

      this.manualTickUnit.setText(Double.toString(this.manualTickUnitValue));
   }

   @Override
   public void setAxisProperties(Axis axis) {
      super.setAxisProperties(axis);
      NumberAxis numberAxis = (NumberAxis)axis;
      if (!this.isAutoTickUnitSelection()) {
         numberAxis.setTickUnit(new NumberTickUnit(this.manualTickUnitValue));
      }
   }
}
