package hu.bme.mit.inf.theta.core.utils;

import static hu.bme.mit.inf.theta.core.expr.impl.Exprs.Add;
import static hu.bme.mit.inf.theta.core.expr.impl.Exprs.And;
import static hu.bme.mit.inf.theta.core.expr.impl.Exprs.Eq;
import static hu.bme.mit.inf.theta.core.expr.impl.Exprs.False;
import static hu.bme.mit.inf.theta.core.expr.impl.Exprs.Geq;
import static hu.bme.mit.inf.theta.core.expr.impl.Exprs.Gt;
import static hu.bme.mit.inf.theta.core.expr.impl.Exprs.Iff;
import static hu.bme.mit.inf.theta.core.expr.impl.Exprs.Imply;
import static hu.bme.mit.inf.theta.core.expr.impl.Exprs.Int;
import static hu.bme.mit.inf.theta.core.expr.impl.Exprs.IntDiv;
import static hu.bme.mit.inf.theta.core.expr.impl.Exprs.Ite;
import static hu.bme.mit.inf.theta.core.expr.impl.Exprs.Leq;
import static hu.bme.mit.inf.theta.core.expr.impl.Exprs.Lt;
import static hu.bme.mit.inf.theta.core.expr.impl.Exprs.Mul;
import static hu.bme.mit.inf.theta.core.expr.impl.Exprs.Neg;
import static hu.bme.mit.inf.theta.core.expr.impl.Exprs.Not;
import static hu.bme.mit.inf.theta.core.expr.impl.Exprs.Or;
import static hu.bme.mit.inf.theta.core.expr.impl.Exprs.Rat;
import static hu.bme.mit.inf.theta.core.expr.impl.Exprs.RatDiv;
import static hu.bme.mit.inf.theta.core.expr.impl.Exprs.Sub;
import static hu.bme.mit.inf.theta.core.expr.impl.Exprs.True;
import static hu.bme.mit.inf.theta.core.type.impl.Types.Int;
import static hu.bme.mit.inf.theta.core.utils.impl.ExprUtils.evaluate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import hu.bme.mit.inf.theta.core.decl.ConstDecl;
import hu.bme.mit.inf.theta.core.decl.Decl;
import hu.bme.mit.inf.theta.core.decl.impl.Decls;
import hu.bme.mit.inf.theta.core.expr.Expr;
import hu.bme.mit.inf.theta.core.expr.LitExpr;
import hu.bme.mit.inf.theta.core.model.Assignment;
import hu.bme.mit.inf.theta.core.model.impl.AssignmentImpl;
import hu.bme.mit.inf.theta.core.type.IntType;

public class ExprEvaluationTest {

	private final ConstDecl<IntType> ca = Decls.Const("a", Int());
	private final ConstDecl<IntType> cb = Decls.Const("b", Int());

	private final Expr<IntType> a = ca.getRef();
	private final Expr<IntType> b = cb.getRef();

	@Test
	public void testNot() {
		// @formatter:off
		Assert.assertEquals(False(), evaluate(Not(And(True(), True()))));
		Assert.assertEquals(True(), evaluate(Not(And(False(), True()))));
		// @formatter:on
	}

	@Test
	public void testAnd() {
		// @formatter:off
		Assert.assertEquals(False(), evaluate(And(True(), False(), True())));
		Assert.assertEquals(True(), evaluate(And(True(), True(), True())));
		Assert.assertEquals(True(), evaluate(And(Collections.emptySet())));
		// @formatter:on
	}

	@Test
	public void testOr() {
		// @formatter:off
		Assert.assertEquals(False(), evaluate(Or(False(), False(), False())));
		Assert.assertEquals(True(), evaluate(Or(False(), True(), False())));
		Assert.assertEquals(True(), evaluate(Or(Collections.emptySet())));
		// @formatter:on
	}

	@Test
	public void testImply() {
		// @formatter:off
		Assert.assertEquals(True(), evaluate(Imply(False(), True())));
		Assert.assertEquals(True(), evaluate(Imply(True(), True())));
		Assert.assertEquals(False(), evaluate(Imply(True(), False())));
		Assert.assertEquals(True(), evaluate(Imply(False(), False())));
		// @formatter:on
	}

	@Test
	public void testIff() {
		// @formatter:off
		Assert.assertEquals(False(), evaluate(Iff(False(), True())));
		Assert.assertEquals(True(), evaluate(Iff(True(), True())));
		Assert.assertEquals(False(), evaluate(Iff(True(), False())));
		Assert.assertEquals(True(), evaluate(Iff(False(), False())));
		// @formatter:on
	}

	@Test
	public void testEq() {
		// @formatter:off
		Assert.assertEquals(True(), evaluate(Eq(False(), False())));
		Assert.assertEquals(False(), evaluate(Eq(False(), True())));
		Assert.assertEquals(True(), evaluate(Eq(Int(2), Int(2))));
		Assert.assertEquals(False(), evaluate(Eq(Int(2), Int(-2))));
		Assert.assertEquals(True(), evaluate(Eq(Rat(1, 2), Rat(1, 2))));
		Assert.assertEquals(False(), evaluate(Eq(Rat(1, 2), Rat(-1, 2))));
		// @formatter:on
	}

	@Test
	public void testGeq() {
		// @formatter:off
		Assert.assertEquals(True(), evaluate(Geq(Rat(8, 4), Int(2))));
		Assert.assertEquals(True(), evaluate(Geq(Rat(3, 4), Rat(2, 3))));
		Assert.assertEquals(True(), evaluate(Geq(Rat(9, 4), Int(2))));
		Assert.assertEquals(False(), evaluate(Geq(Int(2), Rat(9, 4))));
		// @formatter:on
	}

	@Test
	public void testGt() {
		// @formatter:off
		Assert.assertEquals(False(), evaluate(Gt(Rat(8, 4), Int(2))));
		Assert.assertEquals(True(), evaluate(Gt(Rat(3, 4), Rat(2, 3))));
		Assert.assertEquals(True(), evaluate(Gt(Rat(9, 4), Int(2))));
		Assert.assertEquals(False(), evaluate(Gt(Int(2), Rat(9, 4))));
		// @formatter:on
	}

	@Test
	public void testLeq() {
		// @formatter:off
		Assert.assertEquals(True(), evaluate(Leq(Rat(8, 4), Int(2))));
		Assert.assertEquals(True(), evaluate(Leq(Rat(2, 3), Rat(3, 4))));
		Assert.assertEquals(True(), evaluate(Leq(Int(2), Rat(9, 4))));
		Assert.assertEquals(False(), evaluate(Leq(Rat(9, 4), Int(2))));
		// @formatter:on
	}

	@Test
	public void testLt() {
		// @formatter:off
		Assert.assertEquals(False(), evaluate(Lt(Int(2), Rat(8, 4))));
		Assert.assertEquals(True(), evaluate(Lt(Rat(2, 3), Rat(3, 4))));
		Assert.assertEquals(True(), evaluate(Lt(Int(2), Rat(9, 4))));
		Assert.assertEquals(False(), evaluate(Lt(Rat(9, 4), Int(2))));
		// @formatter:on
	}

	@Test
	public void testIntDiv() {
		// @formatter:off
		Assert.assertEquals(Int(0), evaluate(IntDiv(Int(1), Int(2))));
		Assert.assertEquals(Int(1), evaluate(IntDiv(Int(3), Int(2))));
		// @formatter:on
	}

	@Test
	public void testRatDiv() {
		// @formatter:off
		Assert.assertEquals(Rat(8, 9), evaluate(RatDiv(Rat(2, 3), Rat(3, 4))));
		Assert.assertEquals(Rat(1, 2), evaluate(RatDiv(Rat(2, 3), Rat(4, 3))));
		Assert.assertEquals(Rat(1, 3), evaluate(RatDiv(Rat(2, 3), Int(2))));
		Assert.assertEquals(Rat(1, 2), evaluate(RatDiv(Int(2), Int(4))));
		// @formatter:on
	}

	@Test
	public void testNeg() {
		// @formatter:off
		Assert.assertEquals(Rat(1, 2), evaluate(Neg(Neg(Neg(Neg(Rat(1, 2)))))));
		Assert.assertEquals(Rat(-1, 2), evaluate(Neg(Neg(Neg(Neg(Neg(Rat(1, 2))))))));
		Assert.assertEquals(Int(182), evaluate(Neg(Neg(Neg(Neg(Int(182)))))));
		Assert.assertEquals(Int(-182), evaluate(Neg(Neg(Neg(Neg(Neg(Int(182))))))));
		// @formatter:on
	}

	@Test
	public void testSub() {
		// @formatter:off
		Assert.assertEquals(Int(-1), evaluate(Sub(Int(7), Int(8))));
		Assert.assertEquals(Rat(1, 4), evaluate(Sub(Rat(3, 4), Rat(1, 2))));
		Assert.assertEquals(Rat(-1, 4), evaluate(Sub(Rat(3, 4), Int(1))));
		// @formatter:on
	}

	@Test
	public void testAdd() {
		// @formatter:off
		Assert.assertEquals(Int(6), evaluate(Add(Int(1), Int(2), Int(3))));
		Assert.assertEquals(Int(0), evaluate(Add(Int(1), Int(2), Int(-3))));
		Assert.assertEquals(Rat(7, 12), evaluate(Add(Rat(1, 3), Rat(1, 4))));
		// @formatter:on
	}

	@Test
	public void testMul() {
		// @formatter:off
		Assert.assertEquals(Int(30), evaluate(Mul(Int(2), Int(3), Int(5))));
		Assert.assertEquals(Int(1), evaluate(Mul(Int(2), Int(1), Rat(1, 2))));
		Assert.assertEquals(Rat(3, 4), evaluate(Mul(Rat(3, 2), Int(1), Rat(1, 2))));
		// @formatter:on
	}

	@Test
	public void testIte() {
		// @formatter:off
		Assert.assertEquals(Int(1), evaluate(Ite(True(), Int(1), Int(2))));
		Assert.assertEquals(Int(2), evaluate(Ite(False(), Int(1), Int(2))));
		Assert.assertEquals(Int(1), evaluate(Ite(True(), Ite(True(), Ite(True(), Int(1), Int(2)), Int(3)), Int(4))));
		// @formatter:on
	}

	@Test
	public void testModel() {
		final Map<Decl<?, ?>, LitExpr<?>> declToExpr = new HashMap<>();
		declToExpr.put(ca, Int(5));
		declToExpr.put(cb, Int(10));
		final Assignment assignment = new AssignmentImpl(declToExpr);

		// @formatter:off
		Assert.assertEquals(Int(15), evaluate(Add(a, b), assignment));
		Assert.assertEquals(Int(50), evaluate(Mul(a, b), assignment));
		Assert.assertEquals(Int(0), evaluate(IntDiv(a, b), assignment));
		Assert.assertEquals(Rat(1, 2), evaluate(RatDiv(a, b), assignment));
		Assert.assertEquals(Rat(5, 10), evaluate(RatDiv(a, b), assignment));
		// @formatter:on
	}

	@Test(expected = RuntimeException.class)
	public void testException() {
		evaluate(Add(a, Int(1)));
	}
}