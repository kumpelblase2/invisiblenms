package de.eternalwings.bukkit.invisiblenms.annotations;

import java.lang.annotation.*;

/**
 * Defines an interface to be a Mixin. Any class that is annotated with {@link CopyDefaults}
 * will thus copy all default methods from interfaces with this annotation. If a default
 * implementation should not be copied, that method can be annotated with {@link DontCopy}.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Mixin {
}
