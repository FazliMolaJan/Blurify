package com.chteuchteu.blurify.hlpr;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;

public class BitmapUtil {
	/**
	 * Applies mask to source bitmap. Returns a bitmap with the size
	 * of the mask (or lower)
	 * @param source Source bitmap
	 * @param mask Bitmap mask
	 * @param maskPosX X pos of the mask
	 * @param maskPosY Y pos of the mask
	 * @return Bitmap
	 */
	public static Bitmap applyMask(Bitmap source, Bitmap mask, int maskPosX, int maskPosY) {
		int maskWidth = mask.getWidth();
		int maskHeight = mask.getHeight();

		// Copy the original bitmap
		Bitmap bitmap;
		if (source.isMutable())
			bitmap = source;
		else {
			bitmap = source.copy(Bitmap.Config.ARGB_8888, true);
			if (!source.isRecycled())
				source.recycle();
		}
		bitmap.setHasAlpha(true);

		/*if (maskPosX < 0)
			maskPosX = 0;
		if (maskPosY < 0)
			maskPosY = 0;*/

		// If the mask is larger than the source, resize the mask
		if (mask.getWidth() > bitmap.getWidth() || mask.getHeight() > bitmap.getHeight())
			mask = resizeBitmap(mask, bitmap.getWidth(), bitmap.getHeight());

		/* If the mask goes outside the picture, we have to create a new bitmap
		 * to avoid bitmap creation failure
		 * (maskPosX >= 0 && maskPosX + maskWidth <= bitmapWidth constraint)
		 */
		// Crop bitmap to fit mask
		if (maskPosX < 0 || maskPosX + maskWidth > bitmap.getWidth()
				|| maskPosY < 0 || maskPosY + maskHeight > bitmap.getHeight()) {
			Log.i("", "(bitmap is overlapping)");

			// Create a blank bitmap which will contain the piece of mask
			Bitmap blankBitmap = Bitmap.createBitmap(maskWidth, maskHeight, Bitmap.Config.ARGB_8888);

			// Compute dimensions of the piece of bitmap inside the mask
			// Width
			int w;
			if (maskPosX >= 0 && maskPosX + maskWidth <= bitmap.getWidth())
				w = maskWidth;
			else if (maskPosX < 0)
				w = maskWidth + maskPosX;
			else // maskPosX + maskWidth > bitmap.getWidth()
				w = bitmap.getWidth() - maskPosX;

			// Height
			int h;
			if (maskPosY >= 0 && maskPosY + maskHeight <= bitmap.getHeight())
				h = maskHeight;
			else if (maskPosY < 0)
				h = maskHeight + maskPosY;
			else // maskPosY + maskHeight > bitmap.getHeight()
				h = bitmap.getHeight() - maskPosY;


			int pieceDelX = maskPosX < 0 ? 0 : maskPosX;
			int pieceDelY = maskPosY < 0 ? 0 : maskPosY;
			Bitmap pieceOfMask = Bitmap.createBitmap(bitmap, pieceDelX, pieceDelY, w, h);

			// Put the piece on the blankBitmap
			// Get position of the piece inside the canvas
			int cx = maskPosX < 0 ? -maskPosX : 0;
			int cy = maskPosY < 0 ? -maskPosY : 0;

			Canvas canvas = new Canvas(blankBitmap);
			canvas.drawBitmap(pieceOfMask, cx, cy, new Paint());

			bitmap = blankBitmap;
		} else
			bitmap = Bitmap.createBitmap(bitmap, maskPosX, maskPosY, maskWidth, maskHeight);
		// Finished cropping bitmap

		// Apply mask
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		canvas.drawBitmap(mask, 0, 0, paint);

		if (!mask.isRecycled())
			mask.recycle();
		return bitmap;
	}

	public static Bitmap resizeBitmap(Bitmap source, int maxWidth, int maxHeight) {
		int outWidth;
		int outHeight;
		int inWidth = source.getWidth();
		int inHeight = source.getHeight();
		if (inWidth > inHeight) {
			outWidth = maxWidth;
			outHeight = (inHeight * maxWidth) / inWidth;
		} else {
			outHeight = maxHeight;
			outWidth = (inWidth * maxHeight) / inHeight;
		}

		return Bitmap.createScaledBitmap(source, outWidth, outHeight, false);
	}

	public static Bitmap getDrawableAsBitmap(Context context, int drawable) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			options.inMutable = true;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;

		Resources res = context.getResources();
		return BitmapFactory.decodeResource(res, drawable, options);
	}

	public static int getDominantColor(Bitmap bitmap) {
		try {
			Bitmap onePixelBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
			return onePixelBitmap.getPixel(0,0);
		} catch (Exception ex) {
			return Color.BLACK;
		}
	}

	public static int[] mapBitmapCoordinatesFromImageView(int posX, int posY, ImageView imageView) {
		Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

		int ivW = imageView.getWidth();
		int ivH = imageView.getHeight();
		int bW = bitmap.getWidth();
		int bH = bitmap.getHeight();

		int newX = posX * bW / ivW;
		int newH = posY * bH / ivH;

		return new int[] { newX, newH };
	}

	public static void recycle(Bitmap bitmap) {
		if (bitmap != null && !bitmap.isRecycled())
			bitmap.recycle();
	}
}