package org.ozsoft.texasholdem.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.nio.file.Files;
import static java.nio.file.StandardCopyOption.*;

import org.ozsoft.texasholdem.Deck;

public enum InputOutputMgr {
	INSTANCE;

	public enum InputType {
		Random, File
	}
	private InputType inputType = InputType.Random;

	private static final String inputFolderStr = "Input";
	private static final String outputFolderStr = "Output";
	private static final String backupFolderStr = "bak";
	private static final String progressRecordStr = "Progress.Record";
	private File inputFolder;
	private File outputFolder;
	private File backupFolder;
	private File outputFile;
	private File progressRecordFile;
	private ArrayList<File> inputFiles = new ArrayList<File>();
	private int inputFileIndex = -1;
	private ArrayList<String> inputRows = new ArrayList<String>();
	private int inputRowIndex = 0;
	private boolean loadProgress = false;
	private int dealerPosition;
	private int smallBlind;
	private int bigBlind;
	private int money1;
	private int money2;
	private int ante;
	private Deck deck;
	
	private InputOutputMgr()
	{
		inputFolder = createFolder(inputFolderStr);
		outputFolder = createFolder(outputFolderStr);
		backupFolder = createFolder(inputFolderStr + "/" + backupFolderStr);
		progressRecordFile = new File(inputFolder, progressRecordStr);
		
		// create new filename filter
        FilenameFilter fileNameFilter = new FilenameFilter() {
  
           @Override
           public boolean accept(File dir, String name) {
              if(name.lastIndexOf('.')>0)
              {
                 // get last index for '.' char
                 int lastIndex = name.lastIndexOf('.');
                 
                 // get extension
                 String str = name.substring(lastIndex);
                 
                 // match path name extension
                 if(str.equalsIgnoreCase(".csv"))
                 {
                    return true;
                 }
              }
              return false;
           }
        };
		
	    for (File file : inputFolder.listFiles(fileNameFilter)) {
	        if (!file.isDirectory()) {
	        	inputFiles.add(file);
	        }
	    }    
	}

	public InputType getInputType() {
		return inputType;
	}

	public void setInputType(InputType inputType) {
		this.inputType = inputType;
		
		if (inputType == InputType.File)
			readProgress();
	}

	public int getDealerPosition() {
		return dealerPosition;
	}

	public int getSmallBlind() {
		return smallBlind;
	}

	public int getBigBlind() {
		return bigBlind;
	}

	public int getMoney1() {
		return money1;
	}

	public int getMoney2() {
		return money2;
	}

	public Deck getDeck() {
		return deck;
	}
	
	public int getAnte(){
		return ante;
	}

	private static File createFolder(String folderName) {
		File theFolder = new File(folderName);

		// if the folder does not exist, create it
		if (!theFolder.exists()) {
			theFolder.mkdir();
		}

		return theFolder;
	}

	public boolean next()
	{
		if (inputType != InputType.File)
			return false;
		
		inputRowIndex++;
		
		if (inputRows.size() < inputRowIndex + 1){
			//Current input file is done, backup it!
			backupCurrentInputFile();
			
			//Reset
			if (loadProgress){
				inputRowIndex--;
				loadProgress = false;
			} else {
				inputRowIndex = 0;
			}
			inputRows.clear();
			
			inputFileIndex++;
			if (inputFiles.size() < inputFileIndex + 1)
				return false;	//no more file to read.
			
			readInputFile(inputFiles.get(inputFileIndex));
			return next();
		}
		
		try
		{
			String[] data = inputRows.get(inputRowIndex).split(",");
			parseData(data);
		}
		catch(Exception e)
		{
			return next();
		}
		
		return true;
	}
	
	public void logResult(String msg){
		if (outputFile == null)
			return;
		
		try {
			//Log result
			try (Writer writer = new BufferedWriter(new OutputStreamWriter(
			        new FileOutputStream(outputFile, true), "UTF-8"))) {
				writer.write(msg);
			}
			
			//Record progress
			try (Writer writer = new BufferedWriter(new OutputStreamWriter(
			        new FileOutputStream(progressRecordFile, false), "UTF-8"))) {
				writer.write(inputFiles.get(inputFileIndex).getName() + " : " + inputRowIndex);
			}
		} catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public String getInputFileTimeStamp()
	{
		String res;
		try {
			String fileName = inputFiles.get(inputFileIndex).getName();
			res = fileName.substring(fileName.lastIndexOf(".")-12, fileName.lastIndexOf("."));
		} catch (Exception e) {
			//System.out.println("Exception in getInputFileTimeStamp() : " + e.getMessage());
			res = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
		}
		
		return res;
	}
	
	private void parseData(String[] data)
	{
		dealerPosition = Integer.parseInt(data[0].trim());
		smallBlind = Integer.parseInt(data[1].trim());
		bigBlind = Integer.parseInt(data[2].trim());
		ante = Integer.parseInt(data[3].trim());
		money1 = Integer.parseInt(data[4].trim());
		money2 = Integer.parseInt(data[5].trim());
		String[] cards = Arrays.copyOfRange(data, 6, 58);
		deck = new Deck(Arrays.asList(cards));
	}
	
	private void readInputFile(File inputFile)
	{
		try {
			try(BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
			    for(String line; (line = br.readLine()) != null; ) {
			        inputRows.add(line);
			    }
			}
		
			//output file name
			String name = inputFile.getName();
			int pos = name.lastIndexOf(".");
			if (pos > 0) {
			    name = name.substring(0, pos);
			}
			name = name + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))
					+ ".txt";
			outputFile = new File(outputFolder, name);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private void backupCurrentInputFile()
	{
		if (inputFileIndex < 0 || inputFileIndex > inputFiles.size()-1)
			return;
		
		try {
			Files.move(inputFiles.get(inputFileIndex).toPath(),
				backupFolder.toPath().resolve(inputFiles.get(inputFileIndex).getName()), REPLACE_EXISTING);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void readProgress()
	{
		if (progressRecordFile.exists() && progressRecordFile.isFile()){
			String line;
			try {
				try(BufferedReader br = new BufferedReader(new FileReader(progressRecordFile))) {
				    line = br.readLine();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			
			String[] strs = line.split(":");
			String fileName = strs[0].trim();
			int index = Integer.parseInt(strs[1].trim());
			
			if(inputFiles.get(0).getName().equals(fileName)){
				//Yes, we need to load progress from the saved progress file.
				loadProgress = true;
				inputRowIndex = index;
			}
		}
	}
	
	// 0 or 1, 1 = player is dealer.
	// Small blind, Big blind.
	// Dealer's money, robot's money
	// 52 cards.
	public static void main(String[] args) throws IOException {
		int numberOfFiles = 1;
		int numberOfRows = 100;

		if (!(args == null || args.length < 1)) {
			try {
				numberOfFiles = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				System.out.println(String.format("Error : %s is not a number!", args[0]));
			}
		}

		if (!(args == null || args.length < 2)) {
			try {
				numberOfRows = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				System.out.println(String.format("Error : %s is not a number!", args[1]));
			}
		}

		Deck deck = new Deck();

		String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
		for (int i = 0; i < numberOfFiles; i++) {
			String fileName = String.format("%s/Input_%d_%s.csv", inputFolderStr, i, time);
			try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"))) {
				writer.write("DealerPosition, SmallBlind, BigBlind, Money1, Money2, Ante, Cards\n");
				for (int j = 0; j < numberOfRows; j++) {
					deck.shuffle();
					writer.write(String.format("0, 5, 10, 50, 500, 500 %s\n", deck.toString().replaceAll(" ", ",")));
				}
			}
		}
	}

	/*
	//Test
	public static void main(String[] args) throws IOException {
		String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
		String fileName = String.format("%s/Onput_%s.csv", outputFolderStr, time);
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"))) {
			writer.write("DealerPosition, SmallBlind, BigBlind, Money1, Money2, Cards\n");
			while(InputOutputMgr.INSTANCE.next())
			{
				writer.write(String.format("%d, %d, %d, %d, %d, %s\n", 
						InputOutputMgr.INSTANCE.getDealerPosition(),
						InputOutputMgr.INSTANCE.getSmallBlind(),
						InputOutputMgr.INSTANCE.getBigBlind(),
						InputOutputMgr.INSTANCE.getMoney1(),
						InputOutputMgr.INSTANCE.getMoney2(),
						InputOutputMgr.INSTANCE.getDeck().toString().replaceAll(" ", ",")));
			}
		}
	}*/
}