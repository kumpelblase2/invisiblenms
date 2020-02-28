import net.minecraft.server.v1_13_R2.IBlockData;
import net.minecraft.server.v1_13_R2.NBTTagCompound;

import de.eternalwings.bukkit.invisiblenms.Super;
import de.eternalwings.bukkit.invisiblenms.annotations.Mixin;
import de.eternalwings.bukkit.invisiblenms.annotations.CopyAs;

@Mixin
public interface EntityMixin {

    default void a(IBlockData iblockdata) {
        Super.call(iblockdata);
    }

    @CopyAs("a")
    default void onEntitySave(NBTTagCompound compound) {

    }

    default void W() {
        System.out.println("inner tick!");
        Super.call();
    }
}
