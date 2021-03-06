/*
Stratagem is a model checker for transition systems described using rewriting
rules and strategies.
Copyright (C) 2013 - SMV@Geneva University.
Program written by Edmundo Lopez Bobeda <edmundo [at] lopezbobeda.net>.
This program is free software; you can redistribute it and/or modify
it under the  terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package ch.unige.cui.smv.stratagem.ts

import org.scalatest.FlatSpec
import ch.unige.cui.smv.metamodel.ts.Strategy
import ch.unige.cui.smv.stratagem.adt.ATermHelper.term2RichTerm
import ch.unige.cui.smv.stratagem.util.StrategyDSL.Choice
import ch.unige.cui.smv.stratagem.util.StrategyDSL.DeclaredStrategyInstance
import ch.unige.cui.smv.stratagem.util.StrategyDSL.Identity
import ch.unige.cui.smv.stratagem.util.StrategyDSL.One
import ch.unige.cui.smv.stratagem.util.StrategyDSL.Sequence
import ch.unige.cui.smv.stratagem.util.StrategyDSL.TransitionSystem
import ch.unige.cui.smv.stratagem.util.StrategyDSL.Union
import ch.unige.cui.smv.stratagem.util.StrategyDSL.VariableStrategy
import ch.unige.cui.smv.stratagem.util.StrategyDSL.transitionSystem2RichTransitionSystem
import ch.unige.smv.cui.metamodel.adt.ATerm
import ch.unige.smv.cui.metamodel.adt.AdtFactory
import ch.unige.cui.smv.stratagem.modelchecker.Main
import ch.unige.cui.smv.stratagem.util.AuxFunctions
import ch.unige.cui.smv.stratagem.util.IllegalTransitionSystemException
import org.scalatest.BeforeAndAfter
import ch.unige.smv.cui.metamodel.adt.AdtPackage
import ch.unige.cui.smv.metamodel.ts.TsPackage
import org.eclipse.ocl.examples.pivot.OCL
import org.eclipse.ocl.examples.xtext.oclinecore.OCLinEcoreStandaloneSetup
import org.eclipse.ocl.examples.xtext.oclstdlib.OCLstdlibStandaloneSetup
import ch.unige.cui.smv.stratagem.xtext.TransitionSystemDslStandaloneSetup
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.common.util.URI

// scalastyle:off number.of.methods
class TransitionSystemTest extends FlatSpec with BeforeAndAfter {

  before {
    AdtPackage.eINSTANCE.eClass()
    TsPackage.eINSTANCE.eClass()

    val injector = (new TransitionSystemDslStandaloneSetup()).createInjectorAndDoEMFRegistration();
    OCL.initialize(null);
    org.eclipse.ocl.examples.pivot.model.OCLstdlib.install();
    org.eclipse.ocl.examples.pivot.delegate.OCLDelegateDomain.initialize(null)
    OCLinEcoreStandaloneSetup.doSetup()
    OCLstdlibStandaloneSetup.doSetup()
  }

  // scalastyle:on
  "A transition system" should "allow to declare strategies" in {
    val signature = AdtFactory.eINSTANCE.createSignature()
      .withSort("ph")
      .withSort("state")
      .withSort("fork")
      .withGenerator("eating", "state")
      .withGenerator("thinking", "state")
      .withGenerator("waiting", "state")
      .withGenerator("waitingForLeftFork", "state")
      .withGenerator("waitingForRightFork", "state")
      .withGenerator("forkUsed", "fork")
      .withGenerator("forkFree", "fork")
      .withGenerator("emptytable", "ph")
      .withGenerator("philo", "ph", "state", "fork", "ph")

    val adt = { val a = AdtFactory.eINSTANCE.createADT(); a.setName("philoModel"); a.setSignature(signature); a }
      .declareVariable("x", "fork")
      .declareVariable("p", "ph")
      .declareVariable("s", "state")
      .declareVariable("f", "fork")
    // definitions to simplify the reading of terms.
    def eating = adt.term("eating")
    def thinking = adt.term("thinking")
    def waiting = adt.term("waiting")
    def waitingForLeftFork = adt.term("waitingForLeftFork")
    def waitingForRightFork = adt.term("waitingForRightFork")
    def forkUsed = adt.term("forkUsed")
    def forkFree = adt.term("forkFree")
    def emptytable = adt.term("emptytable")
    def X = adt.term("x")
    def P = adt.term("p")
    def S = adt.term("s")
    def F = adt.term("f")
    def philo(state: ATerm, fork: ATerm, ph: ATerm) = adt.term("philo", state, fork, ph)

    val ts = (TransitionSystem(adt, philo(thinking, forkFree, philo(thinking, forkFree, philo(thinking, forkFree, emptytable)))))
      .declareStrategy("goToWaitPhilo", philo(thinking, X, P) -> philo(waiting, X, P))(false)
      .declareStrategy("takeRightForkFromWaitingPhilo", philo(waiting, forkFree, P) -> philo(waitingForLeftFork, forkUsed, P))(false)
      .declareStrategy("takeRightForkFromWaitingForRightForkPhilo", philo(waitingForRightFork, forkFree, P) -> philo(eating, forkUsed, P))(false)
      .declareStrategy("takeLeftForkFromWaitingPhilo", philo(S, forkFree, philo(waiting, F, P)) -> philo(S, forkUsed, philo(waitingForRightFork, F, P)))(false)
      .declareStrategy("takeLeftForkFromWaitingForLeftForkPhilo", philo(S, forkFree, philo(waitingForLeftFork, forkUsed, P)) -> philo(S, forkUsed, philo(eating, forkUsed, P)))(false)
      .declareStrategy("goToThinkPhilo", philo(S, forkUsed, philo(eating, forkUsed, P)) -> philo(S, forkFree, philo(S, forkFree, P)))(false)

  }

  "A transition system" should "allow to declare the strategies for the philosopher's model" in {
    val signature = AdtFactory.eINSTANCE.createSignature()
      .withSort("ph")
      .withSort("state")
      .withSort("fork")
      .withGenerator("eating", "state")
      .withGenerator("thinking", "state")
      .withGenerator("waiting", "state")
      .withGenerator("waitingForLeftFork", "state")
      .withGenerator("waitingForRightFork", "state")
      .withGenerator("forkUsed", "fork")
      .withGenerator("forkFree", "fork")
      .withGenerator("emptytable", "ph")
      .withGenerator("philo", "ph", "state", "fork", "ph")

    val adt = { val a = AdtFactory.eINSTANCE.createADT(); a.setName("philoModel"); a.setSignature(signature); a }
      .declareVariable("x", "fork")
      .declareVariable("p", "ph")
      .declareVariable("s", "state")
      .declareVariable("f", "fork")
    // definitions to simplify the reading of terms.
    def eating = adt.term("eating")
    def thinking = adt.term("thinking")
    def waiting = adt.term("waiting")
    def waitingForLeftFork = adt.term("waitingForLeftFork")
    def waitingForRightFork = adt.term("waitingForRightFork")
    def forkUsed = adt.term("forkUsed")
    def forkFree = adt.term("forkFree")
    def emptytable = adt.term("emptytable")
    def X = adt.term("x")
    def P = adt.term("p")
    def S = adt.term("s")
    def F = adt.term("f")
    def philo(state: ATerm, fork: ATerm, ph: ATerm) = adt.term("philo", state, fork, ph)

    def S1 = VariableStrategy("S1")
    def S2 = VariableStrategy("S2")

    def Try(s: Strategy) = DeclaredStrategyInstance("try", s)
    def Repeat(s: Strategy) = DeclaredStrategyInstance("repeat", s)
    def OnceBottomUp(s: Strategy) = DeclaredStrategyInstance("onceBottomUp", s)
    def DoForAllPhil(s: Strategy) = DeclaredStrategyInstance("doForAllPhil", s)
    def DoForLastPhil(s: Strategy) = DeclaredStrategyInstance("doForLastPhil", s)

    var ts = (TransitionSystem(adt, philo(thinking, forkFree, philo(thinking, forkFree, philo(thinking, forkFree, emptytable)))))
      .declareStrategy("goToWaitPhilo", philo(thinking, X, P) -> philo(waiting, X, P))(false)
      .declareStrategy("takeRightForkFromWaitingPhilo", philo(waiting, forkFree, P) -> philo(waitingForLeftFork, forkUsed, P))(false)
      .declareStrategy("takeRightForkFromWaitingForRightForkPhilo", philo(waitingForRightFork, forkFree, P) -> philo(eating, forkUsed, P))(false)
      .declareStrategy("takeLeftForkFromWaitingPhilo", philo(S, forkFree, philo(waiting, F, P)) -> philo(S, forkUsed, philo(waitingForRightFork, F, P)))(false)
      .declareStrategy("takeLeftForkFromWaitingForLeftForkPhilo", philo(S, forkFree, philo(waitingForLeftFork, forkUsed, P)) -> philo(S, forkUsed, philo(eating, forkUsed, P)))(false)
      .declareStrategy("goToThinkPhilo", philo(S, forkUsed, philo(eating, forkUsed, P)) -> philo(S, forkFree, philo(S, forkFree, P)))(false)
    ts = ts
      .declareStrategy("try", S1) { Choice(S1, Identity) }(false)
      .declareStrategy("repeat", S1) { Try(Sequence(S1, Repeat(S1))) }(false)
      .declareStrategy("onceBottomUp", S1) { Choice(One(OnceBottomUp(S1)), S1) }(false)
      .declareStrategy("doForAllPhil", S1) { Union(Try(S1), Choice(One(DoForAllPhil(S1)), Identity)) }(false)
      .declareStrategy("doForLastPhil", S1) { Choice(One(DoForLastPhil(S1)), Identity) }(false)
      .declareStrategy("goToWait") { DoForAllPhil(DeclaredStrategyInstance("goToWaitPhilo")) }(true)
      .declareStrategy("takeRightForkFromWaiting") { DoForAllPhil(DeclaredStrategyInstance("takeRightForkFromWaitingPhilo")) }(true)
      .declareStrategy("takeRightForkFromWaitingForRightFork") { DoForAllPhil(DeclaredStrategyInstance("takeRightForkFromWaitingForRightForkPhilo")) }(true)
      .declareStrategy("takeLeftForkFromWaiting") { DoForAllPhil(DeclaredStrategyInstance("takeLeftForkFromWaitingPhilo")) }(true)
      .declareStrategy("takeLeftForkFromWaitingForLeftFork") { DoForAllPhil(DeclaredStrategyInstance("takeLeftForkFromWaitingForLeftForkPhilo")) }(true)
      .declareStrategy("goToThink") { DoForAllPhil(DeclaredStrategyInstance("goToThinkPhilo")) }(true)
  }

  "A transition system" should "not allow to use a strategy that has not been declared" in {
    val signature = AdtFactory.eINSTANCE.createSignature()
      .withSort("ph")
      .withGenerator("p0", "ph")

    val adt = { val a = AdtFactory.eINSTANCE.createADT(); a.setName("philoModel"); a.setSignature(signature); a }

    def S1 = VariableStrategy("S1")

    def Try(s: Strategy) = DeclaredStrategyInstance("try", s)
    val ts = TransitionSystem(adt, adt.term("p0"))
      .declareStrategy("newStrategy", S1) { Try(S1) } { false }

    val e = intercept[IllegalTransitionSystemException] {
      AuxFunctions.doLinking(ts)
      AuxFunctions.doDiagnostics(ts)
    }
    assert(e.errors.head.startsWith("Usage of invalid strategy try in declared strategy newStrategy"))
  }

  "A transition system" should "not allow to use a strategy with the wrong number of parameters" in {
    val signature = AdtFactory.eINSTANCE.createSignature()
      .withSort("ph")
      .withGenerator("p0", "ph")

    val adt = { val a = AdtFactory.eINSTANCE.createADT(); a.setName("philoModel"); a.setSignature(signature); a }

    def S1 = VariableStrategy("S1")
    def S2 = VariableStrategy("S2")

    val ts = TransitionSystem(adt, adt.term("p0"))
      .declareStrategy("try", S1) { Identity }(false)
      .declareStrategy("newStrategy", S1) { DeclaredStrategyInstance("try", S1, S2) } { false }
      .declareStrategy("trans") { Identity } { true }

    val e = intercept[IllegalTransitionSystemException] {
      AuxFunctions.doLinking(ts)
      AuxFunctions.doDiagnostics(ts)
    }
    assert(e.errors.head.startsWith("Invalid number of parameters for strategy try. Required Set{1}, found Set{2}"))
  }

  "A transition system" should "not allow to define a strategy that uses a variable that is not exatly the same as that used in its definition." in {
    val signature = AdtFactory.eINSTANCE.createSignature()
      .withSort("ph")
      .withGenerator("p0", "ph")

    val adt = { val a = AdtFactory.eINSTANCE.createADT(); a.setName("philoModel"); a.setSignature(signature); a }

    def S1 = VariableStrategy("S1")
    def S2 = VariableStrategy("S2")

    val ts = TransitionSystem(adt, adt.term("p0"))
      .declareStrategy("try", S1) { Identity }(false)
      .declareStrategy("newStrategy", S1) { DeclaredStrategyInstance("try", S2) } { false }
      .declareStrategy("newStrategy2") { Identity } { true }

    // this is necessary to activate xtext checks (for some reason :-S)
    val resSet = new ResourceSetImpl()
    val resource = resSet.createResource(URI.createURI("temp.ts"))
    resource.getContents().add(ts)
    val e = intercept[IllegalTransitionSystemException] {
      AuxFunctions.doLinking(ts)
      AuxFunctions.doDiagnostics(ts)
    }
    println(e.errors)
    assert(e.errors.head.endsWith("Strategy variable name 'S2' is not in declaration. If you wanted to use a declared strategy you need to append parentheses to it, like this: S2()"))
  }

  "A transition system" should "not allow to declare twice a strategy with the same name" in {
    val signature = AdtFactory.eINSTANCE.createSignature()
      .withSort("ph")
      .withSort("state")
      .withSort("fork")
      .withGenerator("eating", "state")
      .withGenerator("thinking", "state")
      .withGenerator("waiting", "state")
      .withGenerator("waitingForLeftFork", "state")
      .withGenerator("waitingForRightFork", "state")
      .withGenerator("forkUsed", "fork")
      .withGenerator("forkFree", "fork")
      .withGenerator("emptytable", "ph")
      .withGenerator("philo", "ph", "state", "fork", "ph")

    val adt = { val a = AdtFactory.eINSTANCE.createADT(); a.setName("philoModel"); a.setSignature(signature); a }
      .declareVariable("x", "fork")
      .declareVariable("p", "ph")
      .declareVariable("s", "state")
      .declareVariable("f", "fork")
    // definitions to simplify the reading of terms.
    def eating = adt.term("eating")
    def thinking = adt.term("thinking")
    def waiting = adt.term("waiting")
    def waitingForLeftFork = adt.term("waitingForLeftFork")
    def waitingForRightFork = adt.term("waitingForRightFork")
    def forkUsed = adt.term("forkUsed")
    def forkFree = adt.term("forkFree")
    def emptytable = adt.term("emptytable")
    def X = adt.term("x")
    def P = adt.term("p")
    def S = adt.term("s")
    def F = adt.term("f")
    def philo(state: ATerm, fork: ATerm, ph: ATerm) = adt.term("philo", state, fork, ph)
    intercept[IllegalArgumentException] {
      val ts = (TransitionSystem(adt, philo(thinking, forkFree, philo(thinking, forkFree, philo(thinking, forkFree, emptytable))))
        .declareStrategy("goToWaitPhilo", philo(thinking, X, P) -> philo(waiting, X, P))(false)
        .declareStrategy("goToWaitPhilo", philo(waiting, forkFree, P) -> philo(waitingForLeftFork, forkUsed, P))(false))
    }
  }

  "A transition system" should "not allow an initial state which is not built using the transition system's adt" in {
    val signature = AdtFactory.eINSTANCE.createSignature()
      .withSort("ph")
      .withGenerator("p0", "ph")

    val adt1 = { val a = AdtFactory.eINSTANCE.createADT(); a.setName("philoModel"); a.setSignature(signature); a }
    val adt2 = { val a = AdtFactory.eINSTANCE.createADT(); a.setName("philoModel"); a.setSignature(signature); a }

    intercept[IllegalArgumentException] {
      val ts = (TransitionSystem(adt1, adt2.term("p0")))
    }
  }

}