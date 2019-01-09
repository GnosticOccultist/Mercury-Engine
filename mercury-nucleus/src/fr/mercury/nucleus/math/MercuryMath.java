package fr.mercury.nucleus.math;

import java.util.Random;

import fr.alchemy.utilities.LocalVars;
import fr.mercury.nucleus.math.objects.Color;
import fr.mercury.nucleus.math.objects.Matrix4f;
import fr.mercury.nucleus.math.objects.Quaternion;
import fr.mercury.nucleus.math.objects.Vector2f;
import fr.mercury.nucleus.math.objects.Vector3f;
import fr.mercury.nucleus.math.objects.Vector4f;

/**
 * <code>MercuryMath</code> contains mathematical functions, providing fast
 * utilization for the user.
 * <p>
 * It takes over the {@link Math} class and returns often float equivalent values, 
 * for usability inside mathematical object.
 * 
 * @author GnosticOccultist
 */
public final class MercuryMath {
	
	/**
	 * The instance of random object used to generate random numbers.
	 */
	public static final Random rand = new Random(System.currentTimeMillis());
	/**
	 * The local vars from the main thread.
	 */
	public static final LocalVars LOCAL_VARS = LocalVars.get();
	
	static {
		LOCAL_VARS.register(Vector2f.class);
		LOCAL_VARS.register(Vector3f.class);
		LOCAL_VARS.register(Vector4f.class);
		LOCAL_VARS.register(Quaternion.class);
		LOCAL_VARS.register(Matrix4f.class);
		LOCAL_VARS.register(Color.class);
	}
	
	/**
	 * Return the square root of the provided value.
	 * <p>
	 * It uses the {@link Math#sqrt(double)} function.
	 * 
	 * @param value The value to get the square root from.
	 * @return		The square root of the value.
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
	 * @return		The inverse square root of the value.
	 */
	public static float invSqrt(float value) {
		return (float) (1.0f / Math.sqrt(value));
	}

	/**
	 * Return the tangent of the provided value.
	 * <p>
	 * It uses the {@link Math#tan(double)} function.
	 * 
	 * @param value The value in radians.
	 * @return		The tangent of the value.
	 */
	public static float tan(double value) {
		return (float) Math.tan(value);
	}
}
