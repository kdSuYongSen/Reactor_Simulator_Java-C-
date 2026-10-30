package org.jfree.chart.ui;

import java.awt.Font;
import java.util.ResourceBundle;
import javax.swing.JTextField;
import org.jfree.chart.util.ResourceBundleWrapper;

public class FontDisplayField extends JTextField {
   private Font displayFont;
   protected static final ResourceBundle localizationResources = ResourceBundleWrapper.getBundle("org.jfree.chart.ui.LocalizationBundle");

   public FontDisplayField(Font font) {
      super("");
      this.setDisplayFont(font);
      this.setEnabled(false);
   }

   public Font getDisplayFont() {
      return this.displayFont;
   }

   public void setDisplayFont(Font font) {
      this.displayFont = font;
      this.setText(this.fontToString(this.displayFont));
   }

   private String fontToString(Font font) {
      return font != null ? font.getFontName() + ", " + font.getSize() : localizationResources.getString("No_Font_Selected");
   }
}
