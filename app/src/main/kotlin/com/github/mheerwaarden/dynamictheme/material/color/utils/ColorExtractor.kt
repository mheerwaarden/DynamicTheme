/*
 * Copyright (c) 2024. Marcel van Heerwaarden
 *
 * Copyright (C) 2019 The Android Open Source Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.mheerwaarden.dynamictheme.material.color.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import contrast.Contrast
import dynamiccolor.ContrastCurve
import dynamiccolor.DynamicScheme
import dynamiccolor.Variant
import hct.Hct
import quantize.QuantizerCelebi
import scheme.SchemeContent
import scheme.SchemeExpressive
import scheme.SchemeFidelity
import scheme.SchemeFruitSalad
import scheme.SchemeMonochrome
import scheme.SchemeNeutral
import scheme.SchemeRainbow
import scheme.SchemeTonalSpot
import scheme.SchemeVibrant
import score.Score
import utils.ColorUtils

/*
The Java code from https://github.com/material-foundation/material-color-utilities/blob/main/ is
imported in the java folder (last commit december 2023).
com.google.errorprone annotations and imports are taken out:
    ./scheme/Scheme.java
    ./dynamiccolor/DynamicColor.java


Documentation on usage
======================

To color themes (color_extraction.md)
-------------------------------------
Ever wonder how Pixel extracts colors from a phone wallpaper to create accessible and beautiful UIs?
The steps can be summarized as follows:

1. Quantize to obtain a representative set of colors from the wallpaper.
2. Score to obtain suitable source colors.
3. Present theme options by pairing the resulting source colors with available scheme variants to
   the user and let them pick.
4. Create tonal palettes from the user-selected theme.
5. Assign Material color roles with values from these tonal palettes.
6. Colors get applied to UIs via Material tokens.

How to generate Dynamic Scheme (dynamic_color_scheme.md)
--------------------------------------------------------
Material color schemes start from a source color, a single color from which all other scheme colors
are derived. Source colors can be picked by a designer, or if the scheme is based on an image like
a user's wallpaper, they can be extracted through a process called quantization. Quantization
filters an image down to its most representative colors. One of those colors is selected as the
source color.

First, the color algorithm systematically manipulates the source color's hue and chroma to create
four additional visually complementary key colors.

Next, those five key colors - the source color plus the four additional colors - are each used to
create a tonal palette. The tonal palette contains 13 tones from black, tone 0, to white, tone 100.
Lower tones are darker in luminance, and higher tones are lighter in luminance. This full set of
five tonal palettes is the basis of a Material color scheme. Individual tones are selected from each
palette and assigned to color roles within a scheme.

The variety of colors in each scheme provide for different needs like accessibility and expression.
For example, color roles that may be used in pairs, like container and on container colors, are
chosen to maintain accessible visual control contrast. Colors in the primary, secondary, and
tertiary groups maintain those contrasts while providing a range of style for visual expression.
Both light and dark schemes are created from this process.

Extracting colors from an image (extracting_colors.md)
------------------------------------------------------
1. Step 1 — Image to Pixels
The first step is to convert an image into an array of pixels in ARGB format. Prior to that, please
resize it to 128 × 128 dimensions for faster processing.
MCU does not provide this feature, so you’ll have to rely on the idiomatic method in your
programming language. For example, in Java, one may use the BufferedImage.getRGB method:

    import java.awt.image.BufferedImage;

    class ImageUtils {
      // ...

      public static int[] imageToPixels(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        return image.getRGB(0, 0, width, height, null, 0, width);
      }

      public static int[] imageToPixelsGeneratedByGemini(BufferedImage image) {
        // Resize the image to 128x128
        BufferedImage resizedImage = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(image, 0, 0, 128, 128, null);
        g2d.dispose();

        // Get the pixel data in ARGB format
        int[] pixels = new int[128 * 128];
        resizedImage.getRGB(0, 0, 128, 128, pixels, 0, 128);

        return pixels;
    }
  }

2. Step 2 — Pixels to Prominent Colors
Once you have the array of pixels, pass it into QuantizerCelebi.quantize provided by the quantize
library.
    QuantizerResult quantizerResult = QuantizerCelebi.quantize(pixels, maxColors);
3. Step 3 — Prominent Colors to Source Colors
Use the Score.score method provided by the score library to extract colors that are suitable as
seeds for color schemes, ranked by decreasing suitability.
    List<Integer> colors = Score.score(quantizerResult);

Refining Contrast (refining_contrast.md)
----------------------------------------
You can manually refine color contrast using the MCU contrast library. For optimal color contrast,
we recommend using the DynamicColor and DynamicScheme features in your production.
1. Calculating contrast ratio
To measure the contrast of two colors, use the ratioOfTones method on the tones (L*) of the two
colors.
The tone of an HCT color is the tone component. The tone of an ARGB color can be obtained by
ColorUtils.lstarFromArgb method.
    double contrastRatio = Contrast.ratioOfTones(hct1.getTone(), hct2.getTone());
    double tone1 = ColorUtils.lstarFromArgb(argb1);
    double tone2 = ColorUtils.lstarFromArgb(argb2);
    double contrastRatio = Contrast.ratioOfTones(tone1, tone2);
2. Obtaining well-contrasting colors
The functions darker and lighter (and their variants darkerUnsafe and lighterUnsafe) allow one to
obtain tones that contrast well against the tone of a given color.
The functions darker and lighter will return -1 if the required contrast cannot be reached, whereas
darkerUnsafe and lighterUnsafe will return 0 (tone of black) and 100 (tone of white), respectively,
making the contrast ratio as high as possible.
The word "unsafe" in the names means they may return a color without guaranteeing contrast ratio.
These functions do not crash but their output colors may not meet accessibility standards.

    double original = ColorUtils.lstarFromArgb(0xFF00AA00);  // 60.56

    double darker = Contrast.darker(original, 3.0);  // 29.63
    double lighter = Contrast.lighter(original, 3.0);  // 98.93
    double darkerUnsafe = Contrast.darkerUnsafe(original, 3.0);  // 29.63
    double lighterUnsafe = Contrast.lighterUnsafe(original, 3.0);  // 98.93

    double darker = Contrast.darker(original, 7.0);  // -1.0
    double lighter = Contrast.lighter(original, 7.0);  // -1.0
    double darkerUnsafe = Contrast.darkerUnsafe(original, 7.0);  // 0.0
    double lighterUnsafe = Contrast.lighterUnsafe(original, 7.0);  // 100.0

Creating a Color Scheme (creating_color_scheme.md)
--------------------------------------------------
1. Generating a scheme
1.1. Method 1 — Using a variant
The recommended way to generate a scheme from a source color is to use a scheme variant constructor,
such as SchemeTonalSpot. The following example generates a SchemeTonalSpot scheme in light mode and
default contrast from hct as source color in HCT format.
    DynamicScheme scheme = new SchemeTonalSpot(hct, false, 0.0);
We provide the below variants: Content, Expressive, Fidelity, Fruit salad, Monochrome, Neutral,
Rainbow, Tonal spot, Vibrant
1.2. Method 2 — Specifying palettes
    DynamicScheme scheme = new DynamicScheme(
        /*sourceColorHct=*/ Hct.fromInt(0xFFEB0057),
        /*variant=*/ Variant.VIBRANT,
        /*isDark=*/ false,
        /*contrastLevel=*/ 0.0,
        /*primaryPalette=*/ TonalPalette.fromInt(0xFFEB0057),
        /*secondaryPalette=*/ TonalPalette.fromInt(0xFFF46B00),
        /*tertiaryPalette=*/ TonalPalette.fromInt(0xFF00AB46),
        /*neutralPalette=*/ TonalPalette.fromInt(0xFF949494),
        /*neutralVariantPalette=*/ TonalPalette.fromInt(0xFFBC8877));
2. Obtaining colors
Colors can be in either ARGB or HCT. Below shows how you may obtain the primary color.
    int argb = scheme.getPrimary();
Alternatively,
    MaterialDynamicColors materialDynamicColors = new MaterialDynamicColors();
    int argb = materialDynamicColors.primary().getArgb(scheme);
    Hct hct = materialDynamicColors.primary().getHct(scheme);

(README.md)
Components		| Purpose
-------------------------
blend			| Interpolate, harmonize, animate, and gradate colors in HCT
contrast		| Measure contrast, obtain contrastful colors
dislike			| Check and fix universally disliked colors
dynamiccolor	| Obtain colors that adjust based on UI state (dark theme, style, preferences, contrast requirements, etc.)
hct				| A new color space (hue, chrome, tone) based on CAM16 x L*, that accounts for  viewing conditions
palettes		| Tonal palette — range of colors that varies only in tone
				| Core palette — set of tonal palettes needed to create Material color schemes
quantize		| Turn an image into N colors; composed of Celebi, which runs Wu, then WSMeans
scheme			| Create static and dynamic color schemes from a single color or a core palette
score			| Rank colors for suitability for theming
temperature		| Obtain analogous and complementary colors
utilities		| Color — convert between color spaces needed to implement HCT/CAM16
				| Math — functions for ex. ensuring hue is between 0 and 360, clamping, etc.
				| String - convert between strings and integers

Conversions between color spaces (color_spaces.md)
--------------------------------------------------
    • sRGB ⇌ HCT
        ◦ Hct.fromInt(argb)
        ◦ Hct.from(h, c, t).toInt()
    • sRGB ⇌ XYZ
        ◦ ColorUtils.xyzFromArgb(argb)
        ◦ ColorUtils.argbFromXyz(x, y, z)
    • sRGB ⇌ Cam16
        ◦ Cam16.fromInt(argb)
        ◦ cam16.toInt()
        ◦ Constructing a Cam16 from JCH or UCS:
            ▪ Cam16.fromJch(j, c, h)
            ▪ Cam16.fromUcs(jstar, astar, bstar)
    • XYZ ⇌ Cam16
        ◦ Cam16.fromXyzInViewingConditions(x, y, z, vc)
        ◦ cam16.xyzInViewingConditions(vc, returnArray)
    • sRGB ⇌ L*a*b*
        ◦ ColorUtils.labFromArgb(argb)
        ◦ ColorUtils.argbFromLab(l, a, b)
    • linRGB → sRGB
        ◦ ColorUtils.argbFromLinrgb(linrgb)

*/

private const val QUATIZE_SIZE = 128

/**
 * See [ContrastCurve]:
 * low    Value for contrast level -1.0
 * normal Value for contrast level 0.0
 * medium Value for contrast level 0.5
 * high   Value for contrast level 1.0
 */
enum class ContrastLevel(val level:Double) {
    Low(-1.0),
    Normal(0.0),
    Medium(0.5),
    High(1.0)
}

object ColorExtractor {

    private val whiteArgb = Color.White.toArgb()
    private val blackArgb = Color.Black.toArgb()
    private val toneWhite = ColorUtils.lstarFromArgb(whiteArgb)
    private val toneBlack = ColorUtils.lstarFromArgb(blackArgb)

    /**
     * @param uri The path to an image
     * @return A list of colors in ARGB format sorted by suitability for a UI theme. The most
     *    suitable color is the first item, the least suitable is the last. There will always be at
     *    least one color returned. If all the input colors were not suitable for a theme, a default
     *    fallback color will be provided, Google Blue.
     */
    fun extractColorsFromImage(context: Context, uri: Uri): List<Int> {
        val originalImage: Bitmap =
                context.contentResolver.openInputStream(uri).use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
        val pixelData =
                if (originalImage.width > QUATIZE_SIZE || originalImage.height > QUATIZE_SIZE) {
                    imageToPixels(originalImage)
                } else {
                    val width = originalImage.width
                    val height = originalImage.height
                    val pixels = IntArray(width * height)
                    originalImage.getPixels(pixels, 0, width, 0, 0, width, height)
                    pixels
                }

        // Set maxColors to the default of Score.score
        val quantizerResult = QuantizerCelebi.quantize(pixelData, 4)
        return Score.score(quantizerResult)
    }

    fun createDynamicColorScheme(
        sourceArgb: Int,
        schemeVariant: Variant,
        isDark: Boolean,
        contrastLevel: ContrastLevel = ContrastLevel.Normal,
    ): DynamicScheme {
        val hct = Hct.fromInt(sourceArgb)
        return when (schemeVariant) {
            Variant.CONTENT -> SchemeContent(hct, isDark, contrastLevel.level)
            Variant.EXPRESSIVE -> SchemeExpressive(hct, isDark, contrastLevel.level)
            Variant.FIDELITY -> SchemeFidelity(hct, isDark, contrastLevel.level)
            Variant.FRUIT_SALAD -> SchemeFruitSalad(hct, isDark, contrastLevel.level)
            Variant.MONOCHROME -> SchemeMonochrome(hct, isDark, contrastLevel.level)
            Variant.NEUTRAL -> SchemeNeutral(hct, isDark, contrastLevel.level)
            Variant.RAINBOW -> SchemeRainbow(hct, isDark, contrastLevel.level)
            Variant.TONAL_SPOT -> SchemeTonalSpot(hct, isDark, contrastLevel.level)
            Variant.VIBRANT -> SchemeVibrant(hct, isDark, contrastLevel.level)
        }
    }

    /**
     * Convert an image into an array of pixels in ARGB format. Prior to that, resize it to
     * 128 × 128 dimensions for faster processing.
     */
    private fun imageToPixels(image: Bitmap): IntArray {
        // Resize the image to 128x128
        val resizedImage = Bitmap.createScaledBitmap(image, QUATIZE_SIZE, QUATIZE_SIZE, false)

        // Get the pixel data in ARGB format
        val pixels = IntArray(QUATIZE_SIZE * QUATIZE_SIZE)
        resizedImage.getPixels(pixels, 0, QUATIZE_SIZE, 0, 0, QUATIZE_SIZE, QUATIZE_SIZE)

        return pixels
    }

    fun getContrastColorArgb(colorArgb: Int): Int {
        val tone = ColorUtils.lstarFromArgb(colorArgb)
        val contrastRatioWhite = Contrast.ratioOfTones(tone, toneWhite)
        val contrastRatioBlack = Contrast.ratioOfTones(tone, toneBlack)
        return if (contrastRatioWhite > contrastRatioBlack) whiteArgb else blackArgb
    }

    fun getContrastColor(color: Color): Color {
        val tone = ColorUtils.lstarFromArgb(color.toArgb())
        val contrastRatioWhite = Contrast.ratioOfTones(tone, toneWhite)
        val contrastRatioBlack = Contrast.ratioOfTones(tone, toneBlack)
        return if (contrastRatioWhite > contrastRatioBlack) Color.White else Color.Black
    }

}

