package cip.render.util3d;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(JUnitPlatform.class)
public class TestXfm3f {

    /**
     * For the input XFM, get the inverse and return transformations and do aan inverse-return transformation on vectors and points
     * to verify correctness of the inversion code.
     * @param xfm
     */
    private void inverseInOut(Xfm4x4f xfm) {

    }

    /**
     * Get a new 3D transform, it should be initialized to an identity matrix.
     */
    @Test
    @DisplayName("test init to identity")
    void testInitIdentity() {
        Xfm4x4f xfm = new Xfm4x4f();
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                assertEquals( (row == col) ? 1.0f : 0.0f,xfm.get(row,col));
            }
        }
    }

    /**
     *
     */
    @Test
    @DisplayName("test translate")
    void testTranslate() {
        Xfm4x4f xfm = new Xfm4x4f().translate(1.0f, 2.0f, 3.0f);
        assertEquals( 1.0f, xfm.get(0,3));
        assertEquals( 2.0f, xfm.get(1,3));
        assertEquals( 3.0f, xfm.get(2,3));
    }

    /**
     *
     */
    @Test
    @DisplayName("test translate a translate")
    void testTranslatetranslate() {
        Xfm4x4f xfm = new Xfm4x4f().translate(1.0f, 2.0f, 3.0f).translate(1.0f, 2.0f, 3.0f);
        assertEquals( 2.0f, xfm.get(0,3));
        assertEquals( 4.0f, xfm.get(1,3));
        assertEquals( 6.0f, xfm.get(2,3));
    }
}
