import net.minecraft.server.v1_13_R2.EntityTypes;
import net.minecraft.server.v1_13_R2.EntityZombie;
import net.minecraft.server.v1_13_R2.World;
import net.minecraft.server.v1_13_R2.IBlockData;

import de.eternalwings.bukkit.invisiblenms.annotations.CopyDefaults;

@CopyDefaults
public class CustomZombie extends EntityZombie implements EntityMixin {
    public CustomZombie(EntityTypes<?> entitytypes, World world) {
        super(entitytypes, world);
    }

    public CustomZombie(World world) {
        super(world);
    }

    public void test(World world) {
        System.out.println(world);
    }
}
