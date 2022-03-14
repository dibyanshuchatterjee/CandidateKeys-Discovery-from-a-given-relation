package edu.rit.ibd.a3;

import com.google.common.collect.Sets;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

public class CKDiscovery {
	
	public static void main(String[] args) throws Exception {
//		final String relation = " class (course, title, department, credits, section, semester, year, building, room, capacity)";
//		final String fdsStr = "course -> title, department, credits ; building, room -> capacity ; course, section, semester, year -> building, room";
//		final String outputFile = "/Users/dibyanshuchatterjee/Downloads/BigData Assingments/Assingment 3/output"; //TODO: remove the comments

		final String relation = args[0];
		final String fdsStr = args[1];
		final String outputFile = args[2];
		
		// This stores the attributes of the input relation.
		Set<String> attributes = new HashSet<>();
		// This stores the functional dependencies provided as input.
		Set<String> fds = new HashSet<>();
		// This stores the candidate keys discovered; each key is a set of attributes.
		List<Set<String>> keys = new ArrayList<>();
		
		// TODO 0: Your code here!
		
		// Parse the input relation that include its attributes. Recall that relation and attribute names can be formed by multiple letters.
		attributes = parseAttributes(relation); //function to parse attributes
		// Parse the input functional dependencies. Recall that attributes can be formed by multiple letters.
		Map<List<String>,List<String>> fdInOrder = new HashMap<>();
		fdInOrder.putAll(parseFDS(fdsStr)); //finction to parse fds
		fds.addAll(FDtoSet(parseFDS(fdsStr)));
		// For each attribute a, you must classify as case 1 (a is not in the functional dependencies), case 2 (a is only in the right-hand side),
		//	case 3 (a is only in the left-hand side), case 4 (a is in both left- and right-hand sides).
		Set<String> core = new HashSet<>();
		core.addAll(coreCompute(fdInOrder,attributes));
		// Compute the core (cases 1 and 3) and check whether the core is candidate key based on closure.
		if (computeClosure(fdInOrder,core).containsAll(attributes)){
			//closure is computed
			/**
			 * will add later what to do..But will definetely exit in this case
			 * */
			keys.add(core);

		}
		// If the closure of the core does not contain all the attributes, proceed to combine attributes.
		else{
			Set <String> fromB4 = new HashSet<>();
			fromB4.addAll(takeFromBucket4(fdInOrder,attributes));
			for (int size = 1; size < fromB4.size(); size++){	// For each combination of attributes starting from size 1 classified as case 4:
				for (Set<String> set: Sets.combinations(fromB4,size)){
					Set<String> temp = new HashSet<>(set);
					for (String s:set)
					//	X = comb union core
					temp.addAll(core);
					Set<String> temp_tempset = new HashSet<>();
					temp_tempset.addAll(temp);
					boolean bbb = false;
					for (Set<String> key : keys) {
						if (temp.containsAll(key))
							if (!bbb) {
								bbb = true;
							}
					}
					if (bbb){
						continue;
					}
					if (computeClosure(fdInOrder,temp_tempset).containsAll(attributes)){
						keys.add(temp);
					}

				}
			}
		}
		PrintWriter writer = new PrintWriter(new File(outputFile));
		for (Set<String> key : keys)
			writer.println(key.stream().sorted().collect(java.util.stream.Collectors.toList()).
					toString().replace("[", "").replace("]", ""));
		writer.close();
	}

	public static Set<String> parseAttributes(String relation){
		Set<String> attributesParsed = new HashSet<>();
		int firstBracket = relation.indexOf("(");
		int secondBracket = relation.indexOf(")");
		String toParse = relation.substring(firstBracket+1,secondBracket);
		String[] toStore = toParse.split(",");
		for (String str:toStore){
			String store = str.trim();
			attributesParsed.add(store);
		}


		return  attributesParsed;
	}
	public static Map<List<String>,List<String>> parseFDS(String fdStr){
		Map<List<String>,List<String>> fds = new HashMap<>();
		String [] str = fdStr.split(";");
		List<ArrayList<String>> listToput = new ArrayList<ArrayList<String>>();
		for (String i:str){
			String trimmed = i.trim();
			String[] splitOnArrow;
			splitOnArrow = trimmed.split("->");
			String [] toMakeListForRight = splitOnArrow[1].split(",");//this goes as key
			String[] toMakeListForLeft = splitOnArrow[0].split(",");
			List<String> leftList = new ArrayList<>();
			for (String j:toMakeListForLeft){
				leftList.add(j.trim());
			}
			List<String> rightList = new ArrayList<>();
			for (String k:toMakeListForRight){
				rightList.add(k.trim());
			}
			fds.put(rightList,leftList);
		}
		return fds;
	}
	public static Set<String> coreCompute(Map<List<String>,List<String>> fdInOrder, Set<String> attributes){
		Set<String> core = new HashSet<>();
		boolean bucket1 = false,bucket3 = false;
		//TODO: map's right hand side is actually the left hand side of fd
		for (String atr:attributes){
			for (Map.Entry<List<String>,List<String>> entry:fdInOrder.entrySet()){ //checking for case 1
				if (!entry.getKey().contains(atr) && !entry.getKey().contains(atr)){
					bucket1 = true;
				}
				else {
					bucket1 = false;
					break;
				}
			}
			if (bucket1) core.add(atr);
			for (Map.Entry<List<String>,List<String>> entry:fdInOrder.entrySet()){ //checking case 3
				if (!entry.getKey().contains(atr) && entry.getKey().contains(atr)){
					bucket3 = true;
				}
				else {
					bucket3 = false;
					break;
				}
			}
			if (bucket3) core.add(atr);
		}
		return core;
	}
	public static Set<String> takeFromBucket4(Map<List<String>,List<String>> fdInOrder, Set<String> attributes){
		Set<String> bothSides = new HashSet<>();
		boolean bucket4 = false;
		for (String atr:attributes){
			for (Map.Entry<List<String>,List<String>> entry:fdInOrder.entrySet()){
				if (entry.getKey().contains(atr) && entry.getKey().contains(atr)){
					bothSides.add(atr);
					break;
				}
			}
		}
		return bothSides;
	}
	public static Set<String> FDtoSet(Map<List<String>,List<String>> map){
		Set<String> fds = new HashSet<>();
		for (Map.Entry<List<String>,List<String>> entry:map.entrySet()){
			fds.addAll(entry.getKey());
			fds.addAll(entry.getValue());
		}
		return fds;
	}
	private static Set<String> computeClosure(Map<List<String>,List<String>> fdInOrder, Set<String> core) {
		Set<String> temp = new HashSet<>(core);
		boolean flag = false;
		for (Map.Entry<List<String>,List<String>> entry:fdInOrder.entrySet()){
			for (int size = 1; size<=core.size(); size++){
				for (Set<String> comb:Sets.combinations(core,size)){
					if (comb.containsAll(entry.getValue())){
						if (!core.containsAll(entry.getKey())){
							flag = true;
							temp.addAll(entry.getKey());
						}
					}
				}
			}
		}
		if (flag){
			return computeClosure(fdInOrder,temp);
		}
		return temp;
	}

}
