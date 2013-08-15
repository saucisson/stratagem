package ch.unige.cui.smv.stratagem.ts

import org.scalatest.FlatSpec

import ch.unige.cui.smv.stratagem.adt.ADT
import ch.unige.cui.smv.stratagem.adt.ATerm
import ch.unige.cui.smv.stratagem.adt.Signature

class TransitionSystemTest extends FlatSpec {
  "A transition system" should "allow to declare strategies" in {
    val signature = (new Signature)
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

    val adt = new ADT("philoModel", signature)
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

    val ts = (new TransitionSystem(adt, philo(thinking, forkFree, philo(thinking, forkFree, philo(thinking, forkFree, emptytable)))))
      .declareStrategy("goToWaitPhilo", philo(thinking, X, P) -> philo(waiting, X, P))(false)
      .declareStrategy("takeRightForkFromWaitingPhilo", philo(waiting, forkFree, P) -> philo(waitingForLeftFork, forkUsed, P))(false)
      .declareStrategy("takeRightForkFromWaitingForRightForkPhilo", philo(waitingForRightFork, forkFree, P) -> philo(eating, forkUsed, P))(false)
      .declareStrategy("takeLeftForkFromWaitingPhilo", philo(S, forkFree, philo(waiting, F, P)) -> philo(S, forkUsed, philo(waitingForRightFork, F, P)))(false)
      .declareStrategy("takeLeftForkFromWaitingForLeftForkPhilo", philo(S, forkFree, philo(waitingForLeftFork, forkUsed, P)) -> philo(S, forkUsed, philo(eating, forkUsed, P)))(false)
      .declareStrategy("goToThinkPhilo", philo(S, forkUsed, philo(eating, forkUsed, P)) -> philo(S, forkFree, philo(S, forkFree, P)))(false)

  }

  "A transition system" should "allow to declare the strategies for the philosopher's model" in {
    val signature = (new Signature)
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

    val adt = new ADT("philoModel", signature)
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

    val S1 = VariableStrategy("S1")
    val S2 = VariableStrategy("S2")

    def Try(s: Strategy) = DeclaredStrategyInstance("try", s)
    def Repeat(s: Strategy) = DeclaredStrategyInstance("repeat", s)
    def OnceBottomUp(s: Strategy) = DeclaredStrategyInstance("onceBottomUp", s)
    def DoForAllPhil(s: Strategy) = DeclaredStrategyInstance("doForAllPhil", s)
    def DoForLastPhil(s: Strategy) = DeclaredStrategyInstance("doForLastPhil", s)

    var ts = (new TransitionSystem(adt, philo(thinking, forkFree, philo(thinking, forkFree, philo(thinking, forkFree, emptytable)))))
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
    val signature = (new Signature)
      .withSort("ph")
      .withGenerator("p0", "ph")

    val adt = new ADT("philoModel", signature)

    val S1 = VariableStrategy("S1")

    def Try(s: Strategy) = DeclaredStrategyInstance("try", s)
    val e = intercept[IllegalArgumentException] {
      val ts = new TransitionSystem(adt, adt.term("p0"))
        .declareStrategy("newStrategy", S1) { Try(S1) } { false }
    }
    assert(e.getMessage().endsWith(DeclaredStrategy.errorMessageStringNotDefined.format("try")))

  }

  "A transition system" should "not allow to declare twice a strategy with the same name" in {
    val signature = (new Signature())
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

    val adt = new ADT("philoModel", signature)
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
      val ts = (new TransitionSystem(adt, philo(thinking, forkFree, philo(thinking, forkFree, philo(thinking, forkFree, emptytable))))
        .declareStrategy("goToWaitPhilo", philo(thinking, X, P) -> philo(waiting, X, P))(false)
        .declareStrategy("goToWaitPhilo", philo(waiting, forkFree, P) -> philo(waitingForLeftFork, forkUsed, P))(false))
    }
  }

  "A transition system" should "not allow an initial state which is not built using the transition system's adt" in {
    val signature = (new Signature)
      .withSort("ph")
      .withGenerator("p0", "ph")

    val adt1 = new ADT("philoModel", signature)
    val adt2 = new ADT("philoModel", signature)

    intercept[IllegalArgumentException] {
      val ts = (new TransitionSystem(adt1, adt2.term("p0")))
    }
  }

}