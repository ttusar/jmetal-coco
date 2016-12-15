
import java.util.Random;
import jmetal.util.IRandomGenerator;

public class RandomGenerator implements IRandomGenerator{

	private Random generator;


	public RandomGenerator (long seed){
		this.generator = new Random(seed);
	}

	public int nextInt(int upperLimit) {
		return generator.nextInt(upperLimit);
	}

	public double nextDouble() {
		return generator.nextDouble();
	}

}