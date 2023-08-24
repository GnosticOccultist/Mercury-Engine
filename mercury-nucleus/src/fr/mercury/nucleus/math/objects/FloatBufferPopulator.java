package fr.mercury.nucleus.math.objects;

import java.nio.FloatBuffer;

/**
 * <code>FloatBufferPopulator</code> is an interface to implement a class which contains float value(s) 
 * that can be used to populate a {@link FloatBuffer}.
 * 
 * @author GnosticOccultist
 */
public interface FloatBufferPopulator {

    /**
     * Populates the given {@link FloatBuffer} with the data from the <code>FloatBufferPopulator</code>.
     * <p>
     * Implementations must use relative put method, meaning the float data is written at the current buffer's 
     * position and this position is incremented by how many floats have been inserted.
     * 
     * @param store The buffer to populate with the data (not null).
     * @return      The given store populated with the data.
     */
    FloatBuffer populate(FloatBuffer store);
}
