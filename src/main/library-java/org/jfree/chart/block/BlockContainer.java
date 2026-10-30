package org.jfree.chart.block;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.ui.Size2D;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PublicCloneable;

public class BlockContainer extends AbstractBlock implements Block, Cloneable, PublicCloneable, Serializable {
   private static final long serialVersionUID = 8199508075695195293L;
   private final List blocks;
   private Arrangement arrangement;

   public BlockContainer() {
      this(new BorderArrangement());
   }

   public BlockContainer(Arrangement arrangement) {
      Args.nullNotPermitted(arrangement, "arrangement");
      this.arrangement = arrangement;
      this.blocks = new ArrayList();
   }

   public Arrangement getArrangement() {
      return this.arrangement;
   }

   public void setArrangement(Arrangement arrangement) {
      Args.nullNotPermitted(arrangement, "arrangement");
      this.arrangement = arrangement;
   }

   public boolean isEmpty() {
      return this.blocks.isEmpty();
   }

   public List getBlocks() {
      return Collections.unmodifiableList(this.blocks);
   }

   public void add(Block block) {
      this.add(block, null);
   }

   public void add(Block block, Object key) {
      this.blocks.add(block);
      this.arrangement.add(block, key);
   }

   public void clear() {
      this.blocks.clear();
      this.arrangement.clear();
   }

   @Override
   public Size2D arrange(Graphics2D g2, RectangleConstraint constraint) {
      return this.arrangement.arrange(this, g2, constraint);
   }

   @Override
   public void draw(Graphics2D g2, Rectangle2D area) {
      this.draw(g2, area, null);
   }

   @Override
   public Object draw(Graphics2D g2, Rectangle2D area, Object params) {
      StandardEntityCollection sec = null;
      if (params instanceof EntityBlockParams) {
         EntityBlockParams ebp = (EntityBlockParams)params;
         if (ebp.getGenerateEntities()) {
            sec = new StandardEntityCollection();
         }
      }

      Rectangle2D contentArea = (Rectangle2D)area.clone();
      contentArea = this.trimMargin(contentArea);
      this.drawBorder(g2, contentArea);
      contentArea = this.trimBorder(contentArea);
      contentArea = this.trimPadding(contentArea);

      for (Block block : (java.lang.Iterable<Block>) (this.blocks)) {
         Rectangle2D bounds = block.getBounds();
         Rectangle2D drawArea = new Double(bounds.getX() + area.getX(), bounds.getY() + area.getY(), bounds.getWidth(), bounds.getHeight());
         Object r = block.draw(g2, drawArea, params);
         if (sec != null && r instanceof EntityBlockResult) {
            EntityBlockResult ebr = (EntityBlockResult)r;
            EntityCollection ec = ebr.getEntityCollection();
            sec.addAll(ec);
         }
      }

      BlockResult result = null;
      if (sec != null) {
         result = new BlockResult();
         result.setEntityCollection(sec);
      }

      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof BlockContainer)) {
         return false;
      } else {
         BlockContainer that = (BlockContainer)obj;
         if (!that.canEqual(this)) {
            return false;
         } else if (!Objects.equals(this.arrangement, that.arrangement)) {
            return false;
         } else {
            return !Objects.equals(this.blocks, that.blocks) ? false : super.equals(obj);
         }
      }
   }

   @Override
   public boolean canEqual(Object other) {
      return other instanceof BlockContainer;
   }

   @Override
   public int hashCode() {
      int hash = super.hashCode();
      hash = 79 * hash + Objects.hashCode(this.blocks);
      return 79 * hash + Objects.hashCode(this.arrangement);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return (BlockContainer)super.clone();
   }
}
