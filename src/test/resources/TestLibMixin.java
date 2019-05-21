import de.eternalwings.bukkit.invisiblenms.Super;
import de.eternalwings.bukkit.invisiblenms.annotations.DontCopy;
import de.eternalwings.bukkit.invisiblenms.annotations.Mixin;

@Mixin
public interface TestLibMixin {

    default int getDefaultPort() {
        return 1;
    }
}
