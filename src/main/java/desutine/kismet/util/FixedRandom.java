package desutine.kismet.util;

import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * A Random extension that always gives a positive extreme value.
 * By extreme value we define the following:
 * - If there is a bound, the extreme value will be the biggest value that belongs to the bounded interval
 * - If there is no bound, the extreme value will be 3/4 of the biggest unsigned value the primitive allows
 */
public class FixedRandom extends Random {
    @Override
    public void nextBytes(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = Byte.MAX_VALUE;
        }
    }

    @Override
    public DoubleStream doubles() {
        return super.doubles()
                .map(o -> this.nextDouble());
    }

    @Override
    public DoubleStream doubles(double randomNumberOrigin, double randomNumberBound) {

        return super.doubles(randomNumberOrigin, randomNumberBound)
                .map(o -> Math.nextDown(randomNumberBound));
    }

    @Override
    public DoubleStream doubles(long streamSize) {
        return super.doubles(streamSize)
                .map(o -> this.nextDouble());
    }

    @Override
    public DoubleStream doubles(long streamSize, double randomNumberOrigin, double randomNumberBound) {
        return super.doubles(streamSize, randomNumberOrigin, randomNumberBound)
                .map(o -> Math.nextDown(randomNumberBound));
    }

    @Override
    public IntStream ints() {
        return super.ints()
                .map(o -> nextInt());
    }

    @Override
    public IntStream ints(int randomNumberOrigin, int randomNumberBound) {
        return super.ints(randomNumberOrigin, randomNumberBound)
                .map(o -> nextInt(randomNumberBound));
    }

    @Override
    public IntStream ints(long streamSize) {
        return super.ints(streamSize)
                .map(o -> nextInt());
    }

    @Override
    public IntStream ints(long streamSize, int randomNumberOrigin, int randomNumberBound) {
        return super.ints(streamSize, randomNumberOrigin, randomNumberBound)
                .map(o -> nextInt(randomNumberBound));
    }

    @Override
    public LongStream longs() {
        return super.longs()
                .map(o -> nextLong());
    }

    @Override
    public LongStream longs(long randomNumberOrigin, long randomNumberBound) {
        return super.longs(randomNumberOrigin, randomNumberBound)
                .map(o -> randomNumberBound - 1);
    }

    @Override
    public LongStream longs(long streamSize) {
        return super.longs(streamSize)
                .map(o -> nextLong());
    }

    @Override
    public LongStream longs(long streamSize, long randomNumberOrigin, long randomNumberBound) {
        return super.longs(streamSize, randomNumberOrigin, randomNumberBound)
                .map(o -> randomNumberBound - 1);
    }

    @Override
    public boolean nextBoolean() {
        return true;
    }


    @Override
    public double nextDouble() {
        return Math.nextDown(1.0);
    }

    @Override
    public float nextFloat() {
        return Math.nextDown(1.0f);
    }

    @Override
    public synchronized double nextGaussian() {
        return Double.MAX_VALUE;
    }

    @Override
    public int nextInt() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int nextInt(int bound) {
        if (bound <= 0)
            throw new IllegalArgumentException("bound must be positive");
        return bound - 1;
    }

    @Override
    public long nextLong() {
        return Long.MAX_VALUE;
    }
}
