package hu.bme.mit.inf.ttmc.analysis.tcfa.expl;

import static hu.bme.mit.inf.ttmc.core.type.impl.Types.Int;
import static hu.bme.mit.inf.ttmc.formalism.common.decl.impl.Decls2.Var;

import java.util.Collections;

import org.junit.Test;

import hu.bme.mit.inf.ttmc.analysis.algorithm.Abstractor;
import hu.bme.mit.inf.ttmc.analysis.algorithm.ArgPrinter;
import hu.bme.mit.inf.ttmc.analysis.algorithm.NullLabeling;
import hu.bme.mit.inf.ttmc.analysis.expl.ExplDomain;
import hu.bme.mit.inf.ttmc.analysis.expl.ExplPrecision;
import hu.bme.mit.inf.ttmc.analysis.expl.ExplState;
import hu.bme.mit.inf.ttmc.analysis.expl.GlobalExplPrecision;
import hu.bme.mit.inf.ttmc.analysis.tcfa.TCFAAnalysisContext;
import hu.bme.mit.inf.ttmc.analysis.tcfa.TCFADomain;
import hu.bme.mit.inf.ttmc.analysis.tcfa.TCFAInitFunction;
import hu.bme.mit.inf.ttmc.analysis.tcfa.TCFAState;
import hu.bme.mit.inf.ttmc.analysis.tcfa.TCFATargetPredicate;
import hu.bme.mit.inf.ttmc.analysis.tcfa.TCFATrans;
import hu.bme.mit.inf.ttmc.analysis.tcfa.TCFATransferFunction;
import hu.bme.mit.inf.ttmc.core.type.IntType;
import hu.bme.mit.inf.ttmc.formalism.common.decl.VarDecl;
import hu.bme.mit.inf.ttmc.formalism.tcfa.TCFALoc;
import hu.bme.mit.inf.ttmc.formalism.tcfa.instances.FischerTCFA;
import hu.bme.mit.inf.ttmc.solver.Solver;
import hu.bme.mit.inf.ttmc.solver.SolverManager;
import hu.bme.mit.inf.ttmc.solver.z3.Z3SolverManager;

public class TCFAExplTests {

	@Test
	public void test() {
		final VarDecl<IntType> vlock = Var("lock", Int());
		final FischerTCFA fischer = new FischerTCFA(1, 1, 2, vlock);

		final TCFAAnalysisContext context = new TCFAAnalysisContext(fischer.getInitial(), fischer.getCritical());

		final SolverManager manager = new Z3SolverManager();
		final Solver solver = manager.createSolver(true, true);

		final TCFADomain<ExplState> domain = new TCFADomain<>(ExplDomain.create());
		final TCFAInitFunction<ExplState, ExplPrecision> initFunction = new TCFAInitFunction<>(
				new TCFAExplInitFunction());
		final TCFATransferFunction<ExplState, ExplPrecision> transferFunction = new TCFATransferFunction<>(
				new TCFAExplTransferFunction(solver));
		final TCFATargetPredicate targetPredicate = new TCFATargetPredicate();

		final ExplPrecision precision = GlobalExplPrecision.create(Collections.singleton(vlock),
				Collections.emptySet());

		final Abstractor<TCFAState<ExplState>, ExplPrecision, Void, Void, TCFALoc, TCFATrans, TCFALoc> abstractor = new Abstractor<>(
				context, NullLabeling.getInstance(), domain, initFunction, transferFunction, targetPredicate);

		abstractor.init(precision);
		abstractor.check(precision);

		System.out.println(ArgPrinter.toGraphvizString(abstractor.getARG()));

	}

}