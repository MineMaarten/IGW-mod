package igwmod.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * This class is derived from Vanilla's ContainerCreative class.
 */
class ContainerBlockWiki extends Container{

    public void updateStacks(List<LocatedStack> stacks, List<IPageLink> pageLinks){
        InventoryBasic inventory = new InventoryBasic("tmp", true, 45);
        inventorySlots = new ArrayList();
        inventoryItemStacks = new ArrayList();
        for(int i = 0; i < stacks.size(); i++) {
            addSlotToContainer(new Slot(inventory, i, stacks.get(i).x, stacks.get(i).y));
            inventory.setInventorySlotContents(i, stacks.get(i).stack);
        }
        for(int i = 0; i < pageLinks.size(); i++) {
            if(pageLinks.get(i) instanceof LocatedStack) {
                LocatedStack stack = (LocatedStack)pageLinks.get(i);
                addSlotToContainer(new Slot(inventory, stacks.size() + i, stack.x, stack.y));
                inventory.setInventorySlotContents(stacks.size() + i, stack.stack);
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer par1EntityPlayer){
        return true;
    }

    @Override
    protected void retrySlotClick(int par1, int par2, boolean par3, EntityPlayer par4EntityPlayer){}

    /**
     * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
     */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2){
        if(par2 >= inventorySlots.size() - 2 && par2 < inventorySlots.size()) {
            Slot slot = (Slot)inventorySlots.get(par2);

            if(slot != null && slot.getHasStack()) {
                slot.putStack((ItemStack)null);
            }
        }

        return null;
    }
}
