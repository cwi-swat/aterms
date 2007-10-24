package test;

import test.cyclic.YangFactory;
import test.cyclic.types.Sun;
import aterm.pure.PureFactory;

public class CyclicTest {

	private YangFactory factory;

	public CyclicTest(YangFactory factory) {
		this.factory = factory;
	}

	public YangFactory getYangFactory() {
		return factory;
	}

	public void run1() {
		Sun test = getYangFactory().makeSun_Pong(getYangFactory().makeMoon_Ping(getYangFactory().makeSun_Shi()));
	}

	public final static void main(String[] args) {
		CyclicTest test = new CyclicTest(YangFactory.getInstance(new PureFactory()));
		test.run1();
	}

}
