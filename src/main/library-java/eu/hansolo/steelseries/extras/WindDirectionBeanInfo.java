package eu.hansolo.steelseries.extras;

import java.awt.Image;
import java.beans.SimpleBeanInfo;

public class WindDirectionBeanInfo extends SimpleBeanInfo {
   @Override
   public Image getIcon(int var1) {
      switch (var1) {
         case 1:
            return this.loadImage("/eu/hansolo/steelseries/resources/WindDirection16.png");
         case 2:
            return this.loadImage("/eu/hansolo/steelseries/resources/WindDirection32.png");
         case 3:
            return null;
         case 4:
            return null;
         default:
            return null;
      }
   }
}
