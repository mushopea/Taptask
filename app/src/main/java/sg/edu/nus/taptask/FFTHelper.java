package sg.edu.nus.taptask;

import android.util.Log;

import java.util.Arrays;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class FFTHelper {

    public static double[] FFTReal(short[] realInput) {
        // Convert to complex
        double[] complex = realToComplex(realInput);
        // Perform FFT
        return FFT(complex);
    }

    public static double[] FFTReal(double[] realInput) {
        // Convert to complex
        double[] complex = realToComplex(realInput);
        // Perform FFT
        return FFT(complex);
    }

    public static double[] FFT(double[] complexInput) {
        // Perform FFT
        DoubleFFT_1D fft = new DoubleFFT_1D(complexInput.length/2);
        fft.complexForward(complexInput);
        return complexInput;
    }

    public static double[] FFTInverse(double[] complexInput) {
        // Perform Inverse FFT
        DoubleFFT_1D fft = new DoubleFFT_1D(complexInput.length/2);
        fft.complexInverse(complexInput, true);
        return complexInput;
    }

    public static double[] sobelKernel(int length, int size) {
        if ( size <= 0) {
            return null;
        }
        double[] sobelKernel = new double[length];
        Arrays.fill(sobelKernel, 0);
        for (int i=1 ; i<=size ; i++) {
            sobelKernel[length-i] = -i;
            sobelKernel[i] = i;
        }
        return sobelKernel;
    }

    public static double[] triangleKernel(int length, int size) {
        if ( size <= 0) {
            return null;
        }
        double[] triangleKernel = new double[length];
        Arrays.fill(triangleKernel, 0);
        triangleKernel[0] = 1;
        for (int i=1 ; i<=size ; i++) {
            triangleKernel[length-i] = 1 - (double)i/(double)size;
            triangleKernel[i] = 1 - (double)i/(double)size;
        }
        return triangleKernel;
    }



    public static double[] boxKernel(int length, int size) {
        if ( size <= 0) {
            return null;
        }
        double[] boxKernel = new double[length];
        Arrays.fill(boxKernel, 0);
        boxKernel[0] = 1.0f/(double)(size);
        for (int i=1 ; i<=size ; i++) {
            boxKernel[length-i] = 1.0f/(double)(size);
            boxKernel[i] = 1.0f/(double)(size);
        }
        return boxKernel;
    }

    public static double[] planckTaperWindow(int length, double e) {
        if ( length < 1 || e <= 0 || e >= 0.5f) {
            return null;
        }
        double[] planckTaperWindow = new double[length];
        Arrays.fill(planckTaperWindow, 1);
        planckTaperWindow[0] = 0;
        planckTaperWindow[length-1] = 0;
        for (int i=1 ; i<e*(length-1) ; i++) {
            double za = e*(length-1) * (1.0f/i + 1.0f/(i-e*(length-1)));
            planckTaperWindow[i] = 1.0f/(Math.exp(za)+1);
        }
        for (int i=(int)(1-e)*(length-1) ; i<length-1 ; i++) {
            double zb = e*(length-1) * (1.0f/(length-1+i) + 1.0f/((1-e)*(length-1)*(i)));
            planckTaperWindow[i] = 1.0f/(Math.exp(zb)+1);
        }
        return planckTaperWindow;
    }

    public static double[] FFTConvolution(double[] realSignal, double[] realKernel) {
        return FFTConvolution(realSignal, realKernel, 1);
    }

    public static double[] FFTConvolution(double[] realSignal, double[] realKernel, int times) {
        if (realSignal.length != realKernel.length) {
            Log.e("FFT", "FFTConvolution: Signal length mismatch!");
            return null;
        }
        if (times <= 0) {
            Log.e("FFT", "FFTConvolution: Invalid number of times to do convolution!");
            return null;
        }
        double[] complexSignal = FFTHelper.realToComplex(realSignal);
        double[] complexKernel = FFTHelper.realToComplex(realKernel);
        double[] FFTSignal = FFTHelper.FFT(complexSignal);
        double[] FFTKernel = FFTHelper.FFT(complexKernel);
        double[] FFTProduct = null;
        for (int i=0 ; i<times ; i++) {
            FFTProduct = FFTHelper.complexMultiply(FFTSignal, FFTKernel);
        }
        double[] FFTInverse = FFTHelper.FFTInverse(FFTProduct);
        double[] realConvolutionResult = FFTHelper.complexRealPart(FFTInverse);
        return realConvolutionResult;
    }

    public static double[] complexMultiply(double[] input0, double[] input1) {
        if (input0.length != input1.length) {
            return null;
        }
        // Multiply FFTs together
        // complex multiplication: (a + bj) * (c + dj) = (ac - bd) + (bc + ad)j
        double[] FFTProduct = new double[input0.length];
        for (int i=0 ; i<input0.length/2 ; i++) {
            double a = input0[i*2];
            double b = input0[i*2+1];
            double c = input1[i*2];
            double d = input1[i*2+1];

            FFTProduct[i*2] = a*c - b*d;
            FFTProduct[i*2+1] = b*c + a*d;
        }
        return FFTProduct;
    }

    public static double[] realMultiply(double[] input0, double[] input1) {
        if (input0.length != input1.length) {
            return null;
        }
        // Multiply inputs element-wise
        double[] product = new double[input0.length];
        for (int i=0 ; i<input0.length ; i++) {
            product[i] = input0[i] * input1[i];
        }
        return product;
    }

    public static double[] realToComplex(short[] realInput) {
        double[] complexOutput = new double[realInput.length*2];
        for (int i=0 ; i<complexOutput.length ; i++) {
            if (i%2 == 0) {
                complexOutput[i] = realInput[i/2];
            } else {
                complexOutput[i] = 0;
            }
        }
        return complexOutput;
    }

    public static double[] realToComplex(double[] realInput) {
        double[] complexOutput = new double[realInput.length*2];
        for (int i=0 ; i<complexOutput.length ; i++) {
            if (i%2 == 0) {
                complexOutput[i] = realInput[i/2];
            } else {
                complexOutput[i] = 0;
            }
        }
        return complexOutput;
    }

    public static double[] complexRealPart(double[] complexInput) {
        double[] realOutput = new double[complexInput.length/2];
        for (int i=0; i<complexInput.length/2; i++) {
            realOutput[i] = complexInput[2*i];
        }
        return realOutput;
    }

    public static double[] complexMagnitude(double[] complexInput) {
        double[] magnitude = new double[complexInput.length/2];
        for (int i=0; i<complexInput.length/2; i++) {
            double re = complexInput[2*i];
            double im = complexInput[2*i+1];
            magnitude[i] = Math.log(re*re + im*im + 0.001);
        }
        return magnitude;
    }

    /**
     * Uses a maximum of ~30 loops to reach the limit of int, not so bad...
     */
    public static int nextPowerOf2(int num) {
        int power = 1;
        while(power <= num) {
            power *= 2;
        }
        return power;
    }

    public static double[] padWithZerosPower2(double[] input) {
        int len = nextPowerOf2(input.length);
        return padWithZeros(input, len);
    }

    public static double[] padWithZeros(double[] input, int len) {
        if (len <= input.length) {
            return input;
        } else {
            double[] output = new double[len];
            System.arraycopy(input, 0, output, 0, input.length);
            Arrays.fill(output, input.length, output.length, 0);
            return output;
        }
    }

    public static double[] trim(double[] input, int length) {
        if (input.length == length) {
            return input;
        } else {
            double[] output = new double[length];
            System.arraycopy(input, 0, output, 0, length);
            return output;
        }
    }

    public static int firstElementGreaterThan(double[] input, double limit, int startIndex) {
        for (int i=startIndex ; i<input.length ; i++) {
            if (input[i] > limit) {
                return i;
            }
        }
        return -1;
    }

    public static void clampMaxValue(double[] input, double max) {
        for (int i=0 ; i<input.length ; i++) {
            if (input[i] > max) {
                input[i] = max;
            }
        }
    }

    public static double[] FFTShift(double[] complexInput) {
        double[] shiftedResult = new double[complexInput.length];
        System.arraycopy(complexInput, complexInput.length/2, shiftedResult, 0, complexInput.length/2);
        System.arraycopy(complexInput, 0, shiftedResult, complexInput.length/2, complexInput.length/2);
        return shiftedResult;
    }

    public static double[] normalizeMax(double[] input, double normalizedMax) {
        double[] normalizedResult = new double[input.length];
        double max = Double.MIN_VALUE;
        for (int i=0 ; i<input.length ; i++) {
            if (input[i] > max) {
                max = input[i];
            }
        }
        double scale = normalizedMax/max;
        for (int i=0 ; i<input.length ; i++) {
            normalizedResult[i] = input[i] * scale;
        }
        return normalizedResult;
    }

    public static double[] normalize(double[] input) {
        double absSum = absSum(input);
        double[] normalizedResult = new double[input.length];
        for (int i=0 ; i<input.length ; i++) {
            normalizedResult[i] = input[i] / absSum;
        }
        return normalizedResult;
    }

    // Thresholds input to zero if < threshold, one if >= threshold
    public static void binaryThreshold(double[] input, double threshold) {
        for (int i=0 ; i<input.length ; i++) {
            if (input[i] < threshold) {
                input[i] = 0;
            } else {
                input[i] = 1;
            }
        }
    }

    public static double sum(double[] input) {
        double sum = 0;
        for (int i=0 ; i<input.length ; i++) {
            sum += input[i];
        }
        return sum;
    }

    public static double max(double[] input) {
        double max = Double.MIN_VALUE;
        for (int i=0 ; i<input.length ; i++) {
            if (input[i] > max ) {
                max = input[i];
            }
        }
        return max;
    }

    public static int maxIndex(double[] input) {
        return maxIndex(input, input.length);
    }

    public static int maxIndex(double[] input, int limit) {
        double max = Double.MIN_VALUE;
        int index = -1;
        for (int i=0 ; i<input.length && i<limit ; i++) {
            if (input[i] > max ) {
                max = input[i];
                index = i;
            }
        }
        return index;
    }


    public static double absSum(double[] input) {
        double sum = 0;
        for (int i=0 ; i<input.length ; i++) {
            sum += Math.abs(input[i]);
        }
        return sum;
    }

    public static double absSquareSum(double[] input) {
        double sum = 0;
        for (int i=0 ; i<input.length ; i++) {
            sum += Math.pow(Math.abs(input[i]), 2);
        }
        return sum;
    }

    public static void abs(double[] input) {
        for (int i=0 ; i<input.length ; i++) {
            input[i] += Math.abs(input[i]);
        }
    }

    public static double trapezoidSum(double[] input) {
        double sum = 0;
        for (int i=0 ; i<input.length -1 ; i++) {
            sum += (input[i] + input[i+1]) / 2;
        }
        return sum;
    }

    public static double[] reverse(double[] input) {
        double[] output = new double[input.length];
        for (int i=0 ; i<input.length ; i++) {
            output[i] = input[input.length-1-i];
        }
        return output;
    }

    public static double[] shortToDouble(short[] input) {
        double[] output = new double[input.length];
        for (int i=0 ; i<input.length ; i++) {
            output[i] = input[i];
        }
        return output;
    }

    public static double[] floatToDouble(float[] input) {
        double[] output = new double[input.length];
        for (int i=0 ; i<input.length ; i++) {
            output[i] = input[i];
        }
        return output;
    }
}
