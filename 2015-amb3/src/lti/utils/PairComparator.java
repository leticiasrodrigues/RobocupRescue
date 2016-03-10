package lti.utils;

import java.util.Comparator;
import java.util.List;

import rescuecore2.misc.Pair;

public class PairComparator implements Comparator<Pair<Integer,Integer>> {

	List<Pair<Integer, Integer>> base;
    public PairComparator(List<Pair<Integer, Integer>> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with equals.    
    @Override
    public int compare(Pair<Integer,Integer> a, Pair<Integer,Integer> b) {
        int result = b.second().compareTo(a.second());
        if (result == 0)
        	result = a.first().compareTo(b.first());
        
        return result;
    }

}