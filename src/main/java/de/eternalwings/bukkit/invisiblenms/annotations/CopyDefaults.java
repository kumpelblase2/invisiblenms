package de.eternalwings.bukkit.invisiblenms.annotations;

import java.lang.annotation.*;

/**
 * Tells the processor to search for interfaces marked as {@link Mixin} that are implemented
 * by this class and copies its default methods into this class.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface CopyDefaults {

}
