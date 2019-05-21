import java.net.URL;

import de.eternalwings.bukkit.invisiblenms.Super;
import de.eternalwings.bukkit.invisiblenms.annotations.DontCopy;
import de.eternalwings.bukkit.invisiblenms.annotations.Mixin;

@Mixin
public interface TestMixin {

    String getValue();

    default void testVoid() {
        System.out.println(this.getValue());
    }

    default void testParams(String test) {
        System.out.println(test);

        try {
            new URL("");
        } catch (Exception e) {}
        de.eternalwings.bukkit.invisiblenms.Super.call(test);
    }

    default void b(boolean a) {
        Super.call(a);
    }

    default void b(boolean a, boolean b) { Super.call(a,b); }

    default boolean testBoolean() {
        System.out.println("Hello World!");
        return (System.out != null ? Super.call() : System.err != null);
    }

    @DontCopy
    default void shouldntCopy() {

    }
}
