package fr.mercury.nucleus.math;

/**
 * <code>MercuryMath</code> gathers mathematical functions together, providing fast
 * utilization for the user.
 * <p>
 * It takes over the {@link Math} class and returns often float equivalent values.
 * 
 * @author GnosticOccultist
 */
public final class MercuryMath {
	
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
}
