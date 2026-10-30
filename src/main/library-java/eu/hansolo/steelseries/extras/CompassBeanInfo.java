package eu.hansolo.steelseries.extras;

import java.awt.Image;
import java.beans.SimpleBeanInfo;

public class CompassBeanInfo extends SimpleBeanInfo {
   @Override
   public Image getIcon(int var1) {
      switch (var1) {
         case 1:
            return this.loadImage("/eu/hansolo/steelseries/resources/Compass16.png");
         case 2:
            return this.loadImage("/eu/hansolo/steelseries/resources/Compass32.png");
         case 3:
            return null;
         case 4:
            return null;
         default:
            return null;
      }
   }
}
