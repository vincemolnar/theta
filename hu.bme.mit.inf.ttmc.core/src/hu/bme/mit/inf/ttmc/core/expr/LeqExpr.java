package hu.bme.mit.inf.ttmc.core.expr;

import hu.bme.mit.inf.ttmc.core.type.BoolType;
import hu.bme.mit.inf.ttmc.core.type.RatType;

public interface LeqExpr extends BinaryExpr<RatType, RatType, BoolType> {
	
	@Override
	public LeqExpr withOps(final Expr<? extends RatType> leftOp, final Expr<? extends RatType> rightOp);
	
	@Override
	public LeqExpr withLeftOp(final Expr<? extends RatType> leftOp);

	@Override
	public LeqExpr withRightOp(final Expr<? extends RatType> rightOp);
}