/*
 *  Copyright (C) Chaperon. All rights reserved.
 *  -------------------------------------------------------------------------
 *  This software is published under the terms of the Apache Software License
 *  version 1.1, a copy of which has been included  with this distribution in
 *  the LICENSE file.
 */

package net.sourceforge.chaperon.model.grammar;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

import net.sourceforge.chaperon.common.IntegerList;
import net.sourceforge.chaperon.model.Violations;
import net.sourceforge.chaperon.model.symbol.Symbol;
import net.sourceforge.chaperon.model.symbol.SymbolList;
import net.sourceforge.chaperon.model.symbol.SymbolSet;
import net.sourceforge.chaperon.model.symbol.Nonterminal;
import net.sourceforge.chaperon.model.symbol.Terminal;

/**
 * This class represents a model for a grammar. The content of
 * the grammar includes the productions, start symbol, associativities
 * and priorities.
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels </a>
 * @version CVS $Id: Grammar.java,v 1.1 2003/03/09 00:02:58 pier Exp $
 */
public class Grammar implements Serializable, Cloneable
{
	// Start symbol
	private Nonterminal startsymbol = null;

	// Productions
	private Vector productions = new Vector();

	private Hashtable priorities = new Hashtable();
	private Hashtable associativities = new Hashtable();

	private String location = null;

	/**
	 * Creates an empty grammar.
	 */
	public Grammar()
	{
	}

	/**
	 * Add a production to this grammar.
	 *
	 * @param production Production, which should be added.
	 *
	 * @return Index of the production in this grammar.
	 */
	public int addProduction(Production production)
	{
		if (production==null)
			throw new NullPointerException();

		productions.addElement(production);
		return productions.size()-1;
	}

	/**
	 * Add a list of productions to this grammar.
	 *
	 * @param list Array of productions.
	 */
	public void addProduction(Production[] list)
	{
		for (int i = 0; i<list.length; i++)
		{
			try
			{
				addProduction((Production) list[i].clone());
			}
			catch (CloneNotSupportedException cnse)
			{
				throw new IllegalArgumentException("Could not clone token:"+
																					 cnse.getMessage());
			}
		}
	}

	/**
	 * Removes a production by an index from this grammar.
	 *
	 * @param index Index of the production, which should be removed.
	 */
	public void removeProduction(int index)
	{
		productions.removeElementAt(index);
	}

	/**
	 * Replace a production by an index in this grammar.
	 *
	 * @param index The index, at which the productionshould be replaced.
	 * @param production The production.
	 */
	public void setProduction(int index, Production production)
	{
		if ((index<0) || (index>productions.size()))
			throw new IndexOutOfBoundsException();
		productions.setElementAt(production, index);
	}

	/**
	 * Return a production giving by an index.
	 *
	 * @param index Index of the Production.
	 *
	 * @return The production.
	 */
	public Production getProduction(int index)
	{
		if ((index<0) || (index>productions.size()))
			throw new IndexOutOfBoundsException();

		return (Production) productions.elementAt(index);
	}

	/**
	 * Returns all production for given nonterminal symbol as
   * a list of indices.
	 *
	 * @param ntsymbol Nonterminal symbol
	 *
	 * @return List of indices from the productions
	 */
	public IntegerList getProductionList(Symbol ntsymbol)
	{
		IntegerList list = new IntegerList();
		int i;

		for (i = 0; i<getProductionCount(); i++)
		{
			if (getProduction(i).getSymbol().equals(ntsymbol))
				list.add(i);
		}
		return list;
	}

	/**
	 * Returns the count of productions in this grammar.
	 *
	 * @return Count of productions.
	 */
	public int getProductionCount()
	{
		return productions.size();
	}

	/**
	 * Return the index of a production.
	 *
	 * @param production The production.
	 *
	 * @return Index of the Production.
	 */
	public int indexOf(Production production)
	{
		for (int i = 0; i<productions.size(); i++)
			if (((Production) productions.elementAt(i)).equals(production))
				return i;
		return -1;
	}

	/**
	 * Return the index of the next production, which found
   * by a nonterminal symbol.
	 *
	 * @param ntsymbol Nonterminal symbol
	 *
	 * @return Index of the production.
	 */
	public int indexOf(Symbol ntsymbol)
	{
		for (int i = 0; i<productions.size(); i++)
			if (((Production) productions.elementAt(i)).getSymbol().equals(ntsymbol))
				return i;
		return -1;
	}

	/**
	 * If the grammar contains a production.
	 *
	 * @param production The production, which the grammar should contain.
	 *
	 * @return True, if the grammar contains the production.
	 */
	public boolean contains(Production production)
	{
		return (indexOf(production)!=-1);
	}

	/**
	 * If the grammar contains a production with this nonterminal symbol.
	 *
	 * @param ntsymbol Nonterminal symbol.
	 *
	 * @return True, if the grammar contains a production with the symbol.
	 */
	public boolean contains(Symbol ntsymbol)
	{
		return (indexOf(ntsymbol)!=-1);
	}

	/**
	 * Removes all productions from this grammar.
	 */
	public void removeAllProduction()
	{
		productions.removeAllElements();
	}

	/**
	 * Returns the production, which this grammar contains, as an array.
	 *
	 * @return Array if productions.
	 */
	public Production[] getProduction()
	{
		int size = productions.size();
		Production[] mArray = new Production[size];

		for (int index = 0; index<size; index++)
			mArray[index] = (Production) productions.elementAt(index);
		return mArray;
	}

	/**
	 * Replace the productions of this grammar by an array of productions.
	 *
	 * @param productionArray Array of productions.
	 */
	public void setProduction(Production[] productionArray)
	{
		productions.removeAllElements();
		for (int i = 0; i<productionArray.length; i++)
			productions.addElement(productionArray[i]);
	}

	/**
	 * Set a priority of a terminal symbol.
	 *
	 * @param terminal Terminal symbol.
	 * @param priority Priority of the symbol.
	 */
	public void setPriority(Terminal terminal, int priority)
	{
		if (terminal==null)
			throw new NullPointerException();

		priorities.put(terminal, new Integer(priority));
	}

	/**
	 * Returns the priority of a terminal symbol.
	 *
	 * @param terminal Terminal symbol.
	 *
	 * @return Priority of the symbol.
	 */
	public int getPriority(Terminal terminal)
	{
		Integer priority = (Integer) priorities.get(terminal);

		if (priority==null)
			return 0;
		return priority.intValue();
	}

	/**
	 * Return the priority of a production in this grammar.
	 *
	 * @param production Production.
	 *
	 * @return Priority of the production.
	 */
	public int getPriority(Production production)
	{
		if ( !contains(production))
			return 0;

		if (production.getPrecedence()!=null)
			return getPriority(production.getPrecedence());

		SymbolList definition = production.getDefinition();

		for (int i = definition.getSymbolCount()-1; i>=0; i--)
			if (definition.getSymbol(i) instanceof Terminal)
			{
				int priority = getPriority((Terminal) definition.getSymbol(i));

				return priority;
			}

		return getProductionCount()-indexOf(production);
	}

	/**
	 * Set the associativity of a terminal symbol.
	 *
	 * @param terminal Terminal symbol.
	 * @param assoc Associativity of the symbol.
	 */
	public void setAssociativity(Terminal terminal, Associativity assoc)
	{
		if (terminal==null)
			throw new NullPointerException();

		associativities.put(terminal, assoc);
	}

	/**
	 * Return the associativity of a terminal symbol.
	 *
	 * @param terminal Terminal symbol.
	 *
	 * @return Associativity of the symbol.
	 */
	public Associativity getAssociativity(Terminal terminal)
	{
		Associativity assoc = (Associativity) associativities.get(terminal);

		if (assoc==null)
			return Associativity.NONASSOC;
		return assoc;
	}

	/**
	 * Return the associativity of a production in this grammar.
	 *
	 * @param production Production.
	 *
	 * @return Associativity of the production.
	 */
	public Associativity getAssociativity(Production production)
	{
		if (!contains(production))
			return Associativity.NONASSOC;

		if (production.getPrecedence()!=null)
			return getAssociativity(production.getPrecedence());

		SymbolList definition = production.getDefinition();

		for (int i = definition.getSymbolCount()-1; i>=0; i--)
			if (definition.getSymbol(i) instanceof Terminal)
				return getAssociativity((Terminal) definition.getSymbol(i));

		return Associativity.NONASSOC;
	}

	/**
	 * Return all used symbol in this grammar.
	 *
	 * @return Set of symbols, which were used.
	 */
	public SymbolSet getSymbols()
	{
		SymbolSet set = new SymbolSet();

		for (int i = 0; i<getProductionCount(); i++)
			set.addSymbol(getProduction(i).getSymbols());
		return set;
	}

	/**
	 * Set the start symbol for this grammar.
	 *
	 * @param startsymbol Start symbol.
	 */
	public void setStartSymbol(Nonterminal startsymbol)
	{
		this.startsymbol = startsymbol;
	}

	/**
	 * Return the start symbol.
	 *
	 * @return Start symbol.
	 */
	public Nonterminal getStartSymbol()
	{
		return startsymbol;
	}

	/**
	 * Set the location from the input source.
	 *
	 * @param location Location in the input source.
	 */
	public void setLocation(String location)
	{
		this.location = location;
	}

	/**
	 * Returns the location from the input source.
	 *
	 * @return Location in the input source.
	 */
	public String getLocation()
	{
		return location;
	}

	/**
	 * Validated the grammar.
	 *
	 * @return Return a list of violations, if this
   *         object isn't valid.
	 */
	public Violations validate()
	{
		Violations violations = new Violations();

		if (startsymbol==null)
			violations.addViolation("Start symbol is not defined", location);
		else if ( !contains(startsymbol))
			violations.addViolation("Start symbol \""+startsymbol+"\""+
															"is not defined through a production", location);

		if (getProductionCount()<=0)
			violations.addViolation("No productions are defined", location);

		for (Enumeration e = productions.elements(); e.hasMoreElements(); )
			violations.addViolations(((Production) e.nextElement()).validate());

		SymbolSet ntsymbols = getSymbols().getNonterminals();

		for (int i = 0; i<ntsymbols.getSymbolCount(); i++)
			if ( !contains(ntsymbols.getSymbol(i)))
				violations.addViolation("Nonterminal symbol \""+
																ntsymbols.getSymbol(i)+"\""+
																"is not defined through a production", location);

		return violations;
	}

	/**
	 * Return a string representation of the grammar.
	 *
	 * @return String representation.
	 */
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();

		buffer.append("Terminal symbols:\n");
		SymbolSet tsymbols = getSymbols().getTerminals();

		for (int i = 0; i<tsymbols.getSymbolCount(); i++)
		{
			buffer.append(String.valueOf(i));
			buffer.append(".Terminal: ");
			buffer.append(tsymbols.getSymbol(i));
			buffer.append(" Priority=");
			buffer.append(String.valueOf(getPriority((Terminal) tsymbols.getSymbol(i))));
			buffer.append(" Associativity=");
			buffer.append(String.valueOf(getAssociativity((Terminal) tsymbols.getSymbol(i))));
			buffer.append("\n");
		}

		buffer.append("Produktions:\n");
		for (int i = 0; i<getProductionCount(); i++)
		{
			buffer.append(String.valueOf(i));
			buffer.append(".Production: ");
			buffer.append(getProduction(i).toString());
			buffer.append("\n");
		}

		buffer.append("\n");

		return buffer.toString();
	}

	/**
	 * Creates a clone of this grammar.
	 *
	 * @return Clone of this grammar.
	 *
	 * @throws CloneNotSupportedException If an exception occurs during the
   *                                    cloning.
	 */
	public Object clone() throws CloneNotSupportedException
	{
		Grammar clone = new Grammar();

		clone.startsymbol = startsymbol;
		for (int i = 0; i<productions.size(); i++)
			clone.addProduction((Production) ((Production) productions.elementAt(i)).clone());

		clone.location = location;

		return clone;
	}
}
