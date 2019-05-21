package de.eternalwings.bukkit.invisiblenms.annotations;

import java.lang.annotation.*;

/**
 * Avoids copying a default method inside a {@link Mixin}.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface DontCopy {
}
