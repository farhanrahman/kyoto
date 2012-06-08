package uk.ac.ic.kyoto.math;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** @author Nicolas Khadivi */

// a*X^2 + b*X + c = 0
public class QuadraticEquationTest {
	
	// No roots (b^2-4ac < 0)
	QuadraticEquation ZeroRoots1 = new QuadraticEquation(1.00, 0.00, 1.00);
	QuadraticEquation ZeroRoots2 = new QuadraticEquation(0.00, 0.00, 1.00);
	QuadraticEquation ZeroRoots3 = new QuadraticEquation(3.00, -2.00, 1.00);
	QuadraticEquation ZeroRoots4 = new QuadraticEquation(-1.00, 2.00, -3.00);

	// One root (b^2-4ac == 0)
	QuadraticEquation OneRoot1 = new QuadraticEquation(1.00, 2.00, 1.00);
	QuadraticEquation OneRoot2 = new QuadraticEquation(1.00, 3.00, 2.25);
	QuadraticEquation OneRoot3 = new QuadraticEquation(4.00, -4.00, 1.00);
	QuadraticEquation OneRoot4 = new QuadraticEquation(6.25, -5.00, 1.00);

	// 2 roots (b^2-4ac > 0)
	QuadraticEquation TwoRoots1 = new QuadraticEquation(0.50, -1.50, 1.00);
	QuadraticEquation TwoRoots2 = new QuadraticEquation(2.25, 3.00, 0.00);
	QuadraticEquation TwoRoots3 = new QuadraticEquation(9.00, 12.00, 3.00);
	QuadraticEquation TwoRoots4 = new QuadraticEquation(-3.00, 13.00, -12.00);

	@Test
	public void testZeroRoots() {
		assertTrue((((Double) ZeroRoots1.getRootOne()).isNaN())
				&& (((Double) ZeroRoots1.getRootTwo()).isNaN()));
		assertTrue((((Double) ZeroRoots2.getRootOne()).isNaN())
				&& (((Double) ZeroRoots2.getRootOne()).isNaN()));
		assertTrue((((Double) ZeroRoots3.getRootOne()).isNaN())
				&& (((Double) ZeroRoots3.getRootOne()).isNaN()));
		assertTrue((((Double) ZeroRoots4.getRootOne()).isNaN())
				&& (((Double) ZeroRoots4.getRootOne()).isNaN()));
	}

	@Test
	public void testOneRoot() {
		assertTrue(OneRoot1.getRootOne() == OneRoot1.getRootTwo());
		assertTrue(OneRoot2.getRootOne() == OneRoot2.getRootTwo());
		assertTrue(OneRoot3.getRootOne() == OneRoot3.getRootTwo());
		assertTrue(OneRoot4.getRootOne() == OneRoot4.getRootTwo());

		assertTrue(OneRoot1.getRootOne() == -1.00);
		assertTrue(OneRoot2.getRootTwo() == -1.50);
		assertTrue(OneRoot3.getRootOne() == 0.50);
		assertTrue(OneRoot4.getRootTwo() == 0.40);
	}

	@Test
	public void testTwoRoots() {
		assertTrue(TwoRoots1.getRootOne() != TwoRoots1.getRootTwo());
		assertTrue(TwoRoots2.getRootOne() != TwoRoots2.getRootTwo());
		assertTrue(TwoRoots3.getRootOne() != TwoRoots3.getRootTwo());
		assertTrue(TwoRoots4.getRootOne() != TwoRoots4.getRootTwo());

		assertTrue(TwoRoots1.getRootOne() == 1.00
				&& TwoRoots1.getRootTwo() == 2.00);
		assertTrue(TwoRoots2.getRootOne() == -4 / 3d
				&& TwoRoots2.getRootTwo() == 0.00);
		assertTrue(TwoRoots3.getRootOne() == -1.00
				&& TwoRoots3.getRootTwo() == -1 / 3d);
		assertTrue(TwoRoots4.getRootOne() == 3.00
				&& TwoRoots4.getRootTwo() == 4 / 3d);
	}
}
