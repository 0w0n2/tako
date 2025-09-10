/**
 * Returns a value that has been rounded to a set precision.
 * @param value The value to round.
 * @param precision The precision (decimal places), default: 3.
 * @returns The rounded number.
 */
const round = (value: number, precision: number = 3): number =>
  parseFloat(value.toFixed(precision));

/**
 * Returns a value that has been limited between min & max.
 * @param value The value to clamp.
 * @param min Minimum value to allow, default: 0.
 * @param max Maximum value to allow, default: 100.
 * @returns The clamped number.
 */
const clamp = (value: number, min: number = 0, max: number = 100): number =>
  Math.min(Math.max(value, min), max);

/**
 * Returns a value that has been re-mapped according to the from/to.
 * For example, adjust(10, 0, 100, 100, 0) = 90.
 * @param value The value to re-map (or adjust).
 * @param fromMin Min value to re-map from.
 * @param fromMax Max value to re-map from.
 * @param toMin Min value to re-map to.
 * @param toMax Max value to re-map to.
 * @returns The adjusted number.
 */
const adjust = (
  value: number,
  fromMin: number,
  fromMax: number,
  toMin: number,
  toMax: number
): number =>
  round(
    toMin + ((toMax - toMin) * (value - fromMin)) / (fromMax - fromMin)
  );

export { round, clamp, adjust };
