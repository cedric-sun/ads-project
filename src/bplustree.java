import dsimpl.BPlusTree;

import java.io.*;

public class bplustree {
    static class MalformedInstructionException extends Exception {

    }

    static class ReinitializationException extends Exception {

    }

    static class NotInitializedException extends Exception {

    }


    public static final String OUTPUT_FILENAME = "output_file.txt";

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java bplustree FILENAME");
            return;
        }
        try (
                BufferedReader br = new BufferedReader(new FileReader(args[0]));
                PrintStream ps = new PrintStream(OUTPUT_FILENAME)
        ) {
            String line;
            int lineNum = 1;
            BPlusTree bPlusTree = null;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("[\\(,\\)]");
                for (int i = 0; i < tokens.length; i++) {
                    tokens[i] = tokens[i].trim();
                }
                try {
                    switch (tokens[0]) {
                        case "Initialize":
                            if (tokens.length != 2) throw new MalformedInstructionException();
                            if (bPlusTree == null) {
                                bPlusTree = new BPlusTree(Integer.valueOf(tokens[1]));
                            } else {
                                throw new ReinitializationException();
                            }
                            break;
                        case "Insert":
                            if (bPlusTree == null) throw new NotInitializedException();
                            if (tokens.length != 3) throw new MalformedInstructionException();
                            bPlusTree.insert(Integer.valueOf(tokens[1]), Double.valueOf(tokens[2]));
                            break;
                        case "Delete":
                            if (bPlusTree == null) throw new NotInitializedException();
                            if (tokens.length != 2) throw new MalformedInstructionException();
                            bPlusTree.delete(Integer.valueOf(tokens[1]));
                            break;
                        case "Search":
                            if (bPlusTree == null) throw new NotInitializedException();
                            if (tokens.length == 2) {
                                Double res = bPlusTree.get(Integer.valueOf(tokens[1]));
                                ps.println(res);
                            } else if (tokens.length == 3) {
                                double[] resArr = bPlusTree.range(
                                        Integer.valueOf(tokens[1]), Integer.valueOf(tokens[2]));
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < resArr.length; i++) {
                                    if (i != 0)
                                        sb.append(',');
                                    sb.append(resArr[i]);
                                }
                                ps.println(sb.toString());
                            } else {
                                throw new MalformedInstructionException();
                            }
                            break;
                        default:
                            throw new MalformedInstructionException();
                    }
                } catch (MalformedInstructionException e) {
                    System.err.println("Malformed Instruction. Operation is ignored. Line: " + lineNum);
                } catch (ReinitializationException e) {
                    System.err.println("Reinitialization is not allowed. Operation is ignored. Line: " + lineNum);
                } catch (NotInitializedException e) {
                    System.err.println("B+Tree has not been initialized yet. Operation is ignored. Line: " + lineNum);
                } catch (NumberFormatException e) {
                    System.err.println("Number format is wrong. Operation is ignored. Line: " + lineNum);
                }
                lineNum++;
            }
        } catch (FileNotFoundException e) {
            System.err.println("File doesn't exist.");
        } catch (IOException e) {
            System.err.println("I/O exception!");
        }
    }
}
