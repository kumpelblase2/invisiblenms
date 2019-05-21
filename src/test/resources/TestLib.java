import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import de.eternalwings.bukkit.invisiblenms.annotations.*;
import de.eternalwings.bukkit.invisiblenms.def.TestType;
import de.eternalwings.bukkit.invisiblenms.Super;

@CopyDefaults
public class TestLib extends URLStreamHandler implements TestLibMixin {

    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        return null;
    }
}
