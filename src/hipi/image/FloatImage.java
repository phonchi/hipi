package hipi.image;

import hipi.image.HipiImageHeader;
import hipi.image.HipiImageHeader.HipiImageFormat;
import hipi.image.HipiImageHeader.HipiColorSpace;
import hipi.image.RasterImage;
import hipi.image.PixelArrayFloat;

import hipi.util.ByteUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.BinaryComparable;
import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.io.Writable;

/**
 * A 2D image represented as an array of floats. A FloatImage consists
 * of a flat array of pixel values represented as Java floats in
 * addition to an {@link ImageHeader} object.<br/><br/>
 *
 * The {@link hipi.image.io} package provides classes for reading
 * (decoding) and writing (encoding) FloatImage objects in various
 * compressed and uncompressed image formats such as JPEG and PNG.
 */
public class FloatImage extends RasterImage {

  public FloatImage() {
    super((PixelArray)(new PixelArrayFloat()));
  }

  public FloatImage(int width, int height, int bands) {
    super((PixelArray)(new PixelArrayFloat()));
    HipiImageHeader header = new HipiImageHeader(HipiImageFormat.UNDEFINED, HipiColorSpace.UNDEFINED,
						 width, height, bands, null, null);
    setHeader(header);
  }

  /**
   * Get object type identifier.
   *
   * @return Type of object.
   */
  public HipiImageType getType() {
    return HipiImageType.FLOAT;
  }

  /**
   * Provides direct access to underlying float array of pixel data.
   */
  public float[] getData() {
    return ((PixelArrayFloat)this.pixelArray).getData();
  }

  /**
   * Compares two ByteImage objects for equality allowing for some
   * amount of differences in pixel values.
   *
   * @return True if the two images have equal dimensions, color
   * spaces, and are found to deviate by less than a maximum
   * difference, false otherwise.
   */
  public boolean equalsWithTolerance(RasterImage thatImage, float maxDifference) {
    if (thatImage == null) {
      return false;
    }
    // Verify dimensions in headers are equal
    int w = this.getWidth();
    int h = this.getHeight();
    int b = this.getNumBands();
    if (this.getColorSpace() != thatImage.getColorSpace() ||
	thatImage.getWidth() != w || thatImage.getHeight() != h || 
	thatImage.getNumBands() != b) {
      return false;
    }

    // Get pointers to pixel arrays
    PixelArray thisPA = this.getPixelArray();
    PixelArray thatPA = thatImage.getPixelArray();

    // Check that pixel data is equal.
    for (int i=0; i<w*h*b; i++) {
      double diff = Math.abs(thisPA.getElemFloat(i)-thatPA.getElemFloat(i));
      if (diff > maxDifference) {
	return false;
      }
    }

    // Passed, declare equality
    return true;
  }

  /**
   * Compares two FloatImage objects for equality.
   *
   * @return True if the two images are found to deviate by less than
   * 1.0/255.0 at each pixel and across each band, false otherwise.
   */
  @Override
  public boolean equals(Object that) {
    // Check for pointer equivalence
    if (this == that)
      return true;

    // Verify object types are equal
    if (!(that instanceof FloatImage))
      return false;

    return equalsWithTolerance((FloatImage)that, 0.0f);
  }

  /**
   * Performs in-place addition with another {@link FloatImage}.
   * 
   * @param image Target image to add to the current object.
   *
   * @throws IllegalArgumentException If the image dimensions do not
   * match.
   */
  public void add(FloatImage thatImage) throws IllegalArgumentException {
    // Verify input
    checkCompatibleInputImage(thatImage);

    // Perform in-place addition
    int w = this.getWidth();
    int h = this.getHeight();
    int b = this.getNumBands();
    float[] thisData = this.getData();
    float[] thatData = thatImage.getData();
    
    for (int i=0; i<w*h*b; i++) {
      thisData[i] += thatData[i];
    }
  }

  /**
   * Performs in-place addition of a scalar to each band of every
   * pixel.
   * 
   * @param number Scalar to add to each band of each pixel.
   */
  public void add(float number) {
    int w = this.getWidth();
    int h = this.getHeight();
    int b = this.getNumBands();
    float[] thisData = this.getData();
    for (int i=0; i<w*h*b; i++) {
      thisData[i] += number;
    }
  }

  /**
   * Performs in-place elementwise multiplication of {@link
   * FloatImage} and the current image.
   *
   * @param image Target image to use for  multiplication.
   */
  public void multiply(FloatImage thatImage) throws IllegalArgumentException {

    // Verify input
    checkCompatibleInputImage(thatImage);

    // Perform in-place elementwise multiply
    int w = this.getWidth();
    int h = this.getHeight();
    int b = this.getNumBands();
    float[] thisData = this.getData();
    float[] thatData = thatImage.getData();
    for (int i=0; i<w*h*b; i++) {
      thisData[i] *= thatData[i];
    }
  }

  /**
   * Performs in-place multiplication with scalar.
   *
   * @param value Scalar to multiply with each band of each pixel.
   */
  public void scale(float value) {
    int w = this.getWidth();
    int h = this.getHeight();
    int b = this.getNumBands();
    float[] thisData = this.getData();
    for (int i=0; i<w*h*b; i++) {
      thisData[i] *= value;
    }
  }

  /**
   * Computes hash of float array of image pixel data.
   *
   * @return Hash of pixel data represented as a string.
   *
   * @see ByteUtils#asHex is used to compute the hash.
   */
  @Override
  public String hex() {
    float[] pels = this.getData();
    return ByteUtils.asHex(ByteUtils.floatArrayToByteArray(pels));
  }

  /**
   * Helper routine that verifies two images have compatible
   * dimensions for common operations (addition, elementwise
   * multiplication, etc.)
   *
   * @param image RasterImage to check
   * 
   * @throws IllegalArgumentException if the image do not have
   * compatible dimensions. Otherwise has no effect.
   */
  protected void checkCompatibleInputImage(FloatImage image) throws IllegalArgumentException {
    if (image.getColorSpace() != this.getColorSpace() || image.getWidth() != this.getWidth() || 
	image.getHeight() != this.getHeight() || image.getNumBands() != this.getNumBands()) {
      throw new IllegalArgumentException("Color space and/or image dimensions do not match.");
    }
  }

} // public class FloatImage...
