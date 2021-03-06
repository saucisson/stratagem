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
package ch.unige.cui.smv.stratagem.util

import scala.collection.mutable.HashMap
import ch.unige.cui.smv.stratagem.ipf.IPFAbstractFactory
import scala.collection.mutable.WeakHashMap
/**
 * This class is designed to be used as a mixin. It implements an operation cache
 * for union, intersection and difference.
 * @author mundacho
 *
 */
trait OperationCache extends LatticeElement {

  type LatticeElementType <: LatticeElement { type LatticeElementType = OperationCache.this.LatticeElementType }

  lazy val unionOperationCache = new WeakHashMap[LightWeightWrapper[LatticeElementType], LatticeElementType]
  lazy val interOperationCache = new WeakHashMap[LightWeightWrapper[LatticeElementType], LatticeElementType]
  lazy val differenceOperationCache = new WeakHashMap[LightWeightWrapper[LatticeElementType], LatticeElementType]

  /**
   * We override the standard operation to perform a join with cache.
   * @param that the lattice element to join with.
   * @return the union of the this and that.
   */
  abstract override def v(that: LatticeElementType): LatticeElementType =
    if (that eq this) that
    else if (that eq this.bottomElement) this.asInstanceOf[LatticeElementType]
    else if (this eq this.bottomElement) that
    else if (this.hashCode < that.hashCode) {
      that v this.asInstanceOf[OperationCache.this.LatticeElementType]
    } else {
      unionOperationCache.getOrElseUpdate(that.wrapped, super.v(that))
    }

  /**
   * We override the standard operation to perform a meet with cache.
   * @param that the lattice element to intersect with.
   * @return the intersection of this and that.
   */
  abstract override def ^(that: LatticeElementType): LatticeElementType =
    if ((that eq that.bottomElement) || (this eq this.bottomElement)) this.bottomElement
    else if (that eq this) that
    else if (this.hashCode < that.hashCode) {
      that ^ this.asInstanceOf[OperationCache.this.LatticeElementType]
    } else {
      interOperationCache.getOrElseUpdate(that.wrapped, super.^(that))
    }

  /**
   * We override the standard operation to perform a difference with cache.
   * @param that is the lattice element to subtract.
   * @return the difference of this minus that.
   */
  abstract override def \(that: LatticeElementType): LatticeElementType = {
    if (that eq that.bottomElement) this.asInstanceOf[LatticeElementType] else
      differenceOperationCache.getOrElseUpdate(that.wrapped, super.\(that))
  }
}