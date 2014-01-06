package igwmod.api;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;

/**
 * This event will be fired when a player opens the wiki GUI while looking at a block in the world. Your job as subscriber is to change the
 * pageOpened field when you find the right block. If no subscriber changes the pageOpened field, IGW will try to open a page at
 * assets/igwmod/wiki/block/drawnStack.getUnlocalizedName()>.
 */
public class BlockWikiEvent extends WorldEvent{
    public final int x, y, z, blockId, idPicked, metadata;
    public final Block block;
    public ItemStack drawnStack; //ItemStack that is drown in the top left corner of the GUI.
    public String pageOpened; //current page (ResourceLocation) this gui will go to. It contains the default location, but can be changed.

    public BlockWikiEvent(World world, int x, int y, int z){
        super(world);
        this.x = x;
        this.y = y;
        this.z = z;
        blockId = world.getBlockId(x, y, z);
        metadata = world.getBlockMetadata(x, y, z);
        block = Block.blocksList[blockId];
        idPicked = block.idPicked(world, x, y, z);
        drawnStack = new ItemStack(idPicked != 0 ? idPicked : blockId, 1, metadata);
    }

}
