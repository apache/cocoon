/*
 *  Copyright (C) Chaperon. All rights reserved.
 *  -------------------------------------------------------------------------
 *  This software is published under the terms of the Apache Software License
 *  version 1.1, a copy of which has been included  with this distribution in
 *  the LICENSE file.
 */

package net.sourceforge.chaperon.process;

import net.sourceforge.chaperon.common.Decoder;
import net.sourceforge.chaperon.common.IntegerList;

/**
 * Processor for pattern, which try for matching against a pattern.
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: PatternProcessor.java,v 1.1 2003/03/09 00:02:58 pier Exp $
 */
public class PatternProcessor
{
	private PatternAutomaton automaton;

	private IntegerList states = new IntegerList();
	private IntegerList statekeys = new IntegerList();

	private IntegerList newstates = new IntegerList();
	private IntegerList newstatekeys = new IntegerList();

	// for storing the traversed states
	private IntegerList historystates = new IntegerList();
	private IntegerList dependencies = new IntegerList();

	// for storing the group, which were found
	private String[] groups = new String[10];
	private int[] groupstarts = new int[10];
	private int[] groupends = new int[10];
	private int groupcount = 0;

	/**
	 * Create a new pattern processor.
	 */
	public PatternProcessor()
	{
	}

	/**
	 * Create a new pattern processor.
	 *
	 * @param automaton Automaton, which the processor should use.
	 */
	public PatternProcessor(PatternAutomaton automaton)
	{
		setPatternAutomaton(automaton);
	}

	/**
	 * Set the pattern automaton.
	 *
	 * @param automaton Automaton, which the processor should use. 
	 */
	public void setPatternAutomaton(PatternAutomaton automaton)
	{
		if (automaton==null)
			throw new NullPointerException();

		this.automaton = automaton;
		if (groupstarts.length<=(automaton.getGroupCount()))
		{
			groups = new String[automaton.getGroupCount()+1];
			groupstarts = new int[automaton.getGroupCount()+1];
			groupends = new int[automaton.getGroupCount()+1];
		}
	}

	/**
	 * Search a postion, where the processor is successful.
	 *
	 * @param text Text.
	 *
	 * @return Next first position where the parser is successfull
   *         otherwise -1.
	 */
	public boolean search(char[] text)
	{
		return search(text, 0);
	}

	/**
	 * Search a postion, where the processor is successful.
	 *
	 * @param text Text.
	 * @param start Start position within the text. 
	 *
	 * @return Next first position where the parser is successfull
   *         otherwise -1.
	 */
	public boolean search(char[] text, int start)
	{
		for (int position = start; position<=text.length; position++)
			if (match(text, position))
				return true;

		return false;
	}

  private boolean verbose = false;

  public void setVerbose(boolean verbose)
  {
    this.verbose = verbose;
  }

	/**
	 * Matches for pattern.
	 *
	 * @param text Text.
	 *
	 * @return True, if the processor matches successfully.
	 */
	public boolean match(char[] text)
	{
		return match(text, 0);
	}

	/**
	 * Matches for pattern at specified position within the text.
	 *
	 * @param text Text
	 * @param start Position within the text.  
	 *
	 * @return True, if the processor matches successfully.
	 */
	public boolean match(char[] text, int start)
	{
		if (automaton==null)
			throw new NullPointerException("PatternAutomaton is null");
		if (automaton.getStateCount()<=0)
			return false;

		for (int i = 0; i<=automaton.getGroupCount(); i++)
		{
			groupstarts[i] = start;
			groupends[i] = start;
			groups[i] = null;
		}

		int position = start;
		int state, key;
		boolean found = false;
		int foundkey = 0;
    int foundPosition = start;

		states.clear();
		states.push(automaton.getFirstState());

		newstates.clear();

		statekeys.clear();
		statekeys.push(0);

		historystates.clear();
		historystates.push(automaton.getFirstState());

		dependencies.clear();
		dependencies.push(0);

		while (( !states.isEmpty()) && (position<=text.length))
		{
			state = states.pop();
			key = statekeys.pop();

			if (automaton.isFinalState(state))
			{
				found = true;
				foundkey = key;
        foundPosition = position;
			}
			else
			{
				switch (automaton.getType(state))
				{
				case PatternAutomaton.TYPE_NOMATCH :
					pushState(states, statekeys, state, key);
					break;

				case PatternAutomaton.TYPE_MATCH :
					if ((position<text.length) &&
							(automaton.getIntervalBegin(state)<=text[position]) &&
							(automaton.getIntervalEnd(state)>=text[position]))
						pushState(newstates, newstatekeys, state, key);
					break;

				case PatternAutomaton.TYPE_EXMATCH :
					if ((position<text.length) &&
							((automaton.getIntervalBegin(state)>text[position]) ||
							 (automaton.getIntervalEnd(state)<text[position])))
						pushState(states, statekeys, state, key);
					break;

				case PatternAutomaton.TYPE_MATCHANY :
					pushState(newstates, newstatekeys, state, key);
					break;

				case PatternAutomaton.TYPE_BOL :
					if (position==0)
						pushState(states, statekeys, state, key);
					else if ((position==1) &&
									 (((text[position-1]=='\n') && (text[position]!='\r')) ||
										(text[position-1]=='\r')))
						pushState(states, statekeys, state, key);
					else if ((text[position-1]=='\r') ||
									 ((text[position-1]=='\n') && (text[position]!='\r')))
						pushState(states, statekeys, state, key);
					break;

				case PatternAutomaton.TYPE_EOL :
					if (position>=text.length)
						pushState(states, statekeys, state, key);
					else if (((position+1)==text.length) &&
									 ((text[position]=='\r') || (text[position]=='\n')))
						pushState(states, statekeys, state, key);
					else if ((text[position]=='\r') || (text[position]=='\n'))
						pushState(states, statekeys, state, key);
					break;

				case PatternAutomaton.TYPE_GROUPSTART :
					pushState(states, statekeys, state, key);
					break;

				case PatternAutomaton.TYPE_GROUPEND :
					pushState(states, statekeys, state, key);
					break;
				}
			}

			if (states.isEmpty())
			{
				IntegerList temp = newstates;

				newstates = states;
				states = temp;

				temp = newstatekeys;
				newstatekeys = statekeys;
				statekeys = temp;

				position++; // next character
			}
		}

		position = foundPosition;
		key = foundkey;
		while (key!=0)
		{
			key = dependencies.get(key);
			state = historystates.get(key);

			switch (automaton.getType(state))
			{
			case PatternAutomaton.TYPE_NOMATCH :
				break;

			case PatternAutomaton.TYPE_MATCH :
				position--;
				break;

			case PatternAutomaton.TYPE_EXMATCH :
				break;

			case PatternAutomaton.TYPE_MATCHANY :
				position--;
				break;

			case PatternAutomaton.TYPE_BOL :
				break;

			case PatternAutomaton.TYPE_EOL :
				break;

			case PatternAutomaton.TYPE_GROUPSTART :
				if (groups[automaton.getGroupIndex(state)]==null)
				{
					groupstarts[automaton.getGroupIndex(state)] = position;
					groups[automaton.getGroupIndex(state)] = new String(text, position,
							groupends[automaton.getGroupIndex(state)]-position);

				}
				break;

			case PatternAutomaton.TYPE_GROUPEND :
				if (groups[automaton.getGroupIndex(state)]==null)
					groupends[automaton.getGroupIndex(state)] = position;
				break;
			}
		}

		groupcount = automaton.getGroupCount();
		for (int i = groupcount-1; (i>=0) && (groups[i]==null); i--)
			groupcount--;

		return found;
	}

	/**
	 * Push a state into the history.
	 *
	 * @param states The traversed states.
	 * @param keys Keys of the traversed paths.
	 * @param state Current state.
	 * @param key Current key.
	 */
	private void pushState(IntegerList states, IntegerList keys, int state,
												 int key)
	{
		states.push(automaton.getTransitions(state));
		historystates.push(automaton.getTransitions(state));
		for (int i = automaton.getTransitions(state).length; i>0; i--)
		{
			keys.push(dependencies.getCount());
			dependencies.push(key);
		}
	}

	/**
	 * Return the text, which in last match was found.
	 *
	 * @return Text.
	 */
	public String getGroup()
	{
		return groups[0];
	}

	/**
	 * Return the text of the specifed group, which in 
   * last match was found.
	 *
	 * @param group Index of group;
	 *
	 * @return Text
	 */
	public String getGroup(int group)
	{
		return groups[group];
	}

	/**
	 * Return count of groups.
	 *
	 * @return Count of groups.
	 */
	public int getGroupCount()
	{
		return groupcount;
	}

	/**
	 * Return the start position of the last match.
	 *
	 * @return Start position.
	 */
	public int getGroupStart()
	{
		return groupstarts[0];
	}

	/**
	 * Return the start position of a group 
   * from the last match.
	 *
	 * @param group Index of group.
	 *
	 * @return Start position.
	 */
	public int getGroupStart(int group)
	{
		return groupstarts[group];
	}

	/**
	 * Return the end position of the last match.
	 *
	 * @return End position.
	 */
	public int getGroupEnd()
	{
		return groupends[0];
	}

	/**
	 * Return the end position of a group 
   * from the last match.
   *
   * @param group Index of group.
   *
   * @return End position.
	 */
	public int getGroupEnd(int group)
	{
		return groupends[group];
	}
}
