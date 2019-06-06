package de.eternalwings.bukkit.invisiblenms.annotations;

import java.lang.annotation.*;

/**
 * Annotation for a default method so that it will be copied
 * under the given new name instead of the defined name.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface CopyAs {
    /**
     * The target name of the copied method.
     */
    String value();
}
