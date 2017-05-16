import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Scanner;

import CaseGenerator.EvolutionGenerator;

class StatisticsGenerator {
	public static void main(String[] args) throws IOException {

		//pocet opakovani znahodnenia, vysledky sa spriemeruju
		Integer repeatTimes1 = 32;
		//pocet opakovani generovania jedneho vstupu, kolko pomerov ziskame v druhej statistike
		Integer repeatTimes2 = 16;

		Integer defaultLeaves = 8;
		Integer defaultGenes = 32;
		Integer defaultEvents = 8;
		Integer defaultChromosomes = 8;

		//PIVO a realne
		realStatistics(repeatTimes1);

		//generovane - odkomentovanie spusti tvorbu suborov pre generovane data, moze to trvat dlho
		//statistics(repeatTimes1, repeatTimes2, defaultLeaves, defaultGenes, defaultEvents, defaultChromosomes);
	}



	private static void statistics(Integer repeatTimes1, Integer repeatTimes2,
		Integer defaultLeaves, Integer defaultGenes, Integer defaultEvents, Integer defaultChromosomes) throws IOException {

		FileWriter fw;
		BufferedWriter bw;
		String genDirectoryName = "generated_statistics";
		File genDIR = new File(genDirectoryName);
		deleteDir(genDIR);
		genDIR = new File(genDirectoryName);
		genDIR.mkdir();

		ArrayList<Integer> generatedResult1 = new ArrayList<>();
		ArrayList<Double> generatedResult2 = new ArrayList<>();

		String subdirectory = "/leaves";
		File subDIR = new File(genDirectoryName + subdirectory);
		subDIR.mkdir();

		for (Integer leaves = 1; leaves <= 128; leaves*=2) {
			String[] generatorArgs = {leaves.toString(), defaultGenes.toString(), defaultEvents.toString(), defaultChromosomes.toString()};
			String name = "generated-" + leaves + "-" + defaultGenes + "-" + defaultEvents + "-" + defaultChromosomes + "_";

			generatedResult1.add(leaves);
			generatedResult2.add(leaves.doubleValue());

			generateHistoryAndStatistic2(repeatTimes1, repeatTimes2, genDirectoryName, generatedResult2, subdirectory, generatorArgs, name);

			createStatistics1(genDirectoryName + subdirectory, name + "output", repeatTimes1, generatedResult1);

		}

		File statisticsLeaves = new File(genDirectoryName + "/leaves_statistics1.txt");
		statisticsLeaves.delete();
		statisticsLeaves.createNewFile();
		fw = new FileWriter(statisticsLeaves.getAbsoluteFile());
		bw = new BufferedWriter(fw);
		for (int i = 0; i < generatedResult1.size(); i++) {
			bw.write(generatedResult1.get(i).toString());
			bw.newLine();
		}
		bw.close();
		fw.close();

		File statisticsLeaves2 = new File(genDirectoryName + "/leaves_statistics2.txt");
		statisticsLeaves2.delete();
		statisticsLeaves2.createNewFile();
		fw = new FileWriter(statisticsLeaves2.getAbsoluteFile());
		bw = new BufferedWriter(fw);
		for (int i = 0; i < generatedResult2.size(); i++) {
			bw.write(generatedResult2.get(i).toString());
			bw.newLine();
		}
		bw.close();
		fw.close();

		generatedResult2 = new ArrayList<>();
		generatedResult1 = new ArrayList<>();

		subdirectory = "/genes";
		subDIR = new File(genDirectoryName + subdirectory);
		subDIR.mkdir();

		for (Integer genes = 8; genes <= 1024; genes*=2) {
			Integer chromosomes = genes/4;
			String[] generatorArgs = {defaultLeaves.toString(), genes.toString(), defaultEvents.toString(), chromosomes.toString()};
			String name = "generated-" + defaultLeaves + "-" + genes + "-" + defaultEvents + "-" + chromosomes + "_";

			generatedResult1.add(genes);
			generatedResult2.add(genes.doubleValue());

			generateHistoryAndStatistic2(repeatTimes1, repeatTimes2, genDirectoryName, generatedResult2, subdirectory, generatorArgs, name);

			createStatistics1(genDirectoryName + subdirectory, name + "output", repeatTimes1, generatedResult1);
		}

		File statisticsGenes = new File(genDirectoryName + "/genes_statistics1.txt");
		statisticsGenes.delete();
		statisticsGenes.createNewFile();
		fw = new FileWriter(statisticsGenes.getAbsoluteFile());
		bw = new BufferedWriter(fw);
		for (int i = 0; i < generatedResult1.size(); i++) {
			bw.write(generatedResult1.get(i).toString());
			bw.newLine();
		}
		bw.close();
		fw.close();

		File statisticsGenes2 = new File(genDirectoryName + "/genes_statistics2.txt");
		statisticsGenes2.delete();
		statisticsGenes2.createNewFile();
		fw = new FileWriter(statisticsGenes2.getAbsoluteFile());
		bw = new BufferedWriter(fw);
		for (int i = 0; i < generatedResult2.size(); i++) {
			bw.write(generatedResult2.get(i).toString());
			bw.newLine();
		}
		bw.close();
		fw.close();

		generatedResult2 = new ArrayList<>();
		generatedResult1 = new ArrayList<>();

		subdirectory = "/events";
		subDIR = new File(genDirectoryName + subdirectory);
		subDIR.mkdir();

		for (Integer events = 1; events <= 32; events*=2) {
			String[] generatorArgs = {defaultLeaves.toString(), defaultGenes.toString(), events.toString(), defaultChromosomes.toString()};
			String name = "generated-" + defaultLeaves + "-" + defaultGenes + "-" + events + "-" + defaultChromosomes + "_";

			generatedResult1.add(events);
			generatedResult2.add(events.doubleValue());

			generateHistoryAndStatistic2(repeatTimes1, repeatTimes2, genDirectoryName, generatedResult2, subdirectory, generatorArgs, name);

			createStatistics1(genDirectoryName + subdirectory, name + "output", repeatTimes1, generatedResult1);

		}

		File statisticsEvents = new File(genDirectoryName + "/events_statistics1.txt");
		statisticsEvents.delete();
		statisticsEvents.createNewFile();
		fw = new FileWriter(statisticsEvents.getAbsoluteFile());
		bw = new BufferedWriter(fw);
		for (int i = 0; i < generatedResult1.size(); i++) {
			bw.write(generatedResult1.get(i).toString());
			bw.newLine();
		}
		bw.close();
		fw.close();

		File statisticsEvents2 = new File(genDirectoryName + "/events_statistics2.txt");
		statisticsEvents2.delete();
		statisticsEvents2.createNewFile();
		fw = new FileWriter(statisticsEvents2.getAbsoluteFile());
		bw = new BufferedWriter(fw);
		for (int i = 0; i < generatedResult2.size(); i++) {
			bw.write(generatedResult2.get(i).toString());
			bw.newLine();
		}
		bw.close();
		fw.close();

		generatedResult2 = new ArrayList<>();
		generatedResult1 = new ArrayList<>();

		subdirectory = "/chromosomes";
		subDIR = new File(genDirectoryName + subdirectory);
		subDIR.mkdir();

		for (Integer chromosomes = 1; chromosomes <= defaultGenes/2; chromosomes*=2) {
			String[] generatorArgs = {defaultLeaves.toString(), defaultGenes.toString(), defaultEvents.toString(), chromosomes.toString()};
			String name = "generated-" + defaultLeaves + "-" + defaultGenes + "-" + defaultEvents + "-" + chromosomes + "_";

			generatedResult1.add(chromosomes);
			generatedResult2.add(chromosomes.doubleValue());
			generateHistoryAndStatistic2(repeatTimes1, repeatTimes2, genDirectoryName, generatedResult2, subdirectory, generatorArgs, name);

			createStatistics1(genDirectoryName + subdirectory, name + "output", repeatTimes1, generatedResult1);
		}

		File statisticsChromosomes = new File(genDirectoryName + "/chromosomes_statistics1.txt");
		statisticsChromosomes.delete();
		statisticsChromosomes.createNewFile();
		fw = new FileWriter(statisticsChromosomes.getAbsoluteFile());
		bw = new BufferedWriter(fw);
		for (int i = 0; i < generatedResult1.size(); i++) {
			bw.write(generatedResult1.get(i).toString());
			bw.newLine();
		}
		bw.close();
		fw.close();

		File statisticsChromosomes2 = new File(genDirectoryName + "/chromosomes_statistics2.txt");
		statisticsChromosomes2.delete();
		statisticsChromosomes2.createNewFile();
		fw = new FileWriter(statisticsChromosomes2.getAbsoluteFile());
		bw = new BufferedWriter(fw);
		for (int i = 0; i < generatedResult2.size(); i++) {
			bw.write(generatedResult2.get(i).toString());
			bw.newLine();
		}
		bw.close();
		fw.close();
	}

	private static void realStatistics(Integer repeatTimes1) throws IOException {
		String dirName = "candida_statistics";
		String fileName = "candida";
		createRealStatistics(repeatTimes1, dirName, fileName);

		dirName = "mammal1_statistics";
		fileName = "mammal1";
		createRealStatistics(repeatTimes1, dirName, fileName);

		dirName = "mammal2_statistics";
		fileName = "mammal2";
		createRealStatistics(repeatTimes1, dirName, fileName);

		dirName = "mammal3_statistics";
		fileName = "mammal3";
		createRealStatistics(repeatTimes1, dirName, fileName);
	}

	private static void createRealStatistics(Integer repeatTimes1, String dirName, String fileName) throws IOException {
		ArrayList<Integer> result = new ArrayList<>();
		File dir = new File(dirName);
		deleteDir(dir);
		dir.mkdir();

		File file = new File(fileName + ".history");
		Files.copy(file.toPath(), (new File(dirName + "/" + file.getName())).toPath(), StandardCopyOption.REPLACE_EXISTING);

		createStatistics1(dirName, fileName, repeatTimes1, result);

		File statistics = new File(dirName + "/statistics.txt");
		statistics.delete();
		statistics.createNewFile();
		FileWriter fw = new FileWriter(statistics.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		for (int i = 0; i < result.size(); i++) {
			bw.write(result.get(i).toString());
			bw.newLine();
		}
		bw.close();
		fw.close();
	}

	private static void generateHistoryAndStatistic2(Integer repeatTimes1, Integer repeatTimes2, String genDirectoryName, ArrayList<Double> generatedResult2, String subdirectory, String[] generatorArgs, String name) throws IOException {
		for (int i = 0; i < repeatTimes2; i++) {
			EvolutionGenerator.main(generatorArgs);

			File treeFile = new File("evolution/tree.txt");
			treeFile.renameTo(new File("evolution/" + name + "tree.txt"));
			String[] rocnikovyArgs = {"pivo", "evolution/" + name + "tree.txt", "evolution/evolutionDCJ.txt"};
			EHConverter.main(rocnikovyArgs);

			File generatedFile = new File("evolution/" + name + "output.history");
			Files.copy(generatedFile.toPath(), (new File(genDirectoryName + subdirectory + "/" + generatedFile.getName())).toPath(),
				StandardCopyOption.REPLACE_EXISTING);

			File evolutionDIR = new File("evolution");
			deleteDir(evolutionDIR);
			createStatistics2(genDirectoryName + subdirectory, name + "output", repeatTimes1, generatedResult2);
		}
	}

	private static void createStatistics1(String directoryName, String fileName, Integer repeatTimes,
		ArrayList<Integer> result) throws IOException {
		directoryName += "/";
		Integer crossings = spocitaj_krizenia(directoryName, fileName);
		result.add(crossings);
		Integer crossings_minimized = vypis_a_spocitaj_krizenia(directoryName, fileName);
		result.add(crossings_minimized);

		Integer randomCrossingsSum = 0;
		Integer randomMinimizedCrossingsSum = 0;

		for (int i = 1; i <= repeatTimes; i++) {
			//vytvor random output
			String[] randomizerArgs = {directoryName + fileName + ".history", directoryName + fileName + "_random" + i + ".history"};
			HistoryRandomizer.main(randomizerArgs);

			randomCrossingsSum += (spocitaj_krizenia(directoryName, fileName + "_random" + i));
			randomMinimizedCrossingsSum += (vypis_a_spocitaj_krizenia(directoryName, fileName + "_random" + i));
		}
		result.add(Math.round(randomCrossingsSum / repeatTimes));
		result.add(Math.round(randomMinimizedCrossingsSum / repeatTimes));
	}

	private static void createStatistics2(String directoryName, String fileName, int repeatTimes, ArrayList<Double> result) throws IOException {
		directoryName += "/";

		Double crossings_minimized = vypis_a_spocitaj_krizenia(directoryName, fileName).doubleValue();

		Integer randomMinimizedCrossingsSum = 0;

		for (int i = 1; i <= repeatTimes; i++) {
			//vytvor random output
			String[] randomizerArgs = {directoryName + fileName + ".history", directoryName + fileName + "_random" + i + ".history"};
			HistoryRandomizer.main(randomizerArgs);

			randomMinimizedCrossingsSum += (vypis_a_spocitaj_krizenia(directoryName, fileName + "_random" + i));
		}
		Double crossings_random_minimized = (double) Math.round(randomMinimizedCrossingsSum / repeatTimes);

		result.add(crossings_minimized/crossings_random_minimized);
	}


	private static Integer vypis_a_spocitaj_krizenia(String directoryName, String fileName) throws IOException {
		ArrayList<String> arguments = new ArrayList<>();
		arguments.add("-nogui");
		arguments.add("-input:" + directoryName + fileName +".history");
		arguments.add("-minimized_output:" + directoryName + fileName +"_minimized.history");
		arguments.add("-exportminimized");
		arguments.add("-crossings_output:" + directoryName + fileName + "_minimized_crossings.txt");
		arguments.add("-exportcrossings");

		String[] argumentsArray = new String[arguments.size()];
		EHDraw.main(arguments.toArray(argumentsArray));

		File crossingsFile = new File(directoryName + fileName + "_minimized_crossings.txt");
		Scanner s = new Scanner(crossingsFile);
		Integer result = s.nextInt();
		s.close();
		Files.delete(crossingsFile.toPath());
		return result;
	}

	private static Integer spocitaj_krizenia(String directoryName, String fileName) throws IOException {
		ArrayList<String> arguments = new ArrayList<>();
		arguments.add("-nogui");
		arguments.add("-input:" + directoryName + fileName +".history");
		arguments.add("-crossings_output:" + directoryName + fileName + "_crossings.txt");
		arguments.add("-exportcrossings");

		String[] argumentsArray = new String[arguments.size()];
		EHDraw.main(arguments.toArray(argumentsArray));

		File crossingsFile = new File(directoryName + fileName + "_crossings.txt");
		Scanner s = new Scanner(crossingsFile);
		Integer result = s.nextInt();
		s.close();
		Files.delete(crossingsFile.toPath());
		return result;
	}

	private static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] files = dir.list();
			for (int i = 0; i < files.length; i++) {
				boolean success = deleteDir(new File(dir, files[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}
}
