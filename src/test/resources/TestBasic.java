import de.eternalwings.bukkit.invisiblenms.annotations.*;
import de.eternalwings.bukkit.invisiblenms.def.TestType;
import de.eternalwings.bukkit.invisiblenms.Super;

@CopyDefaults
public class TestBasic extends TestType implements TestMixin {

    @Override
    public String getValue() {
        return "Valueeee";
    }
}
