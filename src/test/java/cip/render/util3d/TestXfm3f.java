package cip.render.util3d;

import cip.render.util.AngleF;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

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
        _testUnitVectors(xfm);
        _testInverse(xfm);
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
        _testUnitVectors(xfm);
        _testInverse(xfm);
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
        _testUnitVectors(xfm);
        _testInverse(xfm);
    }

    @Test
    @DisplayName("test X rotation")
    void testRotateX() {
        Xfm4x4f xfm = new Xfm4x4f().rotate(Xfm4x4f.AXIS_X, new AngleF(AngleF.DEGREES,45.0f));
        _testUnitVectors(xfm);
        _testInverse(xfm);
    }

    @Test
    @DisplayName("test Y rotation")
    void testRotateY() {
        Xfm4x4f xfm = new Xfm4x4f().rotate(Xfm4x4f.AXIS_Y, new AngleF(AngleF.DEGREES,45.0f));
        _testUnitVectors(xfm);
        _testInverse(xfm);
    }

    @Test
    @DisplayName("test Z rotation")
    void testRotateZ() {
        Xfm4x4f xfm = new Xfm4x4f().rotate(Xfm4x4f.AXIS_Z, new AngleF(AngleF.DEGREES,45.0f));
        _testUnitVectors(xfm);
        _testInverse(xfm);
    }

    // =============================================================================================================================
    // Rotation/translation transformation testing.
    // -----------------------------------------------------------------------------------------------------------------------------
    // Rotation/translation is a special case of affine transformation that preserves lengths and angles. Al long as we are using
    // only translation/rotation transformations our angles and lengths are also invariant. Adding shear and scale screws up angles
    // and lengths making ray tracing unpredictable. So if we have a set of reference unit vectors, they should stay unit vectors.
    // Dot products and cross products should be the same after transformation (within the zero tolerance).
    static Vector3f[] _testVectors = {
            new Vector3f(1.0f,0.0f,0.0f),
            new Vector3f(0.0f,1.0f,0.0f),
            new Vector3f(0.0f,0.0f,1.0f),
            new Vector3f((float)Math.sqrt(1.0f/3.0f),(float)Math.sqrt(1.0f/3.0f),(float)Math.sqrt(1.0f/3.0f))
    };

    static float[] _testdotProducts = {
            _testVectors[0].dot(_testVectors[3]),
            _testVectors[1].dot(_testVectors[3]),
            _testVectors[2].dot(_testVectors[3])
    };

    static Vector3f[] _xfmTestVectors = {
            new Vector3f(),
            new Vector3f(),
            new Vector3f(),
            new Vector3f()
    };

    static Vector3f[] _backXfmTestVectors = {
            new Vector3f(),
            new Vector3f(),
            new Vector3f(),
            new Vector3f()
    };

    /**
     *
     * This tests a set of unit vectors to make sure they are still unit vectors after transform. NOTE: scale and shear
     * transformations do not preserve unit vectors.
     * @param xfm (Xfm4x4f) The transformation to be tested.
     */
    static private void _testUnitVectors(Xfm4x4f xfm) {
        // transform the vectrors
        xfm.transform(_testVectors, _xfmTestVectors);
        // test that they are still unit vectors
        for (int i = 0; i < _xfmTestVectors.length; i++) {
            float length = _xfmTestVectors[i].getLength();
            if (!PackageConstants.isZero(length - 1.0f)) {
                fail("expected unit vector after transformation, expected 1.000, but length was: " + length);
            }
        }
        for (int i = 0; i < _testdotProducts.length; i++) {
            float testDot = _testVectors[i].dot(_testVectors[3]);
            if (!PackageConstants.isZero(_testdotProducts[i] - testDot)) {
                fail("unexpected dot product after transformation, expected " + _testdotProducts[i]
                        + " but dot was: " + testDot);
            }
        }
    }

    static Xfm4x4f _inverseXfmForTest = new Xfm4x4f();


    static private void _testInverse(Xfm4x4f xfm) {
        // invert the transform, test transforming the unit vectors through the inverse
        _inverseXfmForTest.invert(xfm);
        _testUnitVectors(_inverseXfmForTest);
        // OK, now back transform and verify the back-transformed vectors are the same as the original vectors.
        xfm.transform(_xfmTestVectors, _backXfmTestVectors);
        for (int i=0; i < _backXfmTestVectors.length; i++) {
            _vectorEquals(_testVectors[i], _backXfmTestVectors[i]);
        }

    }

    static private void _vectorEquals(Vector3f expected, Vector3f actual) {
         if (!PackageConstants.isZero(expected.i - actual.i) &&
             !PackageConstants.isZero(expected.j - actual.j) &&
             !PackageConstants.isZero(expected.k - actual.k)) {
             fail("unexpected unequal vector (" + actual.i + "," + actual.j + "," + actual.k +
                     ") is not equal to the expected vector (" + expected.i + "," + expected.j + "," + expected.k + ").");

         }
    }
}
