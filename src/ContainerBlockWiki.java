import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * This class is derived from Vanilla's ContainerCreative class.
 */
@SideOnly(Side.CLIENT)
class ContainerBlockWiki extends Container{
    /** the list of items in this container */
    public List itemList = new ArrayList();

    public ContainerBlockWiki(){
        for(int i = 0; i < 9; ++i) {
            for(int j = 0; j < 2; ++j) {
                addSlotToContainer(new Slot(GuiBlockWiki.getInventory(), i * 2 + j, 41 + j * 18, 66 + i * 18));
            }
        }
        scrollTo(0.0F);
    }

    @Override
    public boolean canInteractWith(EntityPlayer par1EntityPlayer){
        return true;
    }

    /**
     * Updates the gui slots ItemStack's based on scroll position.
     */
    public void scrollTo(float par1){
        int i = itemList.size() / 2 - 9 + 1;
        int j = (int)(par1 * i + 0.5D);

        if(j < 0) {
            j = 0;
        }

        for(int k = 0; k < 9; ++k) {
            for(int l = 0; l < 2; ++l) {
                int i1 = l + (k + j) * 2;

                if(i1 >= 0 && i1 < itemList.size()) {
                    GuiBlockWiki.getInventory().setInventorySlotContents(l + k * 2, (ItemStack)itemList.get(i1));
                } else {
                    GuiBlockWiki.getInventory().setInventorySlotContents(l + k * 2, (ItemStack)null);
                }
            }
        }
    }

    public boolean hasMoreThan1PageOfItemsInList(){
        return itemList.size() > 18;
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
