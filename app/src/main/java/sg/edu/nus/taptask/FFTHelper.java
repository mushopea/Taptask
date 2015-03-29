package sg.edu.nus.taptask;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class FFTHelper {

    public static double[] FFTReal(short[] realInput) {
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

    public static double[] complexMultiply(double[] input0, double[] input1) {
        if (input0.length != input1.length) {
            return null;
        }
        // Multiply FFTs together
        // complex multiplication: (a + bj) * (c + dj) = (ac - bd) + (bc + ad)j
        double[] FFTProduct = new double[input0.length];
        for (int i=0 ; i<512 ; i++) {
            double a = input0[i*2];
            double b = input0[i*2+1];
            double c = input1[i*2];
            double d = input1[i*2+1];

            FFTProduct[i*2] = a*c - b*d;
            FFTProduct[i*2+1] = b*c + a*d;
        }
        return FFTProduct;
    }

    public static double[] realToComplex(short[] realInput) {
        double[] complexOutput = new double[realInput.length*2];
        for (int i=0 ; i<realInput.length ; i++) {
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
     * Gets the number that is a power of 2, and larger or equal to num
     * Not sure if this works. Might have precision errors...
     */
    public static int nextPower2(int num) {
        double n = Math.log(num)/Math.log(2);
        double floorN = Math.floor(n);
        if (n == floorN) {
            return num;
        } else {
            return (int)Math.pow(2, floorN + 1);
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

    public static double sum(double[] input) {
        double sum = 0;
        for (int i=0 ; i<input.length ; i++) {
            sum += input[i];
        }
        return sum;
    }

    public static double absSum(double[] input) {
        double sum = 0;
        for (int i=0 ; i<input.length ; i++) {
            sum += Math.abs(input[i]);
        }
        return sum;
    }

    public static double[] shortToDouble(short[] input) {
        double[] output = new double[input.length];
        for (int i=0 ; i<input.length ; i++) {
            output[i] = input[i];
        }
        return output;
    }
}
