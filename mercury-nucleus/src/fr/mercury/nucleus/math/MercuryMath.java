package fr.mercury.nucleus.math;

import java.util.Random;

import fr.alchemy.utilities.LocalVars;
import fr.mercury.nucleus.math.objects.Color;
import fr.mercury.nucleus.math.objects.Matrix3f;
import fr.mercury.nucleus.math.objects.Matrix4f;
import fr.mercury.nucleus.math.objects.Quaternion;
import fr.mercury.nucleus.math.objects.Vector2f;
import fr.mercury.nucleus.math.objects.Vector3f;
import fr.mercury.nucleus.math.objects.Vector4f;

/**
 * <code>MercuryMath</code> contains mathematical functions, providing fast
 * utilization for the user.
 * <p>
 * It takes over the {@link Math} class and returns often float equivalent
 * values, for usability inside mathematical object.
 * 
 * @author GnosticOccultist
 */
public final class MercuryMath {

    /**
     * The instance of random object used to generate random numbers.
     */
    public static final Random RAND = new Random(System.currentTimeMillis());
    /**
     * The 'close to zero' value to use in differences checking.
     */
    public static final double EPSILON = 0.0001D;
    /**
     * The PI floating point value.
     */
    public static final float PI = (float) Math.PI;
    /**
     * The factor to multiply a degree value to convert it into radians.
     */
    public static final float DEG_TO_RAD = PI / 180.0f;
    /**
     * The factor to multiply a radian value to convert it into degrees.
     */
    public static final float RAD_TO_DEG = 180.0f / PI;
    /**
     * The local vars from the main thread.
     */
    public static final LocalVars LOCAL_VARS = LocalVars.get();

    static {
        LOCAL_VARS.register(Vector2f.class);
        LOCAL_VARS.register(Vector3f.class);
        LOCAL_VARS.register(Vector4f.class);
        LOCAL_VARS.register(Quaternion.class);
        LOCAL_VARS.register(Matrix3f.class);
        LOCAL_VARS.register(Matrix4f.class);
        LOCAL_VARS.register(Color.class);
    }

    /**
     * Return an instance of {@link Vector2f} using the <code>MercuryMath</code>
     * {@link LocalVars}. If no instance is currently available, it will
     * instantiates a new one.
     * 
     * @return A free and available vector instance, or a newly created one.
     */
    public static Vector2f getVector2f() {
        return LOCAL_VARS.acquireNext(Vector2f.class, Vector2f::new);
    }

    /**
     * Return an instance of {@link Vector3f} using the <code>MercuryMath</code>
     * {@link LocalVars}. If no instance is currently available, it will
     * instantiates a new one.
     * 
     * @return A free and available vector instance, or a newly created one.
     */
    public static Vector3f getVector3f() {
        return LOCAL_VARS.acquireNext(Vector3f.class, Vector3f::new);
    }

    /**
     * Return an instance of {@link Vector4f} using the <code>MercuryMath</code>
     * {@link LocalVars}.
     * 
     * @return A free and available vector instance, or a newly created one.
     */
    public static Vector4f getVector4f() {
        return LOCAL_VARS.acquireNext(Vector4f.class, Vector4f::new);
    }

    /**
     * Return an instance of {@link Quaternion} using the <code>MercuryMath</code>
     * {@link LocalVars}. If no instance is currently available, it will
     * instantiates a new one.
     * 
     * @return A free and available quaternion instance, or a newly created one.
     */
    public static Quaternion getQuaternion() {
        return LOCAL_VARS.acquireNext(Quaternion.class, Quaternion::new);
    }

    /**
     * Return an instance of {@link Matrix3f} using the <code>MercuryMath</code>
     * {@link LocalVars}. If no instance is currently available, it will
     * instantiates a new one.
     * 
     * @return A free and available matrix instance, or a newly created one.
     */
    public static Matrix3f getMatrix3f() {
        return LOCAL_VARS.acquireNext(Matrix3f.class, Matrix3f::new);
    }

    /**
     * Return an instance of {@link Matrix4f} using the <code>MercuryMath</code>
     * {@link LocalVars}. If no instance is currently available, it will
     * instantiates a new one.
     * 
     * @return A free and available matrix instance, or a newly created one.
     */
    public static Matrix4f getMatrix4f() {
        return LOCAL_VARS.acquireNext(Matrix4f.class, Matrix4f::new);
    }

    /**
     * Return an instance of {@link Color} using the <code>MercuryMath</code>
     * {@link LocalVars}. If no instance is currently available, it will
     * instantiates a new one.
     * 
     * @return A free and available color instance, or a newly created one.
     */
    public static Color getColor() {
        return LOCAL_VARS.acquireNext(Color.class, Color::new);
    }

    /**
     * Return the square root of the provided value.
     * <p>
     * It uses the {@link Math#sqrt(double)} function.
     * 
     * @param value The value to get the square root from.
     * @return The square root of the value, or {@link Float#NaN}.
     */
    public static float sqrt(float value) {
        return (float) Math.sqrt(value);
    }

    /**
     * Return the inverse of the square root of the provided value.
     * <p>
     * It uses the {@link Math#sqrt(double)} function and inverse it.
     * 
     * @param value The value to get the inverse square root from.
     * @return The inverse square root of the value.
     */
    public static float invSqrt(float value) {
        return (float) (1.0f / Math.sqrt(value));
    }

    /**
     * Return the cosinus of the provided value.
     * <p>
     * It uses the {@link Math#cos(double)} function.
     * 
     * @param value The angle in radians.
     * @return The cosinus of the value.
     */
    public static float cos(float value) {
        return (float) Math.cos(value);
    }

    /**
     * Return the sinus of the provided value.
     * <p>
     * It uses the {@link Math#sin(double)} function.
     * 
     * @param value The angle in radians.
     * @return The sinus of the value.
     */
    public static float sin(float value) {
        return (float) Math.sin(value);
    }

    /**
     * Return the tangent of the provided value.
     * <p>
     * It uses the {@link Math#tan(double)} function.
     * 
     * @param value The angle in radians.
     * @return The tangent of the value.
     */
    public static float tan(float value) {
        return (float) Math.tan(value);
    }

    /**
     * Return the least common multiple (LCM) of the two provided integers. This is
     * the smallest positive integer for both a and b.
     * 
     * @param a The first integer value (not zero).
     * @param b The second integer value.
     * @return The least common multiple of the two values.
     */
    public static int lcm(int a, int b) {
        return a * (b / gcd(a, b));
    }

    /**
     * Return the greatest common divisor (GCD) of the two provided integers. This
     * is the largest positive integer that divides both a and b.
     * <p>
     * It uses the Euclidean algorithm for computation.
     * 
     * @param a The first integer value (not zero).
     * @param b The second integer value.
     * @return The greatest common divisor of the two values.
     */
    public static int gcd(int a, int b) {
        while (b > 0) {
            var temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
}
