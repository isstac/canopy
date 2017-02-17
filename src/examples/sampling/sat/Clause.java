package sampling.sat;

import java.util.*;

/**
 * Based on the implementation found here:
 * http://giudoku.sourceforge.net/codes.html
 */
public class Clause
{
	private int   m_total;
  private int   m_size;
	private List<Integer> m_literals;
	
	public Clause(int n)
	{
		m_total = n;
    m_size = 0;
    m_literals = new LinkedList<Integer>();
	}
	
	public boolean clausolaVuota()
	{
		return (m_size == 0);
	}
	
	public boolean clausolaUnaria()
	{
		return (m_size == 1);
	}
	
	public int getLetterale() throws IndexOutOfBoundsException
	{
		if (clausolaVuota())
		{
			throw new IndexOutOfBoundsException("Empty clause");
		}
		return m_literals.get(0);
	}
	
	public void addLiteral(int i) throws IndexOutOfBoundsException
	{
		if (isIndexWrong(i) || m_size == m_total || m_literals.indexOf(i) != -1)
		{
			throw new IndexOutOfBoundsException("Empty clause or invalid literal or literal already present");
		}
		
		m_literals.add((Integer)i);
    m_size++;
	}
	
	public void rmvLetterale(int i) throws IndexOutOfBoundsException
	{
		if (isIndexWrong(i) || clausolaVuota())
		{
			throw new IndexOutOfBoundsException("Empty clause or invalid literal");
		}
    
    m_literals.remove((Integer)i);
		m_size--;
	}
	
	public boolean hasLetterale(int i) throws IndexOutOfBoundsException
	{
		if (isIndexWrong(i))
		{
			throw new IndexOutOfBoundsException("Invalid literal");
		}
		
		for (int j = 0; j < m_size; j++)
		{
			if (m_literals.get(j) == i)
			{
				return true;
			}
		}
		
		return false;
	}
	
	public String toString()
	{
		String s = "(";
		
		for (int j = 0; j < m_size; j++)
		{
			s += m_literals.get(j);
			
			if (j != m_size - 1)
			{
				s += ", ";
			}
		}
		
		s += ")";
		return s;
	}
	
	private boolean isIndexWrong(int i)
	{
		return (i == 0) || (i > m_total) || (i < -m_total);
	}
	
}
