import net.minecraft.server.v1_13_R2.IBlockData;

import de.eternalwings.bukkit.invisiblenms.Super;
import de.eternalwings.bukkit.invisiblenms.annotations.Mixin;

@Mixin
public interface EntityMixin {

    default void a(IBlockData iblockdata) {
        Super.call(iblockdata);
    }

    default void W() {
        System.out.println("inner tick!");
        Super.call();
    }

}
