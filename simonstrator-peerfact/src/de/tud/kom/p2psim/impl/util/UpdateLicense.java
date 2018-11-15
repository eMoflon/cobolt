/*
 * Copyright (c) 2005-2011 KOM - Multimedia Communications Lab
 *
 * This file is part of PeerfactSim.KOM.
 * 
 * PeerfactSim.KOM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * PeerfactSim.KOM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PeerfactSim.KOM.  If not, see <http://www.gnu.org/licenses/>.
 *
 */



package de.tud.kom.p2psim.impl.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class UpdateLicense {
	static int counter = 0;

	static int updated = 0;

	static int skipped = 0;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File licenseFile = new File("license.txt");
		String dir;
		// "src/de/tud/kom/p2psim/simulator"
		if (args.length == 0) {
			dir = "src";
			updateLicense(new File(dir), licenseFile);
		} else {
			for (int i = 0; i < args.length; i++) {
				dir = args[i];
				updateLicense(new File(dir), licenseFile);
			}
		}
		System.out.println("Checked " + counter + " files");
		System.out.println("Updated " + counter + " files");
		System.out.println("Skipped " + counter + " files");
	}

	private static void updateLicense(File oldFile, File licenseFile) {
		if (oldFile.isDirectory() && !oldFile.getName().equals("CVS")) {
			File[] files = oldFile.listFiles();
			for (int i = 0; i < files.length; i++) {
				updateLicense(files[i], licenseFile);
			}
		} else if (oldFile.getName().endsWith(".java")) {
			System.out.println("Check file " + oldFile.getAbsolutePath());
			counter++;
			try {
				// create new version
				File newFile = new File(oldFile.getAbsolutePath() + ".new");
				BufferedWriter out = new BufferedWriter(new FileWriter(newFile));
				// copy new license
				copyLicense(licenseFile, out);

				// append the original file, but skip old license
				BufferedReader in = new BufferedReader(new FileReader(oldFile));
				String line = in.readLine();
				boolean skip = line.trim().equals("/*");
				if (skip)
					System.out.println("Found license in " + oldFile);
				do {// first line already read
					skip = skip && !line.trim().startsWith("package ");
					if (skip) {
						// System.out.println("Skip "+line);
					} else {
						out.write(line);
						out.newLine();
					}
				} while ((line = in.readLine()) != null);

				out.close();
				in.close();
				// rename new file in old
				oldFile.delete();
				if (newFile.renameTo(oldFile)) {
					System.out.println("Updated " + newFile);
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}

	}

	private static void copyLicense(File licenseFile, BufferedWriter out)
			throws FileNotFoundException, IOException {
		String line;
		BufferedReader license = new BufferedReader(new FileReader(licenseFile));
		out.write("/*");
		out.newLine();
		while ((line = license.readLine()) != null) {
			out.write(" * ");
			out.write(line);
			out.newLine();
		}
		out.write(" */");
		out.newLine();
		out.newLine();
		license.close();
	}

}
