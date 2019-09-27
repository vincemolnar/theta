/*
 *  Copyright 2017 Budapest University of Technology and Economics
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package hu.bme.mit.theta.xcfa.dsl;

import hu.bme.mit.theta.common.dsl.Scope;
import hu.bme.mit.theta.common.dsl.Symbol;
import hu.bme.mit.theta.core.decl.VarDecl;
import hu.bme.mit.theta.core.stmt.Stmt;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.Type;
import hu.bme.mit.theta.core.type.booltype.BoolType;
import hu.bme.mit.theta.core.utils.TypeUtils;
import hu.bme.mit.theta.xcfa.XCFA;
import hu.bme.mit.theta.xcfa.dsl.gen.XcfaDslBaseVisitor;
import hu.bme.mit.theta.xcfa.dsl.gen.XcfaDslParser;
import hu.bme.mit.theta.xcfa.dsl.gen.XcfaDslParser.AssignStmtContext;
import hu.bme.mit.theta.xcfa.dsl.gen.XcfaDslParser.AssumeStmtContext;
import hu.bme.mit.theta.xcfa.dsl.gen.XcfaDslParser.HavocStmtContext;
import hu.bme.mit.theta.xcfa.dsl.gen.XcfaDslParser.StmtContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static hu.bme.mit.theta.core.stmt.Stmts.*;
import static hu.bme.mit.theta.core.type.booltype.BoolExprs.Bool;

final class XcfaStatement {

	private Stmt stmt = null;

	private final Scope scope;
	private final StmtContext context;

	XcfaStatement(final Scope scope, final StmtContext context) {
		this.scope = checkNotNull(scope);
		this.context = checkNotNull(context);
	}

	Stmt instantiate() {
		if(stmt != null) return stmt;
		final StmtCreatorVisitor visitor = new StmtCreatorVisitor(scope);
		final Stmt stmt = context.accept(visitor);
		if (stmt == null) {
			throw new AssertionError();
		} else {
			return this.stmt = stmt;
		}
	}

	private static final class StmtCreatorVisitor extends XcfaDslBaseVisitor<Stmt> {

		private final Scope scope;

		StmtCreatorVisitor(final Scope scope) {
			this.scope = checkNotNull(scope);
		}

		@Override
		public Stmt visitHavocStmt(final HavocStmtContext ctx) {
			final String lhsId = ctx.lhs.getText();
			Optional<? extends Symbol> opt = scope.resolve(lhsId);
			checkState(opt.isPresent());
			final InstantiatableSymbol lhsSymbol = (InstantiatableSymbol) opt.get();
			final VarDecl<?> var = (VarDecl<?>) lhsSymbol.instantiate();
			return Havoc(var);
		}

		@Override
		public Stmt visitAssumeStmt(final AssumeStmtContext ctx) {
			final XcfaExpression expression = new XcfaExpression(scope, ctx.cond);
			final Expr<BoolType> expr = TypeUtils.cast(expression.instantiate(), Bool());
			return Assume(expr);
		}

		@Override
		public Stmt visitAssignStmt(final AssignStmtContext ctx) {
			final String lhsId = ctx.lhs.getText();
			Optional<? extends Symbol> opt = scope.resolve(lhsId);
			checkState(opt.isPresent());
			final InstantiatableSymbol lhsSymbol = (InstantiatableSymbol) opt.get();
			final VarDecl<?> var = (VarDecl<?>) lhsSymbol.instantiate();

			final XcfaExpression expression = new XcfaExpression(scope, ctx.value);
			final Expr<?> expr = expression.instantiate();

			if (expr.getType().equals(var.getType())) {
				@SuppressWarnings("unchecked")
				final VarDecl<Type> tVar = (VarDecl<Type>) var;
				@SuppressWarnings("unchecked")
				final Expr<Type> tExpr = (Expr<Type>) expr;
				return Assign(tVar, tExpr);
			} else {
				throw new IllegalArgumentException("Type of " + var + " is incompatible with " + expr);
			}
		}

		@Override
		public Stmt visitProcCallStmt(final XcfaDslParser.ProcCallStmtContext ctx) {
			final String lhsId = ctx.lhs.getText();
			final String callee = ctx.funcName.getText();

			Optional<? extends Symbol> opt = scope.resolve(lhsId);
			checkState(opt.isPresent());
			final InstantiatableSymbol lhsSymbol = (InstantiatableSymbol) opt.get();
			final VarDecl<?> var = (VarDecl<?>) lhsSymbol.instantiate();

			opt = scope.resolve(callee);
			checkState(opt.isPresent());
			final InstantiatableSymbol calleeSymbol = (InstantiatableSymbol) opt.get();
			checkState(calleeSymbol.instantiate() instanceof XCFA.Process);
			XCFA.Process process = (XCFA.Process) calleeSymbol.instantiate();

			List<VarDecl<?>> params = new ArrayList<>();

			if(ctx.params != null) {
				ctx.params.forEach(token -> {
					Optional<? extends Symbol> optionalSymbol = scope.resolve(token.getText());
					checkState(optionalSymbol.isPresent());
					final InstantiatableSymbol varSymbol = (InstantiatableSymbol) optionalSymbol.get();
					params.add( (VarDecl<?>) varSymbol.instantiate() );

				});
			}
			return new CallStmt(var, process, params);
		}
	}

}
